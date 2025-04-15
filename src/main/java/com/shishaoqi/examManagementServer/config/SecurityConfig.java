package com.shishaoqi.examManagementServer.config;

import com.shishaoqi.examManagementServer.security.JwtAuthenticationFilter;
import com.shishaoqi.examManagementServer.security.TeacherUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider(
                        TeacherUserDetailsService teacherUserDetailsService,
                        BCryptPasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(teacherUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);
                return authProvider;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        DaoAuthenticationProvider authenticationProvider)
                        throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/auth/api-login", "/api/auth/login",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**")
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/api-login").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**",
                                                                "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**", "/system/**")
                                                .hasAnyRole("ADMIN", "EXAM_ADMIN")
                                                .requestMatchers("/api/teachers/manage/**").hasRole("ADMIN")
                                                .requestMatchers("/api/exam-admin/**").hasRole("EXAM_ADMIN")
                                                .requestMatchers("/api/assignments/manage/**").hasRole("EXAM_ADMIN")
                                                .requestMatchers("/api/training/manage/**")
                                                .hasAnyRole("ADMIN", "EXAM_ADMIN")
                                                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "EXAM_ADMIN")
                                                .requestMatchers("/teachers/**").hasAnyRole("ADMIN", "EXAM_ADMIN")
                                                .requestMatchers("/api/assignments/my/**")
                                                .hasAnyRole("TEACHER", "EXAM_ADMIN", "ADMIN")
                                                .requestMatchers("/api/training/my/**")
                                                .hasAnyRole("TEACHER", "EXAM_ADMIN", "ADMIN")
                                                .requestMatchers("/api/profile/**").authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/api/auth/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/api/profile", true)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/login?logout")
                                                .clearAuthentication(true)
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        if (request.getRequestURI().startsWith("/api/")) {
                                                                response.setContentType(
                                                                                "application/json;charset=UTF-8");
                                                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                                response.getWriter().write(
                                                                                "{\"code\":401,\"message\":\"未授权\",\"data\":null}");
                                                        } else {
                                                                response.sendRedirect("/login?error");
                                                        }
                                                }))
                                .requestCache(cache -> cache
                                                .requestCache(new NullRequestCache()));

                http.authenticationProvider(authenticationProvider);
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}