package com.carpenter.business.customer;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.customer.dto.CustomerProfileResponse;
import com.carpenter.business.customer.dto.CustomerProfileUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    CustomerProfileResponse myProfile(Authentication authentication) {
        return customerService.myProfile(authentication);
    }

    @PutMapping("/me")
    CustomerProfileResponse updateMyProfile(@Valid @RequestBody CustomerProfileUpdateRequest request,
                                            Authentication authentication) {
        return customerService.updateMyProfile(request, authentication);
    }

    @GetMapping
    PageResponse<CustomerProfileResponse> listCustomers(Authentication authentication,
                                                       @PageableDefault(size = 20) Pageable pageable) {
        return customerService.listCustomers(authentication, pageable);
    }

    @GetMapping("/{id}")
    CustomerProfileResponse getCustomer(@PathVariable UUID id, Authentication authentication) {
        return customerService.getCustomer(id, authentication);
    }
}
