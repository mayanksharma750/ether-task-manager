package com.ethara.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        LocalDate dueDate,

        @NotNull
        Long projectId,

        Long assigneeId
) {
}
