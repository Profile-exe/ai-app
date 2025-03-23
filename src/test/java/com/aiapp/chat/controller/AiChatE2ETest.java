package com.aiapp.chat.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.aiapp.auth.jwt.repository.TokenRepository;
import com.aiapp.auth.jwt.service.JwtTokenProvider;
import com.aiapp.chat.dto.ChatRequest;
import com.aiapp.chat.dto.ChatResponse;
import com.aiapp.chat.dto.ThreadResponse;
import com.aiapp.chat.exception.ThreadErrorCode;
import com.aiapp.chat.repository.ChatRepository;
import com.aiapp.chat.repository.ThreadRepository;
import com.aiapp.common.PageTemplate;
import com.aiapp.common.exception.ErrorResponse;
import com.aiapp.user.entity.Role;
import com.aiapp.user.entity.User;
import com.aiapp.user.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Rollback(false)
@DisplayName("AI 챗 E2E 테스트")
class AiChatE2ETest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    private String accessToken;
    private Long userId;

    @BeforeAll
    static void setUpBeforeAll() {
        RestAssured.defaultParser = Parser.JSON; // 기본 파서를 JSON으로 설정
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        // 테스트 사용자 생성
        User user = createTestUser();
        userId = user.getId();

        // 토큰 생성
        accessToken = jwtTokenProvider.generateAccessToken(userId);

        // 테스트용 질문 생성
        List<String> questions = List.of(
                "첫번째 질문",
                "두 번째 질문입니다.",
                "세 번째 질문입니다."
        );

        // 질문을 생성하여 스레드와 채팅 데이터 준비
        for (String question : questions) {
            createChat(port, accessToken, new ChatRequest(question, null))
                    .statusCode(HttpStatus.OK.value());
        }
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        chatRepository.deleteAll();
        threadRepository.deleteAll();
        tokenRepository.deleteById(userId); // 토큰 정리
        userRepository.deleteById(userId);
    }

    private User createTestUser() {
        // 사용자 생성
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("Test User")
                .role(Role.MEMBER)
                .build();

        return userRepository.save(user);
    }

    @Test
    @DisplayName("대화 생성 테스트")
    void createChatTest() {
        // Given
        ChatRequest request = new ChatRequest("새로운 질문입니다.", null);

        // When
        String answer = createChat(port, accessToken, request)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .asString();

        // Then
        assertThat(answer).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("스트리밍 대화 생성 테스트")
    void createStreamingChatTest() {
        // Given
        ChatRequest request = new ChatRequest("스트리밍 질문입니다.", null);

        // When & Then
        // 스트리밍 응답은 HTTP 상태 코드만 확인
        createStreamingChat(port, accessToken, request)
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("대화 목록 조회 테스트")
    void getThreadsTest() {
        // When
        PageTemplate<ThreadResponse> threadResponses = getThreads(port, accessToken)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // Then
        List<ThreadResponse> content = threadResponses.getContent();
        assertThat(content).isNotEmpty().hasSize(1); // 하나의 스레드가 생성되어야 함
    }

    @Test
    @DisplayName("특정 스레드의 대화 목록 조회 테스트")
    void getChatsByThreadTest() {
        // Given
        // 먼저 스레드 목록 조회
        PageTemplate<ThreadResponse> threadResponses = getThreads(port, accessToken)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        List<ThreadResponse> threads = threadResponses.getContent();
        assertThat(threads).isNotEmpty();

        Long threadId = threads.getFirst().id();

        // When
        PageTemplate<ChatResponse> chatResponses = getChatsByThread(port, accessToken, threadId)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // Then
        List<ChatResponse> chats = chatResponses.getContent();
        assertThat(chats).isNotEmpty().hasSize(3); // 3개의 질문-답변 쌍이 있어야 함
    }

    @Test
    @DisplayName("스레드 삭제 테스트")
    void deleteThreadTest() {
        // Given
        // 먼저 스레드 목록 조회
        PageTemplate<ThreadResponse> threadResponses = getThreads(port, accessToken)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        List<ThreadResponse> threads = threadResponses.getContent();
        assertThat(threads).isNotEmpty();

        Long threadId = threads.getFirst().id();

        // When
        deleteThread(port, accessToken, threadId)
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Then
        // 스레드 목록을 다시 조회하여 삭제 확인
        PageTemplate<ThreadResponse> emptyThreadResponses = getThreads(port, accessToken)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        List<ThreadResponse> emptyThreads = emptyThreadResponses.getContent();
        assertThat(emptyThreads).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자의 스레드는 조회할 수 없다")
    void cannotAccessOtherUserThreadTest() {
        // Given
        // 먼저 스레드 목록 조회하여 스레드 ID 가져오기
        PageTemplate<ThreadResponse> threadResponses = getThreads(port, accessToken)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        List<ThreadResponse> threads = threadResponses.getContent();
        assertThat(threads).isNotEmpty();

        Long threadId = threads.getFirst().id();

        // 다른 사용자 생성
        User otherUser = createOtherUser();
        Long otherUserId = otherUser.getId();

        // 다른 사용자 토큰 생성
        String otherAccessToken = jwtTokenProvider.generateAccessToken(otherUserId);
        jwtTokenProvider.generateRefreshToken(otherUserId);

        // When & Then
        // 다른 사용자로 특정 스레드의 대화 목록 조회 시도
        ErrorResponse errorResponse = getChatsByThread(port, otherAccessToken, threadId)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract()
                .as(ErrorResponse.class);

        assertThat(errorResponse.message()).isEqualTo(ThreadErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        // 정리: 다른 사용자 삭제
        tokenRepository.deleteById(otherUserId);
        userRepository.deleteById(otherUserId);
    }

    @Test
    @DisplayName("대화 생성 시 30분 이내에 같은 사용자가 질문하면 같은 스레드에 추가된다")
    void createChatInSameThreadTest() {
        // Given
        // 새로운 질문 생성
        ChatRequest request = new ChatRequest("새로운 질문입니다.", null);
        createChat(port, accessToken, request)
                .statusCode(HttpStatus.OK.value());

        // When
        // 스레드 목록 조회
        PageTemplate<ThreadResponse> threadResponses = getThreads(port, accessToken)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // Then
        List<ThreadResponse> threads = threadResponses.getContent();
        assertThat(threads).hasSize(1); // 여전히 하나의 스레드만 있어야 함

        // 스레드의 대화 목록 조회
        Long threadId = threads.getFirst().id();
        PageTemplate<ChatResponse> chatResponses = getChatsByThread(port, accessToken, threadId)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        List<ChatResponse> chats = chatResponses.getContent();
        assertThat(chats).hasSize(4); // 기존 3개 + 새로운 1개 = 4개
    }

    private User createOtherUser() {
        User user = User.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("Other User")
                .role(Role.MEMBER)
                .build();

        return userRepository.save(user);
    }

    private ValidatableResponse createChat(int port, String accessToken, ChatRequest request) {
        return given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(request)
                .when()
                .log().all()
                .post("/api/v1/chat")
                .then()
                .log().all();
    }

    private ValidatableResponse createStreamingChat(int port, String accessToken, ChatRequest request) {
        return given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(request)
                .when()
                .post("/api/v1/chat/stream")
                .then()
                .log().all();
    }

    private ValidatableResponse getThreads(int port, String accessToken) {
        return given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .when()
                .log().all()
                .get("/api/v1/chat/threads")
                .then()
                .log().all();
    }

    private ValidatableResponse getChatsByThread(int port, String accessToken, Long threadId) {
        return given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .when()
                .get("/api/v1/chat/threads/" + threadId)
                .then()
                .log().all();
    }

    private ValidatableResponse deleteThread(int port, String accessToken, Long threadId) {
        return given()
                .port(port)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .when()
                .delete("/api/v1/chat/threads/" + threadId)
                .then()
                .log().all();
    }
}
