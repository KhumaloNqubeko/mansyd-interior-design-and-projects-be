package com.carpenter.business.order;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.order.dto.OrderResponse;
import com.carpenter.business.order.dto.OrderStatusUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    PageResponse<OrderResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return orderService.all(authentication, pageable);
    }

    @GetMapping("/my")
    PageResponse<OrderResponse> myOrders(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return orderService.myOrders(authentication, pageable);
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id, Authentication authentication) {
        return orderService.get(id, authentication);
    }

    @PatchMapping("/{id}/status")
    OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody OrderStatusUpdateRequest request,
                               Authentication authentication) {
        return orderService.updateStatus(id, request, authentication);
    }
}
