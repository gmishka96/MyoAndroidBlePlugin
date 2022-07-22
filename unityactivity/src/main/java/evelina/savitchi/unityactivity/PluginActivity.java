package evelina.savitchi.unityactivity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.Locale;

public class PluginActivity extends UnityPlayerActivity {
    public static final String BT_SERVICE_CHANNEL_ID = "MYOARMBAND3D_BT_SERVICE_CHANNEL_ID";
    public static final String BT_SERVICE_CHANNEL_NAME = "MyoArmband3D BLE Service";
    public static final String BT_SERVICE_CHANNEL_DESCRIPTION =
            "Service handles communication with a Myo Armband.";

    private static final String TAG = PluginActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 10;
    private static final int REQUEST_PERMISSION_BT = 11;
    private static final int REQUEST_PERMISSION_LOCATION = 20;
    private static final int REQUEST_PERMISSION_FOREGROUND_SERVICE = 30;

    private static final float MyoHwOrientationScale = 16384.0f;
    private static final float MyoHwAccelerometerScale = 2048.0f;
    private static final float MyoHwGyroscopeScale = 16.0f;

    private static final int EMG_SENSOR1_START_INDEX = 0;
    private static final int EMG_SENSOR1_END_INDEX = 8;
    private static final int EMG_SENSOR2_START_INDEX = 8;
    private static final int EMG_SENSOR2_END_INDEX = 16;

    private static final int GET_TUPLE_COUNT = 0;
    private static final int GET_VALUE_X = 1;
    private static final int GET_VALUE_Y = 2;
    private static final int GET_VALUE_Z = 3;
    private static final int SET_VALUE_XYZ = 4;

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bleReceiver;

    private float wOrientation;
    private float xOrientation;
    private float yOrientation;
    private float zOrientation;
    private float xAcceleration;
    private float yAcceleration;
    private float zAcceleration;
    private float xGyroscope;
    private float yGyroscope;
    private float zGyroscope;

    private byte emgSensor0;
    private byte emgSensor1;
    private byte emgSensor2;
    private byte emgSensor3;
    private byte emgSensor4;
    private byte emgSensor5;
    private byte emgSensor6;
    private byte emgSensor7;

    private ArrayList<Float> xAccelerometers;
    private ArrayList<Float> yAccelerometers;
    private ArrayList<Float> zAccelerometers;
    private ArrayList<Float> xGyroscopes;
    private ArrayList<Float> yGyroscopes;
    private ArrayList<Float> zGyroscopes;

    public void showToast(String message) {
        Toast.makeText(UnityPlayer.currentActivity, message, Toast.LENGTH_SHORT).show();
    }

    public float getWOrientation()
    {
        return wOrientation;
    }

    public float getXOrientation()
    {
        return xOrientation;
    }

    public float getYOrientation()
    {
        return yOrientation;
    }

    public float getZOrientation()
    {
        return zOrientation;
    }

    public float getXAcceleration()
    {
        return xAcceleration;
    }

    public float getYAcceleration()
    {
        return yAcceleration;
    }

    public float getZAcceleration()
    {
        return zAcceleration;
    }

    public float getXGyroscope()
    {
        return xGyroscope;
    }

    public float getYGyroscope()
    {
        return yGyroscope;
    }

    public float getZGyroscope()
    {
        return zGyroscope;
    }

    public float getAverageAbsEmg() {
        return (emgSensor0 + emgSensor1 + emgSensor2 + emgSensor3 + emgSensor4 + emgSensor5 +
                emgSensor6 + emgSensor7) / 8.0f;
    }

    public double actOnAccelerometerTuples(int command) {
        double accelerometerValue = 0.0;

        synchronized (this) {
            switch (command) {
                case GET_TUPLE_COUNT:
                    accelerometerValue = Math.min(
                            Math.min(xAccelerometers.size(), yAccelerometers.size()),
                            zAccelerometers.size());
                    break;
                case GET_VALUE_X:
                    if (xAccelerometers.size() > 0) {
                        accelerometerValue = xAccelerometers.get(0);
                        xAccelerometers.remove(0);
                    }
                    break;
                case GET_VALUE_Y:
                    if (yAccelerometers.size() > 0) {
                        accelerometerValue = yAccelerometers.get(0);
                        yAccelerometers.remove(0);
                    }
                    break;
                case GET_VALUE_Z:
                    if (zAccelerometers.size() > 0) {
                        accelerometerValue = zAccelerometers.get(0);
                        zAccelerometers.remove(0);
                    }
                    break;
                case SET_VALUE_XYZ:
                    xAccelerometers.add(xAcceleration);
                    yAccelerometers.add(yAcceleration);
                    zAccelerometers.add(zAcceleration);
                    break;
            }
        }

        return accelerometerValue;
    }

    public double actOnGyroscopeTuples(int command) {
        double gyroscopeValue = 0.0;

        synchronized (this) {
            switch (command) {
                case GET_TUPLE_COUNT:
                    gyroscopeValue = Math.min(
                            Math.min(xGyroscopes.size(), yGyroscopes.size()),
                            zGyroscopes.size());
                    break;
                case GET_VALUE_X:
                    if (xGyroscopes.size() > 0) {
                        gyroscopeValue = xGyroscopes.get(0);
                        xGyroscopes.remove(0);
                    }
                    break;
                case GET_VALUE_Y:
                    if (yGyroscopes.size() > 0) {
                        gyroscopeValue = yGyroscopes.get(0);
                        yGyroscopes.remove(0);
                    }
                    break;
                case GET_VALUE_Z:
                    if (zGyroscopes.size() > 0) {
                        gyroscopeValue = zGyroscopes.get(0);
                        zGyroscopes.remove(0);
                    }
                    break;
                case SET_VALUE_XYZ:
                    xGyroscopes.add(xGyroscope);
                    yGyroscopes.add(yGyroscope);
                    zGyroscopes.add(zGyroscope);
                    break;
            }
        }

        return gyroscopeValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Plugin activity onCreate");
        super.onCreate(savedInstanceState);

        if (initBtAdapter()) {
            tryForBluetooth();
        } else {
            finishAndRemoveTask();
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "Plugin activity onResume");
        super.onResume();

        initializeFields();
        initializeReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(bleReceiver,
                makeIntentFilter());
    }

    @Override
    public void onPause() {
        Log.i(TAG, "Plugin activity onResume");
        if (null != bleReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bleReceiver);
            bleReceiver = null;
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Plugin activity onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK){
                tryForLocation();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                showToast("Bluetooth enabling rejected. Closing app...");
                finishAndRemoveTask();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Plugin activity onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_PERMISSION_BT: {
                if (0 < grantResults.length &&
                        PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tryForLocation();
                        }
                    };
                    handler.postDelayed(r, 10);
                } else {
                    showToast("Bluetooth permission not granted");
                    finishAndRemoveTask();
                }
            }
            break;

            case REQUEST_PERMISSION_LOCATION: {
                if (0 < grantResults.length &&
                        PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tryForForegroundService();
                        }
                    };
                    handler.postDelayed(r, 10);
                } else {
                    showToast("Location permission not granted");
                    finishAndRemoveTask();
                }
            }
            break;

            case REQUEST_PERMISSION_FOREGROUND_SERVICE: {
                if (0 < grantResults.length &&
                        PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            btStartService();
                        }
                    };
                    handler.postDelayed(r, 10);
                } else {
                    showToast("Foreground service permission not granted");
                    finishAndRemoveTask();
                }
            }
            break;

            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createNotificationChannel() {
        Log.i(TAG, "Plugin activity createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if (null != notificationManager) {
                NotificationChannel btServiceNotificationChannel = new NotificationChannel(
                        BT_SERVICE_CHANNEL_ID,
                        BT_SERVICE_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                btServiceNotificationChannel.setDescription(BT_SERVICE_CHANNEL_DESCRIPTION);

                notificationManager.createNotificationChannel(btServiceNotificationChannel);
            }
        }
    }

    private boolean initBtAdapter() {
        Log.i(TAG, "Plugin activity initBtAdapter");
        boolean isInitialized = false;

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("BLE not supported!");
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(
                    Context.BLUETOOTH_SERVICE);

            if (bluetoothManager == null) {
                showToast("Unable to initialize BluetoothManager.");
            } else {
                bluetoothAdapter = bluetoothManager.getAdapter();

                if (bluetoothAdapter == null) {
                    showToast("Unable to obtain a BluetoothAdapter.");
                } else {
                    isInitialized = true;
                }
            }
        }

        return isInitialized;
    }

    private void tryForBluetooth() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) && PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_ADMIN)) {
            if (bluetoothAdapter.isEnabled()) {
                tryForLocation();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_PERMISSION_BT);
        }
    }

    private void tryForLocation() {
        if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) && PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                tryForForegroundService();
            } else {
                btStartService();
            }
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
        }
    }

    private void tryForForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                    Manifest.permission.FOREGROUND_SERVICE))
            {
                btStartService();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        REQUEST_PERMISSION_FOREGROUND_SERVICE);
            }
        } else {
            btStartService();
        }
    }

    private void btStartService() {
        createNotificationChannel();

        Intent serviceNotificationIntent = new Intent(this, BluetoothLeService.class);
        serviceNotificationIntent.putExtra(
                BluetoothLeService.BT_SERVICE_ID,
                BluetoothLeService.BT_SERVICE_START);
        ContextCompat.startForegroundService(this, serviceNotificationIntent);
    }

    private void initializeFields() {
        wOrientation = 0;
        xOrientation = 0;
        yOrientation = 0;
        zOrientation = 0;
        xAcceleration = 0;
        yAcceleration = 0;
        zAcceleration = 0;
        xGyroscope = 0;
        yGyroscope = 0;
        zGyroscope = 0;

        emgSensor0 = 0;
        emgSensor1 = 0;
        emgSensor2 = 0;
        emgSensor3 = 0;
        emgSensor4 = 0;
        emgSensor5 = 0;
        emgSensor6 = 0;
        emgSensor7 = 0;

        xAccelerometers = new ArrayList<>();
        yAccelerometers = new ArrayList<>();
        zAccelerometers = new ArrayList<>();
        xGyroscopes = new ArrayList<>();
        yGyroscopes = new ArrayList<>();
        zGyroscopes = new ArrayList<>();
    }

    private void initializeReceiver() {
        bleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                Log.i(TAG, "Plugin activity onReceive action: " + action);

                if (BluetoothLeService.ACTION_BLE_CONNECTED.equals(action)) {
                    String connectionValue = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    Log.d(TAG, "Connection status: " + connectionValue);
                } else if (BluetoothLeService.ACTION_BLE_DISCONNECTED.equals(action)) {
                    String connectionValue = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    Log.d(TAG, "Connection status: " + connectionValue);
                } else if (BluetoothLeService.ACTION_BLE_BATTERY_LEVEL_AVAILABLE.equals(action)) {
                    String batteryLevelValue = String.format(
                            Locale.getDefault(),"%d",
                            intent.getByteExtra(BluetoothLeService.EXTRA_DATA, (byte) 0));
                    Log.d(TAG, "Battery level (%): " + batteryLevelValue);
                } else if (BluetoothLeService.ACTION_BLE_INFO_AVAILABLE.equals(action)) {
                    String serialNumberValue = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    Log.d(TAG, "Serial number: " + serialNumberValue);
                } else if (BluetoothLeService.ACTION_BLE_FW_AVAILABLE.equals(action)) {
                    String firmwareVersionValue = intent.getStringExtra(BluetoothLeService
                            .EXTRA_DATA);
                    Log.d(TAG, "Firmware version: " + firmwareVersionValue);
                } else if (BluetoothLeService.ACTION_BLE_DEVICE_NAME_AVAILABLE.equals(action)) {
                    String deviceNameValue = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    Log.d(TAG, "Device name: " + deviceNameValue);
                } else if (BluetoothLeService.ACTION_BLE_EMG0_AVAILABLE.equals(action)) {
                    final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    emgSensor0 = calculateAverageAbs(
                            data, EMG_SENSOR1_START_INDEX, EMG_SENSOR1_END_INDEX);
                    emgSensor1 = calculateAverageAbs(
                            data, EMG_SENSOR2_START_INDEX, EMG_SENSOR2_END_INDEX);

                    String emg0Sensor0Value = String.format(
                            Locale.getDefault(), "%d", emgSensor0);
                    String emg0Sensor1Value = String.format(
                            Locale.getDefault(), "%d", emgSensor1);

                    Log.d(TAG, "EMG0_S0: " + emg0Sensor0Value);
                    Log.d(TAG, "EMG0_S1: " + emg0Sensor1Value);
                } else if (BluetoothLeService.ACTION_BLE_EMG1_AVAILABLE.equals(action)) {
                    final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    emgSensor2 = calculateAverageAbs(
                            data, EMG_SENSOR1_START_INDEX, EMG_SENSOR1_END_INDEX);
                    emgSensor3 = calculateAverageAbs(
                            data, EMG_SENSOR2_START_INDEX, EMG_SENSOR2_END_INDEX);

                    String emg1Sensor2Value = String.format(
                            Locale.getDefault(), "%d", emgSensor2);
                    String emg1Sensor3Value = String.format(
                            Locale.getDefault(), "%d", emgSensor3);

                    Log.d(TAG, "EMG1_S2: " + emg1Sensor2Value);
                    Log.d(TAG, "EMG1_S3: " + emg1Sensor3Value);
                } else if (BluetoothLeService.ACTION_BLE_EMG2_AVAILABLE.equals(action)) {
                    final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    emgSensor4 = calculateAverageAbs(
                            data, EMG_SENSOR1_START_INDEX, EMG_SENSOR1_END_INDEX);
                    emgSensor5 = calculateAverageAbs(
                            data, EMG_SENSOR2_START_INDEX, EMG_SENSOR2_END_INDEX);

                    String emg2Sensor4Value = String.format(
                            Locale.getDefault(), "%d", emgSensor4);
                    String emg2Sensor5Value = String.format(
                            Locale.getDefault(), "%d", emgSensor5);

                    Log.d(TAG, "EMG2_S4: " + emg2Sensor4Value);
                    Log.d(TAG, "EMG2_S5: " + emg2Sensor5Value);
                } else if (BluetoothLeService.ACTION_BLE_EMG3_AVAILABLE.equals(action)) {
                    final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    emgSensor6 = calculateAverageAbs(
                            data, EMG_SENSOR1_START_INDEX, EMG_SENSOR1_END_INDEX);
                    emgSensor7 = calculateAverageAbs(
                            data, EMG_SENSOR2_START_INDEX, EMG_SENSOR2_END_INDEX);

                    String emg3Sensor6Value = String.format(
                            Locale.getDefault(), "%d", emgSensor6);
                    String emg3Sensor7Value = String.format(
                            Locale.getDefault(), "%d", emgSensor7);

                    Log.d(TAG, "EMG3_S6: " + emg3Sensor6Value);
                    Log.d(TAG, "EMG3_S7: " + emg3Sensor7Value);
                } else if (BluetoothLeService.ACTION_BLE_IMU_AVAILABLE.equals(action)) {
                    short[] imuData = intent.getShortArrayExtra(BluetoothLeService.EXTRA_DATA);
                    xOrientation = imuData[0] / MyoHwOrientationScale;
                    yOrientation = imuData[1] / MyoHwOrientationScale;
                    zOrientation = imuData[2] / MyoHwOrientationScale;
                    wOrientation = imuData[3] / MyoHwOrientationScale;

                    xAcceleration = imuData[4] / MyoHwAccelerometerScale;
                    yAcceleration = imuData[5] / MyoHwAccelerometerScale;
                    zAcceleration = imuData[6] / MyoHwAccelerometerScale;
                    actOnAccelerometerTuples(SET_VALUE_XYZ);

                    xGyroscope = imuData[7] / MyoHwGyroscopeScale;
                    yGyroscope = imuData[8] / MyoHwGyroscopeScale;
                    zGyroscope = imuData[9] / MyoHwGyroscopeScale;
                    actOnGyroscopeTuples(SET_VALUE_XYZ);

                    String imuOrientWValue = String.format(
                            Locale.getDefault(), "%.1f", wOrientation);
                    String imuOrientXValue = String.format(
                            Locale.getDefault(), "%.1f", xOrientation);
                    String imuOrientYValue = String.format(
                            Locale.getDefault(), "%.1f", yOrientation);
                    String imuOrientZValue = String.format(
                            Locale.getDefault(), "%.1f", zOrientation);
                    String imuAccelXValue = String.format(
                            Locale.getDefault(), "%.1f", xAcceleration);
                    String imuAccelYValue = String.format(
                            Locale.getDefault(), "%.1f", yAcceleration);
                    String imuAccelZValue = String.format(
                            Locale.getDefault(), "%.1f", zAcceleration);
                    String imuGyroXValue = String.format(
                            Locale.getDefault(), "%.1f", xGyroscope);
                    String imuGyroYValue = String.format(
                            Locale.getDefault(), "%.1f", yGyroscope);
                    String imuGyroZValue = String.format(
                            Locale.getDefault(), "%.1f", zGyroscope);

                    Log.i(TAG, "Orientation data (x,y,z,w): " + imuOrientXValue + ", " +
                            imuOrientYValue + ", " + imuOrientZValue + ", " + imuOrientWValue);
                    Log.i(TAG, "Acceleration data (x,y,z): " + imuAccelXValue + ", " +
                            imuAccelYValue + ", " + imuAccelZValue);
                    Log.i(TAG, "Gyroscope data (x,y,z): " + imuGyroXValue + ", " +
                            imuGyroYValue + ", " + imuGyroZValue);
                } else if (BluetoothLeService.ACTION_READABLE_FIELDS_AVAILABLE.equals(action)) {
                    String connectionValue = intent.getStringExtra(BluetoothLeService
                            .EXTRA_CONNECTION_STATE);
                    String deviceNameValue = intent.getStringExtra(BluetoothLeService
                            .EXTRA_DEVICE_NAME);
                    String serialNumberValue = intent.getStringExtra(BluetoothLeService
                            .EXTRA_SERIAL_NUMBER);
                    String firmwareVersionValue = intent.getStringExtra(BluetoothLeService
                            .EXTRA_FIRMWARE_VERSION);
                    String batteryLevelValue = String.format(
                            Locale.getDefault(),"%d",
                            intent.getByteExtra(BluetoothLeService.EXTRA_BATTERY_LEVEL, (byte) 0));

                    Log.d(TAG, "Connection status: " + connectionValue);
                    Log.d(TAG, "Device name: " + deviceNameValue);
                    Log.d(TAG, "Serial number: " + serialNumberValue);
                    Log.d(TAG, "Firmware version: " + firmwareVersionValue);
                    Log.d(TAG, "Battery level (%): " + batteryLevelValue);
                }
            }
        };
    }

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_BATTERY_LEVEL_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_INFO_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_FW_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_DEVICE_NAME_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_EMG0_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_EMG1_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_EMG2_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_EMG3_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_IMU_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_READABLE_FIELDS_AVAILABLE);
        return intentFilter;
    }

    private byte calculateAverageAbs(byte[] data, int startIndex, int endIndex) {
        short sum = 0;
        for (int index = startIndex; index < endIndex; index++)  {
            sum += Math.abs(data[index]);
        }
        return (byte) (sum / (endIndex - startIndex));
    }
}
