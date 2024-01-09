package com.tamnaju.dev.domains.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tamnaju.dev.domains.entities.UserEntity;

@Mapper
public interface UserMapper {
    public UserEntity findUserByEmail(@Param(value = "email") String email);

    public int saveUserEntity(UserEntity userEntity);
}
