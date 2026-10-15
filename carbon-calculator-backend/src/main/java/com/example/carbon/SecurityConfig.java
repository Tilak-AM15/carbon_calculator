package com.example.carbon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository repo) {
        return username -> repo.findByUsername(username)
                .map(u -> User.withUsername(u.username)
                        .password(u.passwordHash)
                        .roles(u.role)
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Admin-only management endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Activity mutation endpoints: explicitly denied to REVIEWER, allowed to ADMIN and STAKEHOLDER
                        .requestMatchers(HttpMethod.POST, "/api/activities").hasAnyRole("ADMIN", "STAKEHOLDER")
                        .requestMatchers(HttpMethod.PUT, "/api/activities/**").hasAnyRole("ADMIN", "STAKEHOLDER")
                        .requestMatchers(HttpMethod.DELETE, "/api/activities/**").hasAnyRole("ADMIN", "STAKEHOLDER")

                        // Logistics transfer legs mutation
                        .requestMatchers(HttpMethod.POST, "/api/transfers/**").hasAnyRole("ADMIN", "STAKEHOLDER")

                        // Authenticated read endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/api/me",
                                "/api/organizations/**",
                                "/api/facilities/**",
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/category-status/**",
                                "/api/factors/**",
                                "/api/activities/**",
                                "/api/batches/**",
                                "/api/transfers/**",
                                "/api/reports/**",
                                "/api/audit/**"
                        ).authenticated()

                        // All other API requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:http://localhost:5173}") String origins) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of(origins.split(",")));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        c.setExposedHeaders(List.of("Authorization"));
        c.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
