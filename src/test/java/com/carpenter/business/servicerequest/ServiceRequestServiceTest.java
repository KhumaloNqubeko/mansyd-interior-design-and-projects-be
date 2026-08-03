package com.carpenter.business.servicerequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.dto.ServiceRequestCreateRequest;
import com.carpenter.business.servicerequest.dto.ServiceRequestStatusUpdateRequest;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
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
class ServiceRequestServiceTest {
    @Mock ServiceRequestRepository requests;
    @Mock CustomerRepository customers;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private ServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new ServiceRequestService(requests, customers, currentUser);
    }

    @Test
    void customerCanCreateServiceRequest() {
        User user = user(Role.CUSTOMER);
        Customer customer = customer(user);
        when(currentUser.requireRole(authentication, Role.CUSTOMER)).thenReturn(user);
        when(customers.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(requests.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new ServiceRequestCreateRequest(" Cupboards ", " Bedroom storage ",
                " Email ", " 1 Main Road "), authentication);

        assertThat(response.title()).isEqualTo("Cupboards");
        assertThat(response.status()).isEqualTo(ServiceRequestStatus.SUBMITTED);
    }

    @Test
    void carpenterCanAdvanceSubmittedRequestStatus() {
        User carpenter = user(Role.CARPENTER);
        ServiceRequest request = new ServiceRequest(customer(user(Role.CUSTOMER)), "Cupboards",
                "Bedroom storage", "Email", "1 Main Road");
        UUID requestId = UUID.randomUUID();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);
        when(requests.findById(requestId)).thenReturn(Optional.of(request));

        var response = service.updateStatus(requestId,
                new ServiceRequestStatusUpdateRequest(ServiceRequestStatus.UNDER_REVIEW), authentication);

        assertThat(response.status()).isEqualTo(ServiceRequestStatus.UNDER_REVIEW);
    }

    @Test
    void invalidStatusTransitionIsRejected() {
        ServiceRequest request = new ServiceRequest(customer(user(Role.CUSTOMER)), "Cupboards",
                "Bedroom storage", "Email", "1 Main Road");
        request.changeStatus(ServiceRequestStatus.CLOSED);
        UUID requestId = UUID.randomUUID();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(requests.findById(requestId)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.updateStatus(requestId,
                new ServiceRequestStatusUpdateRequest(ServiceRequestStatus.UNDER_REVIEW), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    private User user(Role role) {
        User user = new User(role.name().toLowerCase() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private Customer customer(User user) {
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main Road", null,
                "Johannesburg", "2000");
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        return customer;
    }
}
