package com.aiapp.chat.controller;

import com.aiapp.auth.resolver.UserId;
import com.aiapp.chat.dto.ChatRequest;
import com.aiapp.chat.dto.ChatResponse;
import com.aiapp.chat.dto.ThreadResponse;
import com.aiapp.chat.entity.Chat;
import com.aiapp.chat.entity.Thread;
import com.aiapp.chat.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<String> chat(
            @RequestBody ChatRequest request,
            @UserId Long userId) {

        String answer = aiChatService.createNormalChat(
                request.question(),
                userId,
                request.model()
        );

        return ResponseEntity.ok(answer);
    }

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestBody ChatRequest request,
            @UserId Long userId) {

        return aiChatService.createStreamingChat(
                request.question(),
                userId,
                request.model()
        );
    }

    @GetMapping("/threads")
    public ResponseEntity<Page<ThreadResponse>> getThreads(
            @UserId Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Thread> threads = aiChatService.getChatsByUser(
                userId,
                pageable,
                true
        );
        
        Page<ThreadResponse> threadDtos = threads.map(ThreadResponse::from);
        return ResponseEntity.ok(threadDtos);
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<Page<ChatResponse>> getChatsByThread(
            @PathVariable Long threadId,
            @UserId Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<Chat> chats = aiChatService.getChatsByThread(
                threadId,
                userId,
                pageable
        );
        
        Page<ChatResponse> chatResponses = chats.map(chat -> 
            new ChatResponse(chat.getId(), chat.getQuestion(), chat.getAnswer(), chat.getModel(), chat.getCreatedAt())
        );
        
        return ResponseEntity.ok(chatResponses);
    }

    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<Void> deleteThread(
            @PathVariable Long threadId,
            @UserId Long userId) {
        
        aiChatService.deleteThread(threadId, userId);
        return ResponseEntity.noContent().build();
    }
}
