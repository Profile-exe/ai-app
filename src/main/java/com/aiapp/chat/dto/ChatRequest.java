package com.aiapp.chat.dto;

public record ChatRequest(
    String question,
    String model
) {}
