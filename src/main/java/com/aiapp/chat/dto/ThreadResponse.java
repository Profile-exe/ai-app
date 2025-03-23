package com.aiapp.chat.dto;

import com.aiapp.chat.entity.Thread;
import java.time.LocalDateTime;

public record ThreadResponse(
    Long id,
    String title,
    String userName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ThreadResponse from(Thread thread) {
        return new ThreadResponse(
            thread.getId(),
            thread.getTitle(),
            thread.getUser().getName(),
            thread.getCreatedAt(),
            thread.getUpdatedAt()
        );
    }
}
