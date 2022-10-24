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
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;
import com.example.myapplication.Services.AppHelper;
import com.example.myapplication.WarningActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ServiceFragment extends Fragment{
  public abstract BluetoothGattService getBluetoothGattService();
  public abstract ParcelUuid getServiceUUID();
  public static Vibrator vibrator = null;
  public static MediaPlayer player = null;
  public static Thread triggerService = null;
  public static Animation anim = null;
  public static final String TAG = WarningActivity.TAG;

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

  //경고음 쓰레드
  public void alert(){
    //경고임을 알려주는 변수 1로 만들음
    WarningActivity.alert_mode = 1;
    player_and_anim();
    vibrator = (Vibrator) WarningActivity.context.getSystemService(Context.VIBRATOR_SERVICE);

    //여기서 HTTP POST로 태그 수 보내준다.

    //진동은 따로 쓰레드 써서 계속 울리게 해야함
    triggerService = new Thread(new Runnable() {
      @Override
      public void run() {
        while(!triggerService.isInterrupted())
        {
          try {
            Log.v(TAG, "진동 시작합니다");
            vibrator.vibrate(1000);
            Thread.sleep(2000);
          } catch (InterruptedException e){
            //이건 아예 스탑
            if(WarningActivity.alert_mode ==0) {
              if (player != null) {
                player.stop();
                Log.v(TAG, "경고음 stop");
              }
              anim.cancel();
              //getView().setBackgroundColor(Color.WHITE);
              //이거는 각각의 fragment에 넣어야 오류가 안남. view마다 쓰레드?가 달라져서 오류가 생기는 듯.
              Log.v(TAG, "화면 깜박임 stop");
              Thread.currentThread().interrupt();
              e.printStackTrace();
            }
            //이건 일시정지
            else if(WarningActivity.alert_mode == 2) {
              try {
                anim.cancel();
                player.stop();
                Thread.sleep(5000);
                //플레이어랑 애니메 다시 세팅 후 시작, 잔동 Thread는 스스로 시작함
                WarningActivity.alert_mode = 1;
                player_and_anim();
              } catch (InterruptedException ex) {
                ex.printStackTrace();
              }
            }
            //이건 스페셜 정지(
            if(WarningActivity.alert_mode ==3) {
              if (player != null) {
                player.stop();
                Log.v(TAG, "경고음 stop");
              }
              anim.cancel();
              //getView().setBackgroundColor(Color.WHITE);
              //이거는 각각의 fragment에 넣어야 오류가 안남. view마다 쓰레드?가 달라져서 오류가 생기는 듯.
              Log.v(TAG, "화면 깜박임 stop");
              Thread.currentThread().interrupt();
              e.printStackTrace();
            }
          }
        }
        // 한번 울리고 종료할려면
        // Done with our work... stop the service!
        //AlarmService_Service.this.stopSelf();
      }
    }
    );
    triggerService.start();
  }
  public void player_and_anim(){
    //벨소리
    player =  MediaPlayer.create(WarningActivity.context, R.raw.alert);
    player.start();
    //배경 빨갛게 하얗게
    getView().setBackgroundColor(Color.RED);
    anim = new AlphaAnimation(0.0f,1.0f);
    anim.setDuration(100);
    anim.setStartOffset(50);
    anim.setRepeatMode(Animation.REVERSE);
    anim.setRepeatCount(Animation.INFINITE);
    getView().startAnimation(anim);
  }
  public static void alert_stop(){
    WarningActivity.alert_mode = 0;
    if(triggerService!= null) {
      triggerService.interrupt();
      Log.v(TAG, "진동 thread interrupt");
    }
  }


  public void alert_sleep(){
    //일시정지 상태엔 버튼 잠시 누르게 해야해서 2로 설정
    WarningActivity.alert_mode = 2;
    if(triggerService!= null) {
      triggerService.interrupt();
      Log.v(TAG, "진동 thread interrupt");
    }
  }
  public void alert_stop_special(){
    //일시정지 상태엔 버튼 잠시 누르게 해야해서 2로 설정
    WarningActivity.alert_mode = 3;
    if(triggerService!= null) {
      triggerService.interrupt();
      Log.v(TAG, "진동 thread interrupt");
    }
  }
  public void GETRequest() {
    String url = "http://127.0.0.1:8080";
    StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
              @Override
              public void onResponse(String response) {
                println("응답 -> " + response);
              }
            },
            new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
              @Override
              public void onErrorResponse(VolleyError error) {
                println("에러 -> " + error.getMessage());
              }
            }
    ) {

      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Client-Type", "my_app");
        headers.put("system_token", "1234");
        headers.put("session_key", "5678");
        return headers;
      }
    };
    request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
    AppHelper.requestQueue = Volley.newRequestQueue(WarningActivity.context); // requestQueue 초기화 필수
    AppHelper.requestQueue.add(request);
    println("요청 보냄.");

  }


  public void POSTRequest() {
    String url = "http://127.0.0.1:8080";
    StringRequest request = new StringRequest(
            Request.Method.POST,
            url,
            new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
              @Override
              public void onResponse(String response) {
                println("응답 -> " + response);
              }
            },
            new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
              @Override
              public void onErrorResponse(VolleyError error) {
                println("에러 -> " + error.getMessage());
              }
            }
    ) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Client-Type", "my_app");
        headers.put("system_token", "1234");
        headers.put("session_key", "5678");
        return headers;
      }
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("anchor_id", "test1");
        params.put("worker_id", "test2");
        params.put("report_type", "te);
        params.put("measure_dt", "test4");
        return params;
      }

    };
    request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
    AppHelper.requestQueue = Volley.newRequestQueue(WarningActivity.context); // requestQueue 초기화 필수
    AppHelper.requestQueue.add(request);
    println("요청 보냄.");

  }
  public void println(String data) {
    Log.d(TAG, data +"\n");
  }
}
