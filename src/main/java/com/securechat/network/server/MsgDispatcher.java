package com.securechat.network.server;

import com.securechat.auth.AuthService;
import com.securechat.model.MsgFormat;
import com.securechat.network.server.service.ChatService;
import com.securechat.network.server.service.HistoryService;
import com.securechat.auth.AuthService;
public class MsgDispatcher {
    public static void dispatch(MsgFormat msg, ClientHandler clientHandler) {

        // ✅ 인증 전 차단: CHAT/HISTORY
        switch (msg.getType()) {
            case CHAT, HISTORY -> {
                if (!clientHandler.isAuthenticated()) {
                    clientHandler.sendMessage("{\"type\":\"SYSTEM\",\"body\":\"AUTH_REQUIRED\"}");
                    return;
                }
            }
            default -> { /* pass */ }
        }

        switch (msg.getType()) {
            case CHAT:                              //채팅 핸들러
                ChatService.handle(msg);
                break;
            case SIGNUP:                            //회원가입 핸들러
                AuthService.handleSignup(msg);
                break;
            case HISTORY:                           //히스토리 핸들러
                HistoryService.handle(msg);
                break;
            case KEY_EXCHANGE:                      //공개키 교환 핸들러
                //KeyService.handle(msg);
                break;
            case SYSTEM:
               // SystemService.handle(msg);
                break;

            case LOGIN:
                // com.securechat.network.server.MsgDispatcher
                AuthService.handleLogin(msg, clientHandler);
                break;
                // default -> System.out.println("❗Unknown type: " + msg.getType());
        }
    }
}
