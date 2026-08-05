package com.carpenter.business.quotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.order.OrderService;
import com.carpenter.business.quotation.dto.QuotationItemRequest;
import com.carpenter.business.quotation.dto.QuotationRejectRequest;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.servicerequest.ServiceRequestRepository;
import com.carpenter.business.servicerequest.ServiceRequestStatus;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
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
class QuotationServiceTest {
    @Mock QuotationRepository quotations;
    @Mock QuotationItemRepository items;
    @Mock ServiceRequestRepository serviceRequests;
    @Mock OrderService orderService;
    @Mock OrderRepository orders;
    @Mock CurrentUser currentUser;
    @Mock AuditLogService auditLogService;
    @Mock Authentication authentication;
    private QuotationService service;

    @BeforeEach
    void setUp() {
        service = new QuotationService(quotations, items, serviceRequests, new QuotationCalculationService(),
                orderService, orders, currentUser, auditLogService);
    }

    @Test
    void draftQuotationRequiresItemsBeforeSubmission() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        UUID id = quotation.getId();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(quotations.findById(id)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.submit(id, authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void acceptsPendingQuotationAndCreatesOrder() {
        Quotation quotation = quotation(QuotationStatus.PENDING_CUSTOMER);
        quotation.addItem(new QuotationItem(quotation, QuotationItemType.LABOUR, "Install",
                BigDecimal.ONE, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO));
        new QuotationCalculationService().recalculate(quotation);
        Order order = new Order("ORD-TEST", quotation);
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        when(currentUser.requireRole(authentication, Role.CUSTOMER)).thenReturn(quotation.getCustomer().getUser());
        when(quotations.findByIdAndCustomerUserId(quotation.getId(), quotation.getCustomer().getUser().getId()))
                .thenReturn(Optional.of(quotation));
        when(orderService.getOrCreateForQuotation(quotation)).thenReturn(order);

        var response = service.accept(quotation.getId(), authentication);

        assertThat(response.status()).isEqualTo(QuotationStatus.ACCEPTED);
        assertThat(response.orderId()).isEqualTo(order.getId());
    }

    @Test
    void addItemRecalculatesTotals() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(quotations.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        var response = service.addItem(quotation.getId(), new QuotationItemRequest(QuotationItemType.MATERIAL,
                "Boards", new BigDecimal("2"), new BigDecimal("100.00"), new BigDecimal("10.00"),
                new BigDecimal("15.00")), authentication);

        assertThat(response.total()).isEqualByComparingTo("218.50");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void customerRejectsPendingQuotationWithNotes() {
        Quotation quotation = quotation(QuotationStatus.PENDING_CUSTOMER);
        when(currentUser.requireRole(authentication, Role.CUSTOMER)).thenReturn(quotation.getCustomer().getUser());
        when(quotations.findByIdAndCustomerUserId(quotation.getId(), quotation.getCustomer().getUser().getId()))
                .thenReturn(Optional.of(quotation));

        var response = service.reject(quotation.getId(), new QuotationRejectRequest("Too expensive"), authentication);

        assertThat(response.status()).isEqualTo(QuotationStatus.REJECTED);
        assertThat(response.rejectionNotes()).isEqualTo("Too expensive");
    }

    @Test
    void rejectedQuotationCanBeAdjustedAndSubmittedAgain() {
        Quotation quotation = quotation(QuotationStatus.REJECTED);
        quotation.addItem(new QuotationItem(quotation, QuotationItemType.LABOUR, "Install",
                BigDecimal.ONE, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(quotations.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        var updated = service.addItem(quotation.getId(), new QuotationItemRequest(QuotationItemType.MATERIAL,
                "Boards", BigDecimal.ONE, new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO), authentication);
        var submitted = service.submit(quotation.getId(), authentication);

        assertThat(updated.items()).hasSize(2);
        assertThat(submitted.status()).isEqualTo(QuotationStatus.PENDING_CUSTOMER);
    }

    @Test
    void draftQuotationCanBeDeletedAndRequestReopened() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        quotation.getServiceRequest().changeStatus(ServiceRequestStatus.QUOTATION_IN_PROGRESS);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(quotations.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        service.delete(quotation.getId(), authentication);

        verify(quotations).delete(quotation);
        assertThat(quotation.getServiceRequest().getStatus()).isEqualTo(ServiceRequestStatus.UNDER_REVIEW);
    }

    @Test
    void submittedQuotationCannotBeDeleted() {
        Quotation quotation = quotation(QuotationStatus.PENDING_CUSTOMER);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(quotations.findById(quotation.getId())).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.delete(quotation.getId(), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    private Quotation quotation(QuotationStatus status) {
        User user = user(Role.CUSTOMER);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        ReflectionTestUtils.setField(request, "id", UUID.randomUUID());
        Quotation quotation = new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
        ReflectionTestUtils.setField(quotation, "id", UUID.randomUUID());
        quotation.changeStatus(status);
        return quotation;
    }

    private User user(Role role) {
        User user = new User(role.name().toLowerCase() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
