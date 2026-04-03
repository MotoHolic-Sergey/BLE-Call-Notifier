package com.example.blecall;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class CallNotificationService extends NotificationListenerService {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String pkg = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;

        String title = extras.getString("android.title", "");
        String text  = extras.getString("android.text", "");

        if (title == null) title = "";
        if (text == null) text = "";

        String log = "APP: " + pkg +
                     "\nTITLE: " + title +
                     "\nTEXT: " + text;

        handler.post(() -> MainActivity.appendLog(log));
    }
}
