package com.example.blecall;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseCallback;
import android.content.Context;

public class BLEAdvertiserHelper {

    private static BluetoothLeAdvertiser advertiser;
    private static AdvertiseCallback callback;

    public static void start(Context context, byte[] payload) {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertiser == null) return;

        // ✅ THIS is the important line (uses saved channel)
        int manufacturerId = AppSettings.getManufacturerId(context);

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .addManufacturerData(manufacturerId, payload)
                .build();

        callback = new AdvertiseCallback() {};

        advertiser.startAdvertising(settings, data, callback);
    }

    public static void stop() {
        if (advertiser != null && callback != null) {
            advertiser.stopAdvertising(callback);
        }
    }
}