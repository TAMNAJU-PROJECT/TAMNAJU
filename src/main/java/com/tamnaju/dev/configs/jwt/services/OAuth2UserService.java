package com.tamnaju.dev.configs.jwt.services;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.configs.CustomPasswordEncoder;
import com.tamnaju.dev.configs.jwt.domains.PrincipalDetails;
import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    private CustomPasswordEncoder passwordEncoder;
    private UserMapper userMapper;

    OAuth2UserService(CustomPasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes;
        String email;
        String name;
        LocalDate birth;
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId;

        log.info("[OAuth2UserService] loadUser() : step 1" +
                "\n\tattributes : " + oAuth2User.getAttributes());

        // provider에 따른 조건 분기
        switch (provider) {
            case "kakao":
                attributes = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
                email = attributes.get("email").toString();
                name = ((Map<String, String>) attributes.get("profile")).get("nickname");
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
                name = "";
                birth = LocalDate.now();
                providerId = attributes.get("id").toString();
                break;

            case "google":
                attributes = oAuth2User.getAttributes();
                email = "";
                name = "";
                birth = LocalDate.now();
                providerId = attributes.get("sub").toString();
                break;

            default:
                throw new OAuth2AuthenticationException("Unexpected provider");
        }

        log.info("[OAuth2UserService] loadUser() : step 2" +
                "\n\temail : " + email +
                "\n\tname : " + name +
                "\n\tbirth : " + birth +
                "\n\tproviderId : " + providerId);

        // DB에 user 정보 여부에 따른 처리
        UserDto userDto;
        UserEntity dbUserEntityByProviderId = userMapper.findUserByProviderId(providerId);
        if (dbUserEntityByProviderId == null) {
            UserEntity userEntity;
            UserEntity dbUserEntityByEmail = userMapper.findUserByEmail(email);
            if (dbUserEntityByEmail == null) {
                userEntity = UserEntity.builder()
                        .id(providerId)
                        .email(email)
                        .name(name)
                        .password(passwordEncoder.encode(oAuth2User.hashCode() + "").substring(0, 60))
                        .birth(birth)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                userMapper.saveUser(userEntity);
            } else {
                userEntity = UserEntity.builder()
                        .id(dbUserEntityByEmail.getId())
                        .email(dbUserEntityByEmail.getEmail())
                        .name(name)
                        .password(dbUserEntityByEmail.getPassword())
                        .birth(birth)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                userMapper.modifyUserByEmail(userEntity);
            }

            // DB에 없을 시, 새로 생성한 User 정보 할당
            userDto = UserDto.userEntityToUserDto(userEntity);

            log.info("[OAuth2UserService] loadUser() : done" +
                    "\n\tnew member : " + providerId);
        } else {
            // DB에 있을 시, 기존 User 정보 할당
            userDto = UserDto.userEntityToUserDto(dbUserEntityByProviderId);

            log.info("[OAuth2UserService] loadUser() : done" +
                    "\n\told member : " + providerId);
        }

        PrincipalDetails principalDetails = PrincipalDetails.builder()
                .userDto(userDto)
                .attributes(attributes)
                .accessToken(userRequest.getAccessToken().getTokenValue())
                .build();

        return principalDetails;
    }
}
