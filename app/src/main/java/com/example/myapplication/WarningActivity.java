package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.Fragments.WarningServiceFragment;
import com.example.myapplication.Fragments.SettingServiceFragment;
import com.example.myapplication.Fragments.ServiceFragment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class WarningActivity extends AppCompatActivity implements ServiceFragment.ServiceFragmentDelegate {



    //여기서 servicefragment 먼저 생성
    WarningServiceFragment mWarningServiceFragment = new WarningServiceFragment();
    SettingServiceFragment mSettingServiceFragment = new SettingServiceFragment();
    //여기서 Gattservice 변수 선언.
    public static BluetoothGattCharacteristic mSendCharacteristic;
    public static BluetoothGattCharacteristic mReceiveCharacteristic;
    public static float distance_setting_value1;
    public static float distance_setting_value2;
    public static float distance_setting_value3;
    public static Context context;
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //        변수 선언
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////

    Spinner spinner;
    String[] items = {"아이템0","아이템1","아이템2","아이템3","아이템4"};
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = WarningActivity.class.getCanonicalName();
    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT";

    private static final UUID CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");
    //이게 CCCD UUID이다.
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");


    private static final int MIN_UINT = 0;
    private static final int MAX_UINT8 = (int) Math.pow(2, 8) - 1;
    private static final int MAX_UINT16 = (int) Math.pow(2, 16) - 1;
    /**
     * See <a href="https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.health_thermometer.xml">
     * Health Thermometer Service</a>
     * This service exposes two characteristics with descriptors:
     * - Measurement Interval Characteristic:
     * - Listen to notifications to from which you can subscribe to notifications
     * - CCCD Descriptor:
     * - Read/Write to get/set notifications.
     * - User Description Descriptor:
     * - Read/Write to get/set the description of the Characteristic.
     * - Temperature Measurement Characteristic:
     * - Read value to get the current interval of the temperature measurement timer.
     * - Write value resets the temperature measurement timer with the new value. This timer
     * is responsible for triggering value changed events every "Measurement Interval" value.
     * - CCCD Descriptor:
     * - Read/Write to get/set notifications.
     * - User Description Descriptor:
     * - Read/Write to get/set the description of the Characteristic.
     */
    private static final int INITIAL_SEND = 0;
    private static final int INITIAL_RECEIVE = 0;
    //이게 프라이머리 서비스
    private static final UUID UART_SERVICE_UUID = UUID
            .fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * See <a href="https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.temperature_measurement.xml">
     * Temperature Measurement</a>
     */

    //이건 TxChar UUID 설정 부분 (보내는 Char)
    private static final UUID SEND_UUID = UUID
            .fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");  //RxChar UUID
    private static final int SEND_VALUE_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT8;
    private static final String SEND_DESCRIPTION = "This characteristic is used " +
            "as TxChar Nordic Uart device";


    //이건 RxChar UUID 설정 부분 (받아오는 Char)
    /**
     * See <a href="https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.measurement_interval.xml">
     * Measurement Interval</a>
     */
    private static final UUID RECIEVE_UUID = UUID
            .fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");  //TxChar UUID
    private static final int RECEIVE_VALUE_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT8;


    private static final String RECEIVE_DESCRIPTION = "This characteristic is used " +
            "as RxChar of Nordic Uart device";


    private final long finishtime = 1000;
    private long presstime = 0;
    public static int alert_mode = 0;

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //        변수 선언 끝
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int MULTIPLE_PERMISSION = 1004;
    private String[] PERMISSIONS = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public boolean runtimeCheckPermission(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;

                }
            }
        }
        return true;
    }
    //private TextView mAdvStatus;
    private TextView mConnectionStatus;
    private ServiceFragment mCurrentServiceFragment;
    public static BluetoothGattService mBluetoothGattService;
    private HashSet<BluetoothDevice> mBluetoothDevices;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private AdvertiseData mAdvData;
    private AdvertiseData mAdvScanResponse;
    private AdvertiseSettings mAdvSettings;
    private BluetoothLeAdvertiser mAdvertiser;
    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Not broadcasting: " + errorCode);
            int statusText;
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    statusText = R.string.status_advertising;
                    Log.w(TAG, "App was already advertising");
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    statusText = R.string.status_advDataTooLarge;
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    statusText = R.string.status_advFeatureUnsupported;
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    statusText = R.string.status_advInternalError;
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    statusText = R.string.status_advTooManyAdvertisers;
                    break;
                default:
                    statusText = R.string.status_notAdvertising;
                    Log.wtf(TAG, "Unhandled error: " + errorCode);
            }
            //mAdvStatus.setText(statusText);
            Toast.makeText(getApplicationContext(), statusText, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v(TAG, "Broadcasting");
            //mAdvStatus.setText(R.string.status_advertising);
            Toast.makeText(getApplicationContext(), R.string.status_advertising, Toast.LENGTH_SHORT).show();
        }
    };
    private BluetoothGattServer mGattServer;
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothDevices.add(device);
                    //추가코드 : 컨넥션 연결되었을 때 advertisement 멈춤.
                    mAdvertiser.stopAdvertising(mAdvCallback);
                    updateConnectedDevicesStatus();
                    Log.v(TAG, "Connected to device: " + device.getAddress());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    mBluetoothDevices.remove(device);
                    updateConnectedDevicesStatus();

                    Log.v(TAG, "Disconnected from device");
                    //추가코드 : 컨넥션 해제되었을 때 advertisement 다시 시작.
                    mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
                }
            } else {
                mBluetoothDevices.remove(device);
                updateConnectedDevicesStatus();
                // There are too many gatt errors (some of them not even in the documentation) so we just
                // show the error to the user.
                final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WarningActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e(TAG, "Error when connecting: " + status);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()) +  " that is: " + bytesToString(characteristic.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            int status = mCurrentServiceFragment.writeCharacteristic(characteristic, offset, value);
            Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value) + "/ status: " + status +  " that is: " + bytesToString(value));
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
                        /* No need to respond with an offset */ 0,
                        /* No need to respond with a value */ null);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(descriptor.getValue()) +  " that is: " + bytesToString(descriptor.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                    offset, value);
            Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value)  +  " that is: " + bytesToString(value));
            int status = BluetoothGatt.GATT_SUCCESS;
            if (descriptor.getUuid() == CLIENT_CHARACTERISTIC_CONFIGURATION_UUID) {
                BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                boolean supportsNotifications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                boolean supportsIndications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;

                if (!(supportsNotifications || supportsIndications)) {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                } else if (value.length != 2) {
                    status = BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
                } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    mCurrentServiceFragment.notificationsDisabled(characteristic);
                    descriptor.setValue(value);
                } else if (supportsNotifications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    mCurrentServiceFragment.notificationsEnabled(characteristic, false /* indicate */);
                    descriptor.setValue(value);
                } else if (supportsIndications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    mCurrentServiceFragment.notificationsEnabled(characteristic, true /* indicate */);
                    descriptor.setValue(value);
                } else {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                }
            } else {
                status = BluetoothGatt.GATT_SUCCESS;
                descriptor.setValue(value);
            }
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
                        /* No need to respond with offset */ 0,
                        /* No need to respond with a value */ null);
            }
        }
    };

    /////////////////////////////////
    ////// Lifecycle Callbacks //////
    /////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //        여기는 ON CREATE VIEW 등등 액티비티의 시작화면 관련 설정
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_layout);

        //블루투스 권한 요청
        if (!runtimeCheckPermission(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSION);
        } else {
            Log.i("권한 테스트", "권한이 있네요");
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mAdvStatus = (TextView) findViewById(R.id.textView_advertisingStatus);
        mConnectionStatus = (TextView) findViewById(R.id.textView_connectionStatus);
        mBluetoothDevices = new HashSet<>();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();


        //정확히 여기서 4가지 서비스 중 한개로 넘어가는 것 ㅇㅇ Peripherals에서 받아온 리스트 인덱스 번호
        //EXTRA_PERIPHERAL_INDEX를 가지고 어떤 서비스로 넘어갈지 판단한다.
        //If we are not being restored from a previous state then create and add the fragment.
        if (savedInstanceState == null) {

            mCurrentServiceFragment = mWarningServiceFragment;

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mCurrentServiceFragment, CURRENT_FRAGMENT_TAG)
                    .commit();
        } else {
            mCurrentServiceFragment = (ServiceFragment) getFragmentManager()
                    .findFragmentByTag(CURRENT_FRAGMENT_TAG);
        }
        mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattService();

        mAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();
        mAdvData = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(mCurrentServiceFragment.getServiceUUID())
                .build();
        mAdvScanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build();

        //Fragment에 있던 부분 가져옴.
        //이거는 Send
        mSendCharacteristic =
                new BluetoothGattCharacteristic(SEND_UUID,
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_READ,
                        /* No permissions */ BluetoothGattCharacteristic.PERMISSION_READ);

        mSendCharacteristic.addDescriptor(
                WarningActivity.getClientCharacteristicConfigurationDescriptor());

        mSendCharacteristic.addDescriptor(
                WarningActivity.getCharacteristicUserDescriptionDescriptor(SEND_DESCRIPTION));

        //이거는 Receive
        mReceiveCharacteristic =
                new BluetoothGattCharacteristic(
                        RECIEVE_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        mReceiveCharacteristic.addDescriptor(WarningActivity.getClientCharacteristicConfigurationDescriptor());

        mReceiveCharacteristic.addDescriptor(
                WarningActivity.getCharacteristicUserDescriptionDescriptor(RECEIVE_DESCRIPTION));

        mBluetoothGattService = new BluetoothGattService(UART_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mBluetoothGattService.addCharacteristic(mSendCharacteristic);
        mBluetoothGattService.addCharacteristic(mReceiveCharacteristic);

        //디바이스 네임 설정하는 부분
        BluetoothAdapter.getDefaultAdapter().setName("unlab_device");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_warning, menu);
        return true /* show menu */;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                    Toast.makeText(this, R.string.bluetoothAdvertisingNotSupported, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Advertising not supported");
                }
                onStart();
            } else {
                //TODO(g-ortuno): UX for asking the user to activate bt
                Toast.makeText(this, R.string.bluetoothNotEnabled, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Bluetooth not enabled");
                finish();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        resetStatusViews();
        // If the user disabled Bluetooth when the app was in the background,
        // openGattServer() will return null.
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mGattServer == null) {
            ensureBleFeaturesAvailable();
            return;
        }
        // Add a service for a total of three services (Generic Attribute and Generic Access
        // are present by default).
        mGattServer.addService(mBluetoothGattService);

        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
        } else {
            //mAdvStatus.setText(R.string.status_noLeAdv);
            Toast.makeText(getApplicationContext(), R.string.status_noLeAdv, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_disconnect_devices && alert_mode==0) {
            //여기서 AlertDialog를 사용해서 온오프시에 확인창 팝업
            AlertDialog.Builder builder = new AlertDialog.Builder(WarningActivity.this);
            builder.setTitle("경고");
            if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
                builder.setMessage("연결을 끊을까요?");
            }
            else if (mBluetoothAdapter.isEnabled() && mAdvertiser == null) {
                builder.setMessage("연결을 시작할까요?");
            }
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // on/off작업
                            //Off
                            if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
                                disconnectFromDevices();
                            }
                            //On
                            else if (mBluetoothAdapter.isEnabled() && mAdvertiser == null) {
//                                mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
//                                mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
//                                resetStatusViews();
                                //SettingServiceFrag 직접 초기화 시켜주는 것.
                                mSettingServiceFragment = new SettingServiceFragment();
                                onStart();
                            }
                        }
                    });
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    });

            builder.show();




            return true /* event_consumed */;
        }
        else if (item.getItemId() == R.id.action_warning && alert_mode==0) {
            mCurrentServiceFragment = mWarningServiceFragment;
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mCurrentServiceFragment, CURRENT_FRAGMENT_TAG)
                    .commit();

            return true /* event_consumed */;
        }

        else if (item.getItemId() == R.id.action_setting && alert_mode==0) {
            //원래는 이렇게 화면 자체를 새롭게 띄워줬었는데 이렇게 하면 안 될 것 같기도하고...
//            Intent intent = new Intent(this, SettingActivity.class);
//            startActivity(intent);
//            아래 이거는 화면이 겹쳐버린다 ㅜㅜ 일단 아이디어는 Fragment만 바꿔주는 식으로 처리하는 것.
            mCurrentServiceFragment = mSettingServiceFragment;
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mCurrentServiceFragment, CURRENT_FRAGMENT_TAG)
                    .commit();

            return true /* event_consumed */;
        }

        return false /* event_consumed */;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
            // If stopAdvertising() gets called before close() a null
            // pointer exception is raised.
            mAdvertiser.stopAdvertising(mAdvCallback);
        }
        resetStatusViews();
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if (0 <= intervalTime && finishtime >= intervalTime)
        {
            finish();
        }
        else
        {
            presstime = tempTime;
            Toast.makeText(getApplicationContext(), "한번더 누르시면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        boolean indicate = (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                == BluetoothGattCharacteristic.PROPERTY_INDICATE;
        for (BluetoothDevice device : mBluetoothDevices) {
            // true for indication (acknowledge) and false for notification (unacknowledge).
            mGattServer.notifyCharacteristicChanged(device, characteristic, indicate);
        }
    }

    private void resetStatusViews() {
        //mAdvStatus.setText(R.string.status_notAdvertising);
        //Toast.makeText(getApplicationContext(), R.string.status_notAdvertising, Toast.LENGTH_SHORT).show();
        updateConnectedDevicesStatus();
    }

    private void updateConnectedDevicesStatus() {
        final String message = getString(R.string.status_devicesConnected) + " "
                + mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionStatus.setText(message);
            }
        });
    }

    ///////////////////////
    ////// Bluetooth //////
    ///////////////////////

    //여기서 처음에 디스크립터의 기본 값들 설정해줌
    public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        descriptor.setValue(new byte[]{0, 0});
        return descriptor;
    }

    public static BluetoothGattDescriptor getCharacteristicUserDescriptionDescriptor(String defaultValue) {
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                CHARACTERISTIC_USER_DESCRIPTION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        try {
            descriptor.setValue(defaultValue.getBytes("UTF-8"));
        } finally {
            return descriptor;
        }
    }

    private void ensureBleFeaturesAvailable() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetoothNotSupported, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Bluetooth not supported");
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Make sure bluetooth is enabled.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    private void disconnectFromDevices() {
        Log.d(TAG, "Disconnecting devices...");

        //추가코드: 여기가 앵커에 끊는 신호인 {99}값 보내는 곳.
        mCurrentServiceFragment.SendDisconnection();

        for (BluetoothDevice device : mBluetoothManager.getConnectedDevices(
                BluetoothGattServer.GATT)) {
            Log.d(TAG, "Devices: " + device.getAddress() + " " + device.getName());
            mGattServer.cancelConnection(device);
        }
        //추가코드 : 아예 ble서버 확실하게 꺼버리게끔 바꿈.
        if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
            // If stopAdvertising() gets called before close() a null
            // pointer exception is raised.
            mAdvertiser.stopAdvertising(mAdvCallback);
            mAdvertiser = null;
        }
        resetStatusViews();

        //추가코드 : 컨넥션 해제되었을 때 fragment 초기화(다시 시작).
        mCurrentServiceFragment = new WarningServiceFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mCurrentServiceFragment, CURRENT_FRAGMENT_TAG)
                .commit();
    }


    //이건 개인적으로 추가해준 byte array(Ascii array)에서 string으로 변환 함수.
    public String bytesToString(byte[] value) {
//        String converted = "";
//        for (int i : value) {
//            converted = converted.concat(Character.toString((char) i));
//        }
//        return converted;
        StringBuilder builder = new StringBuilder();
        for (byte data : value) {
            builder.append(String.format("%02X ", data));
        }
        return builder.toString();
    }

    public void onButton9Clicked(View view) {
        finish();
    }
}