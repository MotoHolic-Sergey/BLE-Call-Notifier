package com.example.blecall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.ViewGroup;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    public static TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button btn = new Button(this);
        btn.setText("Enable Notification Access");
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        logView = new TextView(this);
        logView.setTextSize(14);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(logView);

        layout.addView(btn);
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
