package com.example.store.dto.response;

public record LoginResponse(
        Integer userId,
        String username,
        String email,
        String accessToken,
        String message
) {
}
