package com.carpenter.business.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.document.dto.DocumentRequest;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.invoice.InvoiceRepository;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.servicerequest.ServiceRequestRepository;
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
class DocumentServiceTest {
    @Mock DocumentRepository documents;
    @Mock CustomerRepository customers;
    @Mock ServiceRequestRepository serviceRequests;
    @Mock ProjectRepository projects;
    @Mock InvoiceRepository invoices;
    @Mock CurrentUser currentUser;
    @Mock NotificationService notificationService;
    @Mock AuditLogService auditLogService;
    @Mock Authentication authentication;
    private DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService(documents, customers, serviceRequests, projects, invoices, currentUser,
                notificationService, auditLogService);
    }

    @Test
    void linkedServiceRequestMustBelongToSelectedCustomer() {
        User carpenter = user(Role.CARPENTER, "carpenter@example.com");
        Customer customer = customer(user(Role.CUSTOMER, "one@example.com"));
        Customer other = customer(user(Role.CUSTOMER, "two@example.com"));
        ServiceRequest request = new ServiceRequest(other, "Other", "Other", "Email", "2 Main");
        ReflectionTestUtils.setField(request, "id", UUID.randomUUID());
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);
        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(serviceRequests.findById(request.getId())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.create(request(customer.getId(), request.getId(), true), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void carpenterCanArchiveDocument() {
        Document document = document();
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(user(Role.CARPENTER, "carpenter@example.com"));
        when(documents.findById(document.getId())).thenReturn(Optional.of(document));

        assertThat(service.archive(document.getId(), authentication).status()).isEqualTo(DocumentStatus.ARCHIVED);
    }

    @Test
    void visibleDocumentCanBeCreated() {
        User carpenter = user(Role.CARPENTER, "carpenter@example.com");
        Customer customer = customer(user(Role.CUSTOMER, "customer@example.com"));
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);
        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(documents.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(request(customer.getId(), null, true), authentication).customerVisible()).isTrue();
    }

    private Document document() {
        Document document = new Document(customer(user(Role.CUSTOMER, "customer@example.com")), null, null, null,
                user(Role.CARPENTER, "carpenter@example.com"), "Design", DocumentType.DESIGN, "design.pdf",
                "application/pdf", 123L, "https://files.example/design.pdf", "", true);
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());
        return document;
    }

    private DocumentRequest request(UUID customerId, UUID serviceRequestId, boolean visible) {
        return new DocumentRequest("Design", DocumentType.DESIGN, "design.pdf", "application/pdf", 123L,
                "https://files.example/design.pdf", "", visible, customerId, serviceRequestId, null, null);
    }

    private Customer customer(User user) {
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        return customer;
    }

    private User user(Role role, String email) {
        User user = new User(email, "hash", role, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
