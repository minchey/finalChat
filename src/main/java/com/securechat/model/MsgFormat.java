package com.securechat.model;

import com.securechat.protocol.MsgType;
import lombok.Data;

//lombok으로 getter, setter, toString 자동 생성
@Data
public class MsgFormat {
    // 필드 선언
    private MsgType type;
    private String sender;
    private String receiver;
    private String body;
    private String timestamp;

    //기본 생성자
    public MsgFormat(){

        //아무 동작 안함. json 변환 시 필요
    }

    //전체 필드 초기화
    public MsgFormat(MsgType type, String sender, String receiver, String body, String timestamp){
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.timestamp = timestamp;
    }

}
