package com.securechat.auth;

import com.securechat.model.MsgFormat;
import com.google.common.hash.Hashing;
public class AuthService {

    public static void handleSignup(MsgFormat msg){
        String id = msg.getSender();
        String pw = msg.getBody();
    }
}


