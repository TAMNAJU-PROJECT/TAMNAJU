package com.tamnaju.dev.configs.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Autowired
    JwtUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager,
            TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    // jwt 로그인
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                request.getParameter("id"),
                request.getParameter("password"));
        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    // jwt 로그인 성공
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        log.info("[JwtUsernamePasswordAuthenticationFilter] successfulAuthentication() :\n"
                + authResult.getPrincipal().toString());

        // token 생성
        TokenDto tokenDto = tokenProvider.generateToken(authResult);

        // access token 추가
        Cookie accessToken = new Cookie(TokenProvider.ACCESS_TOKEN, tokenDto.getAccessToken());
        accessToken.setMaxAge((int) TokenProvider.ACCESS_TOKEN_EXPIRED_AT_SECONDS);
        accessToken.setPath("/");
        response.addCookie(accessToken);

        log.info("[JwtUsernamePasswordAuthenticationFilter] successfulAuthentication() accessToken :\n"
                + accessToken.toString());

        // refresh token 추가
        Cookie refreshToken = new Cookie(TokenProvider.REFRESH_TOKEN, tokenDto.getRefreshToken());
        refreshToken.setMaxAge((int) TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS);
        refreshToken.setPath("/");
        response.addCookie(refreshToken);

        log.info("[JwtUsernamePasswordAuthenticationFilter] successfulAuthentication() refreshToken :\n"
                + refreshToken.toString());

        response.sendRedirect("/");
    }

    // jwt 로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        log.info("[JwtUsernamePasswordAuthenticationFilter] unsuccessfulAuthentication() :\n" + request.getLocalAddr());

        response.sendRedirect("/login");
    }

}
