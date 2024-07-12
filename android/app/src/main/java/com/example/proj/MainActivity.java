package com.example.proj;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "bluetoothChannel";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("connectBluetooth")) {
                                String deviceAddress = call.argument("deviceAddress");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                                            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(this,
                                                new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                                                REQUEST_BLUETOOTH_PERMISSIONS);
                                        result.error("PERMISSION_DENIED", "Bluetooth permissions are not granted", null);
                                        return;
                                    }
                                }
                                boolean connected = connectBluetooth(deviceAddress);
                                result.success(connected);
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    private boolean connectBluetooth(String deviceAddress) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        BluetoothSocket socket = null;

        try {
            // Use UUID for SPP (Serial Port Profile)
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            socket = device.createRfcommSocketToServiceRecord(uuid);

            socket.connect();
            Log.d("Bluetooth", "Connected to device: " + device.getName());
            return true;
        } catch (IOException e) {
            Log.e("Bluetooth", "Failed to connect to device: " + device.getName(), e);
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e("Bluetooth", "Failed to close socket", e);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Bluetooth", "Bluetooth permissions granted");
            } else {
                Log.d("Bluetooth", "Bluetooth permissions denied");
            }
        }
    }
}
