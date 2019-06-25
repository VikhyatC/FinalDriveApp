package com.smarterhomes.wateronmetermap.Helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Created by vikhyat on 4/6/19.
 */

public class CheckConnectivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(isConnected){
            Toast.makeText(context, "Internet Connection Lost", Toast.LENGTH_LONG).show();

            }
        else{
            Toast.makeText(context, "Internet Connected", Toast.LENGTH_LONG).show();

        }
    }
}
