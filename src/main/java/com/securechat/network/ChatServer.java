package com.securechat.network;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.lang.Thread;

public class ChatServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("서버 시작됨 (포트: 9999)");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

