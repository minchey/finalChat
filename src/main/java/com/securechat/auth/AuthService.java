package com.securechat.auth;

import com.google.common.hash.HashCode;
import com.securechat.model.MsgFormat;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class AuthService {

    public static void handleSignup(MsgFormat msg){
        String id = msg.getBody();
        String pw = msg.getBody();
        HashCode hashPw = Hashing.sha256().hashString(pw, StandardCharsets.UTF_8);
    }
}


