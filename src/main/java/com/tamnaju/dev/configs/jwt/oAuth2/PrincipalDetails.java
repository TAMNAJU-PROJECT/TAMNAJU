package com.tamnaju.dev.configs.jwt.oAuth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.tamnaju.dev.domains.dtos.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipalDetails implements UserDetails, OAuth2User {
    private UserDto userDto;
    private Map<String, Object> attributes;
    private String accessToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_USER";
            }
        });
        if (userDto.isAdmin()) {
            authorities.add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_ADMIN";
                }
            });
        }
        return authorities;
    }

    @Override
    public String getName() {
        return userDto.getName();
    }

    @Override
    public String getPassword() {
        return userDto.getPassword();
    }

    @Override
    public String getUsername() {
        return userDto.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userDto.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userDto.getSuspendedAt() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isAccountNonExpired() && this.isAccountNonLocked();
    }
}
