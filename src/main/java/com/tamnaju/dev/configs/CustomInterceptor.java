package com.tamnaju.dev.configs;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerInterceptor;

import com.tamnaju.dev.configs.jwt.TokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenProvider tokenProvider;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        Authentication authentication;

        Cookie[] cookies = request.getCookies();
        try {
            if (cookies != null) {
                String accessToken = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(TokenProvider.ACCESS_TOKEN)).findFirst()
                        .map(cookie -> cookie.getValue())
                        .orElse(null);
                authentication = tokenProvider.getAuthentication(accessToken);
            } else {
                authentication = null;
            }

            request.setAttribute("authentication", authentication);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return true;
    }
}
