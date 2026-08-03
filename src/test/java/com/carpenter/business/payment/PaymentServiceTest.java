package com.carpenter.business.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.invoice.InvoiceService;
import com.carpenter.business.order.Order;
import com.carpenter.business.payment.dto.PaymentDecisionRequest;
import com.carpenter.business.payment.dto.PaymentRequest;
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
class PaymentServiceTest {
    @Mock PaymentRepository payments;
    @Mock InvoiceService invoices;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, invoices, currentUser);
    }

    @Test
    void customerPaymentCannotExceedInvoiceBalance() {
        User customer = user(Role.CUSTOMER);
        Invoice invoice = invoice();
        invoice.issue(LocalDate.now());
        when(currentUser.requireRole(authentication, Role.CUSTOMER)).thenReturn(customer);
        when(invoices.findIssuedCustomerInvoice(invoice.getId(), customer)).thenReturn(invoice);

        assertThatThrownBy(() -> service.submit(new PaymentRequest(invoice.getId(), new BigDecimal("600.00"),
                LocalDate.now(), "POP-1", ""), authentication)).isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void approvingPaymentUpdatesInvoiceBalance() {
        Payment payment = payment(new BigDecimal("200.00"));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(payments.findById(payment.getId())).thenReturn(Optional.of(payment));

        var response = service.approve(payment.getId(), authentication);

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getInvoice().getPaidAmount()).isEqualByComparingTo("200.00");
        assertThat(payment.getInvoice().getBalanceDue()).isEqualByComparingTo("300.00");
    }

    @Test
    void rejectedPaymentDoesNotChangeInvoiceBalance() {
        Payment payment = payment(new BigDecimal("200.00"));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER));
        when(payments.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.reject(payment.getId(), new PaymentDecisionRequest("Wrong reference"), authentication);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getInvoice().getPaidAmount()).isEqualByComparingTo("0.00");
    }

    private Payment payment(BigDecimal amount) {
        Invoice invoice = invoice();
        invoice.issue(LocalDate.now());
        Payment payment = new Payment(invoice, amount, LocalDate.now(), "POP-1", "");
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
        return payment;
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
