package com.carpenter.business.auth;

import com.carpenter.business.auth.dto.LoginRequest;
import com.carpenter.business.auth.dto.CsrfTokenResponse;
import com.carpenter.business.auth.dto.RegistrationRequest;
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

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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
