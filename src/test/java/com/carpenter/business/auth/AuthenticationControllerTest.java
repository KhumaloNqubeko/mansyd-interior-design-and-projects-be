package com.carpenter.business.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carpenter.business.auth.dto.SessionResponse;
import com.carpenter.business.exception.GlobalExceptionHandler;
import com.carpenter.business.exception.DuplicateResourceException;
import com.carpenter.business.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthenticationControllerTest {
    private AuthenticationService service;
    private EmailVerificationService emailVerificationService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AuthenticationService.class);
        emailVerificationService = Mockito.mock(EmailVerificationService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthenticationController(service, emailVerificationService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void loginValidatesRequest() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void sessionReturnsSafeUserData() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.session(any())).thenReturn(new SessionResponse(id, "user@example.com", Role.CUSTOMER, "A User"));

        mvc.perform(get("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void duplicateRegistrationContactsPreventOtpDelivery() throws Exception {
        doThrow(new DuplicateResourceException("An account with this email already exists."))
                .when(service).ensureRegistrationContactsAvailable("taken@example.com", "0123456789");

        mvc.perform(post("/api/auth/email-verifications").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"taken@example.com","phoneNumber":"0123456789"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));

        verify(emailVerificationService, never()).sendCode(any());
    }
}
