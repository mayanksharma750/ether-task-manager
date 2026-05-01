package com.ethara.service;

import com.ethara.dto.AssignTaskRequest;
import com.ethara.dto.CreateTaskRequest;
import com.ethara.dto.TaskResponse;
import com.ethara.dto.UpdateTaskStatusRequest;
import com.ethara.entity.Project;
import com.ethara.entity.Role;
import com.ethara.entity.Task;
import com.ethara.entity.User;
import com.ethara.repository.ProjectRepository;
import com.ethara.repository.TaskRepository;
import com.ethara.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = getAssignableMember(request.assigneeId(), project);
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .project(project)
                .assignee(assignee)
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        User assignee = getAssignableMember(request.userId(), task.getProject());
        task.setAssignee(assignee);

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskAuthorizationService.isAssignedTask(#p0, authentication.name)")
    public TaskResponse updateStatus(Long taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(request.status());
        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public List<TaskResponse> getAssignedTasks(Authentication authentication) {
        return taskRepository.findByAssigneeEmail(authentication.getName())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponse> getTasksByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found");
        }

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getAssignableMember(Long userId, Project project) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.MEMBER) {
            throw new IllegalArgumentException("Only users with MEMBER role can be assigned tasks");
        }

        boolean isProjectMember = project.getMembers()
                .stream()
                .anyMatch(member -> member.getId().equals(user.getId()));

        if (!isProjectMember) {
            throw new IllegalArgumentException("User must be assigned to the project before receiving tasks");
        }

        return user;
    }

    private TaskResponse toResponse(Task task) {
        Project project = task.getProject();
        User assignee = task.getAssignee();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                project.getId(),
                project.getName(),
                assignee == null ? null : assignee.getId(),
                assignee == null ? null : assignee.getEmail(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
