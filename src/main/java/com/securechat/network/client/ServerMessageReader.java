package com.securechat.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerMessageReader implements Runnable { //서버에서 수신한 메시지 띄우는 클래스
    private Socket socket;
    private BufferedReader in;

    //생성자
    public ServerMessageReader(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public void run() { //수신 스레드

        try {
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("서버와의 연결이 끊어졌습니다.");
            }
        }
    }
}

