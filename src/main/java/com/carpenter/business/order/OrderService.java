package com.carpenter.business.order;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.order.dto.OrderResponse;
import com.carpenter.business.order.dto.OrderStatusUpdateRequest;
import com.carpenter.business.project.ProjectService;
import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.security.CurrentUser;
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
public class OrderService {
    private static final EnumMap<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.IN_PROGRESS, OrderStatus.ON_HOLD, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.IN_PROGRESS, EnumSet.of(OrderStatus.ON_HOLD, OrderStatus.READY_FOR_DELIVERY, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.ON_HOLD, EnumSet.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.READY_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.INSTALLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.INSTALLED, OrderStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(OrderStatus.INSTALLED, EnumSet.of(OrderStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;
    private final ProjectService projectService;

    public OrderService(OrderRepository orderRepository, CurrentUser currentUser, ProjectService projectService) {
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
        this.projectService = projectService;
    }

    @Transactional
    public Order getOrCreateForQuotation(Quotation quotation) {
        Order order = orderRepository.findByQuotationId(quotation.getId())
                .orElseGet(() -> orderRepository.save(new Order(generateOrderNumber(), quotation)));
        projectService.getOrCreateForOrder(order);
        return order;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> all(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(orderRepository.findAll(pageable).map(OrderResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(Authentication authentication, Pageable pageable) {
        User user = currentUser.requireRole(authentication, Role.CUSTOMER);
        return PageResponse.from(orderRepository.findByCustomerUserId(user.getId(), pageable).map(OrderResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id, Authentication authentication) {
        Order order = find(id);
        User user = currentUser.require(authentication);
        if (user.getRole() != Role.CARPENTER && !order.getCustomer().getUser().getId().equals(user.getId())) {
            throw new UnauthorisedOperationException("You cannot access another customer's order.");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        Order order = find(id);
        if (!ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of()).contains(request.status())) {
            throw new UnauthorisedOperationException("Invalid order status transition.");
        }
        order.changeStatus(request.status());
        return OrderResponse.from(order);
    }

    private Order find(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found."));
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
