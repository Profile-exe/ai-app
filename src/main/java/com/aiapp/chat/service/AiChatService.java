package com.aiapp.chat.service;

import com.aiapp.chat.entity.Chat;
import com.aiapp.chat.entity.Thread;
import com.aiapp.chat.exception.UnauthorizedAccessException;
import com.aiapp.chat.repository.ChatRepository;
import com.aiapp.user.entity.Role;
import com.aiapp.user.entity.User;
import com.aiapp.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;
    private final UserService userService;
    private final ThreadService threadService;
    private final ChatRepository chatRepository;

    @Transactional
    public String createNormalChat(String question, Long userId, String model) {
        User user = userService.getUserByIdOrThrow(userId);
        Thread thread = threadService.getOrCreateThread(user, question);

        List<Message> messageHistory = getMessages(question, thread);

        // 모델이 지정되었는지 확인하고 ChatOptions 생성
        ChatOptions options = createChatOptions(model);

        // 일반 응답 처리
        ChatResponse response = chatClient
                .prompt(new Prompt(messageHistory, options))
                .call()
                .chatResponse();

        String answer = response.getResult().getOutput().getText();

        // DB에 대화 저장
        Chat chat = Chat.of(thread, question, answer, model);
        chatRepository.save(chat);

        return answer;
    }

    private List<Message> getMessages(String question, Thread thread) {
        // 이전 대화 내용 불러오기
        List<Chat> previousChats = chatRepository.findByThreadOrderByCreatedAtAsc(thread);
        List<Message> messageHistory = convertToMessageHistory(previousChats);

        messageHistory.add(new UserMessage(question));
        return messageHistory;
    }

    @Transactional
    public Flux<String> createStreamingChat(String question, Long userId, String model) {
        User user = userService.getUserByIdOrThrow(userId);
        Thread thread = threadService.getOrCreateThread(user, question);

        // 이전 대화 내용 불러오기
        List<Message> messageHistory = getMessages(question, thread);

        // 모델이 지정되었는지 확인하고 ChatOptions 생성
        ChatOptions options = createChatOptions(model);

        // 응답을 수신하기 전에 미리 DB에 대화를 저장
        Chat chat = Chat.of(thread, question, "", model);
        Chat savedChat = chatRepository.save(chat);

        // 스트리밍 응답 처리
        Flux<ChatResponse> responseFlux = chatClient
                .prompt(new Prompt(messageHistory, options))
                .stream()
                .chatResponse();

        StringBuilder fullResponse = new StringBuilder();

        return responseFlux
                .map(response -> {
                    String content = response.getResult().getOutput().getText();
                    fullResponse.append(content);
                    return content;
                })
                .doOnComplete(() -> {
                    // 스트림이 완료되면 전체 응답을 DB에 저장합니다.
                    savedChat.updateAnswer(fullResponse.toString());
                    chatRepository.save(savedChat);
                });
    }

    private ChatOptions createChatOptions(String model) {
        // OpenAI 모델 지정 model이 null이면 기본 모델을 사용합니다.
        return OpenAiChatOptions.builder()
                .model(model != null ? model : "gpt-3.5-turbo")
                .build();
    }

    private List<Message> convertToMessageHistory(List<Chat> chats) {
        List<Message> messages = new ArrayList<>();

        for (Chat chat : chats) {
            messages.add(new UserMessage(chat.getQuestion()));
            messages.add(new AssistantMessage(chat.getAnswer()));
        }

        return messages;
    }

    @Transactional(readOnly = true)
    public Page<Thread> getChatsByUser(Long userId, Pageable pageable, boolean isAdmin) {
        User user = userService.getUserByIdOrThrow(userId);

        // 관리자인 경우 모든 스레드를 조회할 수 있습니다.
        if (isAdmin && user.getRole() == Role.ADMIN) {
            return threadService.getAllThreads(pageable);
        } else {
            // 일반 사용자는 자신의 스레드만 조회할 수 있습니다.
            return threadService.getUserThreads(user, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<Chat> getChatsByThread(Long threadId, Long userId, Pageable pageable) {
        User user = userService.getUserByIdOrThrow(userId);
        Thread thread = threadService.getThreadByIdOrThrow(threadId);

        // 자신의 스레드이거나 관리자인 경우만 조회 가능
        if (!thread.getUser().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw UnauthorizedAccessException.EXCEPTION;
        }

        return chatRepository.findByThread(thread, pageable);
    }

    @Transactional
    public void deleteThread(Long threadId, Long userId) {
        User user = userService.getUserByIdOrThrow(userId);
        Thread thread = threadService.getThreadByIdOrThrow(threadId);

        if (!thread.getUser().equals(user)) {
            throw UnauthorizedAccessException.EXCEPTION;
        }

        chatRepository.deleteByThread(thread);
        threadService.delete(thread);
    }
}
