package com.securechat.auth.encrypt;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class KeyVault {
    private static final Gson GSON = new Gson();

    // 저장 경로: ~/.securechat/identity.key.enc
    private static final Path VAULT_DIR  =
            Paths.get(System.getProperty("user.home"), ".securechat");
    private static final Path VAULT_FILE =
            VAULT_DIR.resolve("identity.key.enc");

    // 암호 파라미터
    private static final int SALT_LEN = 16;     // PBKDF2 salt 길이 (bytes) - 16B = 128bit
    private static final int IV_LEN   = 12;     // AES-GCM 권장 IV(논스) 길이 (bytes) - 12B = 96bit
    private static final int TAG_LEN  = 128;    // AES-GCM 인증 태그 길이 (bits) - 128bit
    private static final int ITER     = 120_000;// PBKDF2 반복(느리게 만들어 공격 억제). 성능 따라 조정 가능

}
