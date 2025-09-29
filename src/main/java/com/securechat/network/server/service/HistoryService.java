package com.securechat.network.server.service;

import com.securechat.model.MsgFormat;
import com.securechat.network.server.ChatServer;
import com.google.gson.Gson;
import java.util.List;

public class HistoryService {
    private static Gson gson = new Gson();
    public static void handle(MsgFormat msg){
        List<MsgFormat> history = ChatServer.getAllHistory();
        for(MsgFormat m : history){
            String json = gson.toJson(m);
            ChatServer.sendTo(msg.getSender(),json);
        }
    }
}
