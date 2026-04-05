package com.example.blecall;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.*;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;

public class BleAdvertiserManager {

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback callback;
    private boolean isAdvertising = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int ADVERTISE_DURATION_MS = 3000;

    public BleAdvertiserManager(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            advertiser = adapter.getBluetoothLeAdvertiser();
        }
        callback = new AdvertiseCallback() {};
    }

    public void advertise(byte[] payload, int manufacturerId) {

        if (advertiser == null) {
    android.util.Log.e("BLE", "Advertiser is NULL");
    return;
}

        // STOP previous advertisement
        stop();

        byte[] safePayload = Arrays.copyOf(payload, payload.length);

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .setTimeout(0)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .addManufacturerData(manufacturerId, safePayload)
                .build();

        advertiser.startAdvertising(settings, data, callback);
        isAdvertising = true;

        android.util.Log.d("BLE", "Advertising STARTED");

        // AUTO STOP after 3 sec
        handler.postDelayed(this::stop, ADVERTISE_DURATION_MS);
    }

    public void stop() {
        if (advertiser != null && isAdvertising) {
            advertiser.stopAdvertising(callback);
            isAdvertising = false;
        }
    }

    public void shutdown() {
        handler.removeCallbacksAndMessages(null);
        stop();
    }
}