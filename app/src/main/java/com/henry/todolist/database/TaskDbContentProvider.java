package com.henry.todolist.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by henry on 17/1/15.
 */
public class TaskDbContentProvider extends ContentProvider {
    private static final String LOG_TAG = "TaskDbContentProvider";
    public static final String AUTHORITY = "com.henry.todolist.provider";
    private static final UriMatcher sUriMatcher;
    private static final int URI_TYPE_TABLE = 1;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TaskContract.TaskEntry.TABLE, URI_TYPE_TABLE);
    }
    private static final Uri CONTENT_URI_TABLE_TASK = Uri.parse("content://" + AUTHORITY + "/" + TaskContract.TaskEntry.TABLE);


    TaskDbHelper mDBHelper = null;
    @Override
    public boolean onCreate() {
        mDBHelper = new TaskDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (sUriMatcher == null) {
            return null;
        }
        Log.d(LOG_TAG, "query uri = " + uri);


        Cursor cursor = null;

        switch(sUriMatcher.match(uri)) {
            case URI_TYPE_TABLE:
                try {
                    SQLiteDatabase db = mDBHelper.getReadableDatabase();
                    cursor = db.query(TaskContract.TaskEntry.TABLE,
                            null,
                            selection,
                            selectionArgs,
                            null, null, sortOrder);

                    return cursor;

                } catch (Exception e) {
                    Log.w(LOG_TAG, "read address table exception: " + e.getMessage(), e);
                    if(null != cursor)
                        cursor.close();

                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case URI_TYPE_TABLE:
                return "vnd.android.cursor.item/vnd."+AUTHORITY+"." + TaskContract.TaskEntry.TABLE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert uri = " + uri);
        Uri insertedUri = null;
        SQLiteDatabase db = null;

        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();
            long id = 0;

            switch(sUriMatcher.match(uri)) {
                case URI_TYPE_TABLE:
                    id = db.insert(TaskContract.TaskEntry.TABLE, null, values);
                    insertedUri = Uri.withAppendedPath(CONTENT_URI_TABLE_TASK, String.valueOf(id));
                    db.setTransactionSuccessful();
                    // If add address , sync AddressInfo and LocationInfo
                    break;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception occurs when insert data to DB");
            e.printStackTrace();
        } finally {
            if (db != null)
                db.endTransaction();
        }

        getContext().getContentResolver().notifyChange(insertedUri, null);
        return insertedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete uri = " + uri);
        int id = 0;
        SQLiteDatabase db = null;

        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();

            switch(sUriMatcher.match(uri)) {
                case URI_TYPE_TABLE:
                    id = db.delete(TaskContract.TaskEntry.TABLE, selection, selectionArgs);
                    db.setTransactionSuccessful();
                    if(selectionArgs.length>0)
                    {
                        Log.d(LOG_TAG, selectionArgs[0] + " will be deleted");
                    }
                    break;
            }
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Exception occurs when insert data to DB");
            e.printStackTrace();
        }
        finally {
            if (db != null)
                db.endTransaction();
        }

        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (sUriMatcher == null) {
            return -1;
        }
        Log.d(LOG_TAG, "update uri = " + uri);

        int id = -1;
        SQLiteDatabase db = null;

        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();
            switch (sUriMatcher.match(uri)) {
                case URI_TYPE_TABLE:
                    id = db.update(TaskContract.TaskEntry.TABLE, values, selection, selectionArgs);
                    db.setTransactionSuccessful();
                    Uri updatedUri = Uri.withAppendedPath(CONTENT_URI_TABLE_TASK, String.valueOf(id));
                    getContext().getContentResolver().notifyChange(updatedUri, null);
                    if(selectionArgs.length>0)
                    {
                        Log.d(LOG_TAG, selectionArgs[0] + " will be updated");
                    }

                    return id;
            }
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Exception occurs when update data");
            e.printStackTrace();
        }
        finally {
            if (db != null)
                db.endTransaction();
        }

        return -1;
    }
}
