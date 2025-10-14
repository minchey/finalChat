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

import com.securechat.auth.store.UserStore;  // 유저 저장소

/**
 * 인증/회원가입 담당 서비스.
 * - 실패하는 모든 분기에서는 인증 플래그를 false로 강제(setAuthenticated(false))하고 즉시 return.
 * - 로그인 성공한 경우에만 ChatServer.bind(...) + setAuthenticated(true) 수행.
 */
public class AuthService {

    private static final Gson gson = new Gson();

    /* ===========================
     * =        SIGNUP           =
     * =========================== */

    // 회원가입 로직
    public static void handleSignup(MsgFormat msg) {
        SignupPayload p;
        try {
            // body 예: {"id": "minchey", "password": "1234", "nickname":"민제"}
            p = gson.fromJson(msg.getBody(), SignupPayload.class);
        } catch (JsonSyntaxException e) {
            sendErr(msg.getSender(), "INVALID_JSON");
            return;
        }

        // 필수 필드 검증
        if (p == null || isBlank(p.id) || isBlank(p.password) || isBlank(p.nickname)) {
            sendErr(msg.getSender(), "MISSING_FIELDS");
            return;
        }

        // 비밀번호 해시(SHA-256) 계산
        String hashPw = Hashing.sha256().hashString(p.password, StandardCharsets.UTF_8).toString();

        // 중복 아이디 체크 후 저장
        boolean created = UserStore.putIfAbsent(p.id, hashPw, p.nickname);
        if (!created) { // 이미 존재
            sendErr(p.id, "DUPLICATE_ID");
            return;
        }

        // 가입 성공 응답
        sendOk(p.id, "SIGNUP_OK");
    }

    /* ===========================
     * =         LOGIN           =
     * =========================== */

    public static void handleLogin(MsgFormat msg, ClientHandler handler) {
        LoginPayload p;

        // 1) JSON 파싱 (문자열 이중 래핑도 방어)
        try {
            p = gson.fromJson(msg.getBody(), LoginPayload.class);
            if (p == null || isBlank(p.id) || isBlank(p.password)) throw new RuntimeException("missing fields");
        } catch (Exception primary) {
            try {
                // body가 "\"{...}\"" 같은 형태일 때 재파싱
                String inner = gson.fromJson(msg.getBody(), String.class);
                p = gson.fromJson(inner, LoginPayload.class);
            } catch (Exception e) {
                // ❌ 파싱 실패: 인증 금지 + 에러 응답
                handler.setAuthenticated(false);
                sendErr(msg.getSender(), "INVALID_JSON");
                return;
            }
            if (p == null || isBlank(p.id) || isBlank(p.password)) {
                handler.setAuthenticated(false);
                sendErr(msg.getSender(), "MISSING_FIELDS");
                return;
            }
        }

        System.out.println("[LOGIN] request id=" + p.id);

        // 2) 계정 존재 여부
        if (!UserStore.exists(p.id)) {
            handler.setAuthenticated(false);          // ❌ 반드시 false 강제
            sendErr(p.id, "USER_NOT_FOUND");
            return;
        }

        // 3) 비밀번호 검증 (상수시간 비교)
        String storedHash = UserStore.getHashedPassword(p.id);
        String inputHash = Hashing.sha256().hashString(p.password, StandardCharsets.UTF_8).toString();

        // String.equals 대신 바이트 상수시간 비교
        byte[] a = storedHash.getBytes(StandardCharsets.UTF_8);
        byte[] b = inputHash.getBytes(StandardCharsets.UTF_8);
        if (!java.security.MessageDigest.isEqual(a, b)) {
            handler.setAuthenticated(false);          // ❌ 반드시 false 강제
            sendErr(p.id, "WRONG_PASSWORD");
            return;
        }

        // 4) ✅ 여기서부터 "성공" 처리만 수행
        //    - 바인딩/인증은 이 시점에만!
        handler.bindNickname(p.nickname);             // 표시용 닉네임(옵션 필드)
        handler.setUserId(p.id);                      // 세션에 사용자 id 기록
        ChatServer.bind(p.id, handler);               // (id → handler) 매핑
        handler.setAuthenticated(true);               // ✅ 인증 완료

        sendOk(p.id, "LOGIN_OK");

        // (선택) 로그인 직후 히스토리 자동 전송을 서버에서 트리거하려면:
        // HistoryService.handle(new MsgFormat(MsgType.HISTORY, p.id, p.id, "", nowTs()));
    }

    /* ===========================
     * =        Helpers          =
     * =========================== */

    // 성공 응답: AUTH_OK
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

    // 에러 응답: AUTH_ERR
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

    // 공백 체크
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // 타임스탬프
    private static String nowTs() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));
    }

    /* ===========================
     * =          DTOs           =
     * =========================== */

    // 회원가입 DTO
    public static class SignupPayload {
        public String id;
        public String password;
        public String nickname;
    }

    // 로그인 DTO (nickname은 표시용, 서버 저장소에 닉네임이 따로 있다면 거기 것을 우선 사용 권장)
    public static class LoginPayload {
        public String id;
        public String password;
        public String nickname; // optional
    }
}
