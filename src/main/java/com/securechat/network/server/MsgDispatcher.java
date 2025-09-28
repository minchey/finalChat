package com.securechat.network.server;

import com.securechat.model.MsgFormat;
import com.securechat.network.server.service.ChatService;
import com.securechat.network.server.service.HistoryService;

public class MsgDispatcher {
    public static void dispatch(MsgFormat msg) {
        switch (msg.getType()) {
            case CHAT:
                ChatService.handle(msg);
                break;
            case HISTORY:
                HistoryService.handle(msg);
                break;
            case KEY_EXCHANGE:
                KeyService.handle(msg);
                break;
            case SYSTEM:
                SystemService.handle(msg);
                break;
            default -> System.out.println("❗Unknown type: " + msg.getType());
        }
    }
}
