package br.gmacspm.mdtbattery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.config.Settings;
import br.gmacspm.mdtbattery.constants.ServiceConstants;
import br.gmacspm.mdtbattery.dialog.CustomDialog;
import br.gmacspm.mdtbattery.room.background.BackgroundDB;
import br.gmacspm.mdtbattery.services.BatteryMonitorService;
import br.gmacspm.mdtbattery.utils.BatteryOptimization;

// TODO: Refatorar getStrings pra fora dos listeners
public class SettingsActivity extends AppCompatActivity {
    private Button buttonException;
    private Settings settings;
    private SettingsActivity context;
    private BackgroundDB historyDatabase;

    private void findViews() {
        SwitchCompat switchBoot = findViewById(R.id.settings_switch_boot);
        buttonException = findViewById(R.id.settings_button_exception);

        Button buttonMaxCharge = findViewById(R.id.settings_button_max_charge);
        Button buttonMinCharge = findViewById(R.id.settings_button_min_charge);
        SwitchCompat switchVibrate = findViewById(R.id.settings_switch_vibrate);

        Button buttonAvgSize = findViewById(R.id.settings_button_avg_size);
        Button buttonMahTime = findViewById(R.id.settings_button_mah);
        SwitchCompat switchPersist = findViewById(R.id.settings_switch_persist);
        Button buttonMahMult = findViewById(R.id.settings_button_mah_x);
        Button buttonCapacity = findViewById(R.id.settings_button_battery_mah);

        switchBoot.setChecked(settings.getStartBoot());
        switchBoot.setOnCheckedChangeListener((buttonView, isChecked) ->
                settings.setStartBoot(isChecked));

        buttonException.setText(BatteryOptimization.isIgnoringBatteryOptimizations(this) ? String.format(getString(R.string.battery_exception), getString(R.string.yes)) : String.format(getString(R.string.battery_exception), getString(R.string.no)));
        buttonException.setOnClickListener(v -> {
            // Abrir configurações de exceção de bateria.
            BatteryOptimization.openBatteryOptimizationSettings(SettingsActivity.this);
        });

        String strMaxCharge = getString(R.string.max_charge);
        String strDialogMaxCharge = getString(R.string.dialog_maximum_charge);
        buttonMaxCharge.setText(String.format(strMaxCharge, settings.getMaxCharge() + "%"));
        buttonMaxCharge.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(
                    context,
                    strDialogMaxCharge,
                    String.valueOf(settings.getMaxCharge())
            );
            customDialog.setListener(max -> {
                settings.setMaxCharge((int) max);
                buttonMaxCharge.setText(String.format(strMaxCharge, (int) max + "%"));
                updateService();
            });
            customDialog.show();
        });

        buttonMinCharge.setText(String.format(getString(R.string.min_charge), settings.getMinCharge() + "%"));
        buttonMinCharge.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(
                    context,
                    getString(R.string.dialog_minimum_charge),
                    String.valueOf(settings.getMinCharge())
            );
            customDialog.setListener(min -> {
                settings.setMinCharge((int) min);
                buttonMinCharge.setText(String.format(getString(R.string.min_charge), (int) min + "%"));
                updateService();
            });
            customDialog.show();
        });
        switchVibrate.setChecked(settings.getVibrateChange());
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setVibrateChange(isChecked);
            updateService();
        });

        buttonAvgSize.setText(String.format(getString(R.string.avg_base), settings.getAverageSize() + "%"));
        buttonAvgSize.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(
                    context,
                    getString(R.string.dialog_average_base_on),
                    String.valueOf(settings.getAverageSize()));
            customDialog.setListener(avg -> {
                settings.setAverageSize((int) avg);
                buttonAvgSize.setText(String.format(getString(R.string.avg_base), (int) avg + "%"));
                updateService();
            });
            customDialog.show();
        });

        buttonMahTime.setText(String.format(getString(R.string.mah_time), settings.getMahTime() / 1000 + "s"));
        buttonMahTime.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(
                    context,
                    getString(R.string.dialog_mah_collection_time),
                    String.valueOf(settings.getMahTime() / 1000)
            );
            customDialog.setListener(time -> {
                settings.setMahTime((int) time * 1000);
                buttonMahTime.setText(String.format(getString(R.string.mah_time), (int) time + "s"));
                updateService();
            });
            customDialog.show();
        });

        switchPersist.setChecked(settings.getPersistData());
        switchPersist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) historyDatabase.deleteAll();
            settings.setPersistData(isChecked);
        });

        buttonMahMult.setText(String.format(getString(R.string.mah_multiplier), settings.getMahMultiplier()));
        buttonMahMult.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(
                    context,
                    getString(R.string.dialog_mah_multiplier),
                    String.valueOf(settings.getMahMultiplier())
            );
            customDialog.setListener(multiplier -> {
                settings.setMahMultiplier(multiplier);
                buttonMahMult.setText(String.format(getString(R.string.mah_multiplier), multiplier));
                updateService();
            });
            customDialog.show();
        });

        buttonCapacity.setText(String.format(getString(R.string.capacidade_da_bateria), settings.getBatteryCapacity()));
        buttonCapacity.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(
                    context,
                    getString(R.string.dialog_battery_capacity),
                    String.valueOf(settings.getBatteryCapacity())
            );
            customDialog.setListener(capacity -> {
                settings.setBatteryCapacity(capacity);
                buttonCapacity.setText(String.format(getString(R.string.capacidade_da_bateria), capacity));
            });
            customDialog.show();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        context = this;
        settings = new Settings(this);
        historyDatabase = new BackgroundDB(context);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (buttonException != null) {
                buttonException.setText(
                        BatteryOptimization.isIgnoringBatteryOptimizations(context) ?
                                String.format(getString(R.string.battery_exception), getString(R.string.yes)) :
                                String.format(getString(R.string.battery_exception), getString(R.string.no)));
            }
        }, 4000);
    }

    private void updateService() {
        if (BatteryMonitorService.isServiceRunning) {
            Intent updateServiceIntent = new Intent(this, BatteryMonitorService.class);
            updateServiceIntent.setAction(ServiceConstants.UPDATE_VARIABLES);
            startService(updateServiceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
