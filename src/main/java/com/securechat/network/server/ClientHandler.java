package com.securechat.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler {
    //필드
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

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
    private void closeConnection() throws IOException{
        if(in != null) in.close();
        if(out != null) out.close();
        if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
    }
}
