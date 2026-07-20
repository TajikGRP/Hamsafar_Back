package com.tajikGRP.Hansafar.users.dto;

import com.tajikGRP.Hansafar.users.domain.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String email,
        String name,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}