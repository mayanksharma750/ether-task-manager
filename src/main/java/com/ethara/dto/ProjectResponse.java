package com.ethara.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        String ownerEmail,
        List<UserSummaryResponse> members,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
