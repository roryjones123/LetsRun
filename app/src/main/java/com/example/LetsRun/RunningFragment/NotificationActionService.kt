package com.example.LetsRun.RunningFragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

//class just serves as the intent for the notification pending action
class NotificationActionService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.sendBroadcast(
            Intent("notificationAction")
                .putExtra("key2", intent.action)
        //fragment picks this up
        )
    }
}