package com.tamnaju.dev.domains.entities;

import org.springframework.format.annotation.DateTimeFormat;

import com.tamnaju.dev.domains.dtos.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    private String email;
    private String name;
    private String nickname;
    private boolean isAdmin;
    private DateTimeFormat registeredAt;
    private DateTimeFormat deletedAt;
    private DateTimeFormat suspendedAt;

    public static UserDto userEntityToUserDto(UserEntity userEntity) {
        return UserDto.builder()
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .nickname(userEntity.getNickname())
                .isAdmin(userEntity.isAdmin())
                .registeredAt(userEntity.getRegisteredAt())
                .deletedAt(userEntity.getDeletedAt())
                .suspendedAt(userEntity.getSuspendedAt())
                .build();
    }
}
