package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//서버 &클라이언트 창
public class MainActivity3 extends AppCompatActivity {
    TextView textView2;
    TextView textView3;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
    }



    public void onButton8Clicked(View view) {
        finish();
    }

    public void onButton5Clicked(View view){
        Intent intent = new Intent(getApplicationContext(), ServerService.class);
        startService(intent);

    }


    public void onButton6Clicked(View view){
        ClientThread thread = new ClientThread();
        thread.start();

    }

    class ClientThread extends Thread{
        public void run(){
            String host = "192.168.0.9";
            int port = 5050;
            try{
                Socket socket = new Socket(host, port);
                //서버로 데이터 주기
                ObjectOutputStream outstream = new ObjectOutputStream(socket.getOutputStream());
                outstream.writeObject("안녕!");
                outstream.flush();
                Log.d("ClientThread","서버로 보냄");
                //서버에서부터 데이터 받기
                ObjectInputStream instream = new ObjectInputStream(socket.getInputStream());
                final Object input = instream.readObject();
                Log.d("ClientThread", "받은 데이터: "+input);
                //스레드 안에서 UI 접근 -> 핸들러
                handler.post(new Runnable() {
                    @Override public void run() {
                        textView3.setText("받은 데이터: "+input);
                    }
                });
            }catch(Exception e){ e.printStackTrace();}
        }
    }
}
