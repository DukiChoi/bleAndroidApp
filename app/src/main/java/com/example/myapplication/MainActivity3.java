package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//서버 &클라이언트 창
public class MainActivity3 extends AppCompatActivity {
    TextView textView2;
    TextView textView3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
    }



    public void onBackButton8Clicked(View view) {
        finish();
    }

    public void onBackButton5Clicked(View view) throws IOException {
        int port = 5050;
        String resultText = "";
        textView2 = (TextView) findViewById(R.id.textView2);
        //서버 소켓을 생성
        ServerSocket ssk = new ServerSocket(port); //서버 자신의 포트를 설정해준다.

        textView2.setText("접속 대기중");

        while (true) {
            Socket sock = ssk.accept(); // 새로운 소켓을 생성 클라이언트가 들어왔을때 , 접속했을때  실행되는 구문
            textView2.setText("사용자 접속 했습니다");
            textView2.setText("Client ip :" + sock.getInetAddress());
        } //while

    }
    public void onBackButton6Clicked(View view) throws IOException {
        String resultText = "";
        textView3 = (TextView)findViewById(R.id.textView3);
        // 연결 시에 소켓이 생성된다. 연결이 안될경우에는 예외발생한다.
        Socket sk = new Socket("127.0.0.1" , 5050) ;
        resultText = "서버와 접속이 되었습니다.";
        textView3.setText(resultText);
    }
}
