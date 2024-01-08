package com.tamnaju.dev.domains.dtos;

import org.springframework.format.annotation.DateTimeFormat;

import com.tamnaju.dev.domains.entities.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String email;
    private String name;
    private String nickname;
    private boolean isAdmin;
    private DateTimeFormat registeredAt;
    private DateTimeFormat deletedAt;
    private DateTimeFormat suspendedAt;

    public static UserEntity userDtoToUserEntity(UserDto userDto) {
        return UserEntity.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .nickname(userDto.getNickname())
                .isAdmin(userDto.isAdmin())
                .registeredAt(userDto.getRegisteredAt())
                .deletedAt(userDto.getDeletedAt())
                .suspendedAt(userDto.getSuspendedAt())
                .build();
    }
}
