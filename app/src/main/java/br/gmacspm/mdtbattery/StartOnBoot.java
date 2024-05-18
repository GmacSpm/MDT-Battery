package br.gmacspm.mdtbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import br.gmacspm.mdtbattery.config.Settings;
import br.gmacspm.mdtbattery.constants.ServiceConstants;
import br.gmacspm.mdtbattery.services.BatteryMonitorService;

public class StartOnBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) &&
                new Settings(context).getStartBoot()) {
            Intent batteryServiceIntent = new Intent(context, BatteryMonitorService.class);
            batteryServiceIntent.setAction(ServiceConstants.START_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(batteryServiceIntent);
            } else {
                context.startService(batteryServiceIntent);
            }
        }
    }
}
