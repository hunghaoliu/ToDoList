package com.henry.todolist.ui;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.henry.todolist.R;
import com.henry.todolist.database.TaskContract;
import com.henry.todolist.database.TaskDbContentProvider;
import com.henry.todolist.util.Constants;

import java.util.Calendar;
import java.util.UUID;

public class ModifyTaskActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG_TAG = "ModifyTaskActivity";

    private EditText mEditDate;
    private EditText mEditContent;
    private CheckBox mCheckIsFinish;
    private DatePickerDialog mDatePickerDialog;
    private String mUuid = "";
    private String mDate = "";
    private String mTitle = "";
    private boolean mIsFinish = false;
    private int mIsLocal = 0;
    private boolean mIsEditTask = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUuid = bundle.getString(TaskContract.TaskEntry.COL_TASK_UUID);
            mDate = bundle.getString(TaskContract.TaskEntry.COL_TASK_DATE);
            mTitle = bundle.getString(TaskContract.TaskEntry.COL_TASK_TITLE);
            mIsFinish = bundle.getBoolean(TaskContract.TaskEntry.COL_TASK_FINISHED);
            mIsLocal = bundle.getInt(TaskContract.TaskEntry.COL_TASK_LOCAL);
            mIsEditTask = true;
            setTitle(R.string.title_activity_edit_task);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // insert task to db
                if (mEditContent.getText().toString().equals("")) {
                    Toast.makeText(ModifyTaskActivity.this, R.string.no_empty_task_content, Toast.LENGTH_SHORT).show();
                } else {
                    if (mIsEditTask) {
                        ContentResolver resolver = getContentResolver();
                        if (resolver == null) {
                            Log.w(LOG_TAG, "Cannot get ContentResolver");
                            return;
                        }

                        Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);

                        try {
                            ContentValues cv = new ContentValues();
                            cv.put(TaskContract.TaskEntry.COL_TASK_DATE, mEditDate.getText().toString());
                            cv.put(TaskContract.TaskEntry.COL_TASK_TITLE, mEditContent.getText().toString());
                            cv.put(TaskContract.TaskEntry.COL_TASK_FINISHED, mCheckIsFinish.isChecked());
                            cv.put(TaskContract.TaskEntry.COL_TASK_LOCAL, mIsLocal == 2? 2 : 1); // The record is not insert to sheetsu yet
                            cv.put(TaskContract.TaskEntry.COL_TASK_UUID, mUuid);
                            Log.d(LOG_TAG, "Edit a task with uuid " + mUuid);

                            String where = TaskContract.TaskEntry.COL_TASK_UUID + "=?";
                            String[] whereArgs = new String[] { mUuid };
                            resolver.update(uri, cv, where, whereArgs);
                        } catch (Exception e) {
                            Log.w(LOG_TAG, "Edit task failed" + e);
                        }

                        Intent intent = new Intent();
                        Bundle retBundle = new Bundle();
                        retBundle.putString(TaskContract.TaskEntry.COL_TASK_UUID, mUuid);
                        intent.putExtras(retBundle);
                        setResult(RESULT_OK, intent);
                    } else {
                        ContentResolver resolver = getContentResolver();
                        if (resolver == null) {
                            Log.w(LOG_TAG, "Cannot get ContentResolver");
                            return;
                        }

                        Uri uri = Uri.parse("content://" + TaskDbContentProvider.AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);

                        try {
                            ContentValues cv = new ContentValues();
                            cv.put(TaskContract.TaskEntry.COL_TASK_DATE, mEditDate.getText().toString());
                            cv.put(TaskContract.TaskEntry.COL_TASK_TITLE, mEditContent.getText().toString());
                            cv.put(TaskContract.TaskEntry.COL_TASK_FINISHED, 0);
                            cv.put(TaskContract.TaskEntry.COL_TASK_LOCAL, 2);
                            String uuid = UUID.randomUUID().toString();
                            cv.put(TaskContract.TaskEntry.COL_TASK_UUID, uuid);
                            Log.d(LOG_TAG, "Add a new task with uuid " + uuid);
                            resolver.insert(uri, cv);
                        } catch (Exception e) {
                            Log.w(LOG_TAG, "Insert task failed" + e);
                        }
                    }

                    finish();
                }
            }
        });

        mEditDate = (EditText) findViewById(R.id.add_task_date);
        mEditDate.setInputType(InputType.TYPE_NULL);
        mEditDate.setOnClickListener(this);

        mEditContent = (EditText) findViewById(R.id.add_task_content);

        mCheckIsFinish = (CheckBox) findViewById(R.id.add_task_isfinish);

        Calendar calendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                mEditDate.setText(year + "-" + (month + 1) + "-" + day);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsEditTask) {
            mEditDate.setText(mDate);
            mEditContent.setText(mTitle);
            mCheckIsFinish.setChecked(mIsFinish);
            mCheckIsFinish.setVisibility(View.VISIBLE);
        } else {
            Calendar today = Calendar.getInstance();
            mEditDate.setText(today.get(Calendar.YEAR) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mEditDate) {
            mDatePickerDialog.show();
        }
    }
}
