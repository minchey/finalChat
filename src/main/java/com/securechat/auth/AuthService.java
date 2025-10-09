package com.securechat.auth;

import com.securechat.network.server.ChatServer;
import com.securechat.network.server.ClientHandler;
import com.securechat.protocol.MsgType;
import com.securechat.protocol.Protocol;
import com.google.gson.JsonSyntaxException;
import com.securechat.model.MsgFormat;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;

import com.securechat.auth.store.UserStore;  //유저 저장소


public class AuthService {

    private static final Gson gson = new Gson();

    public static void handleSignup(MsgFormat msg) {
        SignupPayload p;
        try {
            // body: {"id": "minchey", "password": "1234"}
            p = gson.fromJson(msg.getBody(), SignupPayload.class);
        } catch (JsonSyntaxException e) {
            sendErr(msg.getSender(), "INVALID_JSON");
            return;
        }
        if (p == null || isBlank(p.id) || isBlank(p.password) || isBlank(p.nickname)) {
            sendErr(msg.getSender(), "MISSING_FIELDS");
            return;
        }

        //비밀번호 해시
        String hashPw = Hashing.sha256().hashString(p.password, StandardCharsets.UTF_8).toString();

        boolean created = UserStore.putIfAbsent(p.id, hashPw);
        if (!created) {
            // 이미 존재하는 아이디
            sendErr(p.id, "DUPLICATE_ID");
            return;
        }

        // 가입 성공 응답
        sendOk(p.id, "SIGNUP_OK");

    }

    // ---------- helpers ----------

    //성공 메서드
    private static void sendOk(String receiver, String body) {
        MsgFormat ok = new MsgFormat(
                MsgType.AUTH_OK,
                "server",
                receiver,
                body,
                nowTs()
        );
        ChatServer.sendTo(receiver, gson.toJson(ok));
    }

    //에러 메서드
    private static void sendErr(String receiver, String reason) {
        MsgFormat err = new MsgFormat(
                MsgType.AUTH_ERR,
                "server",
                receiver,
                reason,
                nowTs()
        );
        ChatServer.sendTo(receiver, gson.toJson(err));
    }

    //빈 값 확인 메서드
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    //시간 메서드
    private static String nowTs() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));
    }

    //DTO
    public static class SignupPayload {
        public String id;
        public String password;
        public String nickname;
    }

    //로그인 DTO
    public static class LoginPayload {
        public String id;
        public String password;
        public String nickname;
    }

    public static void handleLogin(MsgFormat msg, ClientHandler handler) {
        LoginPayload p;
        try {
            p = gson.fromJson(msg.getBody(), LoginPayload.class);
        } catch (JsonSyntaxException e) {
            System.err.println("[LOGIN] INVALID_JSON from " + msg.getSender());
            sendErr(msg.getSender(), "INVALID_JSON");
            return;
        }
        if (p == null || isBlank(p.id) || isBlank(p.password)) {
            System.err.println("[LOGIN] MISSING_FIELDS from " + msg.getSender());
            sendErr(msg.getSender(), "MISSING_FIELDS");
            return;
        }
        System.out.println("[LOGIN] request id=" + p.id);

        // 아이디가 존재하는지 확인
        if (!UserStore.exists(p.id)) {
            sendErr(p.id, "USER_NOT_FOUND");
            return;
        }

        // 저장된 해시값 꺼내기
        String storedHash = UserStore.getHashedPassword(p.id);

        // 입력받은 비밀번호 해시화
        String inputHash = Hashing.sha256()
                .hashString(p.password, StandardCharsets.UTF_8)
                .toString();

        // 비교해서 다르면 실패
        if (!storedHash.equals(inputHash)) {
            sendErr(p.id, "WRONG_PASSWORD");
            return;
        }

        // ✅ 여기까지 왔다면 로그인 성공!
        sendOk(p.id, "LOGIN_OK");

    }
}


