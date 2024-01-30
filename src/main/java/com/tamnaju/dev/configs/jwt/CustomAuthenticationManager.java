package com.tamnaju.dev.configs.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.tamnaju.dev.configs.jwt.domains.CustomAuthentication;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {
    @Autowired
    private TokenProvider tokenProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthentication customAuthentication = new CustomAuthentication();

        return customAuthentication;
    }
}
