package com.wowinfobiz.backendsensor.config;

import com.wowinfobiz.authenticationservice.security.CustomUserDetailsService;
import com.wowinfobiz.authenticationservice.security.JwtAuthenticationFilter;
import com.wowinfobiz.authenticationservice.security.RestAccessDeniedHandler;
import com.wowinfobiz.authenticationservice.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
public class MonolithSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            DaoAuthenticationProvider daoAuthenticationProvider,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(daoAuthenticationProvider)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/**",
                                "/api/v1/ingestion/**",
                                "/api/v1/processing/readings/live",
                                "/api/v1/analytics/live",
                                "/api/v1/analytics/events/live",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/super-admins/**", "/api/v1/vendors/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/admins/**", "/api/v1/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/access/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "VENDOR", "VENDOR_ENGINEER")
                        .requestMatchers("/api/v1/vendors-engineer/**").hasAnyRole("VENDOR", "SUPER_ADMIN")
                        .requestMatchers(
                                "/api/v1/org/**",
                                "/api/v1/device/**",
                                "/api/v1/sensors/**",
                                "/api/v1/sensor-parameter/**",
                                "/api/v1/sensor-type/**",
                                "/api/v1/processing/**",
                                "/api/v1/analytics/**",
                                "/api/v1/audit-logs/**",
                                "/api/v1/dashboard/**",
                                "/api/v1/reports/**",
                                "/api/v1/search/**",
                                "/api/v1/stats/**",
                                "/api/v1/alerts/**",
                                "/api/v1/thresholds/**",
                                "/api/v1/config/**",
                                "/api/v1/configsystem/**",
                                "/api/v1/confignotifications/**",
                                "/api/v1/configthresholds/**",
                                "/api/v1/configalerts/**",
                                "/api/v1/configbackup/**",
                                "/api/v1/configemail/**",
                                "/api/v1/calibrations/**",
                                "/api/v1/batch/**",
                                "/api/v1/comments/**",
                                "/api/v1/favorites/**",
                                "/api/v1/integrations/**",
                                "/api/v1/locations/**",
                                "/api/v1/webhooks/**",
                                "/api/v1/tags/**",
                                "/api/v1/maintenance/**",
                                "/api/v1/schedules/**",
                                "/api/v1/jobs/**",
                                "/api/v1/roles/**",
                                "/api/v1/permissions/**",
                                "/api/v1/upload/**",
                                "/api/v1/download/**",
                                "/api/v1/files/**",
                                "/api/v1/import/**",
                                "/api/v1/export/**",
                                "/api/v1/health/**",
                                "/api/v1/version/**",
                                "/api/v1/system/**"
                        ).hasAnyRole("SUPER_ADMIN", "ADMIN", "VENDOR", "VENDOR_ENGINEER", "USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://0.0.0.0:*",
                "http://103.185.75.179:*",
                "https://103.185.75.179:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
