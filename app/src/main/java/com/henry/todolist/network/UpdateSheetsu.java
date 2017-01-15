package com.henry.todolist.network;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonWriter;
import android.util.Log;

import com.henry.todolist.database.TaskContract;
import com.henry.todolist.database.TaskDbContentProvider;
import com.henry.todolist.database.TaskModel;
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
 * Created by henry on 17/1/16.
 */
public class UpdateSheetsu {
    private static Context mContext;
    private static final String LOG_TAG = "UpdateSheetsu";

    public static void checkUpdate(Context context) {
        mContext = context;
        new BackgroundUpdateTask().execute("");
    }

    private static class BackgroundUpdateTask extends AsyncTask<String, Void, ArrayList<SheetsuModel>> {

        @Override
        protected ArrayList<SheetsuModel> doInBackground(String... strings) {
            HashMap<String, TaskModel> taskMap = DBUtils.loadTasksFromDB(mContext);
            for (TaskModel value : taskMap.values()) {
                if (value.getIsLocal() != 0) {
                    // Sync to sheetsu
                    StringWriter sw = new StringWriter();
                    JsonWriter jw = new JsonWriter(sw);
                    try {
                        jw.beginObject();
                        jw.name("datetime");
                        jw.value(value.getDatetime());
                        jw.name("tasks");
                        jw.value(value.getTasks());
                        jw.name("IsFinish");
                        jw.value(value.getIsFinish()? "TRUE" : "FALSE");
                        jw.name("uuid");
                        jw.value(value.getUuid());
                        jw.endObject();
                        jw.flush();

                        Log.d(LOG_TAG, "payload = " + sw.toString());

                        String url = "";
                        String method = "";
                        if (value.getIsLocal() == 1) {
                            url = Constants.SHEETSURL + "/uuid/" + value.getUuid();
                            method = "PUT";
                        } else {
                            url = Constants.SHEETSURL;
                            method = "POST";
                        }

                        makePostRequest(url, sw.toString(), method);
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "json write failed e = " + e);
                    }

                    // update database
                    ContentResolver resolver = mContext.getContentResolver();
                    if (resolver == null) {
                        Log.w(LOG_TAG, "Cannot get ContentResolver");
                        return null;
                    }

                    Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);
                    ContentValues updateValues = new ContentValues();
                    try {
                        if (updateValues != null) {
                            updateValues.put(TaskContract.TaskEntry.COL_TASK_LOCAL, 0);

                            String where = TaskContract.TaskEntry.COL_TASK_UUID + "=?";
                            String[] whereArgs = new String[] { value.getUuid() };
                            if (resolver.update(uri, updateValues, where, whereArgs) < 0) {
                                Log.w(LOG_TAG, "update " + value.getUuid() + " failed");
                            }
                        }
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "udpate failed" + e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<SheetsuModel> items) {

        }
    }

    private static String makePostRequest(String stringUrl, String payload, String method) throws IOException {
        URL url = new URL(stringUrl);
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        String line;
        StringBuffer jsonString = new StringBuffer();

        uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        uc.setRequestMethod(method);
        uc.setDoInput(true);
        uc.setInstanceFollowRedirects(false);
        uc.connect();
        OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
        writer.write(payload);
        writer.close();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            while((line = br.readLine()) != null){
                jsonString.append(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        uc.disconnect();

        Log.d(LOG_TAG, jsonString.toString());

        return jsonString.toString();
    }
}