package com.carpenter.business.appointment;

import com.carpenter.business.appointment.dto.AppointmentRequest;
import com.carpenter.business.appointment.dto.AppointmentResponse;
import com.carpenter.business.appointment.dto.AppointmentStatusUpdateRequest;
import com.carpenter.business.common.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    AppointmentResponse create(@Valid @RequestBody AppointmentRequest request, Authentication authentication) {
        return appointmentService.create(request, authentication);
    }

    @GetMapping("/appointments")
    PageResponse<AppointmentResponse> all(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return appointmentService.all(authentication, pageable);
    }

    @GetMapping("/appointments/my")
    PageResponse<AppointmentResponse> my(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return appointmentService.my(authentication, pageable);
    }

    @GetMapping("/projects/{projectId}/appointments")
    PageResponse<AppointmentResponse> byProject(@PathVariable UUID projectId, Authentication authentication,
                                                @PageableDefault(size = 20) Pageable pageable) {
        return appointmentService.byProject(projectId, authentication, pageable);
    }

    @PutMapping("/appointments/{id}")
    AppointmentResponse update(@PathVariable UUID id, @Valid @RequestBody AppointmentRequest request,
                               Authentication authentication) {
        return appointmentService.update(id, request, authentication);
    }

    @PostMapping("/appointments/{id}/status")
    AppointmentResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody AppointmentStatusUpdateRequest request,
                                     Authentication authentication) {
        return appointmentService.updateStatus(id, request, authentication);
    }
}
