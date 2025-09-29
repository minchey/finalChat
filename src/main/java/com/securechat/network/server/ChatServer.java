package com.securechat.network.server;

import com.securechat.model.MsgFormat;
import com.securechat.protocol.MsgType;
import com.securechat.network.client.ChatClient;
import com.securechat.protocol.Protocol;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ChatServer {
    private static final List<MsgFormat> history = new CopyOnWriteArrayList<>(); //히스토리 저장소
    private static Map<String,ClientHandler> clients = new ConcurrentHashMap<>(); //자바에서 제공하는 스레드 안전 리스트
    public static void main(String[] args){
        try {
            //포트 9999에서 서버열기
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("서버 시작됨 (포트: 9999)");

            while (true) {
                //클라이언트 접속 대기
                Socket clientSocket = serverSocket.accept();
                String ts = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));

                // 📥 접속 직후 첫 줄에서 닉네임 받기
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                String nickname = in.readLine();  // 클라이언트가 먼저 닉네임 전송

                ClientHandler handler = new ClientHandler(clientSocket,nickname);
                clients.put(nickname,handler); //리스트에 추가

                System.out.println("클라이언트 연결됨: " + clientSocket);
                Thread t = new Thread(handler);
                t.start();
                MsgFormat auto = new MsgFormat(MsgType.HISTORY, nickname, "server", "auto", ts);

                MsgDispatcher.dispatch(auto);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //전체 클라이언트에게 메시지 전송
    public static void broadcast(String message){
        for(ClientHandler c : clients.values()){
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

    //닉넹임 받고 메시지 반환
    public static void sendTo(String nickname, String message){
        ClientHandler target = clients.get(nickname);
        if(target != null) target.sendMessage(message);
        else System.err.println("클라이언트 없음: " + nickname);
    }
}

