package com.edutech.studify.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter is called automatically for EVERY HTTP request, right after the request enters Spring Security’s filter chain
 * — not by your controller, not by you.
 * */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
/**
 * ### Visual Flow:
 * Request with Token
 *        ↓
 * 1. Extract JWT from "Authorization: Bearer <token>"
 *        ↓
 * 2. Validate token (signature, expiration)
 *        ↓
 * 3. Extract username from token payload
 *        ↓
 * 4. Load full user details from database
 *        ↓
 * 5. Create Authentication object with authorities
 *        ↓
 * 6. Store in SecurityContext (thread-local storage)
 *        ↓
 * 7. Pass request to next filter/controller
 * */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract JWT token from request
            String jwt = parseJwt(request);

            // Validate token and set authentication
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt); /** Extract username from token payload */

                UserDetails userDetails = userDetailsService.loadUserByUsername(username); /** Load full user details from database (including roles/persmissions) */

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities() // Create Authentication object with authorities
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                /** SecurityContextHolder:
                 - Thread-local storage for authentication
                 - Now Spring knows who the user is for this request
                 - Controllers can access with @AuthenticationPrincipal*/
            }
        } catch (Exception e) {
            System.err.println("Cannot set user authentication: " + e.getMessage());
        }

        filterChain.doFilter(request, response); // Pass request to next filter/controller
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}
