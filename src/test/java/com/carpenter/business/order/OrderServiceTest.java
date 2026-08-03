package com.carpenter.business.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.project.ProjectService;
import com.carpenter.business.quotation.Quotation;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orders;
    @Mock ProjectService projects;
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, null, projects);
    }

    @Test
    void getOrCreateForQuotationReturnsExistingOrder() {
        Quotation quotation = quotation();
        Order existing = new Order("ORD-EXIST", quotation);
        when(orders.findByQuotationId(quotation.getId())).thenReturn(Optional.of(existing));

        Order result = service.getOrCreateForQuotation(quotation);

        assertThat(result).isSameAs(existing);
        verify(orders, never()).save(org.mockito.ArgumentMatchers.any());
        verify(projects).getOrCreateForOrder(existing);
    }

    private Quotation quotation() {
        User user = new User("customer@example.com", "hash", Role.CUSTOMER, AccountStatus.ACTIVE);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        Quotation quotation = new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
        ReflectionTestUtils.setField(quotation, "id", UUID.randomUUID());
        return quotation;
    }
}
