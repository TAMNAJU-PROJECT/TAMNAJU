package com.tamnaju.dev.domains.services;

import org.springframework.stereotype.Service;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;
import com.tamnaju.dev.domains.results.UserJoinResult;

import net.minidev.json.JSONObject;

@Service
public class UserService {
    private UserMapper userMapper;

    public UserJoinResult insertUserDto(JSONObject responseObject, UserDto userDto) {
        // PK에 대한 중복 여부 검증
        if (userMapper.findUserByEmail(userDto.getEmail()) != null) {
            return UserJoinResult.FAILURE_DUPLICATE_EMAIL;
        }
        UserEntity userEntity = UserDto.userDtoToUserEntity(userDto);
        return userMapper.saveUserEntity(userEntity) > 0
                ? UserJoinResult.SUCCESS
                : UserJoinResult.FAILURE;
    }
}
