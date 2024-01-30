package com.tamnaju.dev.configs.jwt.handlers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.tamnaju.dev.configs.jwt.TokenProvider;
import com.tamnaju.dev.configs.jwt.domains.TokenDto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
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

        log.info("[CustomOAuth2SuccessHandler] onAuthenticationSuccess()" +
                "\n\taccessTokenCookie : " + accessTokenCookie +
                "\n\trefreshTokenCookie : " + refreshTokenCookie);

        response.sendRedirect("/");
    }
}
