package com.carpenter.business.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carpenter.business.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CustomerControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        CustomerService service = Mockito.mock(CustomerService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CustomerController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void updateProfileValidatesRequiredFields() throws Exception {
        mvc.perform(put("/api/customers/me").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "",
                                  "phoneNumber": "",
                                  "addressLine1": "",
                                  "city": "",
                                  "postalCode": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
