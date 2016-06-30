package com.notetimer;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author shashankm
 */
public class AppUtils {
    private static AppUtils instance;
    private final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();

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

    public void itemClickAnimation(final View view) {
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).setInterpolator(DECCELERATE_INTERPOLATOR);
                        view.setPressed(true);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();
                        boolean isInside = (x > 0 && x < view.getWidth() && y > 0 && y < view.getHeight());
                        if (view.isPressed() != isInside) {
                            view.setPressed(isInside);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        view.animate().scaleX(1).scaleY(1).setInterpolator(DECCELERATE_INTERPOLATOR);
                        if (view.isPressed()) {
                            view.performClick();
                            view.setPressed(false);
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .show();
    }

    public void showKeyBoard(Context context, View view){
        if (view != null) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService
                    (Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        }
    }

    public String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return df.format(c.getTime());
    }
}
