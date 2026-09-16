package com.carpenter.business.auth;

import com.carpenter.business.auth.dto.LoginRequest;
import com.carpenter.business.auth.dto.CsrfTokenResponse;
import com.carpenter.business.auth.dto.RegistrationRequest;
import com.carpenter.business.auth.dto.PhoneCodeVerificationRequest;
import com.carpenter.business.auth.dto.PhoneVerificationRequest;
import com.carpenter.business.auth.dto.PhoneVerificationResponse;
import com.carpenter.business.auth.dto.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final PhoneVerificationService phoneVerificationService;

    public AuthenticationController(AuthenticationService authenticationService,
                                    PhoneVerificationService phoneVerificationService) {
        this.authenticationService = authenticationService;
        this.phoneVerificationService = phoneVerificationService;
    }

    @PostMapping("/phone-verifications")
    ResponseEntity<Void> sendPhoneVerification(@Valid @RequestBody PhoneVerificationRequest request) {
        phoneVerificationService.sendCode(request.phoneNumber());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/phone-verifications/verify")
    PhoneVerificationResponse verifyPhone(@Valid @RequestBody PhoneCodeVerificationRequest request) {
        return new PhoneVerificationResponse(phoneVerificationService.verifyCode(request.phoneNumber(), request.code()));
    }

    @PostMapping("/register")
    ResponseEntity<SessionResponse> register(@Valid @RequestBody RegistrationRequest request,
                                             HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request, httpRequest));
    }

    @PostMapping("/login")
    SessionResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authenticationService.login(request, httpRequest);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    SessionResponse session(Authentication authentication) {
        return authenticationService.session(authentication);
    }

    @GetMapping("/csrf")
    CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getToken());
    }
}
