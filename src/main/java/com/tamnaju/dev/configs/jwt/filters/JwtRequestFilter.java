package com.tamnaju.dev.configs.jwt.filters;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = null;

        try {
            if (request.getCookies() != null) {
                accessToken = Arrays.stream(request.getCookies())
                        .filter(cookie -> cookie.getName().equals(TokenProvider.ACCESS_TOKEN)).findFirst()
                        .map(cookie -> cookie.getValue())
                        .orElse(null);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        if (accessToken == null) {
            log.info("[JwtRequestFilter] doFilterInternal()" +
                    "\n\tMethod : " + request.getMethod() +
                    "\n\tRequestURL : " + request.getRequestURL() +
                    "\n\tNo Cookie");
        } else {
            try {
                if (tokenProvider.validateToken(accessToken)) {
                    Authentication authentication = tokenProvider.getAuthentication(accessToken);
                    UserEntity userEntity = userMapper.findUserById(authentication.getName());
                    if (userEntity != null) {
                        log.info("[JwtRequestFilter] doFilterInternal()" +
                                "\n\tMethod : " + request.getMethod() +
                                "\n\tRequestURL : " + request.getRequestURL() +
                                "\n\tUserEntity : " + userEntity);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.error("[JwtRequestFilter] doFilterInternal()" +
                                "\n\tMethod : " + request.getMethod() +
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
}
