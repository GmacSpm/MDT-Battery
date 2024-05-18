package br.gmacspm.mdtbattery.utils;

import android.content.Context;
import android.util.TypedValue;

import br.gmacspm.mdtbattery.R;

public class ThemeColors {
    public static int getPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int getTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColor, typedValue, true);
        return typedValue.data;
    }

    public static int getGraphColor(Context context) {

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.graphColor, typedValue, true);
        return typedValue.data;
    }
}
