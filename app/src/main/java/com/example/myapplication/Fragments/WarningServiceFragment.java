/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication.Fragments;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.myapplication.ExampleActivities.Peripheral;
import com.example.myapplication.R;

import java.util.Arrays;
import java.util.UUID;

public class WarningServiceFragment extends ServiceFragment {


  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //        변수 선언
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////

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


  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //       텍스트 뷰, 클릭 리스너 설정 ( 사용자 상호작용 설정 )
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////


  private BluetoothGattService mNordicUartService;
  private BluetoothGattCharacteristic mSendCharacteristic;
  private BluetoothGattCharacteristic mReceiveCharacteristic;
  private BluetoothGattDescriptor mReceiveCCCDescriptor;


  private ServiceFragmentDelegate mDelegate;

  //원래있던 TemperatureMeasurement를 Send로 바꿔줌.
  private EditText mEditTextSendValue;
  private TextView mTextViewReceiveValue1;
  private TextView mTextViewReceiveValue2;
  private TextView mTextViewReceiveValue3;
  //이건 Text Editor에 수정을 할 시에 그걸 가지고 보낼 값(Characteristic Value)을 바꾸는 것.
  private final OnEditorActionListener mOnEditorActionListenerSend = new OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        String newSENDValueString = textView.getText().toString();
        if (isValidCharacteristicValue(newSENDValueString,
                SEND_VALUE_FORMAT)) {
//          //첫번째, String 형식을 byte 형식으로 바꾸기
//          byte[] newSENDbytes = mEditTextSendValue.getText().toString().getBytes(StandardCharsets.US_ASCII);
//          mSendCharacteristic.setValue(newSENDbytes);
          //두번째. int로 바꾸기
          int newSendValue = Integer.parseInt(newSENDValueString);

          mSendCharacteristic.setValue(newSendValue,
                  SEND_VALUE_FORMAT,
                  /* offset */ 0);
        } else {
          Toast.makeText(getActivity(), "Chracteristic 형식이 틀립니다.",
                  Toast.LENGTH_SHORT).show();
        }
      }
      return false;
    }
  };

  //이건 사실 필요없음. TxChar값이 들어가는 건 EditText가 아닌 입력 불가능한 TextView라서 굳이 리스닝 해줄 필요 없기 떄문.
  private final OnEditorActionListener mOnEditorActionListenerReceive = new OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        String newReceiveValueString = textView.getText().toString();
        if (isValidCharacteristicValue(newReceiveValueString,
                RECEIVE_VALUE_FORMAT)) {
          int newReceiveValue = Integer.parseInt(newReceiveValueString);
          mReceiveCharacteristic.setValue(newReceiveValue,
                  RECEIVE_VALUE_FORMAT,
                  /* offset */ 1);
        } else {
          Toast.makeText(getActivity(), "Chracteristic 형식이 틀립니다.",
                  Toast.LENGTH_SHORT).show();
        }
      }
      return false;
    }
  };


  private static final String TAG = Peripheral.class.getCanonicalName();
  //이 부분은 Send하는 부분이라 Warning에서는 주석처리함.
//  //이건 Notify 버튼 즉 Send 버튼을 리스닝 해주는 함수
//  private final View.OnClickListener mNotifyButtonListener = new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//
////      //첫번째 방법, String 형식을 byte 형식으로 바꿔서 보내기
////      byte[] newSENDbytes = mEditTextSendValue.getText().toString().getBytes(StandardCharsets.US_ASCII);
////      mSendCharacteristic.setValue(newSENDbytes);
//
//      //두번째 방법, Integer로 보내기([값]형태)
//      int integer_to_send = Integer.parseInt(mEditTextSendValue.getText().toString());
//      mSendCharacteristic.setValue(integer_to_send,
//              SEND_VALUE_FORMAT,
//              /* offset */ 0);
//
//      //★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
//      //정확히는 여기에서 NOTIFICATION을 SEND 해준다. (TxChar을 통해서)
//      //★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
//      mDelegate.sendNotificationToDevices(mSendCharacteristic);
//      //Log.v(TAG, "sent: " + Arrays.toString(mSendCharacteristic.getValue()) + " / that is: " + bytesToString(mSendCharacteristic.getValue()));
//      Log.v(TAG, "sent: " + Arrays.toString(mSendCharacteristic.getValue()));
//    }
//  };



  /*
  원래있던 MeasurementInterval을 ReceiveValue로 바꿔줌
  BLUETOOTH GATT 다루는 부분은 아래 사이트 참고
  https://developer.android.com/reference/android/bluetooth/BluetoothGattCharacteristic#setValue(int,%20int,%20int)
  sendValue getValue 등 쓸 수 있는 함수들 인자 관련 설명 있음.
  */

  public WarningServiceFragment() {

    //이거는 Send
    mSendCharacteristic =
            new BluetoothGattCharacteristic(SEND_UUID,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_READ,
                    /* No permissions */ BluetoothGattCharacteristic.PERMISSION_READ);

    mSendCharacteristic.addDescriptor(
            Peripheral.getClientCharacteristicConfigurationDescriptor());

    mSendCharacteristic.addDescriptor(
            Peripheral.getCharacteristicUserDescriptionDescriptor(SEND_DESCRIPTION));

    //이거는 Receive
    mReceiveCharacteristic =
            new BluetoothGattCharacteristic(
                    RECIEVE_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

    mReceiveCharacteristic.addDescriptor(Peripheral.getClientCharacteristicConfigurationDescriptor());

    mReceiveCharacteristic.addDescriptor(
            Peripheral.getCharacteristicUserDescriptionDescriptor(RECEIVE_DESCRIPTION));

    mNordicUartService = new BluetoothGattService(UART_SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY);
    mNordicUartService.addCharacteristic(mSendCharacteristic);
    mNordicUartService.addCharacteristic(mReceiveCharacteristic);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //        여기는 ON CREATE VIEW 등등 시작화면 관련 설정
  //  https://developer.android.com/reference/android/app/Fragment 참고
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_warning, container, false);
//    mEditTextSendValue = (EditText) view
//            .findViewById(R.id.EditText_Sendvalue);
//    mEditTextSendValue
//            .setOnEditorActionListener(mOnEditorActionListenerSend);
    mTextViewReceiveValue1 = (TextView) view
            .findViewById(R.id.Textview_Recievevalue_a1);
    mTextViewReceiveValue2 = (TextView) view
            .findViewById(R.id.Textview_Recievevalue_a2);
    mTextViewReceiveValue3 = (TextView) view
            .findViewById(R.id.Textview_Recievevalue_a3);
    //여기서 Editor 리스너를 써줬기는 한데 여기선 사실상 필요가 없다.(EditText 대신 TextView 써줘서 ㅇㅇ)
//    mTextViewReceiveValue1
//            .setOnEditorActionListener(mOnEditorActionListenerReceive);
//    Button notifyButton = (Button) view.findViewById(R.id.button_poweron);
//    notifyButton.setOnClickListener(mNotifyButtonListener);
//    setSendValue(INITIAL_SEND, INITIAL_RECEIVE);
    byte[] value = {0,0,0,0,0,0,0,0};
    int[] members = classification(value);
    int value1 = members[0];
    int value2 = members[1];
    int value3 = members[2];
    //두번째 방법, Integer로 받기([값]형태)
    //mTextViewReceiveValue.setText(Arrays.toString(value));

    mTextViewReceiveValue1.setText(Integer.toString(value1));
    mTextViewReceiveValue2.setText(Integer.toString(value2));
    mTextViewReceiveValue3.setText(Integer.toString(value3));
    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mDelegate = (ServiceFragmentDelegate) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
              + " must implement ServiceFragmentDelegate");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mDelegate = null;
  }

  @Override
  public void onStop() {
    super.onStop();
  }
  @Override
  public void onPause(){
    super.onPause();
  }
  @Override
  public void onResume(){
    super.onResume();
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //        화면설정 끝
  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public BluetoothGattService getBluetoothGattService() {
    return mNordicUartService;
  }

  @Override
  public ParcelUuid getServiceUUID() {
    return new ParcelUuid(UART_SERVICE_UUID);
  }

  //이건 처음에 onCreate에서 값 세팅 해주는 것. 그냥 시작에만 한번 딱 설정해준다.
  private void setSendValue(int SendValue, int ReceiveValue) {

    /* Set the org.bluetooth.characteristic.temperature_measurement
     * characteristic to a byte array of size 5 so
     * we can use setValue(value, format, offset);
     *
     * Flags (8bit) + Temperature Measurement Value (float) = 5 bytes
     *
     * Flags:
     *   Temperature Units Flag (0) -> Celsius
     *   Time Stamp Flag (0) -> Time Stamp field not present
     *   Temperature Type Flag (0) -> Temperature Type field not present
     *   Unused (00000)
     */

    //보낼 값이니까 Send Characteristic(TxChar)의 value 값을 변경해주는데 byte array형식으로 집어넣는다.
    //이건 앞으로 (uint8형식으로 넣는다는 뜻) flag를 8(uint8)로 맞춰주는 것.
    //mSendCharacteristic.setValue(new byte[]{0b00001000, 0, 0, 0});
    mSendCharacteristic.setValue(new byte[]{0});
    //mReceiveCharacteristic.setValue(new byte[]{0b00001000, 0, 0, 0});
    mReceiveCharacteristic.setValue(new byte[]{0});

    // Characteristic Value: [flags, 0, 0, 0]


    mSendCharacteristic.setValue(SendValue,
            SEND_VALUE_FORMAT,
            /* offset */ 1);

    mReceiveCharacteristic.setValue(ReceiveValue,
            RECEIVE_VALUE_FORMAT,
            /* offset */ 1);
    // Characteristic Value: [flags, heart rate value, 0, 0]
    mEditTextSendValue.setText(Integer.toString(SendValue));
    mTextViewReceiveValue1.setText(Integer.toString(ReceiveValue));
    //여기서 보낼 값으로 에딧 텍스트 값도 변경해줌
  }


  //★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
  //이건 Receive 값 받아오는 함수(Write 권한 있는 RxChar)
  //★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
  @Override
  public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
    if (offset != 0) {
      return BluetoothGatt.GATT_INVALID_OFFSET;
    }
    // Heart Rate control point is a 8bit characteristic
    //글자수 제한인데 원래 1글자만 받던거 풀어줌.
    if (value.length > 1000) {
      return BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
//        //첫번째 방법, byte 형식을 String 형식으로 바꿔서 받기
//        //이 부분에서 아스키 코드로 된 btye array를 string으로 변환해줌
//        mTextViewReceiveValue.setText(bytesToString(value));
//        //로그에서 원래 아스키코드 배열과 / 변환되어 나온 string값을 보여줌
//        Log.v(TAG, "Received: " + Arrays.toString(value) + " / converted into:" + bytesToString(value));
        //여기서 이제 Value를 [태그, 거리, 태그, 거리, 태그, 거리, 태그, 거리] 로 받기로 했으므로,
        int[] members = classification(value);
        int value1 = members[0];
        int value2 = members[1];
        int value3 = members[2];
        Log.v(TAG, "members are: " + value1 + ", " + value2 + ", " + value3);
        //두번째 방법, Integer로 받기([값]형태)
        //mTextViewReceiveValue.setText(Arrays.toString(value));

        mTextViewReceiveValue1.setText(String.valueOf(value1));
        mTextViewReceiveValue2.setText(String.valueOf(value2));
        mTextViewReceiveValue3.setText(String.valueOf(value3));

        //로그에서 원래 아스키코드 배열과 / 변환되어 나온 string값을 보여줌
        Log.v(TAG, "Received: " + Arrays.toString(value));
      }
    });
    return BluetoothGatt.GATT_SUCCESS;
  }


  // 노티피케이션을 쓸 수 있게 되면 앱 푸시 알림
  @Override
  public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
    if (characteristic.getUuid() != SEND_UUID) {
      Log.v(TAG, "UUID가 SendUUID와 다릅니다: " + characteristic.getUuid());
      return;
    }
    if (indicate) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
//        int newSENDValueString = Integer.parseInt(mEditTextSendValue.getText().toString());
//
//        mSendCharacteristic.setValue(newSENDValueString,
//                SEND_VALUE_FORMAT,
//                /* offset */ 1);
        Toast.makeText(getActivity(), R.string.notificationsEnabled, Toast.LENGTH_SHORT)
                .show();
      }
    });
  }

  //노티피케이션을 못 쓰게 되면 앱 푸시 알림
  @Override
  public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
    if (characteristic.getUuid() != SEND_UUID) {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getActivity(), R.string.notificationsNotEnabled, Toast.LENGTH_SHORT)
                .show();
      }
    });
  }

  // 유효한 특성 값인지 알아내는 함수. 예를 들어 String값을 보낸다면
  // 그게 하나하나 파싱 했을 때 저 비트 안에 들어가는지 ㅇㅇ
  private boolean isValidCharacteristicValue(String s, int format) {
    try {
      int value = Integer.parseInt(s);
      if (format == BluetoothGattCharacteristic.FORMAT_UINT8) {
        return (value >= MIN_UINT) && (value <= MAX_UINT8);
      } else if (format == BluetoothGattCharacteristic.FORMAT_UINT16) {
        return (value >= MIN_UINT) && (value <= MAX_UINT16);
      } else {
        throw new IllegalArgumentException(format + " is not a valid argument");
      }
    } catch (NumberFormatException e) {
      return false;
    }
  }

  //이건 그냥 btye array에서 Hex값으로 바꿔주는 함수(안 씀)
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
  /*
  이건 string to ASCII. Notify 할 때 쓸 수 있음. string을 editText에 입력 받으면
  그걸 다시 ASCII로 변환한 다음에 그걸 전송하고
  */
  public String bytesToString(byte[] value) {
    String converted = "";
    for (int i : value) {
      converted = converted.concat(Character.toString((char) i));
    }
    return converted;
  }

  public int[] classification(byte[] value){
    //테스트값: 4, 9, 16, 18
    //0001010202030303
    byte[] test_value = {0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03, 0x03};
    int[] member_for_distances = {0, 0, 0};
    for(int i = 0; i < 4; i++){
      //위험반경 : 5미만
      if(value[2*i+1] == 0x01){
        member_for_distances[0]++;
      }
      //경고반경 : 5이상 10미만
      else if(value[2*i+1] == 0x02){
        member_for_distances[1]++;
      }
      //접근반경 : 10 이상;
      else if(value[2*i+1] == 0x03){
        member_for_distances[2]++;
      }
    }
    return member_for_distances;
  }
  @Override
  public void SendDisconnection(){
    byte[] disconnectionValue = {99};
    mSendCharacteristic.setValue(disconnectionValue);
    Log.v(TAG, "sent: " + Arrays.toString(disconnectionValue));
  }
}
