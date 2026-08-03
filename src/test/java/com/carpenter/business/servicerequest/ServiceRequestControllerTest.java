package com.carpenter.business.servicerequest;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carpenter.business.exception.GlobalExceptionHandler;
import com.carpenter.business.servicerequest.dto.ServiceRequestCreateRequest;
import com.carpenter.business.servicerequest.dto.ServiceRequestResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ServiceRequestControllerTest {
    private ServiceRequestService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ServiceRequestService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ServiceRequestController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createValidatesRequiredFields() throws Exception {
        mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"\",\"preferredContactMethod\":\"\",\"siteAddress\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createReturnsCreatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Mockito.when(service.create(any(ServiceRequestCreateRequest.class), any())).thenReturn(
                new ServiceRequestResponse(id, customerId, "Customer", "Built-in cupboards",
                        "Bedroom cupboards", "Email", "1 Main Road", ServiceRequestStatus.SUBMITTED,
                        Instant.parse("2026-08-03T08:00:00Z"), Instant.parse("2026-08-03T08:00:00Z")));

        mvc.perform(post("/api/service-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Built-in cupboards",
                                  "description": "Bedroom cupboards",
                                  "preferredContactMethod": "Email",
                                  "siteAddress": "1 Main Road"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }
}
