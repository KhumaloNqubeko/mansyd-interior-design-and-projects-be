package com.carpenter.business.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carpenter.business.audit.AuditLogService;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PortfolioImageServiceTest {
    @Mock PortfolioImageRepository repository;
    @Mock CurrentUser currentUser;
    @Mock AuditLogService auditLogService;
    @Mock Authentication authentication;

    private PortfolioImageService service;

    @BeforeEach
    void setUp() {
        service = new PortfolioImageService(repository, currentUser, auditLogService);
    }

    @Test
    void carpenterCanUploadImage() throws Exception {
        User carpenter = new User("carpenter@example.com", "hash", Role.CARPENTER, AccountStatus.ACTIVE);
        MockMultipartFile file = new MockMultipartFile("file", "kitchen.jpg", "image/jpeg", new byte[] {1, 2, 3});
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);
        when(repository.save(any(PortfolioImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioImageResponse response = service.upload("Oak kitchen", "Kitchen", "Warm timber finish", file, authentication);

        assertThat(response.title()).isEqualTo("Oak kitchen");
        assertThat(response.contentType()).isEqualTo("image/jpeg");
        assertThat(response.fileSizeBytes()).isEqualTo(3);
    }

    @Test
    void nonImageUploadIsRejected() {
        User carpenter = new User("carpenter@example.com", "hash", Role.CARPENTER, AccountStatus.ACTIVE);
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", new byte[] {1});
        when(currentUser.requireRole(authentication, Role.CARPENTER)).thenReturn(carpenter);

        assertThatThrownBy(() -> service.upload("Notes", "Other", "", file, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPEG, PNG and WebP");
    }
}
