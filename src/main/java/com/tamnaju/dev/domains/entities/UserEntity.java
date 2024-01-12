package com.tamnaju.dev.domains.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String password;
    private LocalDate birth;
    private boolean isAdmin;
    private LocalDateTime registeredAt;
    private LocalDateTime deletedAt;
    private LocalDateTime suspendedAt;

    // UserEntity를 UserDto로 변환하는 정적 메소드
    public static UserDto userEntityToUserDto(UserEntity userEntity) {
        return UserDto.builder()
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .nickname(userEntity.getNickname())
                .password(userEntity.getPassword())
                .birth(userEntity.getBirth())
                .isAdmin(userEntity.isAdmin())
                .registeredAt(userEntity.getRegisteredAt())
                .deletedAt(userEntity.getDeletedAt())
                .suspendedAt(userEntity.getSuspendedAt())
                .build();
    }
}
