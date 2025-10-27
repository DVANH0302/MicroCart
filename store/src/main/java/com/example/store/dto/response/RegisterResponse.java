package com.example.store.dto.response;

public record RegisterResponse(
        Integer userId,
        String username,
        String message
) {
}
