package com.notetimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        contentValues.put(TaskDB.TASK_ELAPSED_TIME, task.getElapsedTime());
        contentValues.put(TaskDB.TASK_STATUS, task.getTaskStatus());
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
            Task task = new Task(res.getString(1), res.getInt(3), res.getInt(2), res.getString(4));
            task.setId(res.getInt(0));
            tasks.add(0, task);
        }
        closeDbAndCursor(db, res);
        return tasks;
    }

    private void closeDbAndCursor(SQLiteDatabase db, Cursor res) {
        db.close();
        res.close();
    }

    public void updateTimerStatus(Context context, long id, int taskStatus) {
        updateDB(context, taskStatus, id, TaskDB.TASK_STATUS);
    }

    public void updateTime(Context context, int time, long id) {
        updateDB(context, time, id, TaskDB.TASK_ELAPSED_TIME);
    }

    public void stopTimerForTask(Context context, int statusId, int time, long id) {
        updateDB(context, statusId, id, TaskDB.TASK_STATUS);
        updateDB(context, time, id, TaskDB.TASK_ELAPSED_TIME);
    }

    private void updateDB(Context context, int param, long id, String key) {
        SQLiteDatabase db = TaskDB.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, param);
        db.update(TaskDB.TASK_TABLE, contentValues, TaskDB.TASK_ID + " = " + id, null);
        db.close();
    }

    public void deleteTask(Context context, long id) {
        SQLiteDatabase db = TaskDB.getInstance(context).getWritableDatabase();
        db.delete(TaskDB.TASK_TABLE, TaskDB.TASK_ID + " =? ", new String[]{Long.toString(id)});
        db.close();
    }
}
