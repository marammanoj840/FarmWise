package com.farmwise.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.farmwise.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FarmWiseBackendApplicationTests {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoadsAndFlywaySeedsFoundationRoles() {
        assertThat(roleRepository.findByName("ADMIN")).isPresent();
        assertThat(roleRepository.findByName("FARMER")).isPresent();
        assertThat(roleRepository.findByName("CUSTOMER")).isPresent();
    }

    @Test
    void passwordEncoderHashesAndMatchesPasswords() {
        String encoded = passwordEncoder.encode("FarmWise#2026");
        assertThat(encoded).isNotEqualTo("FarmWise#2026");
        assertThat(passwordEncoder.matches("FarmWise#2026", encoded)).isTrue();
    }
}
