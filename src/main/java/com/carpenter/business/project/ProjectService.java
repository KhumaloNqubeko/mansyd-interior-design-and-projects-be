package com.carpenter.business.project;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.project.dto.ProjectResponse;
import com.carpenter.business.project.dto.ProjectStatusUpdateRequest;
import com.carpenter.business.project.dto.ProjectTimelineRequest;
import com.carpenter.business.project.dto.ProjectTimelineResponse;
import com.carpenter.business.project.dto.ProjectUpdateRequest;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private static final EnumMap<ProjectStatus, Set<ProjectStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ProjectStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ProjectStatus.CREATED, EnumSet.of(ProjectStatus.SCHEDULED, ProjectStatus.IN_PROGRESS, ProjectStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.SCHEDULED, EnumSet.of(ProjectStatus.IN_PROGRESS, ProjectStatus.ON_HOLD, ProjectStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.IN_PROGRESS, EnumSet.of(ProjectStatus.AWAITING_MATERIALS, ProjectStatus.ON_HOLD, ProjectStatus.QUALITY_INSPECTION, ProjectStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.AWAITING_MATERIALS, EnumSet.of(ProjectStatus.IN_PROGRESS, ProjectStatus.ON_HOLD, ProjectStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.ON_HOLD, EnumSet.of(ProjectStatus.IN_PROGRESS, ProjectStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.QUALITY_INSPECTION, EnumSet.of(ProjectStatus.READY_FOR_DELIVERY, ProjectStatus.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(ProjectStatus.READY_FOR_DELIVERY, EnumSet.of(ProjectStatus.DELIVERED, ProjectStatus.INSTALLED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.DELIVERED, EnumSet.of(ProjectStatus.INSTALLED, ProjectStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.INSTALLED, EnumSet.of(ProjectStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.COMPLETED, EnumSet.noneOf(ProjectStatus.class));
        ALLOWED_TRANSITIONS.put(ProjectStatus.CANCELLED, EnumSet.noneOf(ProjectStatus.class));
    }

    private final ProjectRepository projectRepository;
    private final ProjectUpdateRepository projectUpdateRepository;
    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;

    public ProjectService(ProjectRepository projectRepository, ProjectUpdateRepository projectUpdateRepository,
                          OrderRepository orderRepository, CurrentUser currentUser) {
        this.projectRepository = projectRepository;
        this.projectUpdateRepository = projectUpdateRepository;
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public Project getOrCreateForOrder(Order order) {
        return projectRepository.findByOrderId(order.getId())
                .orElseGet(() -> projectRepository.save(new Project(generateProjectNumber(), order)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(projectRepository.findAll(pageable).map(ProjectResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> myProjects(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(projectRepository.findByCustomerUserId(user.getId(), pageable).map(ProjectResponse::from));
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID id, Authentication authentication) {
        Project project = findAndAuthorize(id, authentication);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectUpdateRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Project project = find(id);
        validatePlannedDates(request.plannedStartDate(), request.plannedCompletionDate());
        project.update(request.plannedStartDate(), request.plannedCompletionDate(), clean(request.notes()));
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateStatus(UUID id, ProjectStatusUpdateRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Project project = find(id);
        validateStatusChange(project, request);
        project.changeStatus(request.status(), request.status() == ProjectStatus.COMPLETED ? 100 : request.progress(),
                request.status() == ProjectStatus.COMPLETED ? requireCompletionDate(request.actualCompletionDate()) : request.actualCompletionDate());
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectTimelineResponse addUpdate(UUID id, ProjectTimelineRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Project project = find(id);
        User user = currentUser.require(authentication);
        ProjectUpdate update = projectUpdateRepository.save(new ProjectUpdate(project, user, trim(request.title()), trim(request.message())));
        return ProjectTimelineResponse.from(update);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectTimelineResponse> updates(UUID id, Authentication authentication, Pageable pageable) {
        findAndAuthorize(id, authentication);
        return PageResponse.from(projectUpdateRepository.findByProjectIdOrderByCreatedAtDesc(id, pageable)
                .map(ProjectTimelineResponse::from));
    }

    private Project findAndAuthorize(UUID id, Authentication authentication) {
        Project project = find(id);
        User user = currentUser.require(authentication);
        if (user.getRole() != Role.CARPENTER && !project.getCustomer().getUser().getId().equals(user.getId())) {
            throw new UnauthorisedOperationException("You cannot access another customer's project.");
        }
        return project;
    }

    private Project find(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }

    private void validateStatusChange(Project project, ProjectStatusUpdateRequest request) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(project.getStatus(), Set.of()).contains(request.status())) {
            throw new UnauthorisedOperationException("Invalid project status transition.");
        }
        if (request.status() == ProjectStatus.COMPLETED && request.progress() != 100) {
            throw new UnauthorisedOperationException("Completed projects must have 100 percent progress.");
        }
        if (request.actualCompletionDate() != null && request.actualCompletionDate().isAfter(LocalDate.now())) {
            throw new UnauthorisedOperationException("Actual completion date cannot be in the future.");
        }
    }

    private LocalDate requireCompletionDate(LocalDate actualCompletionDate) {
        if (actualCompletionDate == null) {
            throw new UnauthorisedOperationException("Completed projects require an actual completion date.");
        }
        return actualCompletionDate;
    }

    private void validatePlannedDates(LocalDate start, LocalDate completion) {
        if (start != null && completion != null && completion.isBefore(start)) {
            throw new UnauthorisedOperationException("Planned completion date cannot be before planned start date.");
        }
    }

    private String generateProjectNumber() {
        return "PRJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
    private String trim(String value) { return value.trim(); }
}
