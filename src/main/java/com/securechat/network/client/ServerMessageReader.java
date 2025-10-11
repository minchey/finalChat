package com.securechat.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.securechat.model.MsgFormat;
import com.google.gson.Gson;

public class ServerMessageReader implements Runnable { //서버에서 수신한 메시지 띄우는 클래스
    private Socket socket;
    private BufferedReader in;
    private Gson gson = new Gson();

    //생성자
    public ServerMessageReader(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); //UTF-8로 인코딩
    }

    @Override
    public void run() { //수신 스레드

        try {
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                // ✅ JSON → 객체 변환
                MsgFormat msg = gson.fromJson(line, MsgFormat.class);

                // ✅ 타입별 처리
                switch (msg.getType()) {
                    case AUTH_OK:
                        System.out.println("✅ 로그인 성공!");
                        break;
                    case AUTH_ERR:
                        System.out.println("❌ 로그인 실패: " + msg.getBody());
                        break;
                    case CHAT:
                        System.out.println("💬 " + msg.getSender() + ": " + msg.getBody());
                        break;
                    case SYSTEM:
                        System.out.println("📢 시스템 메시지: " + msg.getBody());
                        break;
                    default:
                        System.out.println("[서버 응답] " + msg.getType() + ": " + msg.getBody());
                }
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { //종료
            try {
                if (in != null) in.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("서버와의 연결이 끊어졌습니다.");
            }
        }
    }
}

