package com.securechat.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MsgFormat {
    // 필드 선언
    private String type;
    private String sender;
    private String receiver;
    private String body;
    private String timestamp;

    //기본 생성자
    public MsgFormat(){

        //아무 동작 안함. json 변환 시 필요
    }

    //전체 필드 초기화
    public MsgFormat(String type, String sender, String receiver, String body, String timestamp){
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getSender(){
        return sender;
    }

    public void setSender(String sender){
        this.sender = sender;
    }
}
