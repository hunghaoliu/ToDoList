package com.henry.todolist.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by hunghao on 2017/1/16.
 */

public class NetworkReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "[ToDo] NWReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getExtras()!=null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i(LOG_TAG, "Network " + ni.getTypeName() + " connected");
                UpdateSheetsu.checkUpdate();
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(LOG_TAG, "There's no network connectivity");
            }
        }
    }
}
