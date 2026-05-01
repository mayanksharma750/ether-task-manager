package com.ethara.dto;

public record DashboardResponse(
        long totalTasks,
        long completedTasks,
        long overdueTasks
) {
}
