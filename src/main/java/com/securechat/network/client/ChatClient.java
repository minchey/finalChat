package com.securechat.network.client;

import com.google.gson.Gson;
import com.securechat.model.MsgFormat;
import com.securechat.protocol.MsgType;
import com.securechat.protocol.Protocol;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.net.Socket;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;

public class ChatClient {
    private volatile boolean authenticated = false;

    // 메뉴 루프에서 reader 생성 시 this 전달
    private void startReader(Socket socket) throws IOException {
        Thread t = new Thread(new ServerMessageReader(socket, this));
        t.setDaemon(true);
        t.start();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    // ✅ 회원가입 성공 → 메뉴로 복귀(인증 false 유지)
    public void onSignupOk() {
        this.authenticated = false;
        // 여기서 아무 것도 안 하고 메뉴 루프가 계속 돌도록 설계
    }

    // ✅ 로그인 성공 → 인증 true로 전환하고 게이트/플래그 해제
    public void onAuthOkLogin() {
        this.authenticated = true;
        // 히스토리 요청 등 후속 처리하고 채팅 모드 진입
    }

    // ❌ 인증 실패 → 인증 false 유지, 메뉴로 복귀
    public void onAuthErr(String reason) {
        this.authenticated = false;
        // 실패 안내 출력은 reader에서 이미 했으므로 여기선 상태만 유지
    }

    public static void main(String[] args) {
        Gson gson = new Gson();           //gson 객체 생성
        Scanner sc = new Scanner(System.in);
        String msg;
        String nicknameForAuth = "";
        String userId = null;
        ChatClient client = new ChatClient();
        int signupOrLogin = 0;
        try {
            //서버에 연결
            Socket socket = new Socket("localhost", 9999);
            PrintWriter out = new PrintWriter( //UTF-8 인코딩
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            while (true) {
                // (메뉴 표시/선택)
                if (signupOrLogin == 0) {
                    System.out.println("1.회원가입  2.로그인");
                    String line = sc.nextLine().trim();

                    int choice;
                    try {
                        choice = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        choice = 0; // 숫자 아니면 다시 메뉴
                    }

                    switch (choice) {
                        case 1 -> signupOrLogin = 1;
                        case 2 -> signupOrLogin = 2;
                        default -> signupOrLogin = 0; // 잘못 입력 시 메뉴 유지
                    }
                    continue;
                }

                //회원가입 분기
                if (signupOrLogin == 1) {
                    System.out.println("아이디를 입력하세요 : ");
                    String id = sc.nextLine().trim();
                    System.out.println("비밀번호를 입력하세요 : ");
                    String pw = sc.nextLine();
                    System.out.println("닉네임을 입력하세요 : ");
                    nicknameForAuth = sc.nextLine().trim();
                    String body = gson.toJson(Map.of("id", id, "password", pw, "nickname", nicknameForAuth)); //id + pw 해시저장 + 닉네임
                    MsgFormat signUp = new MsgFormat(
                            MsgType.SIGNUP,
                            id,
                            "server",
                            body,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN)));
                    out.println(gson.toJson(signUp));
                    userId = id;
                    signupOrLogin = 0;  //메뉴로 복귀
                    continue;
                }

                //로그인 분기
                if (signupOrLogin == 2) {
                    System.out.println("로그인 - 아이디를 입력하세요: ");
                    String id = sc.nextLine().trim();
                    System.out.println("로그인 - 비밀번호를 입력하세요");
                    String pw = sc.nextLine();

                    String body = gson.toJson(java.util.Map.of(
                            "id", id,
                            "password", pw
                    ));

                    MsgFormat login = new MsgFormat(
                            MsgType.LOGIN,
                            id,
                            "server",
                            body,
                            java.time.LocalDateTime.now().format(
                                    java.time.format.DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN)
                            )
                    );
                    out.println(gson.toJson(login));
                    userId = id;
                    break;
                }
            }
            System.out.println("서버에 연결됨: " + socket + " as " + (userId != null ? userId : ""));
            //스레드 시작
            Thread t = new Thread(new ServerMessageReader(socket, client));
            t.start();

            /*로그인 확인 대기*/
            System.out.println("로그인 확인 중. . . (최대 5초)");
            long deadline = System.currentTimeMillis() + 5000;  //5초 뒤
            while (!client.isAuthenticated() && System.currentTimeMillis() < deadline) { //인증 될 동안 반복
                try { Thread.sleep(100); } catch (InterruptedException ignore) {}
            }
            if (!client.isAuthenticated()) { // 인증 되지 않았을 경우
                System.out.println("❌ 로그인 실패 또는 타임아웃. 프로그램을 종료합니다.");
                try { socket.close(); } catch (Exception ignore) {}
                return;
            }

            while (true) {
                msg = sc.nextLine(); //메시지 한줄 입력
                if (msg.isBlank() || msg.isEmpty()) continue;
                //timestamp
                String ts = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));

                // DTO 만들기 (type이 String이라면 name() 사용)
                MsgFormat message = new MsgFormat(MsgType.CHAT, userId, "all", msg, ts);

                // JSON 직렬화 및 전송
                String json = gson.toJson(message);
                out.println(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
