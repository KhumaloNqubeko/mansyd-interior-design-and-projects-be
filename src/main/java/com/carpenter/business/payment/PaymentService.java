package com.carpenter.business.payment;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.audit.AuditAction;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.invoice.InvoiceService;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.notification.NotificationType;
import com.carpenter.business.payment.dto.PaymentDecisionRequest;
import com.carpenter.business.payment.dto.PaymentRequest;
import com.carpenter.business.payment.dto.PaymentResponse;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public PaymentService(PaymentRepository paymentRepository, InvoiceService invoiceService, CurrentUser currentUser,
                          NotificationService notificationService, AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public PaymentResponse submit(PaymentRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        Invoice invoice = invoiceService.findIssuedCustomerInvoice(request.invoiceId(), user);
        BigDecimal amount = money(request.amount());
        if (amount.compareTo(invoice.getBalanceDue()) > 0) {
            throw new UnauthorisedOperationException("Payment cannot exceed the invoice balance.");
        }
        Payment payment = paymentRepository.save(new Payment(invoice, amount, request.paymentDate(),
                trim(request.proofReference()), clean(request.notes())));
        auditLogService.record(user, AuditAction.SUBMITTED, "Payment", payment.getId(),
                "Submitted payment for " + invoice.getInvoiceNumber());
        notificationService.notifyRole(Role.CARPENTER, NotificationType.PAYMENT, "Payment submitted",
                user.getEmail() + " submitted a payment for " + invoice.getInvoiceNumber(), "/carpenter/billing");
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(paymentRepository.findAll(pageable).map(PaymentResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> myPayments(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(paymentRepository.findByCustomerUserId(user.getId(), pageable).map(PaymentResponse::from));
    }

    @Transactional
    public PaymentResponse approve(UUID id, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Payment payment = requirePending(find(id));
        if (payment.getAmount().compareTo(payment.getInvoice().getBalanceDue()) > 0) {
            throw new UnauthorisedOperationException("Payment cannot exceed the invoice balance.");
        }
        payment.approve();
        payment.getInvoice().applyPayment(payment.getAmount());
        auditLogService.record(user, AuditAction.APPROVED, "Payment", payment.getId(),
                "Approved payment for " + payment.getInvoice().getInvoiceNumber());
        notificationService.notifyUser(payment.getCustomer().getUser(), NotificationType.PAYMENT, "Payment approved",
                "Your payment for " + payment.getInvoice().getInvoiceNumber() + " was approved.", "/customer/billing");
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse reject(UUID id, PaymentDecisionRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Payment payment = requirePending(find(id));
        payment.reject(clean(request.notes()));
        auditLogService.record(user, AuditAction.REJECTED, "Payment", payment.getId(),
                "Rejected payment for " + payment.getInvoice().getInvoiceNumber());
        notificationService.notifyUser(payment.getCustomer().getUser(), NotificationType.PAYMENT, "Payment rejected",
                "Your payment for " + payment.getInvoice().getInvoiceNumber() + " was rejected.", "/customer/billing");
        return PaymentResponse.from(payment);
    }

    private Payment find(UUID id) {
        return paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment was not found."));
    }

    private Payment requirePending(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING_REVIEW) {
            throw new UnauthorisedOperationException("Only pending payments can be reviewed.");
        }
        return payment;
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private String trim(String value) { return value.trim(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
