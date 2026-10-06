package com.carpenter.business.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.notification.NotificationType;
import com.carpenter.business.order.Order;
import com.carpenter.business.project.dto.CompletionDecisionRequest;
import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.user.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectCollaborationServiceTest {
    @Mock ProjectRepository projects;
    @Mock ProjectActivityRepository activity;
    @Mock CurrentUser currentUser;
    @Mock NotificationService notifications;
    @Mock AuditLogService audit;
    @Mock Authentication auth;
    ProjectCollaborationService service;
    User customer;
    Project project;
    @BeforeEach void setup() {
        service = new ProjectCollaborationService(projects, activity, currentUser, notifications, audit);
        customer = user(Role.CUSTOMER);
        Customer profile = new Customer(customer, "Customer", "+27111111111", "1 Road", null, "Johannesburg", "2000");
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        ServiceRequest request = new ServiceRequest(profile, "Kitchen", "Cabinetry", "Email", "1 Road");
        Quotation quote = new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
        ReflectionTestUtils.setField(quote, "id", UUID.randomUUID());
        Order order = new Order("ORD-TEST", quote);
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        project = new Project("PRJ-TEST", order);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
    }
    private User user(Role role) {
        User user = new User(UUID.randomUUID() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID()); return user;
    }
    private void locked() { when(projects.findLockedById(project.getId())).thenReturn(Optional.of(project)); }
    private void saveActivity() { when(activity.save(any())).thenAnswer(call -> { ProjectActivity entry = call.getArgument(0); ReflectionTestUtils.setField(entry, "id", UUID.randomUUID()); return entry; }); }
    private void pending() { project.changeStatus(ProjectStatus.INSTALLED, 95, LocalDate.now(), null); project.requestCompletionReview(); }

    @Test void cannotCommentOnAnotherCustomersProject() {
        locked(); when(currentUser.require(auth)).thenReturn(user(Role.CUSTOMER));
        assertThatThrownBy(() -> service.comment(project.getId(), "Hello", auth)).isInstanceOf(UnauthorisedOperationException.class);
        verifyNoInteractions(activity, notifications, audit);
    }
    @Test void commentsNotifyOwnersAndPreserveFullText() {
        locked(); saveActivity(); when(currentUser.require(auth)).thenReturn(customer);
        var result = service.comment(project.getId(), "a".repeat(2000), auth);
        assertThat(result.message()).hasSize(2000);
        verify(notifications).notifyRole(eq(Role.CARPENTER), eq(NotificationType.PROJECT), anyString(), argThat(message -> message.length() <= 1000), eq("/admin/projects"));
    }
    @Test void ownerRepliesNotifyTheProjectCustomer() {
        locked(); saveActivity(); when(currentUser.require(auth)).thenReturn(user(Role.CARPENTER));
        service.comment(project.getId(), "We will visit tomorrow.", auth);
        verify(notifications).notifyUser(eq(customer), eq(NotificationType.PROJECT), anyString(), anyString(), contains(project.getId().toString()));
    }
    @Test void cannotReadAnotherCustomersPhoto() {
        when(projects.findById(project.getId())).thenReturn(Optional.of(project)); when(currentUser.require(auth)).thenReturn(user(Role.CUSTOMER));
        assertThatThrownBy(() -> service.photoContent(project.getId(), UUID.randomUUID(), auth)).isInstanceOf(UnauthorisedOperationException.class);
        verifyNoInteractions(activity);
    }
    @Test void photoMustBelongToTheRequestedProject() {
        when(projects.findById(project.getId())).thenReturn(Optional.of(project)); when(currentUser.require(auth)).thenReturn(customer);
        Project other = new Project("PRJ-OTHER", project.getOrder()); ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
        ProjectActivity photo = new ProjectActivity(other, customer, ProjectActivity.Kind.PHOTO, ""); UUID id = UUID.randomUUID(); when(activity.findById(id)).thenReturn(Optional.of(photo));
        assertThatThrownBy(() -> service.photoContent(project.getId(), id, auth)).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void rejectsNonImageBytesEvenWhenMimeTypeSaysImage() {
        locked(); when(currentUser.require(auth)).thenReturn(customer);
        var file = new MockMultipartFile("file", "fake.png", "image/png", "<script>alert(1)</script>".getBytes());
        assertThatThrownBy(() -> service.photo(project.getId(), "", file, auth)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(activity, notifications);
    }
    @Test void storesARealImageWithItsDetectedType() throws Exception {
        locked(); saveActivity(); when(currentUser.require(auth)).thenReturn(customer);
        var bytes = new java.io.ByteArrayOutputStream(); javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", bytes);
        var result = service.photo(project.getId(), "Site photo", new MockMultipartFile("file", "unsafe.exe", "application/octet-stream", bytes.toByteArray()), auth);
        assertThat(result.contentType()).isEqualTo("image/png"); assertThat(result.fileName()).isEqualTo("project-photo.png");
    }
    @Test void ownerCanRequestReviewOnlyAfterInstallation() {
        locked(); when(currentUser.requireRole(auth, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        assertThatThrownBy(() -> service.requestReview(project.getId(), auth)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(activity, notifications);
    }
    @Test void requestingReviewTwiceDoesNotDuplicateNotifications() {
        locked(); saveActivity(); when(currentUser.requireRole(auth, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        project.changeStatus(ProjectStatus.INSTALLED, 95, LocalDate.now(), null);
        UUID review = service.requestReview(project.getId(), auth).completionReviewId();
        assertThat(service.requestReview(project.getId(), auth).completionReviewId()).isEqualTo(review);
        verify(activity, times(1)).save(any()); verify(notifications, times(1)).notifyUser(any(), any(), anyString(), anyString(), anyString());
    }
    @Test void customerConfirmationCompletesProjectAndIsIdempotent() {
        locked(); saveActivity(); pending(); when(currentUser.requireRole(auth, Role.CUSTOMER)).thenReturn(customer);
        var decision = new CompletionDecisionRequest(project.getCompletionReviewId(), true, "Looks good");
        var result = service.decide(project.getId(), decision, auth);
        assertThat(result.status()).isEqualTo(ProjectStatus.COMPLETED); assertThat(result.progress()).isEqualTo(100);
        assertThat(result.customerConfirmedBy()).isEqualTo(customer.getId()); assertThat(result.customerConfirmedAt()).isNotNull();
        service.decide(project.getId(), decision, auth); verify(activity, times(1)).save(any());
    }
    @Test void customerCannotConfirmAnotherCustomersWork() {
        locked(); pending(); when(currentUser.requireRole(auth, Role.CUSTOMER)).thenReturn(user(Role.CUSTOMER));
        assertThatThrownBy(() -> service.decide(project.getId(), new CompletionDecisionRequest(project.getCompletionReviewId(), true, ""), auth)).isInstanceOf(UnauthorisedOperationException.class);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.INSTALLED);
    }
    @Test void rejectsOutdatedReviewId() {
        locked(); pending(); when(currentUser.requireRole(auth, Role.CUSTOMER)).thenReturn(customer);
        assertThatThrownBy(() -> service.decide(project.getId(), new CompletionDecisionRequest(UUID.randomUUID(), true, ""), auth)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(activity, notifications);
    }
    @Test void issueRequiresFeedbackAndLeavesProjectInstalled() {
        locked(); pending(); when(currentUser.requireRole(auth, Role.CUSTOMER)).thenReturn(customer);
        assertThatThrownBy(() -> service.decide(project.getId(), new CompletionDecisionRequest(project.getCompletionReviewId(), false, " "), auth)).isInstanceOf(IllegalArgumentException.class);
        saveActivity(); var result = service.decide(project.getId(), new CompletionDecisionRequest(project.getCompletionReviewId(), false, "Door needs adjustment"), auth);
        assertThat(result.status()).isEqualTo(ProjectStatus.INSTALLED); assertThat(result.completionReviewStatus()).isEqualTo(CompletionReviewStatus.ISSUE_REPORTED);
        assertThat(result.customerConfirmedAt()).isNull();
    }
    @Test void ownerCanRequestNewReviewAfterIssueResolution() {
        locked(); saveActivity(); pending(); project.reviewCompletion(false, customer.getId()); UUID oldId = project.getCompletionReviewId();
        when(currentUser.requireRole(auth, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        var result = service.requestReview(project.getId(), auth); assertThat(result.completionReviewId()).isNotEqualTo(oldId); assertThat(result.completionReviewStatus()).isEqualTo(CompletionReviewStatus.PENDING_REVIEW);
    }
}
