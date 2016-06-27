package com.notetimer;

import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author shashankm
 */
public class TimerService extends IntentService {
    private static final String TAG = "Timer Service";
    private NotificationManager mNotificationManager;
    private PendingIntent pendingIntent, stopTaskPendingIntent;
    private static final int NOTIFICATION_ID = 1;
    private RemoteViews expandedView, smallView;

    public TimerService(String name) {
        super(name);
    }

    public TimerService() {
        super("Timer Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotificationManager = (NotificationManager) getSystemService
                (Context.NOTIFICATION_SERVICE);
        setPendingIntents();
        setUpNotification(intent.getStringExtra(AppConstants.TIME),
                intent.getStringExtra(AppConstants.DESCRIPTION));
    }

    private void setUpNotification(String time, String description) {
        expandedView = new RemoteViews(getPackageName(),
                R.layout.task_notif_expanded);
        smallView = new RemoteViews(getPackageName(),
                R.layout.task_notif_small);
        Log.d(TAG, "setUpNotification: Description - " + description);

        expandedView.setOnClickPendingIntent(R.id.done_with_task, stopTaskPendingIntent);
        smallView.setOnClickPendingIntent(R.id.task_done, stopTaskPendingIntent);

        expandedView.setTextViewText(R.id.task_time, time);
        smallView.setTextViewText(R.id.task_time_small, time);

        expandedView.setTextViewText(R.id.description, description);
        smallView.setTextViewText(R.id.description_small, description);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher);
        setNotificationView(notificationBuilder);
    }

    private void setNotificationView(Builder notificationBuilder) {
        Notification notification = notificationBuilder.build();
        notification.contentIntent = pendingIntent;
        notification.contentView = smallView;
        notification.bigContentView = expandedView;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void setPendingIntents() {
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT);
        stopTaskPendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(AppConstants.STOP_TASK), 0);
    }
}
