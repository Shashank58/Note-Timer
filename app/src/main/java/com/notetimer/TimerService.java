package com.notetimer;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

/**
 * @author shashankm
 */
public class TimerService extends Service {
    private static final String TAG = "Timer Service";
    private NotificationManager mNotificationManager;
    private PendingIntent pendingIntent, doneTaskPendingIntent, deletePendingIntent;
    private static final int NOTIFICATION_ID = 1;
    private RemoteViews expandedView, smallView;
    private Handler timerTick = new Handler();
    private Runnable run;
    private Notification notification;
    private int timeInSecs;
    private long taskId;

    @Override
    public void onCreate() {
        super.onCreate();
        setPendingIntents();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (AppConstants.STOP_NOTIFICATION.equals(action)) {
            removeNotification();
        } else {
            mNotificationManager = (NotificationManager) getSystemService
                    (Context.NOTIFICATION_SERVICE);

            timeInSecs = intent.getIntExtra(AppConstants.TIME_IN_SECS, -1);
            taskId = intent.getLongExtra(AppConstants.TASK_ID, -1);
            setUpNotification(intent.getStringExtra(AppConstants.TIME),
                    intent.getStringExtra(AppConstants.DESCRIPTION));
            startTime();
        }
        return START_NOT_STICKY;
    }

    private void setUpNotification(String time, String description) {
        expandedView = new RemoteViews(getPackageName(),
                R.layout.task_notif_expanded);
        smallView = new RemoteViews(getPackageName(),
                R.layout.task_notif_small);

        expandedView.setOnClickPendingIntent(R.id.done_with_task, doneTaskPendingIntent);
        smallView.setOnClickPendingIntent(R.id.task_done, doneTaskPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.remove_notification, deletePendingIntent);
        expandedView.setTextViewText(R.id.task_time, time);
        smallView.setTextViewText(R.id.task_time_small, time);
        expandedView.setTextViewText(R.id.description, description);
        smallView.setTextViewText(R.id.description_small, description);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true).setDeleteIntent(deletePendingIntent);

        setNotificationView(notificationBuilder);
    }

    private void startTime() {
        run = new Runnable() {
            @Override
            public void run() {
                String time = convertToReadableFormat();
                expandedView.setTextViewText(R.id.task_time, time);
                smallView.setTextViewText(R.id.task_time_small, time);
                mNotificationManager.notify(NOTIFICATION_ID, notification);
                timerTick.postDelayed(run, 1000);
            }
        };
        timerTick.post(run);
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppConstants.REMOVE_NOTIFICATION:
                    removeNotification();
                    break;

                case AppConstants.DONE_TASK:
                    if (taskId != -1) {
                        TaskDBHelper.getInstance().stopTimerForTask(context, 1,
                                timeInSecs, taskId);
                    }
                    removeNotification();
                    break;

                case AppConstants.SHOW_TASKS:
                    removeNotification();
                    Intent showTasks = new Intent(context, TasksActivity.class);
                    showTasks.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    showTasks.addFlags(PendingIntent.FLAG_UPDATE_CURRENT);
                    showTasks.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(showTasks);
                    break;
            }
        }
    };

    private void removeNotification() {
        if (run != null) {
            timerTick.removeCallbacks(run);
        }
        unregisterReceiver(broadcastReceiver);
        stopSelf();
    }

    private String convertToReadableFormat() {
        int secs = timeInSecs % 60;
        int mins = (timeInSecs / 60);
        int hours = mins / 60;
        mins = mins % 60;
        timeInSecs++;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    private void setNotificationView(Builder notificationBuilder) {
        notification = notificationBuilder.build();
        notification.contentIntent = pendingIntent;
        notification.contentView = smallView;
        notification.bigContentView = expandedView;

        startForeground(NOTIFICATION_ID, notification);
    }

    private void setPendingIntents() {
        pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(AppConstants.SHOW_TASKS), 0);
        doneTaskPendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(AppConstants.DONE_TASK), 0);
        deletePendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(AppConstants.REMOVE_NOTIFICATION), 0);
        registerReceiverWithFilters();
    }

    private void registerReceiverWithFilters() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.REMOVE_NOTIFICATION);
        intentFilter.addAction(AppConstants.DONE_TASK);
        intentFilter.addAction(AppConstants.SHOW_TASKS);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
