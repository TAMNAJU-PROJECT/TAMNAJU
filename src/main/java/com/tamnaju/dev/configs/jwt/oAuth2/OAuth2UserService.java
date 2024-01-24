package com.tamnaju.dev.configs.jwt.oAuth2;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    private PasswordEncoder passwordEncoder;
    private UserMapper userMapper;

    OAuth2UserService(PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("OAuth2UserService loadUser() oAuth2User :\n"
                + oAuth2User.toString());

        Map<String, Object> attributes;
        String email;
        LocalDate birth;
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId;

        log.info("OAuth2UserService loadUser() oAuth2User.getAttributes() :\n"
                + oAuth2User.getAttributes());

        // provider에 따른 조건 분기
        switch (provider) {
            case "kakao":
                attributes = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
                email = attributes.get("email").toString();
                birth = LocalDate.parse(attributes.get("birthyear").toString()
                        + "-" + attributes.get("birthday").toString()
                                .substring(0, 2)
                        + "-" + attributes.get("birthday").toString()
                                .substring(2));
                providerId = provider + "_" + oAuth2User.getAttributes().get("id").toString();
                break;

            case "naver":
                attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                email = "";
                birth = LocalDate.now();
                providerId = attributes.get("id").toString();
                break;

            case "google":
                attributes = oAuth2User.getAttributes();
                email = "";
                birth = LocalDate.now();
                providerId = attributes.get("sub").toString();
                break;

            default:
                throw new OAuth2AuthenticationException("Unexpected provider");
        }

        log.info("OAuth2UserService loadUser() email :\n"
                + email);
        log.info("OAuth2UserService loadUser() birth :\n"
                + birth);
        log.info("OAuth2UserService loadUser() providerId :\n"
                + providerId);

        // DB에 user 정보 여부에 따른 처리
        UserDto userDto;
        UserEntity dbUserEntity = userMapper.findUserByProviderId(providerId);
        if (dbUserEntity == null) {
            UserEntity userEntity = UserEntity.builder()
                    .id(providerId)
                    .email(email)
                    .password(passwordEncoder.encode(oAuth2User.hashCode() + "").substring(0, 60))
                    .birth(birth)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userMapper.saveUser(userEntity);
            // DB에 없을 시, 새로 생성한 User 정보 할당
            userDto = UserDto.userEntityToUserDto(userEntity);

            log.info("OAuth2UserService loadUser() new member : " + providerId);
        } else {
            // DB에 있을 시, 기존 User 정보 할당
            userDto = UserDto.userEntityToUserDto(dbUserEntity);

            log.info("OAuth2UserService loadUser() old member : " + providerId);
        }

        PrincipalDetails oAuth2UserInfo = PrincipalDetails.builder()
                .userDto(userDto)
                .attributes(attributes)
                .accessToken(userRequest.getAccessToken().getTokenValue())
                .build();

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("OAUTH2_USER")),
                oAuth2User.getAttributes(), "id");
    }
}
