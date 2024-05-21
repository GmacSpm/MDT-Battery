package br.gmacspm.mdtbattery;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.utils.TimeConverter;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


public class ExampleUnitTest {

    public String getTimeEndOn(long millis) {
        Calendar currentTime = Calendar.getInstance();
        int hourNow = currentTime.get(Calendar.HOUR_OF_DAY);
        int minuteNow = currentTime.get(Calendar.MINUTE);
        String result = "--:--";

        // Add time to current time now
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
                    dayOfWeek = "Dom";
                    break;
                case Calendar.MONDAY:
                    dayOfWeek = "Seg";
                    break;
                case Calendar.TUESDAY:
                    dayOfWeek = "Ter";
                    break;
                case Calendar.WEDNESDAY:
                    dayOfWeek = "Qua";
                    break;
                case Calendar.THURSDAY:
                    dayOfWeek = "Qui";
                    break;
                case Calendar.FRIDAY:
                    dayOfWeek = "Sex";
                    break;
                case Calendar.SATURDAY:
                    dayOfWeek = "Sáb";
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

    public long getShortAvgOn(ArrayList<UsageModel> percentOnOffHistory, int size) {
        long shortAverage = 0L;
        int breakCount = 0;
        // Somente executa caso o tamanho do array seja maior que o size exigido
        for (int index = percentOnOffHistory.size() - 1; index >= 0; index--) {
            // Verifica se o valor atual é maior que 1 segundo
            // se sim, adiciona o valor à média curta
            if ((percentOnOffHistory.get(index).timeOn > 999) &&
                    (percentOnOffHistory.get(index).timeOn > percentOnOffHistory.get(index).timeOff)) {
                breakCount = breakCount + 1;
                shortAverage = shortAverage +
                        percentOnOffHistory.get(index).timeOn;
                System.out.println(breakCount);
            }
            // Verifica se é hora de parar, se tivermos adicionado a quantidade
            // correta de valores, interrompe o loop
            if (breakCount >= size) {
                break;
            }
        }
        // Calculamos a média
        shortAverage = shortAverage / ((breakCount == 0) ? 1 : breakCount);
        return shortAverage;
    }


    public String getTimeEndOn() {
        Calendar currentTime = Calendar.getInstance();

        // Add 1 hour and 30 minutes
        long hours = 5400000;
        long minutes = 5400000;
        int lastPlugState = 0;
        currentTime.add(Calendar.HOUR_OF_DAY, TimeConverter.getHours(hours));
        currentTime.add(Calendar.MINUTE, TimeConverter.getMinutes(minutes));

        // Get the updated time
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        return hour + ":" + minute;
    }

    private long getShortAvg(ArrayList<UsageModel> percentOnOffHistory, int size) {
        long shortAverage = 0L;
        int breakCount = 0;

        if (percentOnOffHistory.size() >= size) {
            for (int index = percentOnOffHistory.size() - 1; index >= 0; index--) {
                if (percentOnOffHistory.get(index).timeOn > 999) {
                    breakCount = breakCount + 1;
                    shortAverage = shortAverage +
                            percentOnOffHistory.get(index).timeOn;
                }
                if (breakCount >= size) {
                    break;
                }
            }
            shortAverage = shortAverage / size;
        }
        return shortAverage;
    }

    private int getDischargeTarget(int batteryPct) {
        int dischargeTarget = 20;
        if (batteryPct < 20) {
            dischargeTarget = 1;
        }
        return dischargeTarget;
    }

    private int getRechargeTarget(int batteryPct) {
        int rechargeTarget = 80;
        if (batteryPct > 80) {
            rechargeTarget = 100;
        }
        return rechargeTarget;
    }
    @Test
    public void isBatteryCorrectTarget() {
//        assertEquals(80, getRechargeTarget(80));
//        assertEquals(20, getDischargeTarget(19));
    }

    @Test
    public void isTimeConverterOK() {
//        assertEquals("00m01s", TimeConverter.getHumanTime(1000, false));
//        assertEquals("--:--", TimeConverter.getHumanTime(999, false));
//        assertEquals("01h00m", TimeConverter.getHumanTime(3600000, false));
//        assertEquals("01d00h", TimeConverter.getHumanTime(86400000, false));
//        assertEquals("01d06h", TimeConverter.getHumanTime(108000000, false));
//        assertEquals("02d12h", TimeConverter.getHumanTime(216000000, false));


    }

    @Test
    public void isTimeEndOk() {
        assertEquals("1 dia", getTimeEndOn(86400000 * 2));
    }


    @Test
    public void isShortAverageCorrect() {
        ArrayList<UsageModel> percent = new ArrayList<>();

        ArrayList<UsageModel> percent2 = new ArrayList<>();


        // assertEquals(4000, getShortAvg(percent, 3));

        // assertEquals(4000, getShortAvgOn(percent, 10));

        // assertEquals(5000, getShortAvgOn(percent2, 10));
        // assertEquals(5000, getShortAvgOn(percent2, 2));
    }

    @Test
    public void isTimeEndCorrect() {
       // assertEquals("40", getTimeEndOn());
    }

    @Test
    public void isDayCorrect() {
       // assertEquals(55, TimeConverter.getDays(200000000));
    }
}