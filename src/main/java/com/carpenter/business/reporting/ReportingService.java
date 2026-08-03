package com.carpenter.business.reporting;

import com.carpenter.business.expense.Expense;
import com.carpenter.business.expense.ExpenseRepository;
import com.carpenter.business.expense.ExpenseStatus;
import com.carpenter.business.inventory.Material;
import com.carpenter.business.inventory.MaterialRepository;
import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.invoice.InvoiceRepository;
import com.carpenter.business.invoice.InvoiceStatus;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.payment.Payment;
import com.carpenter.business.payment.PaymentRepository;
import com.carpenter.business.payment.PaymentStatus;
import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.reporting.dto.FinancialReportResponse;
import com.carpenter.business.reporting.dto.InventoryReportResponse;
import com.carpenter.business.reporting.dto.LowStockMaterialResponse;
import com.carpenter.business.reporting.dto.ReportingOverviewResponse;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingService {
    private final OrderRepository orderRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final MaterialRepository materialRepository;
    private final CurrentUser currentUser;

    public ReportingService(OrderRepository orderRepository, ProjectRepository projectRepository,
                            InvoiceRepository invoiceRepository, PaymentRepository paymentRepository,
                            ExpenseRepository expenseRepository, MaterialRepository materialRepository,
                            CurrentUser currentUser) {
        this.orderRepository = orderRepository;
        this.projectRepository = projectRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.materialRepository = materialRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public ReportingOverviewResponse overview(Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        List<Order> orders = orderRepository.findAll();
        List<Project> projects = projectRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();
        List<Material> materials = materialRepository.findAll();

        BigDecimal acceptedOrderValue = sum(orders, Order::getAcceptedTotal);
        BigDecimal invoicedTotal = sum(invoices.stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.CANCELLED)
                .toList(), Invoice::getTotalAmount);
        BigDecimal paidTotal = sum(payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.APPROVED)
                .toList(), Payment::getAmount);
        BigDecimal receivablesTotal = sum(invoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.ISSUED
                        || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID)
                .toList(), Invoice::getBalanceDue);
        BigDecimal approvedExpenseTotal = sum(expenses.stream()
                .filter(expense -> expense.getStatus() == ExpenseStatus.APPROVED
                        || expense.getStatus() == ExpenseStatus.REIMBURSED)
                .toList(), Expense::getAmount);

        BigDecimal stockValue = sum(materials, material -> material.getStockQuantity().multiply(material.getUnitCost()));
        List<LowStockMaterialResponse> lowStockMaterials = materials.stream()
                .filter(Material::isLowStock)
                .sorted(Comparator.comparing(Material::getCode))
                .limit(10)
                .map(material -> new LowStockMaterialResponse(material.getId(), material.getCode(), material.getName(),
                        material.getStockQuantity(), material.getReorderLevel()))
                .toList();

        return new ReportingOverviewResponse(
                new FinancialReportResponse(acceptedOrderValue, invoicedTotal, paidTotal, receivablesTotal,
                        approvedExpenseTotal, paidTotal.subtract(approvedExpenseTotal).setScale(2, RoundingMode.HALF_UP)),
                countBy(orders, order -> order.getStatus().name()),
                countBy(projects, project -> project.getStatus().name()),
                countBy(invoices, invoice -> invoice.getStatus().name()),
                countBy(payments, payment -> payment.getStatus().name()),
                countBy(expenses, expense -> expense.getCategory().name()),
                new InventoryReportResponse(materials.size(), lowStockMaterials.size(), stockValue.setScale(2, RoundingMode.HALF_UP)),
                lowStockMaterials);
    }

    private <T> BigDecimal sum(Collection<T> values, Function<T, BigDecimal> mapper) {
        return values.stream().map(mapper).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private <T> Map<String, Long> countBy(Collection<T> values, Function<T, String> classifier) {
        Map<String, Long> counts = new LinkedHashMap<>();
        values.forEach(value -> counts.merge(classifier.apply(value), 1L, Long::sum));
        return counts;
    }
}
