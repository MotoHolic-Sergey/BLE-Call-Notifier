package com.example.blecall;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;

import java.nio.charset.StandardCharsets;

public class CallNotificationService extends NotificationListenerService {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private static long lastSentTime = 0;
    private static String lastPayloadKey = "";

    private BleAdvertiserManager bleManager;

    private static final int MAX_DISPLAY_LEN = 13;

    @Override
    public void onCreate() {
        super.onCreate();
        bleManager = new BleAdvertiserManager(this);
    }

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

        // -------------------------
        // NEW: Call vs Message logic
        // -------------------------
        boolean isIncomingCall = textLower.contains("incoming") && textLower.contains("call");

        boolean isOngoingOrMissed =
                textLower.contains("ongoing") ||
                textLower.contains("missed");

        boolean isMessage = !isIncomingCall && !isOngoingOrMissed;

        // -------------------------
        // NEW: Load priority contacts
        // -------------------------
        android.content.SharedPreferences prefs =
                getSharedPreferences("ble_settings", MODE_PRIVATE);

        String p1 = prefs.getString("priority_contact_1", "").toLowerCase();
        String p2 = prefs.getString("priority_contact_2", "").toLowerCase();
        String p3 = prefs.getString("priority_contact_3", "").toLowerCase();

        String senderRaw = title == null ? "" : title;
        String sender = normalizeName(senderRaw).toLowerCase();

        boolean hasPriority =
                (!p1.isEmpty() && sender.contains(p1)) ||
                (!p2.isEmpty() && sender.contains(p2)) ||
                (!p3.isEmpty() && sender.contains(p3));

        // -------------------------
        // NEW: Type selection
        // -------------------------
        byte type;

        if (isIncomingCall) {

            if (pkg.contains("dialer")) {
                type = 0x00;
            } else if (pkg.contains("telegram")) {
                type = 0x01;
            } else if (pkg.contains("whatsapp")) {
                type = 0x02;
            } else {
                return;
            }

        } else if (isMessage && hasPriority) {

            if (pkg.contains("dialer")) {
                type = 0x10;
            } else if (pkg.contains("telegram")) {
                type = 0x11;
            } else if (pkg.contains("whatsapp")) {
                type = 0x12;
            } else {
                return;
            }

        } else {
            return;
        }

        // Step 4: Extract caller
        String caller = processCaller(title);

        // Step 5: Deduplication (unchanged, works for messages too)
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

        // Step 7: Manufacturer ID
        int channel = prefs.getInt("channel", 1);
        int manufacturerId = 0x1200 | channel;

        bleManager.advertise(payload, manufacturerId);

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

    @Override
    public void onDestroy() {
        if (bleManager != null) {
            bleManager.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (bleManager != null) {
            bleManager.shutdown();
        }
        super.onTaskRemoved(rootIntent);
    }

    // -----------------------
    // UPDATED Caller Processing
    // -----------------------
    private String processCaller(String raw) {

        if (raw == null || raw.isEmpty()) return "UNKNOWN";

        String result;
        boolean isPhone = isPhoneNumber(raw);

        if (isPhone) {
            result = normalizePhone(raw);
        } else {
            result = normalizeName(raw);
        }

        result = result.trim();

        if (result.isEmpty()) {
            return "UNKNOWN";
        }

        // 🔧 NEW: truncate ONLY names
        if (!isPhone && result.length() > MAX_DISPLAY_LEN) {
            result = result.substring(0, MAX_DISPLAY_LEN - 1) + "…";
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
        name = name.replaceAll("[^\\x20-\\x7E]", "");

        return name;
    }
}