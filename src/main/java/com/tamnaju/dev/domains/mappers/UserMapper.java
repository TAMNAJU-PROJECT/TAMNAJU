package com.tamnaju.dev.domains.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tamnaju.dev.domains.entities.UserEntity;

@Mapper
public interface UserMapper {
    public UserEntity findUserById(@Param(value = "id") String id);

    public UserEntity findUserByProviderId(@Param(value = "providerId") String providerId);

    public int saveUser(UserEntity userEntity);
}
