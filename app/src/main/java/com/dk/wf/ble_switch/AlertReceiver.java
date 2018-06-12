package com.dk.wf.ble_switch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    private String title = "Switch turn off";
    private String message = "Time is up";
    @Override
    public void onReceive(Context context, Intent intent){
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannel1Notification(title,message);
        notificationHelper.getManager().notify(1, nb.build());
    }
}
