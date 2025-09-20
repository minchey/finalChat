# 🔒 E2EE Chat (End-to-End Encrypted Chat)

종단간 암호화(E2EE)를 적용한 실시간 채팅 애플리케이션 프로젝트입니다.  
포트폴리오용으로 제작되며, **Spring Boot + WebSocket** 기반으로 구현합니다.

---

## 🚀 프로젝트 목표
- 사용자 간 **실시간 채팅** 구현
- **AES 대칭키** 기반 메시지 암호화
- **RSA 공개키 암호화**를 통한 AES 키 교환
- 대화 내용 **DB 저장 및 조회(히스토리 기능)**
- 안전한 **E2EE 구조** 설계

---

## ⚙️ 기술 스택
- **Backend**: Spring Boot, Spring Web, WebSocket, Spring Security, Spring Data JPA
- **Database**: MySQL (개발 시 H2)
- **Encryption**: AES, RSA (Java Cryptography Extension)
- **Build Tool**: Maven
- **Language**: Java 21

---

## 📂 패키지 구조 (초안)
com.securechat
┣ network // 네트워크 통신 담당
┃ ┣ ChatServer
┃ ┣ ChatClient
┃ ┣ ServerMessageHandler
┃ ┗ ClientMessageHandler
┣ crypto // 암호화 관련
┃ ┣ AESUtil
┃ ┣ RSAUtil
┃ ┗ KeyManager
┣ model // 데이터 구조 정의
┃ ┣ MsgFormat
┃ ┣ User
┃ ┗ MessageLog
┣ service // 비즈니스 로직
┃ ┣ MessageService
┃ ┗ UserService
┗ E2eeChatApplication (메인 실행 클래스)

---

## 📝 진행 계획
1. `MsgFormat` 클래스 설계 → 통신 메시지 구조 확립
2. 서버(`ChatServer`) & 클라이언트(`ChatClient`) 기본 연결
3. 메시지 송수신 핸들러 추가
4. AES/RSA 암호화 적용
5. 사용자 관리 & 대화 로그 저장
6. README, 문서화, Docker 배포

---

## 📌 상태
- ✅ Spring Boot 프로젝트 초기 세팅 완료
- ⬜ MsgFormat 클래스 구현 예정
- ⬜ 네트워크 계층 구현 예정
- ⬜ 암호화 계층 구현 예정
