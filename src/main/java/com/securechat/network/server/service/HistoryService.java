package com.securechat.network.server.service;

import com.securechat.model.MsgFormat;
import com.securechat.network.server.ChatServer;

import java.util.List;

public class HistoryService {
    public static void handle(MsgFormat msg){
        List<MsgFormat> history = ChatServer.getAllHistory();
    }
}
