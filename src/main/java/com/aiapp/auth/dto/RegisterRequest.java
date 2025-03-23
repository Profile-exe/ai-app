package com.aiapp.auth.dto;

public record RegisterRequest(
        String email,
        String password,
        String name
) {

}
