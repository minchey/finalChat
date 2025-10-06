package com.securechat.auth.store;

import java.util.concurrent.ConcurrentHashMap;
public class UserStore {


    //  회원 정보를 저장할 "메모리 기반 데이터베이스" 역할
    //  key   : 회원 아이디 (String)
    //  value : 해시된 비밀번호 (String)
    //  ConcurrentHashMap : 여러 스레드(클라이언트)가 동시에 접근해도 안전한 Map
    private static final ConcurrentHashMap<String,String> users = new ConcurrentHashMap<>();


    //  회원가입할 때 호출되는 메서드
    // - 아이디가 아직 존재하지 않으면 새 회원을 저장하고 true 반환
    // - 이미 존재하면 저장하지 않고 false 반환
    public static boolean putIfAbsent(String id, String hashedPw) {
        // putIfAbsent() → key가 없을 때만 저장하고, 기존 값이 없으면 null 반환
        return users.putIfAbsent(id, hashedPw) == null;
    }


    //  특정 아이디가 이미 회원 목록에 존재하는지 확인하는 메서드
    // - 회원가입 시 중복 아이디를 막을 때 사용
    public static boolean exists(String id) {
        return users.containsKey(id);
    }


    //  저장된 회원의 해시 비밀번호를 가져오는 메서드
    // - 로그인 시 입력한 비밀번호를 해시한 값과 비교할 때 사용
    // - 존재하지 않으면 null 반환
    public static String getHashedPassword(String id) {
        return users.get(id);
    }
}
