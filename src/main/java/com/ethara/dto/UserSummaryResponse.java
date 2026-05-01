package com.ethara.dto;

import com.ethara.entity.Role;

public record UserSummaryResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}
