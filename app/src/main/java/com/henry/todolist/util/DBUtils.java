package com.henry.todolist.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.henry.todolist.database.TaskContract;
import com.henry.todolist.database.TaskDbContentProvider;
import com.henry.todolist.database.TaskModel;

import java.util.HashMap;

/**
 * Created by henry on 17/1/15.
 */
public class DBUtils {
    private static final String LOG_TAG = "DBUtils";

    public static HashMap<String, TaskModel> loadTasksFromDB(Context context) {
        HashMap<String, TaskModel> taskMap = new HashMap<>();

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            Log.w(LOG_TAG, "Cannot get ContentResolver");
            return taskMap;
        }

        Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);
        Cursor cursor = null;
        cursor = resolver.query(uri, null, null, null, null);

        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DATE);
            String datetime = cursor.getString(idx);
            idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            String task = cursor.getString(idx);
            idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_FINISHED);
            int isFinish = cursor.getInt(idx);
            idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_LOCAL);
            int isLocal = cursor.getInt(idx);
            idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_UUID);
            String uuid = cursor.getString(idx);

            taskMap.put(uuid, new TaskModel(datetime, task, isFinish == 1, isLocal, uuid));
        }
        cursor.close();

        return taskMap;
    }
}
