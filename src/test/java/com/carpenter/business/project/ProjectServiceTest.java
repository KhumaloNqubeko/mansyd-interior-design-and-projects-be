package com.carpenter.business.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.order.Order;
import com.carpenter.business.project.dto.ProjectStatusUpdateRequest;
import com.carpenter.business.project.dto.ProjectTimelineRequest;
import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock ProjectRepository projects;
    @Mock ProjectUpdateRepository updates;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    @Mock com.carpenter.business.notification.NotificationService notifications;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(projects, updates, null, currentUser, notifications);
    }

    @Test
    void createsProjectForOrderOnce() {
        Order order = order();
        when(projects.findByOrderId(order.getId())).thenReturn(Optional.empty());
        when(projects.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project project = service.getOrCreateForOrder(order);

        assertThat(project.getOrder()).isSameAs(order);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.CREATED);
        assertThat(project.getProgress()).isZero();
    }

    @Test
    void projectCannotSkipInvalidStatusTransitions() {
        Project project = project();
        project.changeStatus(ProjectStatus.SCHEDULED, 10, null, null);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(projects.findLockedById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> service.updateStatus(project.getId(),
                new ProjectStatusUpdateRequest(ProjectStatus.COMPLETED), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void statusDeterminesProgressAndCapturesActualStartDate() {
        Project project = project();
        project.changeStatus(ProjectStatus.SCHEDULED, 10, null, null);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(projects.findLockedById(project.getId())).thenReturn(Optional.of(project));

        var response = service.updateStatus(project.getId(), new ProjectStatusUpdateRequest(ProjectStatus.IN_PROGRESS), authentication);

        assertThat(response.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(response.progress()).isEqualTo(25);
        assertThat(response.actualStartDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void carpenterCanAddTimelineUpdate() {
        Project project = project();
        User carpenter = user(Role.CARPENTER);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);
        when(currentUser.require(authentication)).thenReturn(carpenter);
        when(projects.findById(project.getId())).thenReturn(Optional.of(project));
        when(updates.save(any(ProjectUpdate.class))).thenAnswer(invocation -> {
            ProjectUpdate update = invocation.getArgument(0);
            ReflectionTestUtils.setField(update, "id", UUID.randomUUID());
            return update;
        });

        var response = service.addUpdate(project.getId(), new ProjectTimelineRequest("Started", "Work has begun."), authentication);

        assertThat(response.title()).isEqualTo("Started");
        assertThat(response.createdByRole()).isEqualTo(Role.CARPENTER);
    }

    private Project project() {
        Project project = new Project("PRJ-TEST", order());
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        return project;
    }

    private Order order() {
        User user = user(Role.CUSTOMER);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        Quotation quotation = new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
        ReflectionTestUtils.setField(quotation, "id", UUID.randomUUID());
        Order order = new Order("ORD-TEST", quotation);
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        return order;
    }

    private User user(Role role) {
        User user = new User(role.name().toLowerCase() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
