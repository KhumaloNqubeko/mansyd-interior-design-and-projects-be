package com.carpenter.business.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carpenter.business.auth.dto.AddressRequest;
import com.carpenter.business.auth.dto.RegistrationRequest;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock UserRepository users;
    @Mock CustomerRepository customers;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock CurrentUser currentUser;
    @Mock HttpServletRequest httpRequest;
    @Mock HttpSession session;
    @Mock Authentication authentication;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(users, customers, encoder, authenticationManager, currentUser);
    }

    @Test
    void registrationNormalizesEmailHashesPasswordAndCreatesCustomer() {
        RegistrationRequest request = registration(" New.User@Example.COM ");
        when(encoder.encode("StrongPass1!")).thenReturn("bcrypt-hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(httpRequest.getSession(true)).thenReturn(session);

        service.register(request, httpRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(users).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new.user@example.com");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        verify(customers).save(any(Customer.class));
    }

    @Test
    void registrationRejectsDuplicateEmailBeforeEncoding() {
        when(users.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registration("taken@example.com"), httpRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void loginDelegatesCredentialsToAuthenticationManager() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(httpRequest.getSession(true)).thenReturn(session);
        User user = new User("user@example.com", "hash", com.carpenter.business.user.Role.CARPENTER,
                com.carpenter.business.user.AccountStatus.ACTIVE);
        when(currentUser.require(authentication)).thenReturn(user);

        service.login(new com.carpenter.business.auth.dto.LoginRequest(" USER@example.com ", "password"), httpRequest);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("user@example.com");
    }

    private RegistrationRequest registration(String email) {
        return new RegistrationRequest("New User", email, "+27123456789", "StrongPass1!",
                new AddressRequest("1 Main Road", null, "Johannesburg", "2000"));
    }
}

