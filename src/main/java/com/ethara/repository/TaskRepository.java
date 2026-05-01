package com.ethara.repository;

import com.ethara.entity.Task;
import com.ethara.entity.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"project", "assignee"})
    List<Task> findByAssigneeEmail(String email);

    @EntityGraph(attributePaths = {"project", "assignee"})
    List<Task> findByProjectId(Long projectId);

    long countByStatus(TaskStatus status);

    long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    @Override
    @EntityGraph(attributePaths = {"project", "assignee"})
    Optional<Task> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"project", "assignee"})
    List<Task> findAll();
}
