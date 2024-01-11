package com.tamnaju.dev.domains.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tamnaju.dev.domains.entities.UserEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    @NotBlank
    @Pattern(regexp = "^(?=[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}).{1,50}$", message = "서버가 인식할 수 없는 형식의 Email입니다.")
    private String email;

    @Pattern(regexp = "^[A-Za-z가-힣\\s]{2,10}$", message = "영어 또는 한글로 된 10자 이하의 이름을 입력해야 합니다.")
    private String name;

    @Pattern(regexp = "^[A-Za-z가-힣\\s]{2,10}$", message = "영어 또는 한글로 된 10자 이하의 별명을 입력해야 합니다.")
    private String nickname;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,60}$", message = "영어 대소문자, 숫자 및 특수문자를 포함한 8자 이상의 비밀번호를 입력해야 합니다.")
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
