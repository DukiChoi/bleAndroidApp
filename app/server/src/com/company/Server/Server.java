package com.company.Server;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        try {


            int portnumber = 5050;
            String data = "20";
            //서버 소켓을 생성
            ServerSocket ssk = new ServerSocket(portnumber); //서버 자신의 포트를 설정해준다.

            System.out.println("서버 시작함: " + portnumber);

            while (true) {
                Socket sock = ssk.accept(); // 새로운 소켓을 생성 클라이언트가 들어왔을때 , 접속했을때  실행되는 구문
                System.out.println("클라이언트 연결됨: " + sock.getInetAddress() + " : " + portnumber);
                System.out.println("Client ip :" + sock.getInetAddress());
                /*
                //이건 주고받기
                ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
                Object obj = instream.readObject();
                System.out.println("데이터를 클라이언트로부터 받음 : " + obj);

                ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
                outstream.writeObject(obj + " from Server.");
                outstream.flush();
                System.out.println("데이터를 클라이언트로 보냄 : " + obj);
                */
                ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
                Scanner sc = new Scanner(System.in);
                System.out.println("보낼 값을 입력하세요");
                data = sc.next();
                outstream.writeObject(data);
                outstream.flush();
                System.out.println("데이터를 클라이언트로 보냄 : " + data);

                sock.close();
            } //while
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
