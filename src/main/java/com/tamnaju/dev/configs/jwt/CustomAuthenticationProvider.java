package com.tamnaju.dev.configs.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.tamnaju.dev.configs.CustomPasswordEncoder;
import com.tamnaju.dev.configs.jwt.domains.PrincipalDetails;
import com.tamnaju.dev.configs.jwt.services.PrincipalUserDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private PrincipalUserDetailsService principalUserDetailsService;
    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String id = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();

        PrincipalDetails userDetails = (PrincipalDetails) principalUserDetailsService.loadUserByUsername(id);

        if (userDetails == null) {
            throw new BadCredentialsException("[CustomAuthenticationProvider] authenticate()" +
                    "\n\tUser Not Found");
        } else if (!userDetails.getPassword().matches(passwordEncoder.encode(password))) {
            throw new BadCredentialsException("[CustomAuthenticationProvider] authenticate()" +
                    "\n\tPassword Not Matched");
        }

        log.info("[CustomAuthenticationProvider] authenticate()" +
                "\n\t");

        return new UsernamePasswordAuthenticationToken(id, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // TODO authentication 유형 검증 확인 필요
        return true;
    }
}
