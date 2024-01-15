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
    private String id;
    private String email;
    private String name;
    private String password;
    private LocalDate birth;
    private boolean isAdmin;
    private LocalDateTime registeredAt;
    private LocalDateTime deletedAt;
    private LocalDateTime suspendedAt;

    // OAUTH2
    private String provider;
    private String providerId;

    // UserDto를 UserEntity로 변환하는 정적 메소드
    public static UserEntity userDtoToUserEntity(UserDto userDto) {
        return UserEntity.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .password(userDto.getPassword())
                .birth(userDto.getBirth())
                .isAdmin(userDto.isAdmin())
                .registeredAt(userDto.getRegisteredAt())
                .deletedAt(userDto.getDeletedAt())
                .suspendedAt(userDto.getSuspendedAt())
                .provider(userDto.getProvider())
                .providerId(userDto.getProviderId())
                .build();
    }
}
