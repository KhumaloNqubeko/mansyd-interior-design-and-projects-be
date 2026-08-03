package com.carpenter.business.auth.dto;

import com.carpenter.business.user.Role;
import java.util.UUID;

public record SessionResponse(UUID id, String email, Role role, String displayName) { }

