package com.tamnaju.dev.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.tamnaju.dev.configs.jwt.CustomAuthenticationManager;
import com.tamnaju.dev.configs.jwt.TokenProvider;
import com.tamnaju.dev.configs.jwt.filters.JwtRequestFilter;
import com.tamnaju.dev.configs.jwt.filters.JwtUsernamePasswordAuthenticationFilter;
import com.tamnaju.dev.configs.jwt.handlers.CustomAuthenticationFailureHandler;
import com.tamnaju.dev.configs.jwt.handlers.CustomLogoutHandler;
import com.tamnaju.dev.configs.jwt.handlers.CustomLogoutSuccessHandler;
import com.tamnaju.dev.configs.jwt.handlers.CustomOAuth2SuccessHandler;
import com.tamnaju.dev.configs.jwt.services.PrincipalUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private CustomAuthenticationManager authenticationManager;
    private TokenProvider tokenProvider;

    SecurityConfig(CustomAuthenticationManager authenticationManager,
            TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                // session 무효화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 특정 URL에 대한 접근 권한 설정
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // TODO 로그인 하지 않은 사용자에게만 로그인 페이지 접근 허용
                        .requestMatchers("/login").anonymous()
                        // 최상위 경로와 추가 자원 허용
                        .requestMatchers("/", "/images/**", "/scripts/**", "/stylesheets/**").permitAll()
                        .requestMatchers("/jejumap/**").permitAll()
                        .requestMatchers("/user/**", "/notice/**").permitAll()
                        .requestMatchers("/notice/write", "/notice/modify").hasRole("ADMIN")
                        .anyRequest().authenticated())

                // Jwt login
                .addFilter(authenticationFilter())

                // JWT request
                .addFilterBefore(oncePerRequestFilter(), BasicAuthenticationFilter.class)

                // 로그인
                .formLogin(login -> login
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/login")
                        .usernameParameter("id")
                        .passwordParameter("password")
                        .successHandler(successHandler())
                        .failureHandler(failureHandler()))

                // Oauth2 로그인
                .oauth2Login(oauth2Configurer -> oauth2Configurer
                        .loginPage("/login").permitAll()
                        .successHandler(oAuth2SuccessHandler()))

                // 로그아웃
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .addLogoutHandler(logoutHandler())
                        .logoutSuccessHandler(customLogoutSuccessHandler())
                        .deleteCookies("JSESSIONID",
                                TokenProvider.ACCESS_TOKEN,
                                TokenProvider.REFRESH_TOKEN)
                        .invalidateHttpSession(true))

                // TODO password 수정 endpoint 지정
                .passwordManagement(passwordManagement -> passwordManagement
                        .changePasswordPage("/user/password"))

                // 예외처리
                .exceptionHandling(exception -> exception
                        // 권한 부족 예외 처리
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendRedirect("/login?error=" + authException.getMessage()))
                        // 접근 거부 예외 처리
                        .accessDeniedHandler((request, response, accessDeniedException) -> response
                                .sendRedirect("/error")));

        return http.build();
    }

    // JWT 로그인 필터 Bean
    @Bean
    public JwtUsernamePasswordAuthenticationFilter authenticationFilter() {
        JwtUsernamePasswordAuthenticationFilter authenticationFilter = new JwtUsernamePasswordAuthenticationFilter(
                authenticationManager, tokenProvider);
        return authenticationFilter;
    }

    // JWT filter Bean
    @Bean
    public JwtRequestFilter oncePerRequestFilter() {
        JwtRequestFilter requestFilter = new JwtRequestFilter();
        return requestFilter;
    }

    // UserDetailsService Bean
    @Bean
    public PrincipalUserDetailsService userDetailsService() {
        PrincipalUserDetailsService userDetails = new PrincipalUserDetailsService();
        return userDetails;
    }

    // 로그인 성공 Bean
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        AuthenticationSuccessHandler successHandler = new CustomOAuth2SuccessHandler();
        return successHandler;
    }

    // 로그인 실패 Bean
    @Bean
    public CustomAuthenticationFailureHandler failureHandler() {
        CustomAuthenticationFailureHandler failureHandler = new CustomAuthenticationFailureHandler();
        return failureHandler;
    }

    // Oauth2 로그인 성공 Bean
    @Bean
    public CustomOAuth2SuccessHandler oAuth2SuccessHandler() {
        CustomOAuth2SuccessHandler successHandler = new CustomOAuth2SuccessHandler();
        return successHandler;
    }

    // 로그아웃 Bean
    @Bean
    public CustomLogoutHandler logoutHandler() {
        CustomLogoutHandler logoutHandler = new CustomLogoutHandler();
        return logoutHandler;
    }

    // 로그아웃 성공 Bean
    @Bean
    public CustomLogoutSuccessHandler customLogoutSuccessHandler() {
        CustomLogoutSuccessHandler customLogoutSuccessHandler = new CustomLogoutSuccessHandler();
        return customLogoutSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
