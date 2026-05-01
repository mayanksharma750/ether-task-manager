package com.ethara.service;

import com.ethara.dto.AssignProjectMembersRequest;
import com.ethara.dto.CreateProjectRequest;
import com.ethara.dto.ProjectResponse;
import com.ethara.dto.UserSummaryResponse;
import com.ethara.entity.Project;
import com.ethara.entity.Role;
import com.ethara.entity.User;
import com.ethara.repository.ProjectRepository;
import com.ethara.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponse createProject(CreateProjectRequest request, Authentication authentication) {
        User owner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponse assignMembers(Long projectId, AssignProjectMembersRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Set<Long> memberIds = new LinkedHashSet<>(request.memberIds());
        List<User> members = userRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw new IllegalArgumentException("One or more members were not found");
        }

        boolean hasNonMember = members.stream()
                .anyMatch(user -> user.getRole() != Role.MEMBER);

        if (hasNonMember) {
            throw new IllegalArgumentException("Only users with MEMBER role can be assigned to projects");
        }

        new ArrayList<>(project.getMembers()).forEach(project::removeMember);
        members.forEach(project::addMember);

        return toResponse(projectRepository.save(project));
    }

    private ProjectResponse toResponse(Project project) {
        User owner = project.getOwner();
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                owner.getId(),
                owner.getEmail(),
                project.getMembers()
                        .stream()
                        .map(this::toUserSummary)
                        .toList(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
