package com.tamnaju.dev.configs;

import java.io.IOException;
import java.net.URLEncoder;
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

import com.tamnaju.dev.configs.oAuth2.jwt.TokenInfo;
import com.tamnaju.dev.configs.oAuth2.jwt.TokenProvider;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private UserMapper userMapper;
    private TokenProvider tokenProvider;

    SecurityConfig(UserMapper userMapper, TokenProvider tokenProvider) {
        this.userMapper = userMapper;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // TODO CSRF 비활성화, 추후 활성화 여부 판단 필요
                .csrf(AbstractHttpConfigurer::disable)

                // session 무효화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 특정 URL에 대한 접근 권한 설정
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // TODO 로그인 하지 않은 사용자에게만 로그인 페이지 접근 허용
                        .requestMatchers("/join", "/login", "/login/**").permitAll()
                        // 최상위 경로와 추가 자원 경로 허용
                        .requestMatchers("/", "images/**", "/scripts/**", "/stylesheets/**").permitAll()
                        .requestMatchers("/user/**", "/notice/**").permitAll()
                        .requestMatchers("/notice/write", "/notice/modify").hasRole("ADMIN")
                        .anyRequest().authenticated())

                // 로그인
                .formLogin(login -> login
                        .loginPage("/login").permitAll()
                        .successHandler(successHandler())
                        .failureHandler(failureHandler()))

                // JWT
                .addFilterBefore(oncePerRequestFilter(), BasicAuthenticationFilter.class)

                // Oauth2 로그인
                .oauth2Login(oauth2Configurer -> oauth2Configurer
                        .loginPage("/login").permitAll()
                        .successHandler(oAuth2SuccessHandler())
                        .defaultSuccessUrl("/"))

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
                exception
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
                // TODO 로그인 성공 시, 쿠키 발급 등
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
                // TODO 로그인 실패 시
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
                String token = null;
                String importAuth = null;

                try {
                    if (request.getRequestURI().equals("/join")) {
                        Cookie[] cookies = request.getCookies();
                        if (cookies != null) {
                            importAuth = Arrays.stream(cookies)
                                    .filter(cookie -> cookie.getName().equals("importAuth")).findFirst()
                                    .map(cookie -> cookie.getValue())
                                    .orElse(null);
                        }
                        if (importAuth == null) {
                            throw new Exception("쿠키가 존재하지 않습니다");
                        } else {
                            // cookie 에서 JWT token을 가져옵니다.
                            token = Arrays.stream(request.getCookies())
                                    .filter(cookie -> cookie.getName().equals(TokenProvider.AUTHORITIES_KEY))
                                    .findFirst()
                                    .map(cookie -> cookie.getValue())
                                    .orElse(null);
                        }
                    }
                } catch (Exception e) {
                    response.sendRedirect("/login?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
                    return;
                }

                try {
                    if (token == null) {
                        // cookie 에서 JWT token을 가져옵니다.
                        token = Arrays.stream(request.getCookies())
                                .filter(cookie -> cookie.getName().equals(TokenProvider.AUTHORITIES_KEY)).findFirst()
                                .map(cookie -> cookie.getValue())
                                .orElse(null);
                    }
                } catch (Exception ignored) {
                    // 일반적으로 접근하는 요청 URI에 대한 쿠키 예외는 무시한다..
                }

                if (token != null) {
                    try {
                        if (tokenProvider.validateToken(token)) {
                            Authentication authentication = tokenProvider.getAuthentication(token);
                            UserEntity userEntity = userMapper.findUserById(authentication.getName());
                            if (userEntity != null) {
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        }
                    } catch (ExpiredJwtException e) // 토큰만료시 예외처리(쿠키 제거)
                    {
                        System.out.println("[JWTAUTHORIZATIONFILTER] : ...ExpiredJwtException ...." + e.getMessage());
                        // 토큰 만료시 처리(Refresh-token으로 갱신처리는 안함-쿠키에서 제거)
                        Cookie cookie = new Cookie(TokenProvider.AUTHORITIES_KEY, null);
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
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                TokenInfo tokenInfo = tokenProvider.generateToken(authentication);

                Cookie cookie = new Cookie(TokenProvider.AUTHORITIES_KEY, tokenInfo.getAccessToken());
                cookie.setPath("/");
                cookie.setMaxAge((int) TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS);

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
