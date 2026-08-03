package com.carpenter.business.quotation;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderRepository;
import com.carpenter.business.order.OrderService;
import com.carpenter.business.quotation.dto.QuotationItemRequest;
import com.carpenter.business.quotation.dto.QuotationRequest;
import com.carpenter.business.quotation.dto.QuotationResponse;
import com.carpenter.business.quotation.dto.QuotationUpdateRequest;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.servicerequest.ServiceRequestRepository;
import com.carpenter.business.servicerequest.ServiceRequestStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuotationService {
    private final QuotationRepository quotationRepository;
    private final QuotationItemRepository quotationItemRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final QuotationCalculationService calculationService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;

    public QuotationService(QuotationRepository quotationRepository, QuotationItemRepository quotationItemRepository,
                            ServiceRequestRepository serviceRequestRepository,
                            QuotationCalculationService calculationService, OrderService orderService,
                            OrderRepository orderRepository, CurrentUser currentUser) {
        this.quotationRepository = quotationRepository;
        this.quotationItemRepository = quotationItemRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.calculationService = calculationService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public QuotationResponse create(QuotationRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        if (quotationRepository.existsByServiceRequestId(request.serviceRequestId())) {
            throw new DuplicateResourceException("A quotation already exists for this service request.");
        }
        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.serviceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request was not found."));
        if (serviceRequest.getStatus() == ServiceRequestStatus.CANCELLED || serviceRequest.getStatus() == ServiceRequestStatus.CLOSED) {
            throw new UnauthorisedOperationException("Cannot quote a cancelled or closed service request.");
        }
        Quotation quotation = quotationRepository.save(new Quotation(generateQuotationNumber(), serviceRequest,
                request.expiryDate(), cleanNotes(request.notes())));
        serviceRequest.changeStatus(ServiceRequestStatus.QUOTATION_IN_PROGRESS);
        return response(quotation);
    }

    @Transactional(readOnly = true)
    public PageResponse<QuotationResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(quotationRepository.findAll(pageable).map(this::response));
    }

    @Transactional(readOnly = true)
    public PageResponse<QuotationResponse> myQuotations(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(quotationRepository.findByCustomerUserId(user.getId(), pageable).map(this::response));
    }

    @Transactional(readOnly = true)
    public QuotationResponse get(UUID id, Authentication authentication) {
        Quotation quotation = find(id);
        requireAccess(quotation, authentication);
        return response(quotation);
    }

    @Transactional
    public QuotationResponse update(UUID id, QuotationUpdateRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Quotation quotation = requireDraft(find(id));
        quotation.update(request.expiryDate(), cleanNotes(request.notes()));
        return response(quotation);
    }

    @Transactional
    public QuotationResponse addItem(UUID quotationId, QuotationItemRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Quotation quotation = requireDraft(find(quotationId));
        QuotationItem item = new QuotationItem(quotation, request.type(), trim(request.description()),
                request.quantity(), calculationService.money(request.unitPrice()),
                calculationService.money(request.discountAmount()), request.taxRate());
        quotation.addItem(item);
        calculationService.recalculate(quotation);
        return response(quotation);
    }

    @Transactional
    public QuotationResponse updateItem(UUID quotationId, UUID itemId, QuotationItemRequest request,
                                        Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Quotation quotation = requireDraft(find(quotationId));
        QuotationItem item = quotation.getItems().stream().filter(candidate -> candidate.getId().equals(itemId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Quotation item was not found."));
        item.update(request.type(), trim(request.description()), request.quantity(),
                calculationService.money(request.unitPrice()), calculationService.money(request.discountAmount()),
                request.taxRate(), item.getLineTotal());
        calculationService.recalculate(quotation);
        return response(quotation);
    }

    @Transactional
    public QuotationResponse deleteItem(UUID quotationId, UUID itemId, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Quotation quotation = requireDraft(find(quotationId));
        QuotationItem item = quotation.getItems().stream().filter(candidate -> candidate.getId().equals(itemId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Quotation item was not found."));
        quotation.removeItem(item);
        quotationItemRepository.delete(item);
        calculationService.recalculate(quotation);
        return response(quotation);
    }

    @Transactional
    public QuotationResponse submit(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Quotation quotation = requireDraft(find(id));
        if (quotation.getItems().isEmpty()) {
            throw new UnauthorisedOperationException("A quotation must contain at least one item before submission.");
        }
        if (quotation.getExpiryDate().isBefore(LocalDate.now())) {
            throw new UnauthorisedOperationException("Expired quotations cannot be submitted.");
        }
        calculationService.recalculate(quotation);
        quotation.changeStatus(QuotationStatus.PENDING_CUSTOMER);
        quotation.getServiceRequest().changeStatus(ServiceRequestStatus.QUOTED);
        return response(quotation);
    }

    @Transactional
    public QuotationResponse accept(UUID id, Authentication authentication) {
        Quotation quotation = findCustomerQuotation(id, authentication);
        if (quotation.getStatus() == QuotationStatus.ACCEPTED) {
            return response(quotation);
        }
        if (quotation.getStatus() != QuotationStatus.PENDING_CUSTOMER) {
            throw new UnauthorisedOperationException("Only pending quotations can be accepted.");
        }
        if (quotation.getExpiryDate().isBefore(LocalDate.now())) {
            quotation.changeStatus(QuotationStatus.EXPIRED);
            throw new UnauthorisedOperationException("Expired quotations cannot be accepted.");
        }
        quotation.changeStatus(QuotationStatus.ACCEPTED);
        Order order = orderService.getOrCreateForQuotation(quotation);
        return QuotationResponse.from(quotation, order.getId());
    }

    @Transactional
    public QuotationResponse reject(UUID id, Authentication authentication) {
        Quotation quotation = findCustomerQuotation(id, authentication);
        if (quotation.getStatus() != QuotationStatus.PENDING_CUSTOMER) {
            throw new UnauthorisedOperationException("Only pending quotations can be rejected.");
        }
        quotation.changeStatus(QuotationStatus.REJECTED);
        return response(quotation);
    }

    private Quotation find(UUID id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation was not found."));
    }

    private Quotation findCustomerQuotation(UUID id, Authentication authentication) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return quotationRepository.findByIdAndCustomerUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation was not found."));
    }

    private void requireAccess(Quotation quotation, Authentication authentication) {
        User user = currentUser.require(authentication);
        if (user.getRole() != Role.CARPENTER && !quotation.getCustomer().getUser().getId().equals(user.getId())) {
            throw new UnauthorisedOperationException("You cannot access another customer's quotation.");
        }
    }

    private Quotation requireDraft(Quotation quotation) {
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new UnauthorisedOperationException("Only draft quotations can be edited.");
        }
        return quotation;
    }

    private QuotationResponse response(Quotation quotation) {
        return QuotationResponse.from(quotation, orderRepository.findByQuotationId(quotation.getId()).map(Order::getId).orElse(null));
    }

    private String generateQuotationNumber() {
        return "QUO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String cleanNotes(String notes) { return notes == null ? "" : notes.trim(); }
    private String trim(String value) { return value.trim(); }
}
