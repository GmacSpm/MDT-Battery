package br.gmacspm.mdtbattery.config;

import android.content.Context;
import android.content.SharedPreferences;

import br.gmacspm.mdtbattery.constants.Constants;

public class Settings {
    private final SharedPreferences prefs;

    public Settings(Context context) {
        prefs = context.getSharedPreferences(Constants.APP_CONFIG, Context.MODE_PRIVATE);
    }

    public boolean getStartBoot() {
        return prefs.getBoolean(Constants.START_ON_BOOT, true);
    }

    public void setStartBoot(boolean isStart) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.START_ON_BOOT, isStart);
        editor.apply();
    }

    public boolean getVibrateChange() {
        return prefs.getBoolean(Constants.VIBRATE_ON_CHANGE, true);
    }

    public void setVibrateChange(boolean isVibrate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.VIBRATE_ON_CHANGE, isVibrate);
        editor.apply();
    }

    public int getMaxCharge() {
        return prefs.getInt(Constants.RECHARGE_TARGET, 100);
    }

    public void setMaxCharge(int max) {
        if (max >= 1 && max <= 100) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.RECHARGE_TARGET, max);
            editor.apply();
        }
    }

    public int getMinCharge() {
        return prefs.getInt(Constants.DISCHARGE_TARGET, 1);
    }

    public void setMinCharge(int min) {
        if (min >= 1 && min <= 90) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.DISCHARGE_TARGET, min);
            editor.apply();
        }
    }

    public int getAverageSize() {
        return prefs.getInt(Constants.AVERAGE_SIZE, 2);
    }

    public void setAverageSize(int avg) {
        if (avg >= 2 && avg <= 10) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.AVERAGE_SIZE, avg);
            editor.apply();
        }
    }

    public int getMahTime() {
        return prefs.getInt(Constants.MAH_TIME, 6000);
    }

    public void setMahTime(int time) {
        if (time >= 1 && time <= 120) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.MAH_TIME, time);
            editor.apply();
        }
    }

    public float getMahMultiplier() {
        return prefs.getFloat(Constants.MAH_MULTIPLIER, 1.0f);
    }

    public void setMahMultiplier(float multiplier) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(Constants.MAH_MULTIPLIER, multiplier);
        editor.apply();
    }

    public boolean getPersistData() {
        return prefs.getBoolean(Constants.PERSIST_DATA, false);
    }

    public void setPersistData(boolean isPersist) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PERSIST_DATA, isPersist);
        editor.apply();
    }

    public float getBatteryCapacity() {
        return prefs.getFloat(Constants.BATTERY_CAPACITY, 0.0f);
    }

    public void setBatteryCapacity(float capacity) {
        if (capacity >= 1000) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(Constants.BATTERY_CAPACITY, capacity);
            editor.apply();
        }
    }
}
