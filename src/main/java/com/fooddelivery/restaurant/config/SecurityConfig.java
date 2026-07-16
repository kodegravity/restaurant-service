package com.fooddelivery.restaurant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator endpoints
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // Public browse endpoints (require authentication but allow CUSTOMER role)
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurants").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/*/menu").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/*/menu-items/*").authenticated()
                        
                        // Internal service APIs (require SERVICE role)
                        .requestMatchers("/api/v1/internal/**").hasAuthority("SCOPE_SERVICE")
                        
                        // Restaurant management (require owner or admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurants").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurants/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/restaurants/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurants/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        
                        // Business hours management
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurants/*/business-hours").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        
                        // Menu management
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurants/*/menu-categories").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurants/*/menu-categories/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/restaurants/*/menu-categories/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurants/*/menu-categories/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurants/*/menu-items").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurants/*/menu-items/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/restaurants/*/menu-items/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurants/*/menu-items/*").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        
                        // Delivery settings
                        .requestMatchers(HttpMethod.PUT, "/api/v1/restaurants/*/delivery-settings").hasAnyAuthority("SCOPE_RESTAURANT_OWNER", "SCOPE_ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
