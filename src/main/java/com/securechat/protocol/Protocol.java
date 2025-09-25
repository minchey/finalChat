package com.securechat.protocol;

public class Protocol {
    //타임 스탬프 포맷
    public static final String TIMESTAMP_PATTERN = "yyyy-mm-dd 'T' HH:MM:SS";

    //DM 기능 쓸 떄 수신자 앞에 붙이는 문자
    public static final char DM_PREFIX = '@';

    //메시지 최대 길이
    public static final int BODY_MAX = 2000;
}
