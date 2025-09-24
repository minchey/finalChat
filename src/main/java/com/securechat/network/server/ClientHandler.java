package com.securechat.network.server;

import org.springframework.security.core.parameters.P;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler {
    //필드
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    //기본 생성자
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
