package com.carpenter.business.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuditLogControllerTest {
    @Test
    void defaultsToNewestFirstBeforePagination() throws Exception {
        AuditLogService service = mock(AuditLogService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new AuditLogController(service))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();

        mvc.perform(get("/api/audit-logs"));

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).all(isNull(), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
    }
}
