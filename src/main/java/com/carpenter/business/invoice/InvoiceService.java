package com.carpenter.business.invoice;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.invoice.dto.InvoiceRequest;
import com.carpenter.business.invoice.dto.InvoiceResponse;
import com.carpenter.business.invoice.dto.InvoiceUpdateRequest;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderRepository orderRepository, CurrentUser currentUser) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        if (invoiceRepository.existsByOrderId(request.orderId())) {
            throw new DuplicateResourceException("An invoice already exists for this order.");
        }
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found."));
        Invoice invoice = invoiceRepository.save(new Invoice(generateInvoiceNumber(), order, request.dueDate(),
                money(request.totalAmount()), clean(request.notes())));
        return InvoiceResponse.from(invoice);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(invoiceRepository.findAll(pageable).map(InvoiceResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> myInvoices(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(invoiceRepository.findByCustomerUserId(user.getId(), pageable).map(InvoiceResponse::from));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(UUID id, Authentication authentication) {
        Invoice invoice = findAndAuthorize(id, authentication);
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse update(UUID id, InvoiceUpdateRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Invoice invoice = requireDraft(find(id));
        invoice.update(request.dueDate(), money(request.totalAmount()), clean(request.notes()));
        if (invoice.getBalanceDue().signum() < 0) {
            throw new UnauthorisedOperationException("Invoice total cannot be less than approved payments.");
        }
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse issue(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Invoice invoice = requireDraft(find(id));
        invoice.issue(LocalDate.now());
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse cancel(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Invoice invoice = find(id);
        if (invoice.getPaidAmount().signum() > 0) {
            throw new UnauthorisedOperationException("Invoices with approved payments cannot be cancelled.");
        }
        invoice.cancel();
        return InvoiceResponse.from(invoice);
    }

    public Invoice findIssuedCustomerInvoice(UUID id, User user) {
        Invoice invoice = invoiceRepository.findByIdAndCustomerUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice was not found."));
        if (invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new UnauthorisedOperationException("Payments can only be submitted against issued invoices.");
        }
        return invoice;
    }

    private Invoice findAndAuthorize(UUID id, Authentication authentication) {
        Invoice invoice = find(id);
        User user = currentUser.require(authentication);
        if (user.getRole() != Role.CARPENTER && !invoice.getCustomer().getUser().getId().equals(user.getId())) {
            throw new UnauthorisedOperationException("You cannot access another customer's invoice.");
        }
        return invoice;
    }

    private Invoice find(UUID id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice was not found."));
    }

    private Invoice requireDraft(Invoice invoice) {
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new UnauthorisedOperationException("Only draft invoices can be edited.");
        }
        return invoice;
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private String generateInvoiceNumber() { return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
