package com.example.myapplication.ExampleActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

//서버 &클라이언트 창
public class TCPActivity extends AppCompatActivity {
    TextView textView2;
    TextView textView3;
    TextView textView4;
    EditText editText;
    String data_from_server;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
    }



    public void onButton8Clicked(View view) {
        finish();
    }


    //Server 버튼
    public void onButton5Clicked(View view){
        //Intent intent = new Intent(getApplicationContext(), ServerService.class);
        //startService(intent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                startServer();
            }
        }).start();
    }


    //Client 버튼
    public void onButton6Clicked(View view){
        editText = (EditText) findViewById(R.id.editText);
        textView4 = (TextView) findViewById(R.id.textView4);
        String data = editText.getText().toString();

        //ClientThread thread = new ClientThread();
        //thread.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                get(data);
            }
        }).start();


    }


    //앱에서 가져오기

    public void get(String data) {
        try {
            int portNumber = 5050;
            //여기서 아이피를 로컬로하면 핸드폰 내부에서만.
            String host = "192.168.219.116";
            //String host = "127.0.0.1";
            Socket sock = new Socket(host, portNumber); // 소켓 객체 만들기
            printClientLog("소켓 연결함.");

            /*
            ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream()); // 소켓 객체로 데이터 보내기
            outstream.writeObject(data);
            outstream.flush();
            printClientLog("데이터를 서버로 보냄 :" + data);
            */

            ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
            data_from_server = (String) instream.readObject();
            printClientLog("데이터를 서버로부터 받음 : " + data_from_server);


            //여기 속에다가 if문을 넣지 않고 메인에 넣으면 오류가 남...
            if(Integer.parseInt(data_from_server) > 10){
                textView4.setText("안전합니다");
                textView4.setTextColor(Color.GREEN);
            }
            else if(Integer.parseInt(data_from_server) <= 10){
                textView4.setText("위험합니다");
                textView4.setTextColor(Color.RED);
            }
            else{
                textView4.setText("거리값이 이상합니다");
                textView4.setTextColor(Color.BLUE);
            }

            //소켓 닫아주기
            sock.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    //이것은 시험삼아 만들어본 것. 안드로이드 앱 내부에서 서버 돌리는 걸 받기.
    public void send(String data) {
        try {
            int portNumber = 5050;
            String localhost = "localhost";
            Socket sock = new Socket(localhost, portNumber); // 소켓 객체 만들기
            printClientLog("소켓 연결함.");


            ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream()); // 소켓 객체로 데이터 보내기
            outstream.writeObject(data);
            outstream.flush();
            printClientLog("데이터를 서버로 보냄 :" + data);


            ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
            data_from_server = (String) instream.readObject();
            printClientLog("데이터를 서버로부터 받음 : " + data_from_server);


            //여기 속에다가 if문을 넣지 않고 메인에 넣으면 오류가 남...
            if(Integer.parseInt(data_from_server) > 10){
                textView4.setText("안전합니다");
                textView4.setTextColor(Color.GREEN);
            }
            else if(Integer.parseInt(data_from_server) <= 10){
                textView4.setText("위험합니다");
                textView4.setTextColor(Color.RED);
            }
            else{
                textView4.setText("거리값이 이상합니다");
                textView4.setTextColor(Color.BLUE);
            }

            //소켓 닫아주기
            sock.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    public void startServer() {
        try {
            int portNumber = 5050;
            String server_data= "2";
            ServerSocket server = new ServerSocket(portNumber); //소켓 서버 객체 만들기
            printServerLog("서버 시작함 : " + portNumber);

            while(true) {

                //클라이언트가 접속했을 때 만들어지는 소켓 객체 참조하기
                Socket sock = server.accept();
                InetAddress clientHost = sock.getLocalAddress();
                int clientPort = sock.getPort();
                printServerLog("클라이언트 연결됨 : " + clientHost + " : " + clientPort);

                ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
                Object obj = instream.readObject();
                printServerLog("데이터를 클라이언트로부터 받음 : " + obj);

                ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
                outstream.writeObject(server_data + " from Server.");

                outstream.flush();
                printServerLog("데이터를 클라이언트로 보냄 : " + server_data);

                sock.close();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void printServerLog(final String data) { // 화면 좌단에 결과 출력하는 함수
        Log.d("MainActivity", data);
        textView2 = (TextView) findViewById(R.id.textView2);
        handler.post(new Runnable() { // 서버 쪽 로그를 화면에 있는 텍스트뷰에 출력하기 위해 핸들러 사용하기
            @Override
            public void run() {
                textView2.append(data + "\n");
            }
        });
    }

    public void printClientLog(final String data) { // 화면 우단에 결과 출력하는 함수
        Log.d("MainActivity", data);
        textView3 = (TextView) findViewById(R.id.textView3);
        handler.post(new Runnable() { // 클라이언트 쪽 로그를 화면에 있는 텍스트 뷰에 출력하기 위해 핸들러 사용
            @Override
            public void run() {
                textView3.append(data + "\n");
            }
        });

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
