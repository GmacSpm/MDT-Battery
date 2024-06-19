package br.gmacspm.mdtbattery.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.activities.MainActivity;
import br.gmacspm.mdtbattery.config.Settings;
import br.gmacspm.mdtbattery.constants.ServiceConstants;
import br.gmacspm.mdtbattery.interfaces.ServiceCallback;
import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.room.background.BackgroundDB;
import br.gmacspm.mdtbattery.utils.TimeConverter;

public class BatteryMonitorService extends Service {
    private BatteryMonitorService context;
    public static final int DISCONNECTED = 0;
    private RemoteViews notificationLayout;
    private ServiceCallback serviceCallback;
    private BatteryManager batteryManager;
    private int currentMin = 0, currentMax = 0;
    private int tempMin = 0, tempActual = 0, tempMax = 0;
    private Timer timer;
    private long activeTimer;
    private long inativeTimer;
    private long totalOnThisPercent;
    private long totalOffThisPercent;

    // From preferences variables
    private Settings settings;
    private int rechargeTarget, dischargeTarget;
    private boolean isVibrateOnChange;
    private int baseAvgSize;
    private int mahTimerPeriod;
    private boolean persistData;

    // From database
    private BackgroundDB historyDatabase;
    private String stringTargetOn, stringTargetOff;
    private String stringSunday, stringMonday, stringTuesday, stringWednesday, stringThursday, stringFriday, stringSaturday;

    public int getCurrentLevel() {
        return batteryPct;
    }

    public void setServiceCallback(ServiceCallback serviceCallback) {
        this.serviceCallback = serviceCallback;
    }

    public String getTimeEnd(boolean isTimeEndOn) {
        Calendar currentTime = Calendar.getInstance();
        int hourNow = currentTime.get(Calendar.HOUR_OF_DAY);
        int minuteNow = currentTime.get(Calendar.MINUTE);
        long millis;
        String result = "--:--";

        if (lastPlugState == DISCONNECTED) {
            millis = getRemainingToLong(batteryPct, getDischargeTarget(), getAverage(isTimeEndOn));
        } else {
            millis = getRemainingToLong(batteryPct, getRechargeTarget(), getAverage(isTimeEndOn));
        }

        // Add expected time to current time now
        currentTime.add(Calendar.DAY_OF_WEEK, TimeConverter.getDays(millis));
        currentTime.add(Calendar.HOUR_OF_DAY, TimeConverter.getHours(millis));
        currentTime.add(Calendar.MINUTE, TimeConverter.getMinutes(millis));

        // Get the updated time
        int days = TimeConverter.getDays(millis);
        int day = currentTime.get(Calendar.DAY_OF_WEEK);
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        if (days > 0) {
            String dayOfWeek;
            switch (day) {
                case Calendar.SUNDAY:
                    dayOfWeek = stringSunday;
                    break;
                case Calendar.MONDAY:
                    dayOfWeek = stringMonday;
                    break;
                case Calendar.TUESDAY:
                    dayOfWeek = stringTuesday;
                    break;
                case Calendar.WEDNESDAY:
                    dayOfWeek = stringWednesday;
                    break;
                case Calendar.THURSDAY:
                    dayOfWeek = stringThursday;
                    break;
                case Calendar.FRIDAY:
                    dayOfWeek = stringFriday;
                    break;
                case Calendar.SATURDAY:
                    dayOfWeek = stringSaturday;
                    break;
                default:
                    dayOfWeek = "Erro";
                    break;
            }
            result = String.format(Locale.US, "%s, %02d:%02d", dayOfWeek, hour, minute);

        } else if (hour != hourNow || minute != minuteNow) {
            result = String.format(Locale.US, "%02d:%02d", hour, minute);
        }
        return result;
    }


    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getBatteryCurrent();
                if (isCallbackOk()) serviceCallback.updateMah();

            }
        }, 0, mahTimerPeriod);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private int mahMetered;
    private int mahCounter;

    public int getBatteryCurrent() {
        int currentNow = (int) ((float) Math.abs(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000));
        if (currentNow > currentMax || currentMax == 0) {
            currentMax = currentNow;
        } else if (currentNow < currentMin || currentMin == 0) {
            currentMin = currentNow;
        }
        mahMetered = mahMetered + currentNow;
        mahCounter = mahCounter + 1;
        Log.d(TAG + "current", "CURRENT_NOW = " + currentNow);
        return currentNow;
    }

    public int getBatteryCurrentMin() {
        return currentMin;
    }

    public int getBatteryCurrentMax() {
        return currentMax;
    }

    private int getDischargeTarget() {
        if (batteryPct < dischargeTarget) {
            return 1;// If pass
        }
        return dischargeTarget;
    }

    private int getRechargeTarget() {
        if (batteryPct > rechargeTarget) {
            return 100;// If pass
        }
        return rechargeTarget;
    }

    public void resetMonitoring() {
        resetVariables();
        if (isCallbackOk()) serviceCallback.updateUsageData();
        if (isCallbackOk()) serviceCallback.resetMah();
        if (isCallbackOk()) serviceCallback.resetTemp();
    }

    /**
     * @return Return true if callback can be called.
     */
    private boolean isCallbackOk() {
        return serviceCallback != null;
    }

    public int getBatteryTempMin() {
        return tempMin;
    }

    public int getBatteryTemp() {
        if (tempActual > tempMax || tempMax == 0) {
            tempMax = tempActual;
        } else if (tempActual < tempMin || tempMin == 0) {
            tempMin = tempActual;
        }
        tempMetered = tempMetered + tempActual;
        tempCounter = tempCounter + 1;
        return tempActual;
    }

    public int getBatteryTempMax() {
        return tempMax;
    }

    public class MonitorBinder extends Binder {
        public BatteryMonitorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BatteryMonitorService.this;
        }
    }

    private final IBinder binder = new MonitorBinder();
    private static final String CHANNEL_ID = "GSBATT";
    private static final int NOTIFICATION_ID = 154343;
    private static final String TAG = "GSBATT-";
    public static boolean isServiceRunning = false;
    private NotificationManager notificationManager;
    private final ArrayList<UsageModel> usageHistory = new ArrayList<>();
    private long serviceStartTime;

    private void updateNotificationUsage(String avgOn, String avgOff, String remainingToOn, String remainingToOff, int targetPercent) {
        notificationLayout.setTextViewText(R.id.notification_avg_on, avgOn);
        notificationLayout.setTextViewText(R.id.notification_avg_off, avgOff);
        notificationLayout.setTextViewText(R.id.notification_target_percent, String.format(stringTargetOn, targetPercent));
        notificationLayout.setTextViewText(R.id.notification_target_percent_2, String.format(stringTargetOff, targetPercent));
        notificationLayout.setTextViewText(R.id.notification_remaining_on, remainingToOn);
        notificationLayout.setTextViewText(R.id.notification_remaining_off, remainingToOff);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        notificationManager.notify(NOTIFICATION_ID,
                new NotificationCompat.Builder(this, CHANNEL_ID).
                        setSmallIcon(R.drawable.ic_notification).
                        setStyle(new NotificationCompat.DecoratedCustomViewStyle()).
                        setCustomContentView(notificationLayout).
                        setOnlyAlertOnce(true).
                        setContentIntent(pendingIntent).
                        build()
        );
    }

    private void updateNotificationTarget(int targetPercent) {
        notificationLayout.setTextViewText(R.id.notification_target_percent, String.format(stringTargetOn, targetPercent));
        notificationLayout.setTextViewText(R.id.notification_target_percent_2, String.format(stringTargetOff, targetPercent));
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        notificationManager.notify(NOTIFICATION_ID,
                new NotificationCompat.Builder(this, CHANNEL_ID).
                        setSmallIcon(R.drawable.ic_notification).
                        setStyle(new NotificationCompat.DecoratedCustomViewStyle()).
                        setCustomContentView(notificationLayout).
                        setOnlyAlertOnce(true).
                        setContentIntent(pendingIntent).
                        build()
        );
    }

    public void createNotification(Context context) {
        notificationLayout.setTextViewText(R.id.notification_target_percent, String.format(stringTargetOn, isCharging() ? getRechargeTarget() : getDischargeTarget()));
        notificationLayout.setTextViewText(R.id.notification_target_percent_2, String.format(stringTargetOff, isCharging() ? getRechargeTarget() : getDischargeTarget()));
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Para android Oreo e acima (API >= 26), criamos um canal de notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "SweetTunnel Usage", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).
                    setStyle(new NotificationCompat.DecoratedCustomViewStyle()).
                    setSmallIcon(R.drawable.ic_notification).
                    setOnlyAlertOnce(true).
                    setCustomContentView(notificationLayout).
                    setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();

            startForeground(NOTIFICATION_ID, notification);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
        // Para android abaixo do Oreo (API < 26), usamos a API NotificationCompat
        else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).
                    setStyle(new NotificationCompat.DecoratedCustomViewStyle()).
                    setSmallIcon(R.drawable.ic_notification).
                    setOnlyAlertOnce(true).
                    setCustomContentView(notificationLayout).
                    setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

    }

    // -Xlint bug shows a deprecation here even if it seems to be OK...
    private void vibrateOnChange() {
        if (isVibrateOnChange && !isCharging()) {
            if (Build.VERSION.SDK_INT >= 31) { // Android 12
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                Vibrator vibrator = vibratorManager.getDefaultVibrator();
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) { // Android 8
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            }
        }
    }

    private long totalScreenOnTime, totalScreenOffTime;
    private int batteryPct, lastPlugState = -1;
    private int tempMetered;
    private int tempCounter;
    private int lastTemp = -1;
    private final BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        private int lastLevel = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            batteryPct = level * 100 / scale;
            tempActual = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

            if (lastTemp != tempActual) {
                getBatteryTemp();
                lastTemp = tempActual;
                // Atualiza temperatura nas Activities visiveis
                if (isCallbackOk()) serviceCallback.updateTemperature();
            }


            if (lastLevel < 1) lastLevel = batteryPct;
            if (batteryPct != lastLevel) { // Quando o nível muda, executa esse código
                if (isScreenActivePower(context)) {
                    calculateOnTime();
                } else {
                    calculateOffTime();
                }

                totalScreenOnTime = totalScreenOnTime + totalOnThisPercent;
                totalScreenOffTime = totalScreenOffTime + totalOffThisPercent;
                int mahAverage = calculateMahAverage();
                float tempAverage = calculateTempAverage();

                usageHistory.add(
                        new UsageModel(lastLevel,
                                totalOnThisPercent,
                                totalOffThisPercent,
                                mahAverage,
                                tempAverage
                        )
                );

                if (persistData) {
                    historyDatabase.insertUsage(
                            new UsageModel(
                                    lastLevel,
                                    totalOnThisPercent,
                                    totalOffThisPercent,
                                    mahAverage,
                                    tempAverage
                            )
                    );
                }

                String shortAvgOnInMinutes = TimeConverter.getHumanTime(getAverage(true), false);
                String shortAvgOffInMinutes = TimeConverter.getHumanTime(getAverage(false), false);

                int targetPercent;
                if (plugged == DISCONNECTED) {
                    targetPercent = getDischargeTarget();
                } else {
                    targetPercent = getRechargeTarget();
                }
                String remainingToTargetOn = getRemainingToString(batteryPct, targetPercent, getAverage(true));
                String remainingToTargerOff = getRemainingToString(batteryPct, targetPercent, getAverage(false));

                updateNotificationUsage(shortAvgOnInMinutes, shortAvgOffInMinutes,
                        remainingToTargetOn, remainingToTargerOff, targetPercent
                );

                resetOnOffTime();
                lastLevel = batteryPct;
                if (isCallbackOk()) serviceCallback.updateUsageData();
                vibrateOnChange();
                vibrateOnReach();
            }

            if (lastPlugState == -1) {
                lastPlugState = plugged;
                updateNotificationUsage(
                        "--:--",
                        "--:--",
                        "--:--",
                        "--:--",
                        isCharging() ? getRechargeTarget() : getDischargeTarget()
                );
            }
            // Resetar medição ao disconectar e conectar carga
            if (lastPlugState != plugged) {
                lastPlugState = plugged;
                resetVariables();
                if (isCallbackOk()) serviceCallback.updateUsageData();
            }
        }
    };

    // -Xlint bug shows a deprecation here even if it seems to be OK...
    public static void vibrateFourTimes(Context context) {
        long[] pattern = {1000, 500, 1000, 500, 1000, 500, 1000, 500};
        if (Build.VERSION.SDK_INT >= 31) { // Android 12 and above
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            Vibrator vibrator = vibratorManager.getDefaultVibrator();
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                // vibration pattern (wait 1000ms, vibrate for 500ms, wait 1000ms...)
                if (Build.VERSION.SDK_INT >= 26) { // For Android 8.0 and above (API level 26+)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1)); // -1 indicates no repeat
                } else { // For devices below Android 8.0
                    vibrator.vibrate(pattern, -1);
                }
            }
        }
    }

    /**
     * Vibrate if reach dischargeTarget or rechargeTarget.
     */
    private void vibrateOnReach() {
        if (batteryPct >= rechargeTarget && batteryPct < rechargeTarget + 2 &&
                isCharging()) {
            vibrateFourTimes(this);
        } else if (batteryPct <= dischargeTarget && batteryPct > dischargeTarget - 2 &&
                !isCharging()) {
            vibrateFourTimes(this);
        }
    }

    /**
     * @return Retorna a média de temperatura a cada 1% de bateria perdido.
     * {@code @method} Calcula a média a cada 1% de bateria perdido, resetando a medição após uso.
     */
    private float calculateTempAverage() {
        getBatteryTemp();
        float tempAvg = (float) tempMetered / tempCounter / 10;
        resetTempMeter();
        return tempAvg;
    }

    private void resetTempMeter() {
        tempMetered = 0;
        tempCounter = 0;
    }

    /**
     * @return Retorna a média de corrente mAh a cada 1% de bateria perdido.
     * {@code @method} Calcula a média a cada 1% de bateria perdido, resetando a medição após uso.
     */
    private int calculateMahAverage() {
        getBatteryCurrent();
        int mahAvg = mahMetered / mahCounter;
        resetMahMeter();
        return mahAvg;
    }

    private void resetMahMeter() {
        mahMetered = 0;
        mahCounter = 0;
    }

    /**
     * @return Retorna a média de tempo com a tela ativa/inativa até perder 1%, contabiliza os últimos dados recebidos com base no tamanho médio de coleta definido pelo usuário.
     */
    private long getAverage(boolean isAverageOn) {
        long shortAverage = 0L;
        int breakCount = 0;
        for (int index = usageHistory.size() - 1; index >= 0; index--) {
            if (isAverageOn) {
                // Verifica se o valor atual é maior que 1 segundo && se o ativo é maior pro lado ON
                // se sim, adiciona o valor à média curta
                if ((usageHistory.get(index).timeOn > 999) &&
                        (usageHistory.get(index).timeOn > usageHistory.get(index).timeOff)) {
                    breakCount = breakCount + 1;
                    shortAverage = shortAverage +
                            usageHistory.get(index).timeOn;
                }
            } else {
                // Verifica se o valor atual é maior que 1 segundo && se o ativo é maior pro lado OFF
                // se sim, adiciona o valor à média curta
                if (usageHistory.get(index).timeOff > 999 &&
                        (usageHistory.get(index).timeOff > usageHistory.get(index).timeOn)) {
                    breakCount = breakCount + 1;
                    shortAverage = shortAverage +
                            usageHistory.get(index).timeOff;
                }
            }
            // Verifica se é hora de parar, se tivermos adicionado a quantidade
            // correta de valores, interrompe o loop
            if (breakCount >= baseAvgSize) {
                break;
            }
        }
        // Calculamos a média
        shortAverage = shortAverage / ((breakCount == 0) ? 1 : breakCount);
        return shortAverage;
    }

    public String getAverageString(boolean isAverageOn) {
        return TimeConverter.getHumanTime(getAverage(isAverageOn), false);
    }

    public String getRemainingCharge(boolean isRemainingOn) {
        String result;
        if (lastPlugState == DISCONNECTED) {
            result = getRemainingToString(batteryPct, getDischargeTarget(), getAverage(isRemainingOn));
        } else {
            result = getRemainingToString(batteryPct, getRechargeTarget(), getAverage(isRemainingOn));
        }
        return result;
    }


    /**
     * @return Return charging status based on lastPlugState variable, true if is charging.
     */
    public boolean isCharging() {
        return !(lastPlugState == DISCONNECTED);
    }

    private void resetVariables() {
        updateNotificationUsage("--:--", "--:--", "--:--", "--:--", isCharging() ? getRechargeTarget() : getDischargeTarget());
        usageHistory.clear();
        totalScreenOnTime = 0;
        totalScreenOffTime = 0;
        currentMin = 0;
        currentMax = 0;
        tempMin = 0;
        tempMax = 0;
        resetMahMeter();
        resetTempMeter();
        resetOnOffTime();
        if (persistData) historyDatabase.deleteAll();
    }

    private String getRemainingToString(int from, int to, long avg) {
        // avg 2min, from 80% to 20%
        // 60 * 2 = 120
        return TimeConverter.getHumanTime(avg * Math.abs(from - to), false);
    }

    private long getRemainingToLong(int from, int to, long avg) {
        return avg * Math.abs(from - to);
    }

    private static boolean isScreenActivePower(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false; // Unable to access PowerManager service
        }
        return powerManager.isInteractive();
    }


    private final BroadcastReceiver screenChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    calculateOffTime();

                    String timeInMinutes = TimeConverter.getHumanTime(totalScreenOffTime, false);
                    Log.d("TM-Inactive", timeInMinutes + " total: " + TimeConverter.getHumanTime(totalOffThisPercent, false));
                } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    calculateOnTime();

                    String timeInMinutes = TimeConverter.getHumanTime(totalScreenOnTime, false);
                    Log.d("TM-Active", timeInMinutes + " total: " + TimeConverter.getHumanTime(totalOnThisPercent, false));
                }
            }
        }
    };


    private void resetOnOffTime() {
        // Reseta o contador para marcar o gasto no percentual atual
        activeTimer = SystemClock.elapsedRealtime();
        inativeTimer = activeTimer;

        totalOffThisPercent = 0;
        totalOnThisPercent = 0;
    }

    private void calculateOnTime() {
        // We set inactiveTime here because it's when the screen goes off
        inativeTimer = SystemClock.elapsedRealtime();
        if (activeTimer == 0) activeTimer = serviceStartTime;
        // Screen time calculate from (actual)inactiveTimer - (last)activeTime
        totalOnThisPercent = totalOnThisPercent + (inativeTimer - activeTimer);
    }

    private void calculateOffTime() {
        // We set activeTime here because it's when screen goes on
        activeTimer = SystemClock.elapsedRealtime();
        if (inativeTimer == 0) inativeTimer = serviceStartTime;
        // Screen time calculate from (actual)activeTimer - (last)inactiveTime
        totalOffThisPercent = totalOffThisPercent + (activeTimer - inativeTimer);
    }

    public ArrayList<UsageModel> getDischargeList() {
        return usageHistory;
    }

    public String getTotalScreenTime(boolean isScreenOnTime) {
        return isScreenOnTime ? TimeConverter.getHumanTime(totalScreenOnTime, false)
                : TimeConverter.getHumanTime(totalScreenOffTime, false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().contentEquals(ServiceConstants.UPDATE_VARIABLES)) {
                stopTimer();
                loadPreferences();
                updateNotificationTarget(isCharging() ? rechargeTarget : dischargeTarget);
                startTimer();
                Log.i(TAG + "service", "update variables");
            } else if (intent.getAction().contentEquals(ServiceConstants.START_SERVICE)) {
                isServiceRunning = true;
                serviceStartTime = SystemClock.elapsedRealtime();
                batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);

                IntentFilter lockFilter = new IntentFilter();
                lockFilter.addAction(Intent.ACTION_SCREEN_ON);
                lockFilter.addAction(Intent.ACTION_SCREEN_OFF);

                registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                registerReceiver(screenChangeReceiver, lockFilter);
                startTimer();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        historyDatabase = new BackgroundDB(context);
        settings = new Settings(context);
        loadStrings();
        loadPreferences();
        loadHistory();


        notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_custom);
        createNotification(this);
    }

    private void loadStrings() {
        stringTargetOn = getString(R.string.notification_target_on);
        stringTargetOff = getString(R.string.notification_target_off);

        stringSunday = getString(R.string.day_sunday);
        stringMonday = getString(R.string.day_monday);
        stringTuesday = getString(R.string.day_tuesday);
        stringWednesday = getString(R.string.day_wednesday);
        stringThursday = getString(R.string.day_thursday);
        stringFriday = getString(R.string.day_friday);
        stringSaturday = getString(R.string.day_saturday);
    }

    private void loadPreferences() {
        rechargeTarget = settings.getMaxCharge();
        dischargeTarget = settings.getMinCharge();
        isVibrateOnChange = settings.getVibrateChange();
        baseAvgSize = settings.getAverageSize();
        mahTimerPeriod = settings.getMahTime();
        persistData = settings.getPersistData();
    }

    private void loadHistory() {
        if (persistData) {
            historyDatabase.setAllUsageCallback(history -> {
                usageHistory.addAll(history);
                for (UsageModel usageModel : history) {
                    totalScreenOnTime = totalScreenOnTime + usageModel.timeOn;
                    totalScreenOffTime = totalScreenOffTime + usageModel.timeOff;
                }
                if (isCallbackOk()) {
                    serviceCallback.updateUsageData();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryInfoReceiver);
        unregisterReceiver(screenChangeReceiver);
        stopTimer();
        isServiceRunning = false;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
