package com.notetimer;

import android.content.Context;
import android.support.annotation.UiThread;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author shashankm
 */
public class TaskHelper {
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"};
    private TextView timer;
    private int adapterPosition = -1;
    private Timer timerTick;
    private int timeInSecs = -1;

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
        timerTick.cancel();
        adapterPosition = -1;
        timeInSecs = -1;
        timer = null;
    }

    public TextView getTimer() {
        return timer;
    }

    private void startTime() {
        timerTick = new Timer();
        timerTick.scheduleAtFixedRate(new TimerTask() {
            @Override
            @UiThread
            public void run() {
                timer.setText(convertToReadableFormat());
            }
        }, 1000, 1000);
    }

    private String convertToReadableFormat() {
        int secs = timeInSecs % 60;
        int mins = timeInSecs / 60;
        int hours = mins / 60;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }
}
