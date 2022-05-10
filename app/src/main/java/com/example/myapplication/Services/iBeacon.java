//package com.example.myapplication;
//
//import android.Manifest;
//
////여기가 블루투스 부분
//import android.annotation.SuppressLint;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothSocket;
////여기가 블루투스LE 부분
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattService;
//
//import android.bluetooth.le.AdvertiseCallback;
//import android.bluetooth.le.AdvertiseData;
//import android.bluetooth.le.AdvertiseSettings;
//import android.bluetooth.le.AdvertisingSet;
//import android.bluetooth.le.AdvertisingSetCallback;
//import android.bluetooth.le.AdvertisingSetParameters;
//import android.bluetooth.le.BluetoothLeAdvertiser;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.ParcelUuid;
//import android.os.SystemClock;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.Switch;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//public class MainActivity6 extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
//    TextView mTvBluetoothStatus;
//    TextView mTvReceiveData;
//    TextView mTvSendData;
//    Button mBtnBluetoothOn;
//    Button mBtnBluetoothOff;
//    Button mBtnConnect;
//    Button mBtnSendData;
//    private Switch switchView;
//    private EditText etUUID;
//    private EditText etMajor;
//    private EditText etMinor;
//
//    BluetoothAdapter mBluetoothAdapter;
//    Set<BluetoothDevice> mPairedDevices;
//    List<String> mListPairedDevices;
//
//    Handler mBluetoothHandler;
//    ConnectedBluetoothThread mThreadConnectedBluetooth;
//    BluetoothDevice mBluetoothDevice;
//    BluetoothSocket mBluetoothSocket;
//
//    private BluetoothLeAdvertiser mBTAdvertiser;
//    private static final int REQUEST_ENABLE_BT = 1;
//    private static final String LOG_TAG = "MyActivity6";
//
//    private String[] PERMISSIONS = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
//    final static int BT_REQUEST_ENABLE = 1;
//    final static int BT_MESSAGE_READ = 2;
//    final static int BT_CONNECTING_STATUS = 3;
//    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34fb");
//
//    //블루투스 권한 요청 함수
//    public boolean runtimeCheckPermission(Context context, String... permissions) {
//        if (context != null && permissions != null) {
//            for (String permission : permissions) {
//                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//
//                }
//            }
//        }
//        return true;
//    }
//
//    private static final int MULTIPLE_PERMISSION = 1004;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main6);
//
//        //블루투스 권한 요청
//        if (!runtimeCheckPermission(this, PERMISSIONS)) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSION);
//        } else {
//            Log.i("권한 테스트", "권한이 있네요");
//        }
//
//        mTvBluetoothStatus = (TextView) findViewById(R.id.tvBluetoothStatus);
//        mTvReceiveData = (TextView) findViewById(R.id.tvReceiveData);
//        mTvSendData = (EditText) findViewById(R.id.tvSendData);
//        mBtnBluetoothOn = (Button) findViewById(R.id.btnBluetoothOn);
//        mBtnBluetoothOff = (Button) findViewById(R.id.btnBluetoothOff);
//        mBtnConnect = (Button) findViewById(R.id.btnConnect);
//        mBtnSendData = (Button) findViewById(R.id.btnSendData);
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        switchView = (Switch) findViewById(R.id.switch_view);
//        etUUID = (EditText) findViewById(R.id.et_uuid);
//        etMajor = (EditText) findViewById(R.id.et_major);
//        etMinor = (EditText) findViewById(R.id.et_minor);
//
//        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                bluetoothOn();
//            }
//        });
//        mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                bluetoothOff();
//            }
//        });
//        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                listPairedDevices();
//            }
//        });
//        mBtnSendData.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mThreadConnectedBluetooth != null) {
//                    mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
//                    //Toast.makeText(getApplicationContext(), mTvSendData.getText().toString()+"눌렸음 ㅇㅇ", Toast.LENGTH_LONG).show();
//                    mTvSendData.setText("");
//                }
//            }
//        });
//
//
//        mBluetoothHandler = new Handler() {
//            @SuppressLint("HandlerLeak")
//            public void handleMessage(android.os.Message msg) {
//                if (msg.what == BT_MESSAGE_READ) {
//                    String readMessage = null;
//                    try {
//                        readMessage = new String((byte[]) msg.obj, "UTF-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    mTvReceiveData.setText(readMessage);
//                }
//            }
//        };
//
//        setupBLE();
//    }
//    //----------------------------------------------------------------------------------------------
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopAdvertise();
//        switchView.setChecked(false);
//    }
//
//
//    public static boolean isBLESupported(Context context) {
//        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
//    }
//
//    public static BluetoothManager getManager(Context context) {
//        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
//    }
//
//    private void setupBLE() {
//        if (!isBLESupported(this)) {
//            Toast.makeText(this, "device not support ble", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        BluetoothManager manager = getManager(this);
//        if (manager != null) {
//            mBluetoothAdapter = manager.getAdapter();
//        }
//        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
//            Toast.makeText(this, "bluetooth not open", Toast.LENGTH_SHORT).show();
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            return;
//        }
//    }
//
//    private void startAdvertise(String uuid, int major, int minor) {
//        if (mBluetoothAdapter == null) {
//            return;
//        }
//        if (mBTAdvertiser == null) {
//            mBTAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mBluetoothAdapter.setName("Test");
//        if (mBTAdvertiser != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mBTAdvertiser.startAdvertising(
//                    createAdvertiseSettings(true, 0),
//                    createAdvertiseData(
//                            UUID.fromString(uuid),
//                            major, minor, (byte) 0xc5),
//                    mAdvCallback);
//        }
//    }
//
//    private void stopAdvertise() {
//        if (mBTAdvertiser != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mBTAdvertiser.stopAdvertising(mAdvCallback);
//            mBTAdvertiser = null;
//            switchView.setChecked(false);
//        }
//        setProgressBarIndeterminateVisibility(false);
//    }
//
//    public AdvertiseSettings createAdvertiseSettings(boolean connectable, int timeoutMillis) {
//        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
//        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
//        builder.setConnectable(connectable);
//        builder.setTimeout(timeoutMillis);
//        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
//        return builder.build();
//    }
//
//
//    public AdvertiseData createAdvertiseData(UUID proximityUuid, int major,
//                                             int minor, byte txPower) {
//        if (proximityUuid == null) {
//            throw new IllegalArgumentException("proximitiUuid null");
//        }
//        byte[] manufacturerData = new byte[23];
//        ByteBuffer bb = ByteBuffer.wrap(manufacturerData);
//        bb.order(ByteOrder.BIG_ENDIAN);
//        bb.put((byte) 0x02);
//        bb.put((byte) 0x15);
//        bb.putLong(proximityUuid.getMostSignificantBits());
//        bb.putLong(proximityUuid.getLeastSignificantBits());
//        bb.putShort((short) major);
//        bb.putShort((short) minor);
//        bb.put(txPower);
//
//        AdvertiseData.Builder builder = new AdvertiseData.Builder();
//        builder.addManufacturerData(0x004c, manufacturerData);
//        AdvertiseData adv = builder.build();
//        return adv;
//    }
//
//    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
//        public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
//            if (settingsInEffect != null) {
//                Log.d("debug", "onStartSuccess TxPowerLv="
//                        + settingsInEffect.getTxPowerLevel()
//                        + " mode=" + settingsInEffect.getMode()
//                        + " timeout=" + settingsInEffect.getTimeout());
//            } else {
//                Log.d("debug", "onStartSuccess, settingInEffect is null");
//            }
//            switchView.setChecked(true);
//            setProgressBarIndeterminateVisibility(false);
//        }
//
//        public void onStartFailure(int errorCode) {
//            Log.d("debug", "onStartFailure errorCode=" + errorCode);
//            switchView.setChecked(false);
//        }
//    };
//
//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (isChecked) {
//            String uuid = etUUID.getText().toString().trim();
//            int major = 0;
//            if (!TextUtils.isEmpty(etMajor.getText().toString())) {
//                major = Integer.parseInt(etMajor.getText().toString());
//            }
//
//            int minor = 0;
//            if (!TextUtils.isEmpty(etMinor.getText().toString())) {
//                minor = Integer.parseInt(etMinor.getText().toString());
//            }
//
//            if (major < 0 || major > 65535) {
//                Toast.makeText(this, "major ranges：0- 65535", Toast.LENGTH_LONG).show();
//                switchView.setChecked(false);
//                return;
//            }
//
//            if (minor < 0 || minor > 65535) {
//                Toast.makeText(this, "major ranges：0- 65535", Toast.LENGTH_LONG).show();
//                switchView.setChecked(false);
//                return;
//            }
//
//            if (TextUtils.isEmpty(uuid) || major == 0 || minor == 0) {
//                Toast.makeText(this, "please enter params", Toast.LENGTH_LONG).show();
//                switchView.setChecked(false);
//                return;
//            }
//            startAdvertise(uuid, major, minor);
//        } else {
//            stopAdvertise();
//        }
//    }
////--------------------------------------------------------------------------------------------------
//
//
//    void bluetoothOn() {
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
//        } else {
//            if (mBluetoothAdapter.isEnabled()) {
//                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
//                mTvBluetoothStatus.setText("활성화");
//            } else {
//                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
//
//                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
//            }
//        }
//    }
//
//    void bluetoothOff() {
//        if (mBluetoothAdapter.isEnabled()) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mBluetoothAdapter.disable();
//            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
//            mTvBluetoothStatus.setText("비활성화");
//        } else {
//            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case BT_REQUEST_ENABLE:
//                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
//                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
//                    mTvBluetoothStatus.setText("활성화");
//                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
//                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
//                    mTvBluetoothStatus.setText("비활성화");
//                }
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    void listPairedDevices() {
//        if (mBluetoothAdapter.isEnabled()) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mPairedDevices = mBluetoothAdapter.getBondedDevices();
//
//            if (mPairedDevices.size() > 0) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("장치 선택");
//
//                mListPairedDevices = new ArrayList<String>();
//                for (BluetoothDevice device : mPairedDevices) {
//                    mListPairedDevices.add(device.getName());
//                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
//                }
//                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
//                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
//
//                builder.setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        connectSelectedDevice(items[item].toString());
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
//            } else {
//                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    void connectSelectedDevice(String selectedDeviceName) {
//        for (BluetoothDevice tempDevice : mPairedDevices) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            if (selectedDeviceName.equals(tempDevice.getName())) {
//                mBluetoothDevice = tempDevice;
//                break;
//            }
//        }
//        try {
//            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
//            mBluetoothSocket.connect();
//            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
//            mThreadConnectedBluetooth.start();
//            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
//        } catch (IOException e) {
//            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private class ConnectedBluetoothThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final InputStream mmInStream;
//        private final OutputStream mmOutStream;
//
//        public ConnectedBluetoothThread(BluetoothSocket socket) {
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//
//            try {
//                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) {
//                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
//            }
//
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
//        }
//        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
//
//            while (true) {
//                try {
//                    bytes = mmInStream.available();
//                    if (bytes != 0) {
//                        SystemClock.sleep(100);
//                        bytes = mmInStream.available();
//                        bytes = mmInStream.read(buffer, 0, bytes);
//                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//                    }
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }
//        public void write(String str) {
//            str +="\n";
//            byte[] bytes = str.getBytes();
//            try {
//                mmOutStream.write(bytes);
//                mmOutStream.flush();
//                Toast.makeText(getApplicationContext(), bytes+"데이터를 보냈습니다.", Toast.LENGTH_LONG).show();
//            } catch (IOException e) {
//                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
//            }
//        }
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//}