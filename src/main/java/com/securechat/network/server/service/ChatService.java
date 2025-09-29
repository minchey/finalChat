package com.securechat.network.server.service;

import com.securechat.model.MsgFormat;
import com.securechat.protocol.MsgType;
import com.google.gson.Gson;

public class ChatService {
    public static void handle(MsgFormat msg){
        try {
            if (msg.getType() == MsgType.CHAT && msg.getBody() != null) {

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
