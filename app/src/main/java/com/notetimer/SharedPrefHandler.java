package com.notetimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * @author shashankm
 */
public class SharedPrefHandler {
    private static final int PRIVATE_MODE_PREFERENCE = 10;
    private static final String SHARED_PREF = "Note Timer";
    private static final String TAG = "SharedPrefHandler";

    public void saveTimeAndId(Context context, long taskId, String stoppedTime) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);
        Editor editor = pref.edit();
        editor.putString(AppConstants.STOPPED_TIME, stoppedTime);
        editor.putLong(AppConstants.TASK_ID, taskId);
        editor.apply();
    }

    public String getStoppedTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);

        return pref.getString(AppConstants.STOPPED_TIME, "");
    }

    public long getTaskId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);

        return pref.getLong(AppConstants.TASK_ID, -1);
    }

    public void deleteAllData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF,
                PRIVATE_MODE_PREFERENCE);
        Log.d(TAG, "deleteAllData: COming here");
        pref.edit().clear().apply();
    }
}
