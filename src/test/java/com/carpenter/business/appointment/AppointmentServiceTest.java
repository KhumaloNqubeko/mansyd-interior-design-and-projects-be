package com.carpenter.business.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carpenter.business.appointment.dto.AppointmentRequest;
import com.carpenter.business.appointment.dto.AppointmentStatusUpdateRequest;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequestRepository;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.time.LocalDateTime;
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
class AppointmentServiceTest {
    @Mock AppointmentRepository appointments;
    @Mock CustomerRepository customers;
    @Mock ServiceRequestRepository serviceRequests;
    @Mock ProjectRepository projects;
    @Mock CurrentUser currentUser;
    @Mock NotificationService notificationService;
    @Mock AuditLogService auditLogService;
    @Mock Authentication authentication;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(appointments, customers, serviceRequests, projects, currentUser,
                notificationService, auditLogService);
    }

    @Test
    void appointmentEndMustBeAfterStart() {
        Customer customer = customer(user(Role.CUSTOMER));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));

        assertThatThrownBy(() -> service.create(request(customer.getId(), LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1)), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void scheduledAppointmentCanBeConfirmed() {
        Appointment appointment = appointment();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(appointments.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        assertThat(service.updateStatus(appointment.getId(),
                new AppointmentStatusUpdateRequest(AppointmentStatus.CONFIRMED), authentication).status())
                .isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void completedAppointmentCannotBeCancelled() {
        Appointment appointment = appointment();
        appointment.changeStatus(AppointmentStatus.CONFIRMED);
        appointment.changeStatus(AppointmentStatus.COMPLETED);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(appointments.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.updateStatus(appointment.getId(),
                new AppointmentStatusUpdateRequest(AppointmentStatus.CANCELLED), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    private Appointment appointment() {
        Customer customer = customer(user(Role.CUSTOMER));
        Appointment appointment = new Appointment(customer, null, null, user(Role.CARPENTER), "Site visit",
                AppointmentType.SITE_VISIT, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1), "1 Main", "");
        ReflectionTestUtils.setField(appointment, "id", UUID.randomUUID());
        return appointment;
    }

    private AppointmentRequest request(UUID customerId, LocalDateTime start, LocalDateTime end) {
        return new AppointmentRequest("Site visit", AppointmentType.SITE_VISIT, start, end,
                "1 Main", "", customerId, null, null);
    }

    private Customer customer(User user) {
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        return customer;
    }

    private User user(Role role) {
        User user = new User(role.name().toLowerCase() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
