package com.company.Server;

import java.net.Socket;

public class AvailablePorts {

    public static boolean availablePort(String host, int port)
    {
        boolean result = false;

        try {
            (new Socket(host, port)).close();

            result = true;
        }
        catch(Exception e) {

        }
        return result;
    }

    public static void main(String[] args){

        boolean portCheck = availablePort("127.0.0.1",5050);
        if(portCheck){
            System.out.println("port available");
        }
        else
        {
            System.out.println("port unavailable");
        }
    }
}
