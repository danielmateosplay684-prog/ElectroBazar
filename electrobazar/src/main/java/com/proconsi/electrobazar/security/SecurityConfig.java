package com.proconsi.electrobazar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                // login endpoint is public
                                                .requestMatchers("/api/workers/login").permitAll()
                                                // allow public reads for catalog (products, categories, cash register
                                                // status)
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/products/**")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/categories/**")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/cash-registers/**")
                                                .permitAll()
                                                // allow public reads for product prices (catalog display) and RE rate
                                                // info
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/product-prices/**")
                                                .permitAll()
                                                // allow activity log access without auth (used by admin UI)
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/activity-log/**")
                                                .permitAll()
                                                // allow bulk price scheduling without auth (used by admin UI)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/product-prices/bulk-schedule")
                                                .permitAll()
                                                // allow anybody to GET and POST customers (used by TPV for search and
                                                // creation)
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/customers/**")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/customers")
                                                .permitAll()
                                                // Allow authenticated workers to access TPV
                                                .requestMatchers("/tpv/**").authenticated()
                                                // Admin web interface requires ADMIN_ACCESS authority
                                                .requestMatchers("/admin/**").hasAuthority("ADMIN_ACCESS")

                                                // roles access (only authenticated)
                                                .requestMatchers("/api/roles/**").authenticated()
                                                // workers access (only authenticated)
                                                .requestMatchers("/api/workers/**").authenticated()
                                                // sales access (only authenticated)
                                                .requestMatchers("/api/sales/**").authenticated()

                                                // admin controllers require explicit authority
                                                .requestMatchers("/api/admin/**").hasAuthority("ADMIN_ACCESS")
                                                // the rest of the API needs a valid token/session
                                                .requestMatchers("/api/**").authenticated()
                                                .anyRequest().permitAll())
                                .exceptionHandling(exceptions -> exceptions
                                                // Redirect unauthenticated HTML requests to login, while returning 401
                                                // for API
                                                .defaultAuthenticationEntryPointFor(
                                                                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint(
                                                                                "/login"),
                                                                request -> request.getServletPath().startsWith("/tpv"))
                                                .defaultAuthenticationEntryPointFor(
                                                                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint(
                                                                                "/login"),
                                                                request -> request.getServletPath()
                                                                                .startsWith("/admin"))
                                                // Redirect unauthorized HTML requests to TPV or login
                                                .defaultAccessDeniedHandlerFor(
                                                                (request, response, accessDeniedException) -> response
                                                                                .sendRedirect("/tpv"),
                                                                request -> request.getServletPath()
                                                                                .startsWith("/admin")))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
