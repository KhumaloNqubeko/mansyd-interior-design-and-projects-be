package com.carpenter.business.customer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carpenter.business.customer.dto.CustomerProfileUpdateRequest;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock CustomerRepository customers;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    @Mock User user;
    @Mock Customer customer;

    @Test
    void profileUpdateRejectsAnotherCustomersPhoneNumber() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(currentUser.require(authentication)).thenReturn(user);
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        when(user.getId()).thenReturn(userId);
        when(customers.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(customer.getId()).thenReturn(customerId);
        when(customers.existsByPhoneNumberAndIdNot("0123456789", customerId)).thenReturn(true);

        CustomerService service = new CustomerService(customers, currentUser);
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest(
                "Customer", "0123456789", "1 Main Road", null, "Johannesburg", "2000");

        assertThatThrownBy(() -> service.updateMyProfile(request, authentication))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("An account with this cell number already exists.");

        verify(customer, never()).updateProfile(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
