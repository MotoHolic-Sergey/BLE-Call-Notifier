package com.example.blecall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.ViewGroup;
import android.content.Intent;
import android.provider.Settings;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

    public static TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button notifBtn = new Button(this);
        notifBtn.setText("Enable Notification Access");
        notifBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        EditText channelInput = new EditText(this);
        channelInput.setHint("Enter channel (1-8)");
        channelInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        TextView current = new TextView(this);
        current.setText("Current Channel: " + AppSettings.getChannel(this));

        Button setBtn = new Button(this);
        setBtn.setText("Set Channel");

        setBtn.setOnClickListener(v -> {
            try {
                int ch = Integer.parseInt(channelInput.getText().toString());

                if (ch >= 1 && ch <= 8) {
                    AppSettings.setChannel(this, ch);
                    current.setText("Current Channel: " + ch);
                } else {
                    Toast.makeText(this, "Enter 1–8", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        // -------------------------
        // NEW: Priority contacts UI
        // -------------------------
        EditText p1 = new EditText(this);
        p1.setHint("Priority Contact 1");

        EditText p2 = new EditText(this);
        p2.setHint("Priority Contact 2");

        EditText p3 = new EditText(this);
        p3.setHint("Priority Contact 3");

        Button saveBtn = new Button(this);
        saveBtn.setText("Save Priority Contacts");

        saveBtn.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("ble_settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("priority_contact_1", p1.getText().toString().trim());
            editor.putString("priority_contact_2", p2.getText().toString().trim());
            editor.putString("priority_contact_3", p3.getText().toString().trim());

            editor.apply();

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });

        logView = new TextView(this);
        logView.setTextSize(12);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(logView);

        layout.addView(notifBtn);
        layout.addView(channelInput);
        layout.addView(setBtn);
        layout.addView(current);

        // NEW UI elements
        layout.addView(p1);
        layout.addView(p2);
        layout.addView(p3);
        layout.addView(saveBtn);

        layout.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(layout);
    }

    public static void appendLog(String text) {
        if (logView != null) {
            logView.append(text + "\n\n");
        }
    }
}