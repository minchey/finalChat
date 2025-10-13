package com.securechat.network.server.service;

import com.securechat.model.MsgFormat;
import com.securechat.network.server.ChatServer;
import com.google.gson.Gson;
import java.util.List;

public class HistoryService {
    private static final Gson gson = new Gson();
    public static void handle(MsgFormat msg){
        List<MsgFormat> history = ChatServer.getAllHistory();
        String requesterId = msg.getSender(); // 로그인 시 id 바인딩 가정
        for (MsgFormat m : history) {
            ChatServer.sendTo(requesterId, gson.toJson(m));
        }
    }
}
