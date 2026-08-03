package com.carpenter.business.servicerequest;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.servicerequest.dto.ServiceRequestCreateRequest;
import com.carpenter.business.servicerequest.dto.ServiceRequestResponse;
import com.carpenter.business.servicerequest.dto.ServiceRequestStatusUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {
    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServiceRequestResponse create(@Valid @RequestBody ServiceRequestCreateRequest request,
                                  Authentication authentication) {
        return serviceRequestService.create(request, authentication);
    }

    @GetMapping("/my")
    PageResponse<ServiceRequestResponse> myRequests(Authentication authentication,
                                                   @PageableDefault(size = 20) Pageable pageable) {
        return serviceRequestService.myRequests(authentication, pageable);
    }

    @GetMapping
    PageResponse<ServiceRequestResponse> allRequests(Authentication authentication,
                                                    @PageableDefault(size = 20) Pageable pageable) {
        return serviceRequestService.allRequests(authentication, pageable);
    }

    @GetMapping("/{id}")
    ServiceRequestResponse get(@PathVariable UUID id, Authentication authentication) {
        return serviceRequestService.get(id, authentication);
    }

    @PutMapping("/{id}")
    ServiceRequestResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceRequestCreateRequest request,
                                  Authentication authentication) {
        return serviceRequestService.update(id, request, authentication);
    }

    @PatchMapping("/{id}/status")
    ServiceRequestResponse updateStatus(@PathVariable UUID id,
                                        @Valid @RequestBody ServiceRequestStatusUpdateRequest request,
                                        Authentication authentication) {
        return serviceRequestService.updateStatus(id, request, authentication);
    }
}
