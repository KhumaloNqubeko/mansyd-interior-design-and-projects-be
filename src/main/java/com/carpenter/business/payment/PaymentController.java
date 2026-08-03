package com.carpenter.business.payment;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.payment.dto.PaymentDecisionRequest;
import com.carpenter.business.payment.dto.PaymentRequest;
import com.carpenter.business.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PaymentResponse submit(@Valid @RequestBody PaymentRequest request, Authentication authentication) {
        return paymentService.submit(request, authentication);
    }

    @GetMapping
    PageResponse<PaymentResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return paymentService.all(authentication, pageable);
    }

    @GetMapping("/my")
    PageResponse<PaymentResponse> myPayments(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return paymentService.myPayments(authentication, pageable);
    }

    @PatchMapping("/{id}/approve")
    PaymentResponse approve(@PathVariable UUID id, Authentication authentication) {
        return paymentService.approve(id, authentication);
    }

    @PatchMapping("/{id}/reject")
    PaymentResponse reject(@PathVariable UUID id, @Valid @RequestBody PaymentDecisionRequest request,
                           Authentication authentication) {
        return paymentService.reject(id, request, authentication);
    }
}
