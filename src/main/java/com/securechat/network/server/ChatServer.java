package com.securechat.network.server;

import com.securechat.model.MsgFormat;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final List<MsgFormat> history = new CopyOnWriteArrayList<>(); //히스토리 저장소
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>(); //자바에서 제공하는 스레드 안전 리스트
    public static void main(String[] args){
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
                Thread t = new Thread(handler);
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

    //접속 종료시 리스트에서 제거
    public static void remove(ClientHandler handler){
        clients.remove(handler);
    }

    //히스토리 리스트에 추가
    public static void appendHistory(MsgFormat msg){
        history.add(msg);
    }

    //히스토리 리스트 조회
    public static List<MsgFormat> getAllHistory(){
        return history;
    }
}

