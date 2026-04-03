package com.example.blecall;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger {

    private static File logFile;

    public static void init(Context context) {
        File dir = context.getExternalFilesDir(null);
        if (dir != null) {
            logFile = new File(dir, "notifications_log.txt");
        }
    }

    public static void log(String text) {
        if (logFile == null) return;

        try {
            FileWriter writer = new FileWriter(logFile, true); // append mode
            writer.append(text).append("\n\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
