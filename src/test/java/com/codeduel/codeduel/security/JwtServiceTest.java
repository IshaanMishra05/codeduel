package com.codeduel.codeduel.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = JwtService.class)
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateToken() {

        String token = jwtService.generateToken("john");

        System.out.println("========== JWT TOKEN ==========");
        System.out.println(token);
        System.out.println("===============================");
    }
}