package com.aiapp.chat.dto;

import java.time.LocalDateTime;

public record ChatResponse(
    Long id,
    String question,
    String answer,
    String model,
    LocalDateTime createdAt
) {

}
