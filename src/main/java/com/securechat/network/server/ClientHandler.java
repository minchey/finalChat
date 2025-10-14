package com.securechat.network.server;

import com.google.gson.Gson;
import com.securechat.model.MsgFormat;
import com.securechat.protocol.MsgType;
import com.securechat.protocol.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 각 클라이언트 소켓 1개당 1개가 생성되어 수신 루프를 돌며
 * - JSON 파싱 (MsgFormat)
 * - 인증 전/후 허용 타입 분기
 * - 타입별 디스패치(MsgDispatcher) 호출
 * - 종료 시 정리/방송
 *
 * 세션 상태(userId/nickname/authenticated)는 여기에서 관리하며,
 * 인증 성공 시 AuthService가 setUserId / bindNickname / setAuthenticated(true)를 호출함.
 */
public class ClientHandler implements Runnable {

    /* ------------------------------
     *  네트워크/IO 필드
     * ------------------------------ */
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson = new Gson();

    /* ------------------------------
     *  세션 상태 (인증/사용자 식별)
     * ------------------------------ */
    /** 로그인에 성공한 사용자 ID (id 기반 라우팅에 사용) */
    private volatile String userId = null;

    /** 화면에 표기할 닉네임(옵션) */
    private volatile String nickname = null;

    /** 인증 여부 플래그: 로그인 성공 시에만 true */
    private volatile boolean authenticated = false;

    /* ------------------------------
     *  생성자
     * ------------------------------ */
    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

        // 디버깅을 위해 스레드 이름 지정(선택)
        try {
            String who = clientSocket.getRemoteSocketAddress() != null
                    ? clientSocket.getRemoteSocketAddress().toString()
                    : "unknown";
            Thread.currentThread().setName("ClientHandler-" + who);
        } catch (Exception ignore) {}
    }

    /* ------------------------------
     *  런 루프
     * ------------------------------ */
    @Override
    public void run() {
        try {
            while (true) {
                String line = in.readLine();   // 클라이언트 한 줄 수신
                if (line == null) break;       // 소켓 종료

                try {
                    MsgFormat msg = gson.fromJson(line, MsgFormat.class);

                    // 형태/타입 검증
                    if (msg == null || msg.getType() == null) {
                        System.err.println("[ClientHandler] Invalid JSON: " + line);
                        continue;
                    }

                    // 로깅용 표기명 (닉네임 없으면 id, 둘 다 없으면 remote로)
                    String label = (nickname != null) ? nickname
                            : (userId != null ? userId : "remote");

                    System.out.println("[" + label + "][" + msg.getType() + "] " + line);

                    // ✅ 인증 전엔 SIGNUP/LOGIN만 허용 (CHAT/HISTORY 등 차단)
                    if (!isAuthenticated()
                            && msg.getType() != MsgType.SIGNUP
                            && msg.getType() != MsgType.LOGIN) {
                        sendAuthErr(msg.getSender(), "NOT_AUTHENTICATED");
                        continue; // 이 메시지는 폐기
                    }

                    // 타입별 실제 처리
                    MsgDispatcher.dispatch(msg, this);

                } catch (com.google.gson.JsonSyntaxException je) {
                    System.err.println("[ClientHandler] JSON parse error: " + je.getMessage());
                    // 해당 라인만 스킵하고 계속
                }
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] IO error for " + clientSocket + ": " + e.getMessage());
        } finally {
            // 소켓/리소스 정리 + 서버 알림
            try {
                closeConnection();

                // ⚠️ 현재 프로젝트가 nickname 키로 관리 중이면 기존 remove 유지.
                // ➜ 권장: ChatServer.unbind(userId) 로 전환해 id 기반 세션 관리 통일.
                try {
                    if (userId != null) {
                        // ChatServer.unbind(userId); // 권장(서버에 구현 필요)
                    } else if (nickname != null) {
                        ChatServer.remove(nickname); // 기존 코드 호환
                    }
                } catch (Throwable t) {
                    // 위 두 API 중 하나만 존재할 수 있으므로 안전하게 무시
                }

                // 종료 브로드캐스트 (닉네임/ID 표기)
                String who = (nickname != null) ? nickname
                        : (userId != null ? userId : "알 수 없음");
                MsgFormat sys = new MsgFormat(
                        MsgType.SYSTEM,
                        "system",
                        "all",
                        who + "님이 종료하였습니다.",
                        nowTs()
                );
                ChatServer.broadcast(gson.toJson(sys));

            } catch (IOException e) {
                System.err.println("[ClientHandler] close error for " + clientSocket + ": " + e.getMessage());
            }
        }
    }

    /* ------------------------------
     *  퍼블릭 유틸 (AuthService/Server가 사용)
     * ------------------------------ */
    /** 인증 상태 조회 */
    public boolean isAuthenticated() { return authenticated; }

    /** 인증 상태 설정 (로그인 성공 시에만 true) */
    public void setAuthenticated(boolean v) { this.authenticated = v; }

    /** 세션 userId 세팅 (로그인 성공 시) */
    public void setUserId(String userId) { this.userId = userId; }

    /** 세션 userId 조회 */
    public String getUserId() { return userId; }

    /** 닉네임 바인딩(표시용) */
    public void bindNickname(String nickname) { this.nickname = nickname; }

    /** 서버 → 클라이언트 단순 라인 전송 */
    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    /* ------------------------------
     *  내부 헬퍼
     * ------------------------------ */
    /** 연결 종료: 스트림/소켓 닫기 */
    private void closeConnection() throws IOException {
        try { if (in != null) in.close(); } catch (Exception ignore) {}
        try { if (out != null) out.close(); } catch (Exception ignore) {}
        if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
    }

    /** 인증 오류 응답 (AUTH_ERR) */
    private void sendAuthErr(String receiver, String reason) {
        MsgFormat err = new MsgFormat(
                MsgType.AUTH_ERR, "server", receiver, reason, nowTs()
        );
        sendMessage(gson.toJson(err));
    }

    /** 서버 공용 타임스탬프 포맷 */
    private static String nowTs() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));
    }
}
