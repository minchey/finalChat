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

    public static UserRecord of(String id, String passwordHash, String nickname) {
        String nick = (nickname == null || nickname.isBlank()) ? id : nickname;
        return new UserRecord(
                id,
                passwordHash,
                nick,
                java.time.LocalDateTime.now().toString()
        );
    }
}
