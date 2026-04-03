package com.example.blecall;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class CallNotificationService extends NotificationListenerService {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        // Initialize file logger
        FileLogger.init(getApplicationContext());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String pkg = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;

        String title = extras.getString("android.title", "");
        String text  = extras.getString("android.text", "");

        if (title == null) title = "";
        if (text == null) text = "";

        String payload = buildPayload(pkg, title, text);

        // Send via BLE
        BLEAdvertiserHelper.start(payload);

        // Timestamp
        long time = System.currentTimeMillis();

        // Full log
        String log = "TIME: " + time +
                     "\nAPP: " + pkg +
                     "\nTITLE: " + title +
                     "\nTEXT: " + text +
                     "\nBLE: " + payload;

        // UI log
        handler.post(() -> MainActivity.appendLog(log));

        // File log
        FileLogger.log(log);
    }

    private String buildPayload(String pkg, String title, String text) {

        String appId = "U";

        if (pkg.contains("whatsapp")) appId = "W";
        else if (pkg.contains("telegram")) appId = "T";
        else if (pkg.contains("dialer")) appId = "D";
        else if (pkg.contains("messaging")) appId = "S";

        String content = !title.isEmpty() ? title : text;

        if (content.isEmpty()) content = "UNKNOWN";

        // Remove non-ASCII
        content = content.replaceAll("[^\\x20-\\x7E]", "");

        if (content.length() > 14) {
            content = content.substring(0, 14);
        }

        return appId + "|" + content;
    }
}
