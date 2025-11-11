package com.example.expensetracker.config;

import com.example.expensetracker.entity.ERole;
import com.example.expensetracker.entity.Role;
import com.example.expensetracker.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class DataInitializer {

    @Bean
    @Order(1)
    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {
            // Create roles if they don't exist
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_USER));
            }
            if (roleRepository.findByName(ERole.ROLE_MODERATOR).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_MODERATOR));
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }
        };
    }
}
