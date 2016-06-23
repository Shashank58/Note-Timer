package com.notetimer;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author shashankm
 */
public class TaskHelper {
    private static final String TAG = "Task Helper";
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"};
    private TextView timer;
    private int adapterPosition = -1;
    private Handler timerTick = new Handler();
    private int timeInSecs = -1;
    private Activity context;
    private Runnable run;

    public TaskHelper(Activity context) {
        this.context = context;
    }

    public List<Object> getTasks(Context context, int tasksNeeded) {
        List<Task> allTasks = new ArrayList<>(TaskDBHelper.getInstance()
                .getAllTasks(context));
        List<Object> relevantTasks = new ArrayList<>();

        if (allTasks.size() < 1) {
            return relevantTasks;
        }
        final String yesterday = "Yesterday";
        final String today = "Today";
        switch (tasksNeeded) {
            case AppConstants.TASKS_TODAY:
                relevantTasks.add(today);
                for (Task allTask : allTasks) {
                    if (isTaskCreatedToday(allTask.getCreatedAt())) {
                        relevantTasks.add(allTask);
                    }
                }
                break;

            case AppConstants.TASKS_YESTERDAY:
                relevantTasks.add(yesterday);
                for (Task allTask : allTasks) {
                    if (isTaskCreatedYesterday(allTask.getCreatedAt())) {
                        relevantTasks.add(allTask);
                    }
                }
                break;

            case AppConstants.ALL_TASKS:
                for (Task allTask : allTasks) {
                    if (isTaskCreatedToday(allTask.getCreatedAt())) {
                        checkAndAddTask(today, allTask, relevantTasks);
                    } else if (isTaskCreatedYesterday(allTask.getCreatedAt())) {
                        checkAndAddTask(yesterday, allTask, relevantTasks);
                    } else {
                        checkAndAddTask(getDay(allTask.getCreatedAt()), allTask, relevantTasks);
                    }
                }
                break;
        }
        return relevantTasks;
    }

    private void checkAndAddTask(String toBeChecked, Task task, List<Object> relevantTasks) {
        if (!relevantTasks.contains(toBeChecked)) {
            relevantTasks.add(toBeChecked);
        }
        relevantTasks.add(task);
    }

    private String getDay(String date) {
        String[] splitDate = date.split("-");
        String month = months[Integer.parseInt(splitDate[1])];
        return  splitDate[0]+ " " + month;
    }

    private boolean isTaskCreatedToday(String date) {
        return (getDate(date) - currentDate()) == 0;
    }

    private int currentDate() {
        return Integer.parseInt(AppUtils.getInstance().getCurrentDate()
                .split("-")[0]);
    }

    private int getDate(String date) {
        return Integer.parseInt(date.split("-")[0]);
    }

    private boolean isTaskCreatedYesterday(String date) {
        return currentDate() - getDate(date) == 1;
    }

    public void startTimer(TextView timer, int position, int startingTime) {
        this.timer = timer;
        this.adapterPosition = position;
        this.timeInSecs = startingTime;
        startTime();
    }

    public void stopTimer() {
        timerTick.removeCallbacks(run);
        adapterPosition = -1;
        timeInSecs = -1;
    }

    public String storeCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return df.format(c.getTime());
    }

    public int getTimeInSecs() {
        return timeInSecs;
    }

    public TextView getTimer() {
        return timer;
    }

    private void startTime() {
        run = new Runnable() {
            @Override
            public void run() {
                timer.setText(convertToReadableFormat());
                timerTick.postDelayed(this, 1000);
            }
        };
        timerTick.post(run);
    }

    private String convertToReadableFormat() {
        int secs = timeInSecs % 60;
        int mins = timeInSecs / 60;
        int hours = mins / 60;
        timeInSecs++;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }
}
