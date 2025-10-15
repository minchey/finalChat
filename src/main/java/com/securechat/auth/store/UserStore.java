// src/main/java/com/securechat/auth/store/UserStore.java
package com.securechat.auth.store;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 간단한 파일(JSON) 기반 유저 저장소.
 * - key: id, value: UserRecord
 * - putIfAbsent 시 디스크에 즉시 반영(원자적 쓰기)
 * - 서버 재시작 시 자동 로드
 */
public class UserStore {

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, UserRecord>>() {}.getType();

    /** 기본 위치: 실행 디렉토리의 users.json (원하면 절대경로로 바꿔도 됨) */
    private static final Path DB_PATH = Paths.get("users.json");

    private static final ReentrantReadWriteLock RW = new ReentrantReadWriteLock();
    private static final Map<String, UserRecord> USERS = new ConcurrentHashMap<>();

    static {
        loadFromDisk();
    }

    /* =========================
     * public API
     * ========================= */

    /** 이미 존재하면 false, 신규 저장되면 true */
    public static boolean putIfAbsent(String id, String passwordHash, String nickname, String identityPublicKey) {
        if (isBlank(id) || isBlank(passwordHash) || isBlank(identityPublicKey)) return false;
        UserRecord rec = UserRecord.of(id, passwordHash, nickname);
        rec.setIdentityPublicKey(identityPublicKey); // 🔥 추가 필드
        UserRecord prev = USERS.putIfAbsent(id, rec);
        if (prev == null) { persist(); return true; }
        return false;    }


    public static boolean exists(String id) {
        return USERS.containsKey(id);
    }

    public static String getHashedPassword(String id) {
        UserRecord r = USERS.get(id);
        return (r == null) ? null : r.getPasswordHash();
    }

    public static UserRecord findById(String id) {
        return USERS.get(id);
    }

    /* =========================
     * disk I/O
     * ========================= */

    private static void loadFromDisk() {
        RW.writeLock().lock();
        try {
            if (Files.exists(DB_PATH)) {
                try (Reader r = Files.newBufferedReader(DB_PATH, StandardCharsets.UTF_8)) {
                    Map<String, UserRecord> m = GSON.fromJson(r, TYPE);
                    if (m != null) USERS.putAll(m);
                } catch (Exception e) {
                    System.err.println("[UserStore] Load failed: " + e.getMessage());
                }
            }
        } finally {
            RW.writeLock().unlock();
        }
    }

    /** 임시파일→원본 교체로 원자적 쓰기 */
    private static void persist() {
        RW.writeLock().lock();
        try {
            Path tmp = DB_PATH.resolveSibling(DB_PATH.getFileName() + ".tmp");
            try (Writer w = Files.newBufferedWriter(
                    tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(USERS, TYPE, w);
            }
            Files.move(tmp, DB_PATH,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("[UserStore] Persist failed: " + e.getMessage());
        } finally {
            RW.writeLock().unlock();
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
