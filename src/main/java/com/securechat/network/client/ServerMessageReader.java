package com.securechat.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.securechat.model.MsgFormat;
import com.securechat.protocol.MsgType;
import com.google.gson.Gson;

/**
 * 서버에서 오는 라인을 계속 읽어서
 * - AUTH_OK(SIGNUP_OK/LOGIN_OK) 시 ChatClient 콜백 호출
 * - AUTH_ERR 시 실패 콜백
 * - 인증 전에는 CHAT/HISTORY 등의 일반 메시지를 무시
 */
public class ServerMessageReader implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    private final Gson gson = new Gson();
    private final ChatClient client;         // ✅ 콜백 대상

    // ✅ ChatClient를 같이 받도록 변경
    public ServerMessageReader(Socket socket, ChatClient client) throws IOException {
        this.socket = socket;
        this.client = client;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                MsgFormat msg = gson.fromJson(line, MsgFormat.class);
                if (msg == null || msg.getType() == null) {
                    System.err.println("[Reader] invalid JSON: " + line);
                    continue;
                }

                switch (msg.getType()) {
                    case AUTH_OK -> {
                        // body: "SIGNUP_OK" 또는 "LOGIN_OK"
                        String body = msg.getBody();
                        if ("LOGIN_OK".equals(body)) {
                            System.out.println("✅ 로그인 성공!");
                            client.onAuthOkLogin();          // 🔑 인증 완료(채팅 모드 진입 준비)
                        } else if ("SIGNUP_OK".equals(body)) {
                            System.out.println("✅ 회원가입 완료! 메뉴로 돌아갑니다.");
                            client.onSignupOk();             // 🔁 메뉴 복귀(인증은 false 유지)
                        } else {
                            System.out.println("[AUTH_OK] " + body);
                        }
                    }
                    case AUTH_ERR -> {
                        System.out.println("❌ 인증 실패: " + msg.getBody());
                        client.onAuthErr(msg.getBody());     // 🔒 인증 실패(메뉴 대기)
                    }
                    case SYSTEM -> {
                        System.out.println("📢 시스템: " + msg.getBody());
                    }
                    default -> {
                        // 인증 전에는 일반 메시지 무시 (로그만)
                        if (!client.isAuthenticated()) {
                            if (msg.getType() != MsgType.AUTH_OK && msg.getType() != MsgType.AUTH_ERR) {
                                // 디버깅용 로그만 남기고 스킵
                                // System.out.println("[IGNORED pre-auth] " + msg.getType() + ": " + msg.getBody());
                            }
                            break;
                        }
                        // 인증 후에만 화면에 출력
                        if (msg.getType() == MsgType.CHAT) {
                            System.out.println("💬 " + msg.getSender() + ": " + msg.getBody());
                        } else {
                            System.out.println("[" + msg.getType() + "] " + msg.getBody());
                        }
                    }
                }
                // 원본 라인(debug)이 필요하면 아래 주석 해제
                // System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("[Reader] IO error: " + e.getMessage());
        } finally {
            try { in.close(); } catch (Exception ignore) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignore) {}
            System.out.println("서버와의 연결이 종료되었습니다.");
        }
    }
}
