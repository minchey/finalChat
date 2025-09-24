package com.securechat.network.client;

import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String msg;
        try{
            //서버에 연결
            Socket socket = new Socket("localhost",9999);
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            System.out.println("서버에 연결됨: " + socket);
            while (true){
                msg = sc.nextLine();
                out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
