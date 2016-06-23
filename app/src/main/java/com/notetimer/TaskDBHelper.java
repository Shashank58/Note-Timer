package com.notetimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shashankm
 */
public class TaskDBHelper {
    private static final String TAG = "TaskDBHelper";
    private static TaskDBHelper instance;

    public static TaskDBHelper getInstance() {
        if (instance == null) {
            instance = new TaskDBHelper();
        }
        return instance;
    }

    public long insertTask(Context context, Task task) {
        SQLiteDatabase db = TaskDB.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TaskDB.TASK_DESCRIPTION, task.getDescription());
        contentValues.put(TaskDB.TASK_IS_RUNNING, task.getIsRunning());
        contentValues.put(TaskDB.TASK_ELAPSED_TIME, task.getElapsedTime());
        contentValues.put(TaskDB.TASK_PAUSED_TIME, task.getPauseTime());
        contentValues.put(TaskDB.TASK_CREATED_AT, task.getCreatedAt());
        long id = db.insert(TaskDB.TASK_TABLE, null, contentValues);
        db.close();
        return id;
    }

    public List<Task> getAllTasks(Context context) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = TaskDB.getInstance(context).getWritableDatabase();
        String query = "SELECT * FROM " + TaskDB.TASK_TABLE;
        Cursor res = db.rawQuery(query, null);
        if (res.getCount() == 0) {
            closeDbAndCursor(db, res);
            return tasks;
        }
        while (res.moveToNext()) {
            Log.d(TAG, "getAllTasks: First - " + res.getString(0));
            Log.d(TAG, "getAllTasks: Last - " + res.getString(5));
            Task task = new Task(res.getString(1), res.getInt(4), res.getString(2),
                    res.getString(3), res.getString(5));
            task.setId(res.getInt(0));
            tasks.add(task);
        }
        closeDbAndCursor(db, res);
        return tasks;
    }

    private void closeDbAndCursor(SQLiteDatabase db, Cursor res) {
        db.close();
        res.close();
    }

    public void updateTimerStatus(Context context, long id, int isRunning) {
        SQLiteDatabase db = TaskDB.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TaskDB.TASK_IS_RUNNING, isRunning);
        db.update(TaskDB.TASK_TABLE, contentValues, TaskDB.TASK_ID + " = " + id, null);
    }
}
