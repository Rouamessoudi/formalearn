package com.esprit.formation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthHandlers jsonAuthHandlers;

    public SecurityConfig(JwtService jwtService, DatabaseUserDetailsService userDetailsService, JsonAuthHandlers jsonAuthHandlers) {
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        this.jsonAuthHandlers = jsonAuthHandlers;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthHandlers)
                        .accessDeniedHandler(jsonAuthHandlers))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus", "/actuator/info").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/learner/**", "/api/mla/**", "/api/profil", "/api/profil/**").hasRole("APPRENANT")
                        .requestMatchers(HttpMethod.POST, "/api/inscriptions/{id}/annuler").hasRole("APPRENANT")
                        .requestMatchers(HttpMethod.POST, "/api/inscriptions").hasRole("APPRENANT")
                        .requestMatchers(HttpMethod.GET, "/api/inscriptions/moi").hasRole("APPRENANT")
                        .requestMatchers(HttpMethod.PATCH, "/api/inscriptions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/sessions/{id}/inscriptions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories", "/api/categories/**",
                                "/api/formations", "/api/formations/**",
                                "/api/sessions", "/api/sessions/**")
                        .hasAnyRole("ADMIN", "APPRENANT")
                        .requestMatchers(HttpMethod.POST, "/api/categories", "/api/formations", "/api/sessions")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/formations/{formationId}/chapitres")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/formations/{formationId}/chapitres/{chapterId}/position")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/formations/**", "/api/sessions/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/formations/**", "/api/sessions/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/formations/{formationId}/chapitres", "/api/formations/{formationId}/chapitres/{chapterId}")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:8088",
                "http://127.0.0.1:8088"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
