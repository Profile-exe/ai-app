package com.aiapp.auth.dto;

public record LoginRequest(
        String email,
        String password
) {

}
