package com.edutech.studify.security;

import com.edutech.studify.dto.response.GradeResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * global authentication error handler
 * This class handles authentication errors
 * Called when an unauthenticated user tries to access a protected resource
 ** Triggered when:
 * A protected endpoint is accessed
 * SecurityContext has no valid Authentication
 * OR authentication fails (invalid / missing JWT)
 */

/**
 * Why it exists
 * Default Spring response = HTML / plain text ❌
 * APIs need structured JSON errors ✅
 * Centralized handling of unauthenticated access
 * */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Authentication required. Please provide a valid token.")
                .path(request.getRequestURI())
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}