package com.edutech.studify.config;

import com.edutech.studify.security.CustomUserDetailsService;
import com.edutech.studify.security.JwtAuthenticationEntryPoint;
import com.edutech.studify.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Spring will process this class at startup to set up beans
@EnableWebSecurity //  Activates Spring Security features
@EnableMethodSecurity   /**  Enables method-level security (@PreAuthorize, @Secured) */
@RequiredArgsConstructor /** Auto-injects dependencies (cleaner than @Autowired) */
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService; // Load user from DB
    private final JwtAuthenticationEntryPoint authenticationEntryPoint; // Authenticates requests
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder bean (BCrypt)
     * Hash & verify passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider
     * runs ONLY during login
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Security filter chain
     * Defines the security rules
     * ✔ Called once at startup to construct filters
     * ✔ Filters inside it run per request
     * it runs for every HTTP request
     * Spring Boot auto-registers the chain as a Servlet Filter.
     * You never call it.
     * Tomcat does.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT) it is for cookie-based sessions
                .csrf(AbstractHttpConfigurer::disable)

                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                // Session management (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); /** Why:
                JWT must run before Spring’s default auth
                Username/password is NOT used here
             * .addFilterBefore() = Arrange guards
             * doFilter() = Guard checks every visitor
         */

        return http.build();
    }
}

/**
 * STARTUP:
 * build SecurityFilterChain
 *
 * REQUEST:
 * JwtAuthenticationFilter
 *  → SecurityContext
 *  → AuthorizationFilter
 *  → Controller
 *
 * LOGIN:
 * Controller
 *  → AuthenticationManager
 *  → AuthenticationProvider
 *  → PasswordEncoder
 * */