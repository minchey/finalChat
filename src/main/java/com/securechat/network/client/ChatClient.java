package com.securechat.network.client;

import java.net.Socket;
import java.io.IOException;

public class ChatClient {
    public static void main(String[] args) {
        try{
            //서버에 연결
            Socket socket = new Socket("localhost",9999);
            System.out.println("서버에 연결됨: " + socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
