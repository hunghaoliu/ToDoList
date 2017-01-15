package com.henry.todolist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.henry.todolist.adapter.ListAdapter;
import com.henry.todolist.database.TaskContract;
import com.henry.todolist.database.TaskDbContentObserver;
import com.henry.todolist.database.TaskDbContentProvider;
import com.henry.todolist.database.TaskModel;
import com.henry.todolist.network.HttpHelper;
import com.henry.todolist.sheetsu.SheetsuModel;
import com.henry.todolist.ui.ModifyTaskActivity;
import com.henry.todolist.util.Constants;
import com.henry.todolist.util.DBUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private ListView mTaskListView;
    private ListAdapter mListAdapter;
    private TaskDbContentObserver taskObserver;
    private static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add task
                Intent intent = new Intent(MainActivity.this, ModifyTaskActivity.class);
                startActivity(intent);
            }
        });

        mListAdapter = new ListAdapter(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        mTaskListView.setAdapter(mListAdapter);
        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListAdapter.checked(i);

                // update database
                ContentResolver resolver = getContentResolver();
                if (resolver == null) {
                    Log.w(LOG_TAG, "Cannot get ContentResolver");
                    return;
                }

                Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);
                ContentValues updateValues = new ContentValues();
                try {
                    if (updateValues != null) {
                        TaskModel updateTask = (TaskModel) mListAdapter.getItem(i);
                        updateValues.put(TaskContract.TaskEntry.COL_TASK_FINISHED, updateTask.getIsFinish());
                        updateValues.put(TaskContract.TaskEntry.COL_TASK_LOCAL, updateTask.getIsLocal() == 2? 2 : 1); // The record is not insert to sheetsu yet

                        String where = TaskContract.TaskEntry.COL_TASK_UUID + "=?";
                        String[] whereArgs = new String[] { updateTask.getUuid() };
                        if (resolver.update(uri, updateValues, where, whereArgs) < 0) {
                            Log.w(LOG_TAG, "update " + updateTask.getUuid() + " failed");
                        }
                    }
                } catch (Exception e) {
                    Log.w(LOG_TAG, "udpate failed" + e);
                }

                // update sheetsu
            }
        });

        mTaskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // edit task
                Intent intent = new Intent(MainActivity.this, ModifyTaskActivity.class);
                TaskModel editTask = (TaskModel) mListAdapter.getItem(i);
                Bundle bundle = new Bundle();
                bundle.putString(TaskContract.TaskEntry.COL_TASK_UUID, editTask.getUuid());
                bundle.putString(TaskContract.TaskEntry.COL_TASK_DATE, editTask.getDatetime());
                bundle.putString(TaskContract.TaskEntry.COL_TASK_TITLE, editTask.getTasks());
                bundle.putBoolean(TaskContract.TaskEntry.COL_TASK_FINISHED, editTask.getIsFinish());
                bundle.putInt(TaskContract.TaskEntry.COL_TASK_LOCAL, editTask.getIsLocal());
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE);

                return true;
            }
        });

        taskObserver = new TaskDbContentObserver(new Handler(), this);
        Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);
        getContentResolver().registerContentObserver(uri, true, taskObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        new BackgroundDownloadTask(this).execute(Constants.SHEETSURL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(taskObserver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String uuid = bundle.getString(TaskContract.TaskEntry.COL_TASK_UUID);
                mListAdapter.deleteItem(uuid);
            }
        }
    }

    public class BackgroundDownloadTask extends AsyncTask<String, Void, ArrayList<SheetsuModel>> {
        ArrayList<SheetsuModel> todoItems = new ArrayList<>();
        protected Context mContext;

        public BackgroundDownloadTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected ArrayList<SheetsuModel> doInBackground(String... url) {
            Collection<SheetsuModel> model = HttpHelper.loadJSON(url[0]);
            todoItems = new ArrayList<>(model);
            if (todoItems.size() > 0) {
                Log.d(LOG_TAG, todoItems.get(0).getDatetime());
            }

            return todoItems;
        }

        @Override
        protected void onPostExecute(ArrayList<SheetsuModel> items) {

            HashMap<String, TaskModel> taskMap = DBUtils.loadTasksFromDB(mContext);

            // Compare task list from server with local db
            for (int i = 0; i < items.size(); i++) {
                SheetsuModel thisItem = items.get(i);
                if (thisItem == null)
                    continue;

                if (!taskMap.containsKey(thisItem.getUuid())) {
                    // insert to db
                    Log.d(LOG_TAG, "Insert " + thisItem.getUuid() + " to db");

                    ContentResolver resolver = getContentResolver();
                    if (resolver == null) {
                        Log.w(LOG_TAG, "Cannot get ContentResolver");
                        continue;
                    }

                    Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);

                    try {
                        ContentValues cv = new ContentValues();
                        cv.put(TaskContract.TaskEntry.COL_TASK_DATE, thisItem.getDatetime());
                        cv.put(TaskContract.TaskEntry.COL_TASK_TITLE, thisItem.getTasks());
                        cv.put(TaskContract.TaskEntry.COL_TASK_FINISHED, thisItem.getIsFinish().equals("TRUE"));
                        cv.put(TaskContract.TaskEntry.COL_TASK_LOCAL, 0);
                        cv.put(TaskContract.TaskEntry.COL_TASK_UUID, thisItem.getUuid());
                        resolver.insert(uri, cv);
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "Insert task failed" + e);
                    }

                    // add to HashMap
                    taskMap.put(thisItem.getUuid(),
                            new TaskModel(thisItem.getDatetime(), thisItem.getTasks(),
                                          thisItem.getIsFinish().equals("TRUE"), 0, thisItem.getUuid()));
                } else {
                    // check local db is updated then sheetsu one
                    TaskModel localTask = taskMap.get(thisItem.getUuid());
                    if (localTask.getIsLocal() != 0) {
                        // local is updated, need to sync to sheetsu

                    }
                }
            }

            // Add to adapter
            for (TaskModel value : taskMap.values()) {
                mListAdapter.addItem(value);
            }
        }
    }
}

