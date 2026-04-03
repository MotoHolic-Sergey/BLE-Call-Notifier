package com.example.blecall;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseCallback;

public class BLEAdvertiserHelper {

    private static BluetoothLeAdvertiser advertiser;
    private static AdvertiseCallback callback;

    public static void start(String message) {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertiser == null) return;

        byte[] data = message.getBytes();

        // Truncate to 20 bytes
        if (data.length > 20) {
            byte[] truncated = new byte[20];
            System.arraycopy(data, 0, truncated, 0, 20);
            data = truncated;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .addManufacturerData(0x1234, data)
                .build();

        callback = new AdvertiseCallback() {};

        advertiser.startAdvertising(settings, advertiseData, callback);
    }

    public static void stop() {
        if (advertiser != null && callback != null) {
            advertiser.stopAdvertising(callback);
        }
    }
}
