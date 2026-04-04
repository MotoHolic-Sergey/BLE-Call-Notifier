package com.example.blecall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.ViewGroup;
import android.content.Intent;
import android.provider.Settings;

public class MainActivity extends Activity {

    public static TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Notification access button
        Button notifBtn = new Button(this);
        notifBtn.setText("Enable Notification Access");
        notifBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        // Channel input
        EditText channelInput = new EditText(this);
        channelInput.setHint("Enter channel (1-8)");
        channelInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        // Current channel display
        TextView current = new TextView(this);
        current.setText("Current Channel: " + AppSettings.getChannel(this));

        // Set button
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

        // Log view
        logView = new TextView(this);
        logView.setTextSize(12);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(logView);

        layout.addView(notifBtn);
        layout.addView(channelInput);
        layout.addView(setBtn);
        layout.addView(current);
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