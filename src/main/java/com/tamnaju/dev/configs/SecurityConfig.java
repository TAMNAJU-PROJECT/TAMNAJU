package com.tamnaju.dev.configs;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private HikariDataSource dataSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO CSRF 비활성화, 추후 활성화 여부 판단 필요
        http.csrf(AbstractHttpConfigurer::disable);

        // TODO session 무효화, 추후 활성화 여부 판단 필요
        http.sessionManagement(sesstion -> {
            sesstion
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        // 특정 URL에 대한 접근 권한 설정
        http.authorizeHttpRequests(authorizeHttpRequests -> {
            authorizeHttpRequests
                    .requestMatchers("/**").permitAll()
                    .anyRequest().authenticated();
        });

        // 로그인
        http.formLogin(login -> {
            login
                    .permitAll()
                    .loginPage("user/login")
                    .successHandler(successHandler())
                    .failureHandler(failureHandler());
        });

        // Oauth2 로그인
        http.oauth2Login(oauth -> {
            oauth
                    .loginPage("user/login")
                    .successHandler(oauth2SuccessHandler());
        });

        // RememberMe
        http.rememberMe(rememberMe -> {
            rememberMe
                    .key("rememberMe")
                    .rememberMeParameter("remember-me")
                    .alwaysRemember(false)
                    .tokenValiditySeconds(60 * 60 * 24)
                    .tokenRepository(tokenRepository());
        });

        // 로그아웃
        http.logout(logout -> {
            logout
                    .permitAll()
                    .logoutUrl("user/logout")
                    .addLogoutHandler(logoutHandler())
                    .logoutSuccessHandler(logoutSuccessHandler())
                    .deleteCookies("")
                    .invalidateHttpSession(true);
        });

        // 예외처리
        http.exceptionHandling(exception -> {
            // 인증 진입점 리다이렉션
            exception.authenticationEntryPoint((request, response, authException) -> {
                response.sendRedirect("user/login");
            });
            // 접근 거부 예외 처리
            exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                // TODO
            });
        });

        return http.build();
    }

    // 로그인 성공 Bean
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                // TODO 로그인 성공 시, 쿠키 발급 등
            }
        };
        return successHandler;
    }

    // 로그인 실패 Bean
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        AuthenticationFailureHandler failureHandler = new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                    AuthenticationException exception) throws IOException, ServletException {
                // TODO 로그인 실패 시
            }
        };
        return failureHandler;
    }

    // Oauth2 로그인 성공 Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                // TODO Oauth2 로그인 성공 시, 쿠키 발급 등
            }
        };
        return successHandler;
    }

    // RememberMe Bean
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = new JdbcTokenRepositoryImpl();
        jdbcTokenRepositoryImpl.setDataSource(dataSource);
        return jdbcTokenRepositoryImpl;
    }

    // 로그아웃 Bean
    @Bean
    public LogoutHandler logoutHandler() {
        LogoutHandler logoutHandler = new LogoutHandler() {
            @Override
            public void logout(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) {
                // TODO 로그아웃 성공 시
            }
        };
        return logoutHandler;
    }

    // 로그아웃 성공 Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        LogoutSuccessHandler logoutSuccessHandler = new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                // TODO 로그아웃 성공 시, 쿠키 제거 등
            }
        };
        return logoutSuccessHandler;
    }

    // 비밀번호 암호화를 위한 Blow-fish 암호화 알고리즘 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
