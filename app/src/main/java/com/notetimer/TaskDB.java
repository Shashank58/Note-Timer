package com.notetimer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author shashankm
 */
public class TaskDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "task_activity.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TASK_TABLE = "tasks";
    public static final String TASK_DESCRIPTION = "description";
    public static final String TASK_ID = "id";
    public static final String TASK_ELAPSED_TIME = "elapsed_time";
    public static final String TASK_PAUSED_TIME = "paused_time";
    public static final String TASK_IS_RUNNING = "is_running";
    public static final String TASK_CREATED_AT = "created_at";
    private static final String CREATE_TABLE = "CREATE TABLE " + TASK_TABLE + "(" +
            TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK_DESCRIPTION + " TEXT, "
            + TASK_ELAPSED_TIME + " TEXT, " + TASK_PAUSED_TIME + " TEXT, " + TASK_IS_RUNNING
            + " INTEGER, " + TASK_CREATED_AT + " TEXT)";
    private static TaskDB instance;

    public TaskDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized TaskDB getInstance(Context context) {
        if (instance == null) {
            instance = new TaskDB(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
        onCreate(db);
    }
}
