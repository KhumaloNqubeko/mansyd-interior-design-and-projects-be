package com.carpenter.business.customer;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.customer.dto.CustomerProfileResponse;
import com.carpenter.business.customer.dto.CustomerProfileUpdateRequest;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CurrentUser currentUser;

    public CustomerService(CustomerRepository customerRepository, CurrentUser currentUser) {
        this.customerRepository = customerRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse myProfile(Authentication authentication) {
        return CustomerProfileResponse.from(customerForUser(currentUser.require(authentication)));
    }

    @Transactional
    public CustomerProfileResponse updateMyProfile(CustomerProfileUpdateRequest request, Authentication authentication) {
        Customer customer = customerForUser(currentUser.require(authentication));
        customer.updateProfile(trim(request.fullName()), trim(request.phoneNumber()), trim(request.addressLine1()),
                trimToNull(request.addressLine2()), trim(request.city()), trim(request.postalCode()));
        return CustomerProfileResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerProfileResponse> listCustomers(Authentication authentication, Pageable pageable) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return PageResponse.from(customerRepository.findAll(pageable).map(CustomerProfileResponse::from));
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomer(UUID id, Authentication authentication) {
        currentUser.requireRole(authentication, Role.CARPENTER);
        return CustomerProfileResponse.from(customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer was not found.")));
    }

    private Customer customerForUser(User user) {
        if (user.getRole() != Role.CUSTOMER) {
            throw new ResourceNotFoundException("The authenticated user does not have a customer profile.");
        }
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found."));
    }

    private String trim(String value) { return value.trim(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
