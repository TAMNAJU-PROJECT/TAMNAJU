package com.tamnaju.dev.configs.jwt.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.tamnaju.dev.configs.jwt.CustomAuthenticationManager;
import com.tamnaju.dev.configs.jwt.TokenProvider;
import com.tamnaju.dev.configs.jwt.domains.TokenDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private TokenProvider tokenProvider;

    @Autowired
    public JwtUsernamePasswordAuthenticationFilter(CustomAuthenticationManager authenticationManager,
            TokenProvider tokenProvider) {
        super.setAuthenticationManager(authenticationManager);
        this.tokenProvider = tokenProvider;
    }

    // jwt 로그인
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getParameter("id").trim(),
                request.getParameter("password").trim());
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    // jwt 로그인 성공
    @Override
    public void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        log.info("[JwtUsernamePasswordAuthenticationFilter] successfulAuthentication()" +
                "\n\t" + authResult.getPrincipal());

        // token 생성
        TokenDto tokenDto = tokenProvider.generateToken(authResult);

        // access token 추가
        Cookie accessToken = new Cookie(TokenProvider.ACCESS_TOKEN, tokenDto.getAccessToken());
        accessToken.setMaxAge((int) TokenProvider.ACCESS_TOKEN_EXPIRED_AT_SECONDS);
        accessToken.setPath("/");
        response.addCookie(accessToken);

        // refresh token 추가
        Cookie refreshToken = new Cookie(TokenProvider.REFRESH_TOKEN, tokenDto.getRefreshToken());
        refreshToken.setMaxAge((int) TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS);
        refreshToken.setPath("/");
        response.addCookie(refreshToken);

        log.info("[JwtUsernamePasswordAuthenticationFilter] successfulAuthentication()" +
                "\n\tAccessToken : " + accessToken.toString() +
                "\n\tRefreshToken : " + refreshToken.toString());

        response.sendRedirect("/");
    }

    // jwt 로그인 실패
    @Override
    public void unsuccessfulAuthentication(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        log.info("[JwtUsernamePasswordAuthenticationFilter] unsuccessfulAuthentication() " +
                "\n\t" + failed.getMessage());

        response.sendRedirect("/login");
    }
}
