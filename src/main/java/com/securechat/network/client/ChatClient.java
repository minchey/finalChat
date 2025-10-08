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
    public static void main(String[] args) {
        Gson gson = new Gson();           //gson 객체 생성
        Scanner sc = new Scanner(System.in);
        String msg;
        String nicknameForAuth ="";
        int signupOrLogin;
        try{
            //서버에 연결
            Socket socket = new Socket("localhost",9999);
            PrintWriter out = new PrintWriter( //UTF-8 인코딩
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("1.회원가입 2.로그인");
            signupOrLogin = sc.nextInt();
            sc.nextLine(); // 엔터 제거
            if(signupOrLogin == 1){
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
            }
            //System.out.println("닉네임: ");
            String nickname = nicknameForAuth;
            //닉네임 전송
            out.println();
            System.out.println("서버에 연결됨: " + socket + nicknameForAuth);

            //스레드 시작
            Thread t = new Thread(new ServerMessageReader(socket));
            t.start();

            while (true){
                msg = sc.nextLine(); //메시지 한줄 입력
                if(msg.isBlank() || msg.isEmpty()) continue;
                //timestamp
                String ts = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));

                // DTO 만들기 (type이 String이라면 name() 사용)
                MsgFormat message = new MsgFormat(MsgType.CHAT, nickname, "all", msg,ts);

                // JSON 직렬화 및 전송
                String json = gson.toJson(message);
                out.println(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
