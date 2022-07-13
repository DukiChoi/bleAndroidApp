/*
 * Copyright 2015 Google Inc. All rights reserved.
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

import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import com.example.myapplication.WarningActivity;

import java.util.UUID;

public abstract class ServiceFragment extends Fragment{
  public abstract BluetoothGattService getBluetoothGattService();
  public abstract ParcelUuid getServiceUUID();


  public BluetoothGattService mNordicUartService;
  public BluetoothGattCharacteristic mSendCharacteristic;
  public BluetoothGattCharacteristic mReceiveCharacteristic;
  public BluetoothGattDescriptor mReceiveCCCDescriptor;

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


  public ServiceFragment() {
    //이거는 Send
    mSendCharacteristic =
            new BluetoothGattCharacteristic(SEND_UUID,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
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

    mNordicUartService = new BluetoothGattService(UART_SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY);
    mNordicUartService.addCharacteristic(mSendCharacteristic);
    mNordicUartService.addCharacteristic(mReceiveCharacteristic);


  }
  /**
   * Function to communicate to the ServiceFragment that a device wants to write to a
   * characteristic.
   *
   * The ServiceFragment should check that the value being written is valid and
   * return a code appropriately. The ServiceFragment should update the UI to reflect the change.
   * @param characteristic Characteristic to write to
   * @param value Value to write to the characteristic
   * @return {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS} if the write operation
   * was completed successfully. See {@link android.bluetooth.BluetoothGatt} for GATT return codes.
   */
  public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
    throw new UnsupportedOperationException("Method writeCharacteristic not overridden");
  };

  /**
   * Function to notify to the ServiceFragment that a device has disabled notifications on a
   * CCC descriptor.
   *
   * The ServiceFragment should update the UI to reflect the change.
   * @param characteristic Characteristic written to
   */
  public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
    throw new UnsupportedOperationException("Method notificationsDisabled not overridden");
  };

  /**
   * Function to notify to the ServiceFragment that a device has enabled notifications on a
   * CCC descriptor.
   *
   * The ServiceFragment should update the UI to reflect the change.
   * @param characteristic Characteristic written to
   * @param indicate Boolean that says if it's indicate or notify.
   */
  public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
    throw new UnsupportedOperationException("Method notificationsEnabled not overridden");
  };

  /**
   * This interface must be implemented by activities that contain a ServiceFragment to allow an
   * interaction in the fragment to be communicated to the activity.
   */
  public interface ServiceFragmentDelegate {
    void sendNotificationToDevices(BluetoothGattCharacteristic characteristic);
  }

  public void SendDisconnection(){
    //오버라이드 될 것.
  }
}
