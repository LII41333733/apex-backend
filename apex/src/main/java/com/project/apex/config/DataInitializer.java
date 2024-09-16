package com.project.apex.config;

import com.project.apex.model.Admin;
import com.project.apex.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            if (adminRepository.findByUsername("admin") == null) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("t3mp3ranc3")); // Store encrypted password
                adminRepository.save(admin);
                System.out.println("Admin user created: username = admin, password = t3mp3ranc3");
            }
        };
    }
}
