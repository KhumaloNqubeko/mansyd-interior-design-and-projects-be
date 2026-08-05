package com.carpenter.business.document;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.audit.AuditAction;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.document.dto.DocumentRequest;
import com.carpenter.business.document.dto.DocumentResponse;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.invoice.InvoiceRepository;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.notification.NotificationType;
import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.servicerequest.ServiceRequestRepository;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public DocumentService(DocumentRepository documentRepository, CustomerRepository customerRepository,
                           ServiceRequestRepository serviceRequestRepository, ProjectRepository projectRepository,
                           InvoiceRepository invoiceRepository, CurrentUser currentUser,
                           NotificationService notificationService, AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.customerRepository = customerRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.projectRepository = projectRepository;
        this.invoiceRepository = invoiceRepository;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Customer customer = customer(request.customerId());
        ServiceRequest serviceRequest = serviceRequest(request.serviceRequestId());
        Project project = project(request.projectId());
        Invoice invoice = invoice(request.invoiceId());
        validateRelationships(customer, serviceRequest, project, invoice);
        Document document = documentRepository.save(new Document(customer, serviceRequest, project, invoice, user,
                trim(request.title()), request.type(), trim(request.fileName()), trim(request.contentType()),
                request.fileSizeBytes(), trim(request.storageUrl()), clean(request.notes()), request.customerVisible()));
        auditLogService.record(user, AuditAction.CREATED, "Document", document.getId(),
                "Created document " + document.getTitle());
        notifyIfVisible(document, "New document available");
        return DocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(documentRepository.findAll(pageable).map(DocumentResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> my(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(documentRepository
                .findByCustomerUserIdAndCustomerVisibleTrueAndStatus(user.getId(), DocumentStatus.ACTIVE, pageable)
                .map(DocumentResponse::from));
    }

    @Transactional
    public DocumentResponse update(UUID id, DocumentRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Document document = document(id);
        if (document.getStatus() == DocumentStatus.ARCHIVED) {
            throw new UnauthorisedOperationException("Archived documents cannot be edited.");
        }
        Customer customer = customer(request.customerId());
        ServiceRequest serviceRequest = serviceRequest(request.serviceRequestId());
        Project project = project(request.projectId());
        Invoice invoice = invoice(request.invoiceId());
        validateRelationships(customer, serviceRequest, project, invoice);
        boolean newlyVisible = !document.isCustomerVisible() && request.customerVisible();
        document.update(customer, serviceRequest, project, invoice, trim(request.title()), request.type(),
                trim(request.fileName()), trim(request.contentType()), request.fileSizeBytes(),
                trim(request.storageUrl()), clean(request.notes()), request.customerVisible());
        auditLogService.record(user, AuditAction.UPDATED, "Document", document.getId(),
                "Updated document " + document.getTitle());
        if (newlyVisible) notifyIfVisible(document, "Document shared with you");
        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse archive(UUID id, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        Document document = document(id);
        document.archive();
        auditLogService.record(user, AuditAction.ARCHIVED, "Document", document.getId(),
                "Archived document " + document.getTitle());
        return DocumentResponse.from(document);
    }

    private void validateRelationships(Customer customer, ServiceRequest serviceRequest, Project project, Invoice invoice) {
        if (serviceRequest != null && !serviceRequest.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorisedOperationException("Service request belongs to a different customer.");
        }
        if (project != null && !project.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorisedOperationException("Project belongs to a different customer.");
        }
        if (invoice != null && !invoice.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorisedOperationException("Invoice belongs to a different customer.");
        }
    }

    private void notifyIfVisible(Document document, String title) {
        if (document.isCustomerVisible() && document.getStatus() == DocumentStatus.ACTIVE) {
            notificationService.notifyUser(document.getCustomer().getUser(), NotificationType.GENERAL, title,
                    document.getTitle() + " is available in your documents.", "/customer/documents");
        }
    }

    private Document document(UUID id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document was not found."));
    }

    private Customer customer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer was not found."));
    }

    private ServiceRequest serviceRequest(UUID id) {
        return id == null ? null : serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request was not found."));
    }

    private Project project(UUID id) {
        return id == null ? null : projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }

    private Invoice invoice(UUID id) {
        return id == null ? null : invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice was not found."));
    }

    private String trim(String value) { return value.trim(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
