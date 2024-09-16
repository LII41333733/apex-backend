package com.project.apex.controller;

import com.project.apex.service.AdminDetailsService;
import com.project.apex.service.TradeService;
import com.project.apex.util.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AdminDetailsService adminDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public record UIToken(String token) {}

    @PostMapping("/login")
    public ResponseEntity<UIToken> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        final UserDetails userDetails = adminDetailsService.loadUserByUsername(authRequest.getUsername());
        String details = jwtUtil.generateToken(userDetails);
        logger.info(details);
        return new ResponseEntity<>(new UIToken(details), HttpStatus.OK);
    }
}

class AuthRequest {
    private String username;
    private String password;

    // Getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}