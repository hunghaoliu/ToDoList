package com.henry.todolist;

import android.app.ProgressDialog;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
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
import com.henry.todolist.network.UpdateSheetsu;
import com.henry.todolist.sheetsu.SheetsuModel;
import com.henry.todolist.ui.ModifyTaskActivity;
import com.henry.todolist.util.Constants;
import com.henry.todolist.util.DBUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "[ToDo] MainActivity";
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
                        updateValues.put(TaskContract.TaskEntry.COL_TASK_LOCAL, updateTask.getIsLocal() == Constants.SHEETSU_SYNC_NEED_INHSERT? Constants.SHEETSU_SYNC_NEED_INHSERT : Constants.SHEETSU_SYNC_NEED_UPDATE); // The record is not insert to sheetsu yet

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

        registerForContextMenu(mTaskListView);

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
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_todo) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            TaskModel thisItem = (TaskModel) mListAdapter.getItem(info.position);
            menu.setHeaderTitle(thisItem.getTasks());
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        if (menuItemIndex == 0) {
            // Edit a task
            Intent intent = new Intent(MainActivity.this, ModifyTaskActivity.class);
            TaskModel editTask = (TaskModel) mListAdapter.getItem(info.position);
            Bundle bundle = new Bundle();
            bundle.putString(TaskContract.TaskEntry.COL_TASK_UUID, editTask.getUuid());
            bundle.putString(TaskContract.TaskEntry.COL_TASK_DATE, editTask.getDatetime());
            bundle.putString(TaskContract.TaskEntry.COL_TASK_TITLE, editTask.getTasks());
            bundle.putBoolean(TaskContract.TaskEntry.COL_TASK_FINISHED, editTask.getIsFinish());
            bundle.putInt(TaskContract.TaskEntry.COL_TASK_LOCAL, editTask.getIsLocal());
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            // Delete a task
            ContentResolver resolver = getContentResolver();
            if (resolver == null) {
                Log.w(LOG_TAG, "Cannot get ContentResolver");
                return true;
            }

            Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);

            TaskModel deleteTask = (TaskModel) mListAdapter.getItem(info.position);
            try {
                ContentValues cv = new ContentValues();
                cv.put(TaskContract.TaskEntry.COL_TASK_LOCAL, Constants.SHEETSU_SYNC_NEED_DELETE); // The record is not insert to sheetsu yet
                Log.d(LOG_TAG, "Delete a task with uuid " + deleteTask.getUuid());

                String where = TaskContract.TaskEntry.COL_TASK_UUID + "=?";
                String[] whereArgs = new String[] { deleteTask.getUuid() };
                resolver.update(uri, cv, where, whereArgs);

                mListAdapter.deleteItem(deleteTask.getUuid());
            } catch (Exception e) {
                Log.w(LOG_TAG, "Edit task failed" + e);
            }
        }


        return true;
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
        private ProgressDialog waitingDialog;

        public BackgroundDownloadTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {

            waitingDialog = new ProgressDialog(mContext);
            waitingDialog.setMessage(mContext.getResources().getString(R.string.please_wait));
            waitingDialog.show();

        }


        @Override
        protected ArrayList<SheetsuModel> doInBackground(String... url) {
            Collection<SheetsuModel> model = HttpHelper.loadJSON(url[0]);
            if (model != null) {
                todoItems = new ArrayList<>(model);
                if (todoItems != null && todoItems.size() > 0) {
                    Log.d(LOG_TAG, todoItems.get(0).getDatetime());
                }
            } else {
                Log.w(LOG_TAG, "loadJSON return null");
            }

            return todoItems;
        }

        @Override
        protected void onPostExecute(ArrayList<SheetsuModel> items) {

            HashMap<String, TaskModel> taskMap = DBUtils.loadTasksFromDB(mContext);
            boolean needSync = false;

            if (items == null)
                return;

            // Compare task list from server with local db
            for (int i = 0; i < items.size(); i++) {
                SheetsuModel thisItem = items.get(i);
                if (thisItem == null)
                    continue;

                if (TextUtils.isEmpty(thisItem.getUuid()))
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
                    if (localTask.getIsLocal() != Constants.SHEETSU_SYNC_ALREADY) {
                        // local is updated, need to sync to sheetsu
                        needSync = true;
                    }
                }
            }

            if (needSync) {
                UpdateSheetsu.checkUpdate(mContext);
            }

            // Add to adapter
            for (TaskModel value : taskMap.values()) {
                mListAdapter.addItem(value);
            }

            if (waitingDialog.isShowing()) {
                waitingDialog.dismiss();
            }
        }
    }
}

