package com.carpenter.business.customer.dto;

import com.carpenter.business.customer.Customer;
import java.util.UUID;

public record CustomerProfileResponse(UUID id, UUID userId, String email, String fullName, String phoneNumber,
                                      String addressLine1, String addressLine2, String city, String postalCode) {
    public static CustomerProfileResponse from(Customer customer) {
        return new CustomerProfileResponse(customer.getId(), customer.getUser().getId(), customer.getUser().getEmail(),
                customer.getFullName(), customer.getPhoneNumber(), customer.getAddressLine1(),
                customer.getAddressLine2(), customer.getCity(), customer.getPostalCode());
    }
}
