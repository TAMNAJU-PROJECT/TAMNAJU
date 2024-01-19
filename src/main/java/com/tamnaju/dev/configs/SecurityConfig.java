package com.tamnaju.dev.configs;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private DataSource dataSource;

    SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // TODO CSRF 비활성화, 추후 활성화 여부 판단 필요
                .csrf(AbstractHttpConfigurer::disable)

                // TODO session 무효화, 추후 활성화 여부 판단 필요
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 특정 URL에 대한 접근 권한 설정
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // TODO 로그인 하지 않은 사용자에게만 로그인 페이지 접근 허용
                        .requestMatchers("/user/login", "/user/login/**").permitAll()
                        .requestMatchers("/user/**", "/notice/**").permitAll()
                        // 최상위 경로와 추가 자원 허용
                        .requestMatchers("/", "images/**", "/scripts/**", "/stylesheets/**").permitAll()
                        .requestMatchers("/notice/write").hasRole("ADMIN")
                        .anyRequest().authenticated())

                // 로그인
                .formLogin(login -> login
                        .loginPage("/user/login").permitAll()
                        .successHandler(successHandler())
                        .failureHandler(failureHandler()))

                // // Oauth2 로그인
                // .oauth2Login(oauth2Configurer -> oauth2Configurer
                //         .loginPage("/user/login").permitAll()
                //         .userInfoEndpoint(
                //                 userInfoEndpointConfig -> userInfoEndpointConfig
                //                         .userService(oAuth2UserService))
                //         .successHandler(oAuth2SuccessHandler())
                //         .defaultSuccessUrl("/"))

                // // RememberMe
                // .rememberMe(rememberMe -> {
                // rememberMe
                // .key("rememberMe")
                // .rememberMeParameter("remember-me")
                // .alwaysRemember(false)
                // .tokenValiditySeconds(60 * 60 * 24) // 60초 * 60분 * 24시간
                // .tokenRepository(tokenRepository());
                // })

                // 로그아웃
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/user/logout")
                        .addLogoutHandler(logoutHandler())
                        .logoutSuccessHandler(logoutSuccessHandler())
                        // TODO 제거할 token key 추가 예정
                        .deleteCookies("")
                        .invalidateHttpSession(true))

                // password 수정 endpoint 지정
                .passwordManagement(passwordManagement -> passwordManagement
                        .changePasswordPage("/user/password"))

                // 예외처리
                .exceptionHandling(exception ->
                // 인증 진입점 리다이렉션
                exception
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendRedirect("/user/login?error=" + authException.getMessage()))
                        // 접근 거부 예외 처리
                        .accessDeniedHandler((request, response, accessDeniedException) -> response
                                .sendRedirect("/error")));

        return http.build();
    }

    // 로그인 인증 Bean
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }

    // UserDetailsService Bean
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder()
                .username("username")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
