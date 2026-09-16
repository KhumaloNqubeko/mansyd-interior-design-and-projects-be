package com.carpenter.business.auth;

import com.carpenter.business.auth.dto.LoginRequest;
import com.carpenter.business.auth.dto.RegistrationRequest;
import com.carpenter.business.auth.dto.SessionResponse;
import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CurrentUser currentUser;
    private final PhoneVerificationService phoneVerificationService;

    public AuthenticationService(UserRepository userRepository, CustomerRepository customerRepository,
                                 PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                                 CurrentUser currentUser, PhoneVerificationService phoneVerificationService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.currentUser = currentUser;
        this.phoneVerificationService = phoneVerificationService;
    }

    @Transactional
    public SessionResponse register(RegistrationRequest request, HttpServletRequest httpRequest) {
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
        phoneVerificationService.consume(request.phoneNumber(), request.phoneVerificationToken());
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()),
                Role.CUSTOMER, AccountStatus.ACTIVE));
        Customer customer = customerRepository.save(new Customer(user, request.fullName().trim(),
                request.phoneNumber().trim(), request.address().addressLine1().trim(),
                trimToNull(request.address().addressLine2()), request.address().city().trim(),
                request.address().postalCode().trim()));
        Authentication authentication = authenticate(email, request.password(), httpRequest);
        return response(user, customer.getFullName());
    }

    public SessionResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = normalize(request.email());
        Authentication authentication = authenticate(email, request.password(), httpRequest);
        return session(authentication);
    }

    @Transactional(readOnly = true)
    public SessionResponse session(Authentication authentication) {
        User user = currentUser.require(authentication);
        String displayName = user.getRole() == Role.CUSTOMER
                ? customerRepository.findByUserId(user.getId()).map(Customer::getFullName).orElse(user.getEmail())
                : "Carpenter";
        return response(user, displayName);
    }

    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
    }

    private Authentication authenticate(String email, String password, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return authentication;
    }

    private SessionResponse response(User user, String displayName) {
        return new SessionResponse(user.getId(), user.getEmail(), user.getRole(), displayName);
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
