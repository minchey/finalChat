package com.securechat.network.server.service;

import com.securechat.model.MsgFormat;
import com.securechat.network.server.ChatServer;
import com.securechat.protocol.MsgType;
import com.google.gson.Gson;

public class ChatService {
    public static void handle(MsgFormat msg){
        Gson gson = new Gson();
        try {
            if (msg.getType() == MsgType.CHAT && msg.getBody() != null) {

                // 1) 히스토리 저장
                ChatServer.appendHistory(msg);

                // 2) 라우팅
                String json = gson.toJson(msg);
                if ("all".equalsIgnoreCase(msg.getReceiver())) {
                    ChatServer.broadcast(json);                   // 전체 방송
                } else {
                    ChatServer.sendTo(msg.getReceiver(), json);   // 귓속말
                    ChatServer.sendTo(msg.getSender(), json);     // 발신자 에코(자기 화면에도 표시)
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
