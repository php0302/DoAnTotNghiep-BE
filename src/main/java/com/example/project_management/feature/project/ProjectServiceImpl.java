package com.example.project_management.feature.project;

import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.project.dto.ProjectMemberRequest;
import com.example.project_management.feature.project.dto.ProjectRequest;
import com.example.project_management.feature.project.dto.ProjectResponse;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectMemberRepository projectMemberRepository,
                              UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user id"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepository.save(project);

        // Add creator as PROJECT_MANAGER automatically
        ProjectMember member = new ProjectMember();
        member.setProject(savedProject);
        member.setUser(currentUser);
        member.setRole(com.example.project_management.feature.user.Role.PROJECT_MANAGER);
        projectMemberRepository.save(member);

        return ProjectResponse.fromEntity(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());

        return ProjectResponse.fromEntity(projectRepository.save(project));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return ProjectResponse.fromEntity(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }
        // TODO: delete members and tasks before deleting project, or handle cascade
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addMemberToProject(Long projectId, ProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new InvalidRequestException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.role());
        projectMemberRepository.save(member);
    }
}
