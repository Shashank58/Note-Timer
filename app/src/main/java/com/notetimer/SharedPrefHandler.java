package com.notetimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author shashankm
 */
public class SharedPrefHandler {
    private static final int PRIVATE_MODE_PREFERENCE = 10;
    private static final String SHARED_PREF = "Note Timer";

    public void saveTimeAndPosition(Context context, int position, String stoppedTime) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);
        Editor editor = pref.edit();
        editor.putString(AppConstants.STOPPED_TIME, stoppedTime);
        editor.putInt(AppConstants.ADAPTER_POSITION, position);
        editor.apply();
    }

    public String getStoppedTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);

        return pref.getString(AppConstants.STOPPED_TIME, "");
    }

    public int getAdapterPosition(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);

        return pref.getInt(AppConstants.ADAPTER_POSITION, -1);
    }

    public void deleteAllData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);
        pref.edit().clear().apply();
    }
}
