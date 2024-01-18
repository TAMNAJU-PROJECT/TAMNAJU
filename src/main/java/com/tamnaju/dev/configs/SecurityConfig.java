package com.tamnaju.dev.configs;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.tamnaju.dev.configs.oAuth2.OAuth2UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private DataSource dataSource;
    private OAuth2UserService oAuth2UserService;

    SecurityConfig(DataSource dataSource, OAuth2UserService oAuth2UserService) {
        this.dataSource = dataSource;
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO CSRF 비활성화, 추후 활성화 여부 판단 필요
        http.csrf(AbstractHttpConfigurer::disable);

        // TODO session 무효화, 추후 활성화 여부 판단 필요
        http.sessionManagement(session -> {
            session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        // 특정 URL에 대한 접근 권한 설정
        http.authorizeHttpRequests(authorizeHttpRequests -> {
            authorizeHttpRequests
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/", "images/**", "/mainPage/**", "/stylesheets/**").permitAll()
//                    .requestMatchers("/user/login").permitAll()
                    .requestMatchers("/user/**").permitAll()
                    .requestMatchers("/notice/write").hasRole("ADMIN")
                    .anyRequest().authenticated();
        });

        // 로그인
        http.formLogin(login -> {
            login
                    .permitAll()
                    .loginPage("/user/login")
                    .successHandler(successHandler())
                    .failureHandler(failureHandler());
        });

        // Oauth2 로그인
        http.oauth2Login(oauth2Configurer -> {
            oauth2Configurer
                    .loginPage("/user/login")
                    .userInfoEndpoint(userInfoEndpointConfig -> {
                        userInfoEndpointConfig.userService(oAuth2UserService);
                    })
                    .successHandler(oAuth2SuccessHandler())
                    .defaultSuccessUrl("/");
        });

        // // RememberMe
        // http.rememberMe(rememberMe -> {
        // rememberMe
        // .key("rememberMe")
        // .rememberMeParameter("remember-me")
        // .alwaysRemember(false)
        // .tokenValiditySeconds(60 * 60 * 24) // 60초 * 60분 * 24시간
        // .tokenRepository(tokenRepository());
        // });

        // 로그아웃
        http.logout(logout -> {
            logout
                    .permitAll()
                    .logoutUrl("/user/logout")
                    .addLogoutHandler(logoutHandler())
                    .logoutSuccessHandler(logoutSuccessHandler())
                    .deleteCookies("") // TODO 제거할 token key 추가 예정
                    .invalidateHttpSession(true);
        });

        // 예외처리
        http.exceptionHandling(exception -> {
            // 인증 진입점 리다이렉션
            exception.authenticationEntryPoint((request, response, authException) -> {
                response.sendRedirect("/user/login?error=" + authException.getMessage());
            });
            // 접근 거부 예외 처리
            exception.accessDeniedHandler(new AccessDeniedHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {
                    response.sendRedirect("/error");
                }
            });
        });

        return http.build();
    }

    // 로그인 성공 Bean
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                    HttpServletResponse response,
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
            public void onAuthenticationFailure(HttpServletRequest request,
                    HttpServletResponse response,
                    AuthenticationException exception) throws IOException, ServletException {
                // TODO 로그인 실패 시
            }
        };
        return failureHandler;
    }

    // Oauth2 로그인 성공 Bean
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                Cookie cookie = new Cookie(null, null);
                response.addCookie(cookie);
            }
        };
    }

    // // RememberMe Bean
    // @Bean
    // public PersistentTokenRepository tokenRepository() {
    // JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = new
    // JdbcTokenRepositoryImpl();
    // // TODO 아래 경고를 우회하기 위해 if문 추가
    // // 타당성 검증 필요
    // // Null type safety: The expression of type 'DataSource' needs unchecked
    // // conversion to conform to '@NonNull DataSource'Java(16778128)
    // if (dataSource != null) {
    // jdbcTokenRepositoryImpl.setDataSource(dataSource);
    // }
    // return jdbcTokenRepositoryImpl;
    // }

    // 로그아웃 Bean
    @Bean
    public LogoutHandler logoutHandler() {
        LogoutHandler logoutHandler = new LogoutHandler() {
            @Override
            public void logout(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) {
                // TODO 로그아웃 시
            }
        };
        return logoutHandler;
    }

    // 로그아웃 성공 Bean
    @Bean
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

    // @Bean
    // public ClientRegistrationRepository clientRegistrationRepository() {
    // return null;
    // }
}
