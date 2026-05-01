package com.ethara.service;

import com.ethara.dto.DashboardResponse;
import com.ethara.entity.TaskStatus;
import com.ethara.repository.TaskRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                taskRepository.count(),
                taskRepository.countByStatus(TaskStatus.DONE),
                taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE)
        );
    }
}
