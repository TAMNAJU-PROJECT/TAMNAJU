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

    // UserDto를 UserEntity로 변환하는 정적 메소드
    public static UserEntity userDtoToUserEntity(UserDto userDto) {
        return UserEntity.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .nickname(userDto.getNickname())
                .password(userDto.getPassword())
                .birth(userDto.getBirth())
                .isAdmin(userDto.isAdmin())
                .registeredAt(userDto.getRegisteredAt())
                .deletedAt(userDto.getDeletedAt())
                .suspendedAt(userDto.getSuspendedAt())
                .build();
    }
}
