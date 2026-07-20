package com.tajikGRP.Hansafar.users.dto;

import com.tajikGRP.Hansafar.users.domain.Users;

public record UserSummaryResponse(
        Long userId,
        String email,
        String name
) {

    public static UserSummaryResponse from(Users user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}