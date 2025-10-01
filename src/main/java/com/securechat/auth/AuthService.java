package com.securechat.auth;

import com.securechat.network.server.ChatServer;
import com.securechat.protocol.MsgType;
import com.securechat.protocol.Protocol;
import com.google.common.hash.HashCode;
import com.google.gson.JsonSyntaxException;
import com.securechat.model.MsgFormat;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;

public class AuthService {

    private static final Gson gson = new Gson();
    public static void handleSignup(MsgFormat msg){
        SignupPayLoad p;
        try {
            // body: {"id": "minjae", "password": "1234"}
            p = gson.fromJson(msg.getBody(),SignupPayLoad.class);
        }catch (JsonSyntaxException e){
            sendErr(msg.getSender(), "INVALID_JSON");
            return;
        }

    }

    // ---------- helpers ----------

    //성공 메서드
    private static void sendOk(String receiver, String body) {
        MsgFormat ok = new MsgFormat(
                MsgType.AUTH_OK,
                "server",
                receiver,
                body,
                nowTs()
        );
        ChatServer.sendTo(receiver, gson.toJson(ok));
    }

    //에러 메서드
    private static void sendErr(String receiver, String reason) {
        MsgFormat err = new MsgFormat(
                MsgType.AUTH_ERR,
                "server",
                receiver,
                reason,
                nowTs()
        );
        ChatServer.sendTo(receiver, gson.toJson(err));
    }

    //시간 메서드
    private static String nowTs() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(Protocol.TIMESTAMP_PATTERN));
    }

}


