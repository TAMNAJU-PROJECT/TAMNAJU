package com.tamnaju.dev.configs.jwt.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.configs.jwt.domains.PrincipalDetails;
import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PrincipalUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[PrincipalUserDetailsService] loadUserByUsername()" +
                "\n\tUsername : " + username);

        UserEntity userEntity = userMapper.findUserById(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("[PrincipalUserDetailsService] loadUserByUsername()" +
                    "\n\tException UsernameNotFound : " + username);
        }
        if (userEntity.getSuspendedAt() != null) {
            throw new RuntimeException("[PrincipalUserDetailsService] loadUserByUsername()" +
                    "\n\tException Suspended : " + username);
        }
        if (userEntity.getDeletedAt() != null) {
            throw new RuntimeException("[PrincipalUserDetailsService] loadUserByUsername()" +
                    "\n\tException Deleted : " + username);
        }

        UserDto userDto = UserDto.userEntityToUserDto(userEntity);

        return PrincipalDetails.builder()
                .userDto(userDto)
                .build();
    }
}
