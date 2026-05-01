package com.ethara.service;

import com.ethara.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAuthorizationService {

    private final TaskRepository taskRepository;

    public boolean isAssignedTask(Long taskId, String email) {
        return taskRepository.findById(taskId)
                .map(task -> task.getAssignee() != null && task.getAssignee().getEmail().equals(email))
                .orElse(false);
    }
}
