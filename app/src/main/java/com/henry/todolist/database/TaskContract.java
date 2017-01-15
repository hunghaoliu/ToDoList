package com.henry.todolist.database;

import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "ToDoList.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_DATE = "datetime";
        public static final String COL_TASK_TITLE = "tasks";
        public static final String COL_TASK_FINISHED = "isFinish";
        public static final String COL_TASK_LOCAL = "isLocal";
        public static final String COL_TASK_UUID = "uuid";
    }
}
