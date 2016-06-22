package com.notetimer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author shashankm
 */
public class AppUtils {
    private static AppUtils instance;

    public static AppUtils getInstance() {
        if (instance == null) {
            instance = new AppUtils();
        }
        return instance;
    }

    public String getCurrentDate() {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(c.getTime());
    }
}
