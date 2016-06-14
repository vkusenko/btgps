package com.vkusenko.btgpslogger.common;

import android.content.Context;

import com.vkusenko.btgpslogger.R;

import java.text.DecimalFormat;

public class Strings {

    public static String getDistanceDisplay(Context context, double meters) {
        DecimalFormat df = new DecimalFormat("#.###");
        String result = df.format(meters) + " " + context.getString(R.string.meters);

        if (meters >= 1000) {
            result = df.format(meters / 1000) + " " + context.getString(R.string.kilometers);
        }
        return result;
    }
}
