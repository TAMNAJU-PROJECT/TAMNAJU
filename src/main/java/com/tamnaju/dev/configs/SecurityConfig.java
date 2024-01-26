package com.tamnaju.dev.configs;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tamnaju.dev.configs.jwt.TokenDto;
import com.tamnaju.dev.configs.jwt.TokenProvider;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserMapper userMapper;
    private final TokenProvider tokenProvider;

    SecurityConfig(UserMapper userMapper,
            TokenProvider tokenProvider) {
        this.userMapper = userMapper;
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

                // JWT
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
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .deleteCookies("JSESSIONID",
                                TokenProvider.ACCESS_TOKEN,
                                TokenProvider.REFRESH_TOKEN)
                        .invalidateHttpSession(true))

                // password 수정 endpoint 지정
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
                log.info("[SecurityConfig] successfulAuthentication() :\n"
                        + authentication.getPrincipal().toString());

                // token 생성
                TokenDto tokenDto = tokenProvider.generateToken(authentication);

                // access token 추가
                Cookie accessToken = new Cookie(TokenProvider.ACCESS_TOKEN, tokenDto.getAccessToken());
                accessToken.setMaxAge((int) TokenProvider.ACCESS_TOKEN_EXPIRED_AT_SECONDS);
                accessToken.setPath("/");
                response.addCookie(accessToken);

                log.info("[SecurityConfig] successfulAuthentication() accessToken :\n"
                        + accessToken.toString());

                // refresh token 추가
                Cookie refreshToken = new Cookie(TokenProvider.REFRESH_TOKEN, tokenDto.getRefreshToken());
                refreshToken.setMaxAge((int) TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS);
                refreshToken.setPath("/");
                response.addCookie(refreshToken);

                log.info("[SecurityConfig] successfulAuthentication() refreshToken :\n"
                        + refreshToken.toString());

                response.sendRedirect("/");
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
                response.sendRedirect("/login");
            }
        };
        return failureHandler;
    }

    // JWT filter 정의
    @Bean
    public OncePerRequestFilter oncePerRequestFilter() {
        OncePerRequestFilter oncePerRequestFilter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                    @NonNull FilterChain filterChain) throws ServletException, IOException {
                String accessToken = null;
                try {
                    accessToken = Arrays.stream(request.getCookies())
                            .filter(cookie -> cookie.getName().equals(TokenProvider.ACCESS_TOKEN)).findFirst()
                            .map(cookie -> cookie.getValue())
                            .orElse(null);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }

                if (accessToken == null) {
                    log.info("[SecurityConfig] oncePerRequestFilter()" +
                            "\n\tRequestURL : " + request.getRequestURL() +
                            "\n\tNo Cookie");
                } else {
                    try {
                        if (tokenProvider.validateToken(accessToken)) {
                            Authentication authentication = tokenProvider.getAuthentication(accessToken);
                            UserEntity userEntity = userMapper.findUserById(authentication.getName());
                            if (userEntity != null) {
                                log.info("[SecurityConfig] oncePerRequestFilter()" +
                                        "\n\tRequestURL : " + request.getRequestURL() +
                                        "\n\tUserEntity : " + userEntity);

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            } else {
                                log.error("[SecurityConfig] oncePerRequestFilter()" +
                                        "\n\tRequestURL : " + request.getRequestURL() +
                                        "\n\tUserEntity : Null");
                            }
                        }
                    } catch (ExpiredJwtException e) {
                        // TODO 토큰 만료시 처리(Refresh-token으로 갱신처리는 안함-쿠키에서 제거)
                        Cookie cookie = new Cookie(TokenProvider.ACCESS_TOKEN, null);
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    } catch (Exception e2) {
                    }
                }

                filterChain.doFilter(request, response);
            }
        };

        return oncePerRequestFilter;
    }

    // Oauth2 로그인 성공 Bean
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                TokenDto tokenInfo = tokenProvider.generateToken(authentication);

                Cookie accessTokenCookie = new Cookie(TokenProvider.ACCESS_TOKEN,
                        tokenInfo.getAccessToken());
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge((int) TokenProvider.ACCESS_TOKEN_EXPIRED_AT_SECONDS);
                response.addCookie(accessTokenCookie);

                Cookie refreshTokenCookie = new Cookie(TokenProvider.REFRESH_TOKEN,
                        tokenInfo.getRefreshToken());
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge((int) TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS);
                response.addCookie(refreshTokenCookie);

                log.info("[SecurityConfig] oAuth2SuccessHandler()" +
                        "\n\taccessTokenCookie : " + accessTokenCookie +
                        "\n\trefreshTokenCookie : " + refreshTokenCookie);

                response.sendRedirect("/");
            }
        };
        return successHandler;
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
