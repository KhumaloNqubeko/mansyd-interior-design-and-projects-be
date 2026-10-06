package com.carpenter.business.project;

import com.carpenter.business.audit.AuditAction;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.notification.NotificationType;
import com.carpenter.business.project.dto.*;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProjectCollaborationService {
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;
    private final ProjectRepository projects;
    private final ProjectActivityRepository activity;
    private final CurrentUser currentUser;
    private final NotificationService notifications;
    private final AuditLogService audit;

    public ProjectCollaborationService(ProjectRepository projects, ProjectActivityRepository activity,
            CurrentUser currentUser, NotificationService notifications, AuditLogService audit) {
        this.projects = projects; this.activity = activity; this.currentUser = currentUser;
        this.notifications = notifications; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectActivityResponse> list(UUID projectId, Authentication auth, Pageable pageable) {
        authorize(find(projectId, false), currentUser.require(auth));
        return PageResponse.from(activity.activity(projectId, pageable));
    }

    @Transactional
    public ProjectActivityResponse comment(UUID projectId, String message, Authentication auth) {
        User actor = currentUser.require(auth);
        Project project = authorize(find(projectId, true), actor);
        requireOpen(project);
        String text = text(message, true);
        var entry = save(project, actor, ProjectActivity.Kind.COMMENT, text);
        notifyOther(project, actor, "New project comment", text);
        return ProjectActivityResponse.from(entry);
    }

    @Transactional
    public ProjectActivityResponse photo(UUID projectId, String description, MultipartFile file, Authentication auth) throws IOException {
        User actor = currentUser.require(auth);
        Project project = authorize(find(projectId, true), actor);
        requireOpen(project);
        String message = text(description, false);
        if (file == null || file.isEmpty() || file.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Choose a JPEG or PNG photo up to 5 MB.");
        }
        byte[] bytes = file.getBytes();
        String contentType = validatePhoto(bytes);
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        ProjectActivity entry = new ProjectActivity(project, actor, ProjectActivity.Kind.PHOTO, message);
        entry.attachPhoto("project-photo" + extension, contentType, bytes);
        entry = activity.save(entry);
        audit.record(actor, AuditAction.CREATED, "Project", projectId, "Uploaded a private project photo.");
        notifyOther(project, actor, "New project photo", message.isBlank() ? "A photo was added to " + project.getProjectNumber() : message);
        return ProjectActivityResponse.from(entry);
    }

    @Transactional(readOnly = true)
    public ProjectActivity photoContent(UUID projectId, UUID activityId, Authentication auth) {
        authorize(find(projectId, false), currentUser.require(auth));
        ProjectActivity entry = activity.findById(activityId).orElseThrow(() -> new ResourceNotFoundException("Photo was not found."));
        if (!entry.getProject().getId().equals(projectId) || entry.getKind() != ProjectActivity.Kind.PHOTO) {
            throw new ResourceNotFoundException("Photo was not found.");
        }
        return entry;
    }

    @Transactional
    public ProjectResponse requestReview(UUID projectId, Authentication auth) {
        User owner = currentUser.requireRole(auth, Role.CARPENTER);
        Project project = find(projectId, true);
        if (project.getStatus() != ProjectStatus.INSTALLED) {
            throw new IllegalArgumentException("Only installed work can be sent for customer review.");
        }
        if (project.getCompletionReviewStatus() == CompletionReviewStatus.PENDING_REVIEW) return ProjectResponse.from(project);
        if (project.getCompletionReviewStatus() == CompletionReviewStatus.CONFIRMED) {
            throw new IllegalArgumentException("The customer has already confirmed this work.");
        }
        project.requestCompletionReview();
        save(project, owner, ProjectActivity.Kind.REVIEW_REQUESTED, "Please review the installed work and confirm completion or report an issue.");
        notifications.notifyUser(project.getCustomer().getUser(), NotificationType.PROJECT, "Your work is ready for review",
                project.getProjectNumber() + " is awaiting your confirmation.", customerLink(project));
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse decide(UUID projectId, CompletionDecisionRequest request, Authentication auth) {
        User customer = currentUser.requireRole(auth, Role.CUSTOMER);
        Project project = authorize(find(projectId, true), customer);
        if (!request.reviewId().equals(project.getCompletionReviewId())) {
            throw new IllegalArgumentException("This review has changed. Refresh the project before responding.");
        }
        if (request.confirmed() && project.getCompletionReviewStatus() == CompletionReviewStatus.CONFIRMED) return ProjectResponse.from(project);
        if (project.getCompletionReviewStatus() != CompletionReviewStatus.PENDING_REVIEW || project.getStatus() != ProjectStatus.INSTALLED) {
            throw new IllegalArgumentException("This project is not awaiting customer review.");
        }
        String message = text(request.message(), !request.confirmed());
        project.reviewCompletion(request.confirmed(), customer.getId());
        if (request.confirmed()) {
            project.changeStatus(ProjectStatus.COMPLETED, 100, project.getActualStartDate(), LocalDate.now());
        }
        save(project, customer, request.confirmed() ? ProjectActivity.Kind.COMPLETION_CONFIRMED : ProjectActivity.Kind.ISSUE_REPORTED,
                message.isBlank() ? "Customer confirmed the completed work." : message);
        notifications.notifyRole(Role.CARPENTER, NotificationType.PROJECT,
                request.confirmed() ? "Customer confirmed completion" : "Customer reported a project issue",
                preview(project.getProjectNumber() + (request.confirmed() ? " has been confirmed." : ": " + message)), "/admin/projects");
        return ProjectResponse.from(project);
    }

    private ProjectActivity save(Project project, User actor, ProjectActivity.Kind kind, String message) {
        ProjectActivity entry = activity.save(new ProjectActivity(project, actor, kind, message));
        audit.record(actor, AuditAction.CREATED, "Project", project.getId(), kind.name().replace('_', ' '));
        return entry;
    }
    private void notifyOther(Project project, User actor, String title, String message) {
        message = preview(message);
        if (actor.getRole() == Role.CUSTOMER) notifications.notifyRole(Role.CARPENTER, NotificationType.PROJECT, title, message, "/admin/projects");
        else notifications.notifyUser(project.getCustomer().getUser(), NotificationType.PROJECT, title, message, customerLink(project));
    }
    private String preview(String message) { return message.length() <= 1000 ? message : message.substring(0, 997) + "..."; }
    private String customerLink(Project project) { return "/customer/projects?projectId=" + project.getId(); }
    private Project find(UUID id, boolean lock) {
        return (lock ? projects.findLockedById(id) : projects.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }
    private Project authorize(Project project, User actor) {
        if (actor.getRole() != Role.CARPENTER && !project.getCustomer().getUser().getId().equals(actor.getId())) {
            throw new UnauthorisedOperationException("You cannot access another customer's project.");
        }
        return project;
    }
    private void requireOpen(Project project) {
        if (project.getStatus() == ProjectStatus.CANCELLED) throw new IllegalArgumentException("Cancelled projects cannot receive new activity.");
    }
    private String text(String value, boolean required) {
        String cleaned = value == null ? "" : value.trim();
        if (required && cleaned.isBlank()) throw new IllegalArgumentException("A message is required.");
        if (cleaned.length() > 2000) throw new IllegalArgumentException("Messages must not exceed 2000 characters.");
        return cleaned;
    }
    private String validatePhoto(byte[] bytes) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("The uploaded file is not a valid JPEG or PNG photo.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("JPEG") && !format.equalsIgnoreCase("PNG")) throw new IllegalArgumentException("Only JPEG and PNG photos are supported.");
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > 25_000_000) throw new IllegalArgumentException("Photo dimensions are too large.");
                if (reader.read(0) == null) throw new IllegalArgumentException("The photo could not be decoded.");
                return format.equalsIgnoreCase("PNG") ? "image/png" : "image/jpeg";
            } finally { reader.dispose(); }
        } catch (IOException ex) { throw new IllegalArgumentException("The photo could not be decoded.", ex); }
    }
}
