package com.tamnaju.dev.domains.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;
import com.tamnaju.dev.domains.results.UserJoinResult;

import net.minidev.json.JSONObject;

@Service
public class UserService {
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserJoinResult insertUserDto(JSONObject responseObject, UserDto userDto) {
        UserEntity userEntity = UserDto.userDtoToUserEntity(userDto);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        // PK에 대한 중복 여부 검증
        if (userMapper.findUserByEmail(userEntity.getEmail()) != null) {
            return UserJoinResult.FAILURE_DUPLICATE_EMAIL;
        }
        return userMapper.saveUser(userEntity) > 0
                ? UserJoinResult.SUCCESS
                : UserJoinResult.FAILURE;
    }
}
