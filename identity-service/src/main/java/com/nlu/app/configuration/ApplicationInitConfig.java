package com.nlu.app.configuration;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nlu.app.constant.PredefinedRole;
import com.nlu.app.entity.Role;
import com.nlu.app.entity.User;
import com.nlu.app.repository.RoleRepository;
import com.nlu.app.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    @NonFinal
    static final String ADMIN_USER_NAME = "admin";
    @NonFinal
    static final String ADMIN_PASSWORD = "admin";
    @EventListener(ApplicationReadyEvent.class)
    void applicationRunner() {
        log.info("Initializing application.....");
        if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
            roleRepository.save(Role.builder()
                    .name(PredefinedRole.USER_ROLE)
                    .description("User role")
                    .build());

            Role adminRole = roleRepository.save(Role.builder()
                    .name(PredefinedRole.ADMIN_ROLE)
                    .description("Admin role")
                    .build());

            var roles = new HashSet<Role>();
            roles.add(adminRole);

            User user = User.builder()
                    .username(ADMIN_USER_NAME)
                    .emailVerified(true)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .roles(roles)
                    .build();

            userRepository.save(user);
            log.warn("admin user has been created with default password: admin, please change it");
        }
        log.info("Application initialization completed .....");
    }
}
