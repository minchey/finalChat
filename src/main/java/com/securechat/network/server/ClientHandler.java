package com.securechat.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable{
    //필드
    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final String nickname;

    //기본 생성자
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.nickname = "Guest";

        try { //예외처리
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),StandardCharsets.UTF_8) );
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //연결 종료시 소켓, 입출력 닫기
    private void closeConnection() throws IOException{
        if(in != null) in.close();
        if(out != null) out.close();
        if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                System.out.println("[" + nickname + "] : " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message){ //메시지 전송 메서드
        out.println(message);
    }

}
