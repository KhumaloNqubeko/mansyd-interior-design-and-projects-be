package com.carpenter.business.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.expense.Expense;
import com.carpenter.business.expense.ExpenseCategory;
import com.carpenter.business.expense.ExpenseRepository;
import com.carpenter.business.inventory.Material;
import com.carpenter.business.inventory.MaterialRepository;
import com.carpenter.business.inventory.UnitOfMeasure;
import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.invoice.InvoiceRepository;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.payment.Payment;
import com.carpenter.business.payment.PaymentRepository;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.reporting.dto.ReportingOverviewResponse;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {
    @Mock OrderRepository orders;
    @Mock ProjectRepository projects;
    @Mock InvoiceRepository invoices;
    @Mock PaymentRepository payments;
    @Mock ExpenseRepository expenses;
    @Mock MaterialRepository materials;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private ReportingService service;

    @BeforeEach
    void setUp() {
        service = new ReportingService(orders, projects, invoices, payments, expenses, materials, currentUser);
    }

    @Test
    void overviewAggregatesFinancialAndInventoryMetrics() {
        User carpenter = new User("carpenter@example.com", "hash", Role.CARPENTER, AccountStatus.ACTIVE);
        Order order = order("1000.00");
        Invoice invoice = new Invoice("INV-1", order, LocalDate.now().plusDays(10), new BigDecimal("800.00"), "");
        invoice.issue(LocalDate.now());
        invoice.applyPayment(new BigDecimal("300.00"));
        Payment payment = new Payment(invoice, new BigDecimal("300.00"), LocalDate.now(), "POP-1", "");
        payment.approve();
        Expense expense = new Expense("Labour", ExpenseCategory.LABOUR, new BigDecimal("125.00"),
                LocalDate.now(), "RCPT-1", "", null, null, null, carpenter);
        expense.approve();
        Material material = new Material("PLY", "Plywood", UnitOfMeasure.SHEET, new BigDecimal("50.00"),
                new BigDecimal("4.000"), null);
        material.applyQuantityChange(new BigDecimal("3.000"));

        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);
        when(orders.findAll()).thenReturn(List.of(order));
        when(projects.findAll()).thenReturn(List.of());
        when(invoices.findAll()).thenReturn(List.of(invoice));
        when(payments.findAll()).thenReturn(List.of(payment));
        when(expenses.findAll()).thenReturn(List.of(expense));
        when(materials.findAll()).thenReturn(List.of(material));

        ReportingOverviewResponse overview = service.overview(authentication);

        assertThat(overview.financial().acceptedOrderValue()).isEqualByComparingTo("1000.00");
        assertThat(overview.financial().paidTotal()).isEqualByComparingTo("300.00");
        assertThat(overview.financial().approvedExpenseTotal()).isEqualByComparingTo("125.00");
        assertThat(overview.financial().netCashPosition()).isEqualByComparingTo("175.00");
        assertThat(overview.inventory().stockValue()).isEqualByComparingTo("150.00");
        assertThat(overview.inventory().lowStockCount()).isEqualTo(1);
        assertThat(overview.ordersByStatus()).containsEntry("CREATED", 1L);
        assertThat(overview.expensesByCategory()).containsEntry("LABOUR", 1L);
    }

    private Order order(String total) {
        User user = new User("customer@example.com", "hash", Role.CUSTOMER, AccountStatus.ACTIVE);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        Quotation quotation = new Quotation("QUO-1", request, LocalDate.now().plusDays(7), "");
        quotation.updateTotals(new BigDecimal(total), BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2), new BigDecimal(total));
        return new Order("ORD-1", quotation);
    }
}
