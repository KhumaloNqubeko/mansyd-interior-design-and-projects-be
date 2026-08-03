package com.carpenter.business.invoice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.invoice.dto.InvoiceUpdateRequest;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
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
class InvoiceServiceTest {
    @Mock InvoiceRepository invoices;
    @Mock OrderRepository orders;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private InvoiceService service;

    @BeforeEach
    void setUp() {
        service = new InvoiceService(invoices, orders, currentUser);
    }

    @Test
    void invoiceWithApprovedPaymentCannotBeCancelled() {
        Invoice invoice = invoice();
        invoice.issue(LocalDate.now());
        invoice.applyPayment(new BigDecimal("100.00"));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(invoices.findById(invoice.getId())).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.cancel(invoice.getId(), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void draftInvoiceTotalCannotBeReducedBelowPaidAmount() {
        Invoice invoice = invoice();
        invoice.applyPayment(new BigDecimal("250.00"));
        ReflectionTestUtils.setField(invoice, "status", InvoiceStatus.DRAFT);
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(invoices.findById(invoice.getId())).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.update(invoice.getId(),
                new InvoiceUpdateRequest(LocalDate.now().plusDays(7), new BigDecimal("100.00"), ""), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    private Invoice invoice() {
        Invoice invoice = new Invoice("INV-TEST", order(), LocalDate.now().plusDays(14), new BigDecimal("500.00"), "");
        ReflectionTestUtils.setField(invoice, "id", UUID.randomUUID());
        return invoice;
    }

    private Order order() {
        User user = user(Role.CUSTOMER);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        Quotation quotation = new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
        return new Order("ORD-TEST", quotation);
    }

    private User user(Role role) {
        User user = new User(role.name().toLowerCase() + "@example.com", "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
