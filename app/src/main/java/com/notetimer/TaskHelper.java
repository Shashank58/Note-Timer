package com.notetimer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
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
    private static int timeInSecs;
    private Runnable run;

    public List<Object> getTasks(Context context) {
        List<Task> allTasks = new ArrayList<>(TaskDBHelper.getInstance()
                .getAllTasks(context));
        List<Object> relevantTasks = new ArrayList<>();
        if (allTasks.size() < 1) {
            return relevantTasks;
        }
        Task runningTask = null;
        List<Task> pendingTasks = new ArrayList<>();
        List<Task> pausedTasks = new ArrayList<>();
        List<Task> finishedTasks = new ArrayList<>();
        for (Task allTask : allTasks) {
            switch (allTask.getTaskStatus()) {
                case AppConstants.TASK_STATUS_FINISHED:
                    finishedTasks.add(0, allTask);
                    break;

                case AppConstants.TASK_STATUS_IDLE:
                    pendingTasks.add(0, allTask);
                    break;

                case AppConstants.TASK_STATUS_PAUSED:
                    pausedTasks.add(0, allTask);
                    break;

                case AppConstants.TASK_STATUS_RUNNING:
                    runningTask = allTask;
                    break;
            }
        }

        return reOrderData(context, relevantTasks, runningTask, pendingTasks,
                pausedTasks, finishedTasks);
    }

    private List<Object> reOrderData(Context context, List<Object> relevantTasks,
             Task runningTask, List<Task> pendingTasks, List<Task> pausedTasks,
                                     List<Task> finishedTasks) {
        if (runningTask != null) {
            relevantTasks.add(context.getString(R.string.running_task));
            relevantTasks.add(runningTask);
        }
        if (pausedTasks.size() > 0) {
            relevantTasks.add(context.getString(R.string.paused_tasks));
            int pos = relevantTasks.size();
            for (Task pausedTask : pausedTasks) {
                relevantTasks.add(pos, pausedTask);
            }
        }
        if (pendingTasks.size() > 0) {
            relevantTasks.add(context.getString(R.string.pending_tasks));
            int pos = relevantTasks.size();
            for (Task pendingTask : pendingTasks) {
                relevantTasks.add(pos, pendingTask);
            }
        }
        if (finishedTasks.size() > 0) {
            relevantTasks.add(context.getString(R.string.finished_tasks));
            int pos = relevantTasks.size();
            for (Task finishedTask : finishedTasks) {
                relevantTasks.add(pos, finishedTask);
            }
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

    public void startTimer(TextView timer, int position, int startTime) {
        this.timer = timer;
        this.adapterPosition = position;
        timeInSecs = startTime;
        startTime();
    }

    public void stopTimer() {
        timerTick.removeCallbacks(run);
        adapterPosition = -1;
        timeInSecs = -1;
    }

    public int getTimeInSecs() {
        return timeInSecs;
    }

    public CharSequence getTimer() {
        return timer.getText();
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
        int mins = (timeInSecs / 60);
        int hours = mins / 60;
        mins = mins % 60;
        timeInSecs++;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public String convertToReadableFormat(int timeInSecs) {
        int secs = timeInSecs % 60;
        int mins = (timeInSecs / 60);
        int hours = mins / 60;
        mins = mins % 60;
        Log.d(TAG, "convertToReadableFormat: Hours - " + hours + " mins - " +
                mins + " secs - " + secs);
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public int calculateTimeDifference(String stoppedTime, int elapsedTime) {
        int timeInSecs = elapsedTime;
        String[] stopDateTime = stoppedTime.split(" ");
        String[] currentDateTime = AppUtils.getInstance().getCurrentDateTime().split(" ");
        String[] stopDate = stopDateTime[0].split("/");
        String[] currentDate = currentDateTime[0].split("/");
        String[] stopTime = stopDateTime[1].split(":");
        String[] currentTime = currentDateTime[1].split(":");

        int stoppedMonth = Integer.parseInt(stopDate[1]);
        int currentMonth = Integer.parseInt(currentDate[1]);
        int stoppedYear = Integer.parseInt(stopDate[2]);
        int currentYear = Integer.parseInt(currentDate[2]);
        int stoppedDay = Integer.parseInt(stopDate[0]);
        int currentDay = Integer.parseInt(currentDate[0]);

        int yearDiff = currentYear - stoppedYear;
        int monthDiff = currentMonth - stoppedMonth;
        int dayDiff = currentDay - stoppedDay;

        if (monthDiff < 0) {
            yearDiff--;
            monthDiff = (12 - stoppedMonth) + currentMonth;
        }

        if (dayDiff < 0) {
            monthDiff--;
            dayDiff = (getDaysInMonth(stoppedMonth, stoppedYear) - stoppedDay) + currentDay;
        }

        if (yearDiff > 0 || monthDiff > 0 || dayDiff > 4) {
            return -1;
        }
        timeInSecs += (dayDiff * 24 * 3600);

        int stoppedHour = Integer.parseInt(stopTime[0]);
        int currentHour = Integer.parseInt(currentTime[0]);
        int stoppedMin = Integer.parseInt(stopTime[1]);
        int currentMin = Integer.parseInt(currentTime[1]);
        int stoppedSec = Integer.parseInt(stopTime[2]);
        int currentSec = Integer.parseInt(currentTime[2]);

        int hourDiff = currentHour - stoppedHour;
        int minDiff = currentMin - stoppedMin;
        int secDiff = currentSec - stoppedSec;

        if (minDiff < 0) {
            hourDiff--;
            minDiff = (60 - stoppedMin) + currentMin;
        }

        if (secDiff < 0) {
            minDiff--;
            secDiff = (60 - stoppedSec) + currentSec;
        }
        timeInSecs += ((hourDiff * 3600) + (minDiff * 60) + secDiff);
        return timeInSecs;
    }

    private int getDaysInMonth(int stoppedMonth, int stoppedYear) {
        if (stoppedMonth == 2) {
            return ((stoppedYear % 400 == 0) || (stoppedYear % 100 != 0
                    && stoppedYear % 4 == 0)) ? 29 : 28;
        }

        if ((stoppedMonth < 8 && stoppedMonth % 2 != 0) || (stoppedMonth > 7
                && stoppedMonth % 2 == 0)) {
            return 31;
        }

        return 30;
    }
}
