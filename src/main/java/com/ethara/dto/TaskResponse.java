package com.ethara.dto;

import com.ethara.entity.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
