package com.securechat.auth.store;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
public class UserRecord {
    /** 계정 ID (고유키) */
    private String id;
    /** SHA-256 등으로 해시된 비밀번호 */
    private String passwordHash;
    /** 표시용 닉네임 (미입력 시 id와 동일하게 저장) */
    private String nickname;
    /** ISO-8601 문자열로 저장한 생성시각 */
    private String createdAt;

    private String identityPublicKey; // 🔥 Base64

    // --- getters/setters ---
    public String getId() { return id; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public String getCreatedAt() { return createdAt; }
    public String getIdentityPublicKey() { return identityPublicKey; }

    //public void setIdentityPublicKey(String identityPublicKey) { this.identityPublicKey = identityPublicKey; }
    //public void setNickname(String nickname) { this.nickname = nickname; }

    public static UserRecord of(String id, String passwordHash, String nickname, String identityPublicKey) {
        UserRecord r = new UserRecord();
        r.id = id;
        r.passwordHash = passwordHash;
        r.nickname = (nickname == null || nickname.isBlank()) ? id : nickname;
        r.identityPublicKey = identityPublicKey;
        r.createdAt = java.time.LocalDateTime.now().toString();
        return r;
    }

    // (기존에 쓰던 오버로드를 위해) 닉네임만 있는 버전
    public static UserRecord of(String id, String passwordHash, String nickname) {
        return of(id, passwordHash, nickname, null);
    }


}
