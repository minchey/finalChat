package com.securechat.network.server;

import com.google.gson.Gson;
import com.securechat.model.MsgFormat;
import com.securechat.protocol.MsgType;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    //필드
    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final String nickname;
    private final Gson gson;

    //기본 생성자
    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.nickname = "Guest";
        this.gson = new Gson();
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

    }

    //연결 종료시 소켓, 입출력 닫기
    private void closeConnection() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                try {
                    MsgFormat msg = gson.fromJson(line, MsgFormat.class);   //이름정 + 검증
                    if (msg == null || msg.getType() == null) {
                        // 형식 불량 방어: 무시하거나 경고 로그만 남김
                        System.err.println("[ClientHandler] invalid JSON from " + nickname + ": " + line);
                        continue;

                    }
                    System.out.println("[" + nickname + "][" + msg.getType() + "] " + line);
                    ChatServer.broadcast(line);
                } catch (com.google.gson.JsonSyntaxException je) {
                    System.err.println("[ClientHandler] JSON parse error from " + nickname + ": " + je.getMessage());
                    continue; // 해당 라인만 스킵하고 루프 유지
                }

            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] IO error for " + clientSocket + ": " + e.getMessage());
        } finally {
            try {
                closeConnection();
                ChatServer.remove(this);
                MsgFormat sys = new MsgFormat(
                        MsgType.SYSTEM,               // type
                        "system",               // sender
                        "all",                  // receiver
                        nickname + "님이 종료하였습니다.", // body
                        java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern(com.securechat.protocol.Protocol.TIMESTAMP_PATTERN)
                        )
                );
                ChatServer.broadcast(gson.toJson(sys));
            } catch (IOException e) {
                System.err.println("[ClientHandler] IO error for " + clientSocket + ": " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) { //메시지 전송 메서드
        if (out != null) out.println(message);
    }

}
