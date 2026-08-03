package com.carpenter.business.security;

import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BusinessUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public BusinessUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(user.getAccountStatus() == AccountStatus.DISABLED)
                .accountLocked(user.getAccountStatus() == AccountStatus.LOCKED)
                .build();
    }
}

