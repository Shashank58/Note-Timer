package com.notetimer;

/**
 * Created by shashankm on 22/06/16.
 */
public class AppConstants {
    public static final int TYPE_TITLE = 0;
    public static final int TYPE_TASK = 1;
    public static final int TASKS_TODAY = 2;
    public static final int TASKS_YESTERDAY = 3;
    public static final int ALL_TASKS = 5;

    /**
     * Task statuses
     */
    public static final int TASK_STATUS_RUNNING = 20;
    public static final int TASK_STATUS_IDLE = 25;
    public static final int TASK_STATUS_PAUSED = 30;
    public static final int TASK_STATUS_FINISHED = 35;

    /**
     * Intent extras constants
     */
    public static final String STOPPED_TIME = "Stopped time";
    public static final String ADAPTER_POSITION = "Adapter position";
    public static final String TIME = "Time";
    public static final String TASK_ID = "Task Id";
    public static final String DESCRIPTION = "Description";
    public static final String TIME_IN_SECS = "Time in secs";
    public static final String STOP_NOTIFICATION = "Stop notification";

    /**
     * Broadcast constants
     */
    private static final String PACKAGE_NAME = "com.notetimer";
    public static final String DONE_TASK = PACKAGE_NAME + ".done";
    public static final String REMOVE_NOTIFICATION = PACKAGE_NAME + ".remove";
    public static final String SHOW_TASKS = PACKAGE_NAME + ".show.task";
}
