package com.securechat.model;

public class MsgFormat {
    // 필드 선언
    private String type;
    private String sender;
    private String receiver;
    private String body;
    private String timestamp;

    //기본 생성자
    public MsgFormat(String type, String sender, String receiver, String body, String timestamp){
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.timestamp = timestamp;
    }
}
