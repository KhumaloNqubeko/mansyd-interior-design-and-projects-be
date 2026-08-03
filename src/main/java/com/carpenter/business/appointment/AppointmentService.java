package com.carpenter.business.appointment;

import com.carpenter.business.appointment.dto.AppointmentRequest;
import com.carpenter.business.appointment.dto.AppointmentResponse;
import com.carpenter.business.appointment.dto.AppointmentStatusUpdateRequest;
import com.carpenter.business.common.PageResponse;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectRepository;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.servicerequest.ServiceRequestRepository;
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
public class AppointmentService {
    private static final EnumMap<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(AppointmentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(AppointmentStatus.SCHEDULED, EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(AppointmentStatus.CONFIRMED, EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW, AppointmentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class));
        ALLOWED_TRANSITIONS.put(AppointmentStatus.CANCELLED, EnumSet.noneOf(AppointmentStatus.class));
        ALLOWED_TRANSITIONS.put(AppointmentStatus.NO_SHOW, EnumSet.noneOf(AppointmentStatus.class));
    }

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUser currentUser;

    public AppointmentService(AppointmentRepository appointmentRepository, CustomerRepository customerRepository,
                              ServiceRequestRepository serviceRequestRepository, ProjectRepository projectRepository,
                              CurrentUser currentUser) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.projectRepository = projectRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CARPENTER);
        validateWindow(request);
        Customer customer = customer(request.customerId());
        ServiceRequest serviceRequest = serviceRequest(request.serviceRequestId());
        Project project = project(request.projectId());
        validateRelationships(customer, serviceRequest, project);
        Appointment appointment = appointmentRepository.save(new Appointment(customer, serviceRequest, project, user,
                trim(request.title()), request.type(), request.scheduledStart(), request.scheduledEnd(),
                trim(request.location()), clean(request.notes())));
        return AppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(appointmentRepository.findAll(pageable).map(AppointmentResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> my(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(appointmentRepository.findByCustomerUserId(user.getId(), pageable).map(AppointmentResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> byProject(UUID projectId, Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(appointmentRepository.findByProjectId(projectId, pageable).map(AppointmentResponse::from));
    }

    @Transactional
    public AppointmentResponse update(UUID id, AppointmentRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        validateWindow(request);
        Appointment appointment = appointment(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new UnauthorisedOperationException("Only active appointments can be rescheduled.");
        }
        Customer customer = customer(request.customerId());
        ServiceRequest serviceRequest = serviceRequest(request.serviceRequestId());
        Project project = project(request.projectId());
        validateRelationships(customer, serviceRequest, project);
        appointment.update(customer, serviceRequest, project, trim(request.title()), request.type(),
                request.scheduledStart(), request.scheduledEnd(), trim(request.location()), clean(request.notes()));
        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID id, AppointmentStatusUpdateRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Appointment appointment = appointment(id);
        if (!ALLOWED_TRANSITIONS.getOrDefault(appointment.getStatus(), Set.of()).contains(request.status())) {
            throw new UnauthorisedOperationException("Invalid appointment status transition.");
        }
        appointment.changeStatus(request.status());
        return AppointmentResponse.from(appointment);
    }

    private void validateWindow(AppointmentRequest request) {
        if (!request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new UnauthorisedOperationException("Appointment end time must be after the start time.");
        }
    }

    private void validateRelationships(Customer customer, ServiceRequest serviceRequest, Project project) {
        if (serviceRequest != null && !serviceRequest.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorisedOperationException("Service request belongs to a different customer.");
        }
        if (project != null && !project.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorisedOperationException("Project belongs to a different customer.");
        }
    }

    private Appointment appointment(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment was not found."));
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

    private String trim(String value) { return value.trim(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
