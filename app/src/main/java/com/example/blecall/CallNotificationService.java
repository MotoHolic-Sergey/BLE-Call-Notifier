package com.example.blecall;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.nio.charset.StandardCharsets;

public class CallNotificationService extends NotificationListenerService {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private static long lastSentTime = 0;
    private static String lastPayloadKey = "";

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
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

        String textLower = text.toLowerCase();

        // Step 1: App filter
        if (!(pkg.equals("com.google.android.dialer") ||
              pkg.equals("com.whatsapp") ||
              pkg.equals("org.telegram.messenger"))) {
            return;
        }

        // Step 2: Incoming call filter
        if (!(textLower.contains("incoming") && textLower.contains("call"))) {
            return;
        }

        // Step 3: Determine type
        byte type;
        if (pkg.contains("dialer")) {
            type = 0x00;
        } else if (pkg.contains("telegram")) {
            type = 0x01;
        } else if (pkg.contains("whatsapp")) {
            type = 0x02;
        } else {
            return;
        }

        // Step 4: Extract caller
        String caller = processCaller(title);

        // Step 5: Deduplication
        long now = System.currentTimeMillis();
        String payloadKey = type + "|" + caller;

        if (payloadKey.equals(lastPayloadKey) && (now - lastSentTime) < 3000) {
            return;
        }

        lastPayloadKey = payloadKey;
        lastSentTime = now;

        // Step 6: Build binary payload
        byte[] nameBytes = caller.getBytes(StandardCharsets.US_ASCII);

        int len = Math.min(nameBytes.length, 17);
        byte[] payload = new byte[1 + len];

        payload[0] = type;
        System.arraycopy(nameBytes, 0, payload, 1, len);

        // Step 7: Send via BLE
        BLEAdvertiserHelper.start(getApplicationContext(), payload);

        // Step 8: Logging
        String log = "TIME: " + now +
                     "\nAPP: " + pkg +
                     "\nTITLE: " + title +
                     "\nTEXT: " + text +
                     "\nTYPE: " + type +
                     "\nCALLER: " + caller;

        handler.post(() -> MainActivity.appendLog(log));
        FileLogger.log(log);
    }

    // -----------------------
    // Caller Processing Logic
    // -----------------------

    private String processCaller(String raw) {

        if (raw == null || raw.isEmpty()) return "UNKNOWN";

        String result;

        if (isPhoneNumber(raw)) {
            result = normalizePhone(raw);
        } else {
            result = normalizeName(raw);
        }

        // Trim AFTER normalization
        result = result.trim();

        if (result.isEmpty()) {
            return "UNKNOWN";
        }

        // Truncate to 17 chars
        if (result.length() > 17) {
            result = result.substring(0, 17);
        }

        return result;
    }

    private boolean isPhoneNumber(String input) {
        if (input == null || input.isEmpty()) return false;

        String cleaned = input.replaceAll("[^0-9+]", "");
        String digitsOnly = cleaned.replace("+", "");

        return digitsOnly.length() >= 7;
    }

    private String normalizePhone(String input) {
        if (input == null) return "";

        String cleaned = input.replaceAll("[^0-9+]", "");

        if (cleaned.indexOf('+') > 0) {
            cleaned = cleaned.replace("+", "");
        }

        return cleaned;
    }

    private String normalizeName(String input) {
        if (input == null) return "";

        String name = input.trim();

        // Keep ASCII only
        name = name.replaceAll("[^\\x20-\\x7E]", "");

        return name;
    }
}