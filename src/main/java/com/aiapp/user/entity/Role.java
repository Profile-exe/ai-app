package com.aiapp.user.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    MEMBER("member"), ADMIN("admin");

    private final String value;
}
