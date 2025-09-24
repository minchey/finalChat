package com.securechat.network.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    //필드
    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final String nickname;

    //기본 생성자
    public ClientHandler(Socket clientSocket) throws IOException{
        this.clientSocket = clientSocket;
        this.nickname = "Guest";

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
                System.out.println("[" + nickname + "] : " + line);
                ChatServer.broadcast(line);
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] IO error for " + clientSocket + ": " + e.getMessage());
        } finally {
            try {
                closeConnection();
                ChatServer.remove(this);
                ChatServer.broadcast("[system]: " + nickname + "님이 종료하였습니다.");
            } catch (IOException e) {
                System.err.println("[ClientHandler] IO error for " + clientSocket + ": " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) { //메시지 전송 메서드
        if (out != null) out.println(message);
    }

}
