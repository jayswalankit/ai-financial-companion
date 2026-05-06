package com.aifinance.financialcompanion.security.jwt;

import com.aifinance.financialcompanion.security.userDetails.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class JwtServiceTest {

    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @Autowired
    JwtService jwtService;

    @Test
    void testTokenIsValid(){
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("Ankitjayswal950@gmail.com");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);

        boolean isValid = jwtService.isTokenValid(token , userDetails);

        assertTrue(isValid);
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("Ankitjayswal950@gmail.com");

        String token = jwtService.generateToken(userDetails);

        Thread.sleep(190000);

        try {
            jwtService.isTokenValid(token, userDetails);
            fail("Expected token to be expired");
        } catch (ExpiredJwtException e) {
            assertTrue(true); // test passed
        }
    }
}
