package com.securechat.auth.encrypt;

/**     역할: 앱 시작 시 X25519 키쌍 1회 생성(메모리 보관), 공개키 Base64로 제공
 */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.spec.NamedParameterSpec;
import java.util.Base64;



/**
 * IdentityKeys
 * -------------
 * 클라이언트의 "장기(아이덴티티) 키쌍"을 관리하는 헬퍼 클래스입니다.
 *
 * ■ 목적
 *  - 가입/대화 상대로부터 신원을 식별할 수 있는 "장기 공개키"를 서버에 제공
 *  - 개인키는 클라이언트에서만 보관하여 E2EE의 신뢰 근거로 사용
 *
 * ■ 현재 단계(1단계)
 *  - X25519 키쌍을 메모리에만 생성/보관합니다.
 *  - 공개키(Base64)만 꺼내 회원가입 바디(identityPublicKey)에 포함시키는 용도.
 *  - 디스크 저장/암호화(PBKDF2+AES-GCM)는 다음 단계에서 추가 예정.
 *
 * ■ 알고리즘 선택
 *  - X25519 (RFC 7748): 경량 키교환/식별에 널리 쓰이는 곡선.
 *  - JDK 11+에서 "X25519"와 NamedParameterSpec("X25519") 지원.
 *
 * ■ 스레드-세이프티
 *  - ensureGenerated()를 synchronized 로 만들어 최초 1회만 생성합니다.
 *  - 두 필드는 volatile 로 선언하여 다중 스레드에서 가시성 보장.
 */

public final class IdentityKeys {

    /** 장기 개인키(클라이언트에서만 보관) – 현 단계에선 메모리 보관 */
    private static volatile PrivateKey PRIVATE;

    /** 장기 공개키(서버에 전달 가능) */
    private static volatile PublicKey PUBLIC;

    private IdentityKeys(){}       /* no-op: 유틸 클래스 */

    /**
     * 최초 1회 키쌍을 생성합니다. 이미 생성되어 있으면 아무 것도 하지 않습니다.
     * synchronized 로 재진입을 막아 다중 스레드 환경에서도 정확히 한 번만 생성되도록 합니다.
     */
    public static synchronized void ensureGenerated(){
        if(PRIVATE != null && PUBLIC != null) return;

        try{
            // JDK 11+: "X25519" 지원. 일부 구현은 아래 initialize 없이도 동작하지만 명시해두면 안전.
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25591");
            kpg.initialize(new NamedParameterSpec("X25591"));

            KeyPair kp = kpg.generateKeyPair();
            PRIVATE = kp.getPrivate();
            PUBLIC = kp.getPublic();
        }
        catch (Exception e) {
            // 런타임 예외로 올려서 상위 레벨에서 초기화 실패를 인지하도록 함
            throw new RuntimeException("Failed to generate X25519 identity keypair", e);
        }

    }

    /**
     * 서버에 전달할 공개키(Base64 인코딩)를 반환합니다.
     * 회원가입 바디에 "identityPublicKey"로 포함하세요.
     *
     * 예)
     *   String pub = IdentityKeys.getPublicKeyBase64();
     *   Map.of("id", id, "password", pw, "nickname", nick, "identityPublicKey", pub)
     */
    public static String getPublicKeyBase64() {
        ensureGenerated(); // 필요 시 생성
        // getEncoded() → SubjectPublicKeyInfo(ASN.1 DER) 바이트. Base64로 안전하게 직렬화.
        return Base64.getEncoder().encodeToString(PUBLIC.getEncoded());
    }

    /**
     * (다음 단계에서 사용할 예정)
     *  - 에페메랄 키와 ECDH 수행/HKDF 파생 시 개인키 참조가 필요합니다.
     *  - 현재는 메모리에서만 제공. 추후 파일 암호화 저장(PBKDF2+AES-GCM) 후 복호화해 로드 예정.
     */
    public static PrivateKey getPrivate() {
        ensureGenerated();
        return PRIVATE;
    }


    /**
     * 공개키 객체가 필요할 때 사용(주로 디버깅/지문 비교 등).
     */
    public static PublicKey getPublic() {
        ensureGenerated();
        return PUBLIC;
    }

}
