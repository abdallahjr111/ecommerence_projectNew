package com.example.ecommerence_project.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String body = String.format(
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please provide a valid JWT token.\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
            request.getRequestURI(),
            LocalDateTime.now()
        );

        response.getWriter().write(body);
    }
}
