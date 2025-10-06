package com.securechat.auth.store;

import java.time.LocalDateTime;

public class UserRecord {
    private final String id;            // 로그인 아이디(고유)
    private final String passwordHash;  // 해시된 비밀번호
    private final String nickname;      // 표시용 닉네임
    private final LocalDateTime createdAt;

    public UserRecord(String id, String passwordHash, String nickname, LocalDateTime createdAt) {
        this.id = id;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // 필요시 equals/hashCode는 id 기준으로만 (선택)
}
