package com.securechat.network.server;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>(); //자바에서 제공하는 스레드 안전 리스트
    public static void main(String[] args) {
        try {
            //포트 9999에서 서버열기
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("서버 시작됨 (포트: 9999)");

            while (true) {
                //클라이언트 접속 대기
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler); //리스트에 추가

                System.out.println("클라이언트 연결됨: " + clientSocket);
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //전체 클라이언트에게 메시지 전송
    public static void broadcast(String message){
        for(ClientHandler c : clients){
            c.sendMessage(message);
        }
    }
}

