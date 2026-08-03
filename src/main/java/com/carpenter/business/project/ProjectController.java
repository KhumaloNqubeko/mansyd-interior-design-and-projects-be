package com.carpenter.business.project;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.project.dto.ProjectResponse;
import com.carpenter.business.project.dto.ProjectStatusUpdateRequest;
import com.carpenter.business.project.dto.ProjectTimelineRequest;
import com.carpenter.business.project.dto.ProjectTimelineResponse;
import com.carpenter.business.project.dto.ProjectUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    PageResponse<ProjectResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return projectService.all(authentication, pageable);
    }

    @GetMapping("/my")
    PageResponse<ProjectResponse> myProjects(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return projectService.myProjects(authentication, pageable);
    }

    @GetMapping("/{id}")
    ProjectResponse get(@PathVariable UUID id, Authentication authentication) {
        return projectService.get(id, authentication);
    }

    @PutMapping("/{id}")
    ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectUpdateRequest request,
                           Authentication authentication) {
        return projectService.update(id, request, authentication);
    }

    @PatchMapping("/{id}/status")
    ProjectResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody ProjectStatusUpdateRequest request,
                                 Authentication authentication) {
        return projectService.updateStatus(id, request, authentication);
    }

    @PostMapping("/{id}/updates")
    ProjectTimelineResponse addUpdate(@PathVariable UUID id, @Valid @RequestBody ProjectTimelineRequest request,
                                      Authentication authentication) {
        return projectService.addUpdate(id, request, authentication);
    }

    @GetMapping("/{id}/updates")
    PageResponse<ProjectTimelineResponse> updates(@PathVariable UUID id, Authentication authentication,
                                                  @PageableDefault(size = 20) Pageable pageable) {
        return projectService.updates(id, authentication, pageable);
    }
}
