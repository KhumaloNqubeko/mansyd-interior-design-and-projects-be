package com.carpenter.business.project;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.project.dto.*;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class ProjectCollaborationController {
    private final ProjectCollaborationService service;
    public ProjectCollaborationController(ProjectCollaborationService service) { this.service = service; }
    @GetMapping("/activity")
    PageResponse<ProjectActivityResponse> activity(@PathVariable UUID projectId, Authentication auth, @PageableDefault(size = 20) Pageable pageable) {
        return service.list(projectId, auth, pageable);
    }
    @PostMapping("/comments") @ResponseStatus(HttpStatus.CREATED)
    ProjectActivityResponse comment(@PathVariable UUID projectId, @Valid @RequestBody ProjectCommentRequest request, Authentication auth) {
        return service.comment(projectId, request.message(), auth);
    }
    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    ProjectActivityResponse photo(@PathVariable UUID projectId, @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        return service.photo(projectId, description, file, auth);
    }
    @GetMapping("/activity/{activityId}/photo")
    ResponseEntity<byte[]> photoContent(@PathVariable UUID projectId, @PathVariable UUID activityId, Authentication auth) {
        ProjectActivity photo = service.photoContent(projectId, activityId, auth);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(photo.getFileName()).build().toString())
                .header("X-Content-Type-Options", "nosniff").body(photo.getPhotoData());
    }
    @PostMapping("/completion-review")
    ProjectResponse requestReview(@PathVariable UUID projectId, Authentication auth) { return service.requestReview(projectId, auth); }
    @PostMapping("/completion-review/decision")
    ProjectResponse decide(@PathVariable UUID projectId, @Valid @RequestBody CompletionDecisionRequest request, Authentication auth) {
        return service.decide(projectId, request, auth);
    }
}
