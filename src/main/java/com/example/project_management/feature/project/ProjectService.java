package com.example.project_management.feature.project;

import com.example.project_management.feature.project.dto.ProjectMemberRequest;
import com.example.project_management.feature.project.dto.ProjectRequest;
import com.example.project_management.feature.project.dto.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    ProjectResponse getProjectById(Long id);
    List<ProjectResponse> getAllProjects();
    void deleteProject(Long id);
    void addMemberToProject(Long projectId, ProjectMemberRequest request);
    java.util.List<com.example.project_management.feature.user.dto.UserResponse> getProjectMembers(Long projectId);
}
