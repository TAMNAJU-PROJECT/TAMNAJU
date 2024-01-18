package com.tamnaju.dev.configs.oAuth2;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

@Service
public class PrincipalDetailsService implements UserDetailsService {
    private UserMapper userMapper;

    PrincipalDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userMapper.findUserByProviderId(username);
        if (userEntity == null) {
            return null;
        }
        UserDto userDto = UserDto.userEntityToUserDto(userEntity);
        PrincipalDetails principalDetails = PrincipalDetails.builder()
                .userDto(userDto)
                .build();
        return principalDetails;
    }

}
