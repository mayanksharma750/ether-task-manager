package com.ethara.controller;

import com.ethara.dto.AssignTaskRequest;
import com.ethara.dto.CreateTaskRequest;
import com.ethara.dto.TaskResponse;
import com.ethara.dto.UpdateTaskStatusRequest;
import com.ethara.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PutMapping("/{taskId}/assignee")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request
    ) {
        return taskService.assignTask(taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasRole('ADMIN') or @taskAuthorizationService.isAssignedTask(#p0, authentication.name)")
    public TaskResponse updateStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return taskService.updateStatus(taskId, request);
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public List<TaskResponse> getAssignedTasks(Authentication authentication) {
        return taskService.getAssignedTasks(authentication);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponse> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponse> getAllTasks() {
        return taskService.getAllTasks();
    }
}
