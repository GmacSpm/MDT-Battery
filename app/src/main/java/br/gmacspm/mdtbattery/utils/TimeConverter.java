package br.gmacspm.mdtbattery.utils;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeConverter {
    private static final int ONE_SECOND = 1000;
    private static final int ONE_HOUR = 3600000;
    private static final int ONE_DAY = 86400000;

    @NonNull
    public static String getHumanTime(long millis, boolean showZeros) {
        String humanTime = "--:--";
        // Se for maior que ou igual a um dia (24h)
        if (millis >= ONE_DAY) {
            humanTime = String.format(Locale.US, "%02dd%02dh",
                    TimeUnit.MILLISECONDS.toDays(millis),
                    (TimeUnit.MILLISECONDS.toHours(millis) -
                            TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis))));
        }
        // Se for maior que ou igual a 1 hora (60min) e menor que 1 dia (24h)
        else if (millis >= ONE_HOUR && millis < ONE_DAY) {
            humanTime = String.format(Locale.US, "%02dh%02dm",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    (TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))));
        }
        // Se for maior que ou igual a 1s (1000ms) e menor que 1 hora (60min)
        else if (millis >= ONE_SECOND && millis < ONE_HOUR) {
            humanTime = String.format(Locale.US, "%02dm%02ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    (TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
        } else if (showZeros) {
            humanTime = "00m00s";
        }
        return humanTime;
    }

    public static float getFloatTime(long millis) {
        return millis / 60000f;
    }

    public static int getMinutes(long millis) {
        return (int) ((millis / (1000 * 60)) % 60);
    }

    public static int getHours(long millis) {
        return (int) ((millis / (1000 * 60 * 60)) % 24);
    }

    public static int getDays(long millis) {
        return (int) (millis / (1000 * 60 * 60 * 24));
    }

    public static float getSeconds(long millis) {
        return millis / 1000.0f;
    }
}
