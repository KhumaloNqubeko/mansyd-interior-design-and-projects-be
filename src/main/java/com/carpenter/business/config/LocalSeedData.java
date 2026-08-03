package com.carpenter.business.config;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.customer.CustomerRepository;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("local")
public class LocalSeedData {
    @Bean
    CommandLineRunner seedUsers(UserRepository users, CustomerRepository customers, PasswordEncoder encoder,
                                @Value("${app.seed-local-users:false}") boolean enabled) {
        return args -> {
            if (!enabled) return;
            if (!users.existsByEmailIgnoreCase("carpenter@local.test")) {
                users.save(new User("carpenter@local.test", encoder.encode("Carpenter123!"), Role.CARPENTER, AccountStatus.ACTIVE));
            }
            if (!users.existsByEmailIgnoreCase("customer@local.test")) {
                User user = users.save(new User("customer@local.test", encoder.encode("Customer123!"), Role.CUSTOMER, AccountStatus.ACTIVE));
                customers.save(new Customer(user, "Local Customer", "+27 00 000 0000", "1 Workshop Road", null, "Johannesburg", "2000"));
            }
        };
    }
}

