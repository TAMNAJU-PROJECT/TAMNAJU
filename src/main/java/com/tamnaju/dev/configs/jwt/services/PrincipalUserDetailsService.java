package com.tamnaju.dev.configs.jwt.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.configs.jwt.PrincipalDetails;
import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PrincipalUserDetailsService implements UserDetailsService {
    private UserMapper userMapper;

    PrincipalUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[PrincipalUserDetailsService] loadUserByUsername() username :\n"
                + username);

        UserEntity userEntity = userMapper.findUserById(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("Exception UsernameNotFound : " + username);
        }
        if (userEntity.getSuspendedAt() != null) {
            throw new RuntimeException("Exception Suspended : " + username);
        }
        if (userEntity.getDeletedAt() != null) {
            throw new RuntimeException("Exception Deleted : " + username);
        }

        UserDto userDto = UserDto.userEntityToUserDto(userEntity);

        return PrincipalDetails.builder()
                .userDto(userDto)
                .build();
    }
}
