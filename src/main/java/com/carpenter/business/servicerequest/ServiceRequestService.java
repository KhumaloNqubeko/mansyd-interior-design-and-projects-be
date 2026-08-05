package com.carpenter.business.servicerequest;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.audit.AuditAction;
import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.notification.NotificationService;
import com.carpenter.business.notification.NotificationType;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.dto.ServiceRequestCreateRequest;
import com.carpenter.business.servicerequest.dto.ServiceRequestResponse;
import com.carpenter.business.servicerequest.dto.ServiceRequestStatusUpdateRequest;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceRequestService {
    private static final EnumMap<ServiceRequestStatus, Set<ServiceRequestStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ServiceRequestStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.SUBMITTED, EnumSet.of(ServiceRequestStatus.UNDER_REVIEW,
                ServiceRequestStatus.SITE_VISIT_REQUIRED, ServiceRequestStatus.QUOTATION_IN_PROGRESS,
                ServiceRequestStatus.CANCELLED, ServiceRequestStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.UNDER_REVIEW, EnumSet.of(ServiceRequestStatus.SITE_VISIT_REQUIRED,
                ServiceRequestStatus.QUOTATION_IN_PROGRESS, ServiceRequestStatus.CANCELLED, ServiceRequestStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.SITE_VISIT_REQUIRED, EnumSet.of(ServiceRequestStatus.QUOTATION_IN_PROGRESS,
                ServiceRequestStatus.CANCELLED, ServiceRequestStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.QUOTATION_IN_PROGRESS, EnumSet.of(ServiceRequestStatus.QUOTED,
                ServiceRequestStatus.CANCELLED, ServiceRequestStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.QUOTED, EnumSet.of(ServiceRequestStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.CANCELLED, EnumSet.noneOf(ServiceRequestStatus.class));
        ALLOWED_TRANSITIONS.put(ServiceRequestStatus.CLOSED, EnumSet.noneOf(ServiceRequestStatus.class));
    }

    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerRepository customerRepository;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 CustomerRepository customerRepository, CurrentUser currentUser,
                                 NotificationService notificationService, AuditLogService auditLogService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.customerRepository = customerRepository;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ServiceRequestResponse create(ServiceRequestCreateRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found."));
        ServiceRequest saved = serviceRequestRepository.save(new ServiceRequest(customer, trim(request.title()),
                trim(request.description()), trim(request.preferredContactMethod()), trim(request.siteAddress())));
        auditLogService.record(user, AuditAction.CREATED, "ServiceRequest", saved.getId(),
                "Created service request " + saved.getTitle());
        notificationService.notifyRole(Role.CARPENTER, NotificationType.SERVICE_REQUEST, "New service request",
                customer.getFullName() + " submitted " + saved.getTitle(), "/carpenter/requests");
        return ServiceRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> myRequests(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(serviceRequestRepository.findByCustomerUserId(user.getId(), pageable)
                .map(ServiceRequestResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> allRequests(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(serviceRequestRepository.findAll(pageable).map(ServiceRequestResponse::from));
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse get(UUID id, Authentication authentication) {
        ServiceRequest request = find(id);
        requireRequestAccess(request, authentication);
        return ServiceRequestResponse.from(request);
    }

    @Transactional
    public ServiceRequestResponse update(UUID id, ServiceRequestCreateRequest update, Authentication authentication) {
        ServiceRequest request = find(id);
        User user = currentUser.require(authentication);
        if (user.getRole() == Role.CUSTOMER) {
            requireCustomerOwnership(request, user);
            if (request.getStatus() != ServiceRequestStatus.SUBMITTED
                    && request.getStatus() != ServiceRequestStatus.UNDER_REVIEW) {
                throw new UnauthorisedOperationException("This service request can no longer be edited by the customer.");
            }
        } else if (user.getRole() != Role.CARPENTER) {
            throw new UnauthorisedOperationException("You do not have permission to update this service request.");
        }
        request.update(trim(update.title()), trim(update.description()), trim(update.preferredContactMethod()),
                trim(update.siteAddress()));
        return ServiceRequestResponse.from(request);
    }

    @Transactional
    public ServiceRequestResponse updateStatus(UUID id, ServiceRequestStatusUpdateRequest update,
                                               Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        ServiceRequest request = find(id);
        if (!ALLOWED_TRANSITIONS.getOrDefault(request.getStatus(), Set.of()).contains(update.status())) {
            throw new UnauthorisedOperationException("Invalid service request status transition.");
        }
        request.changeStatus(update.status());
        auditLogService.record(user, AuditAction.STATUS_CHANGED, "ServiceRequest", request.getId(),
                "Changed service request status to " + request.getStatus());
        notificationService.notifyUser(request.getCustomer().getUser(), NotificationType.SERVICE_REQUEST,
                "Request status updated", request.getTitle() + " is now " + request.getStatus(), "/customer/requests");
        return ServiceRequestResponse.from(request);
    }

    private ServiceRequest find(UUID id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request was not found."));
    }

    private void requireRequestAccess(ServiceRequest request, Authentication authentication) {
        User user = currentUser.require(authentication);
        if (user.getRole() == Role.CARPENTER) return;
        requireCustomerOwnership(request, user);
    }

    private void requireCustomerOwnership(ServiceRequest request, User user) {
        if (!request.getCustomer().getUser().getId().equals(user.getId())) {
            throw new UnauthorisedOperationException("You cannot access another customer's service request.");
        }
    }

    private String trim(String value) { return value.trim(); }
}
