package com.henry.todolist.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.JsonWriter;
import android.util.Log;

import com.henry.todolist.network.UpdateSheetsu;
import com.henry.todolist.sheetsu.SheetsuModel;
import com.henry.todolist.util.Constants;
import com.henry.todolist.util.DBUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by henry on 17/1/15.
 */
public class TaskDbContentObserver extends ContentObserver {
    private static final String LOG_TAG = "[ToDo] TaskDbContentObserver";
    private Context mContext;

    public TaskDbContentObserver(Handler handler, Context context) {
        super(handler);
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(LOG_TAG, "[onChange] uri = " + uri);

        UpdateSheetsu.checkUpdate(mContext);
    }
}
