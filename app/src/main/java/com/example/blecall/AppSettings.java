package com.example.blecall;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREF_NAME = "ble_settings";
    private static final String KEY_CHANNEL = "channel";

    public static void setChannel(Context context, int channel) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_CHANNEL, channel).apply();
    }

    public static int getChannel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_CHANNEL, 1); // default = 1
    }

    public static int getManufacturerId(Context context) {
        int ch = getChannel(context);
        return 0x1200 | (ch & 0xFF);
    }
}
