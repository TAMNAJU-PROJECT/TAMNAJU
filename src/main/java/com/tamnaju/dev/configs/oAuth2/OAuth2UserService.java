package com.tamnaju.dev.configs.oAuth2;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.entities.UserEntity;
import com.tamnaju.dev.domains.mappers.UserMapper;

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

        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");

        // provider에 따른 조건 분기
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes;
        String providerId;
        switch (provider) {
            case "kakao":
                attributes = (Map<String, Object>) oAuth2User.getAttributes().get("properties");
                providerId = attributes.get("id").toString();
                break;
            case "naver":
                attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                providerId = attributes.get("id").toString();
                break;
            case "google":
                attributes = oAuth2User.getAttributes();
                providerId = attributes.get("sub").toString();
                break;
            default:
                throw new OAuth2AuthenticationException("Unexpected provider");
        }

        // DB에 user 정보 여부에 따른 처리
        UserDto userDto;
        UserEntity dbUserEntity = userMapper.findUserByProviderId(providerId);
        if (dbUserEntity == null) {
            UserEntity userEntity = UserEntity.builder()
                    .id(providerId.substring(0, 33))
                    .password(passwordEncoder.encode(oAuth2User.hashCode() + ""))
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userMapper.saveUser(userEntity);
            // DB에 없을 시, 새로 생성한 User 정보 할당
            userDto = UserDto.userEntityToUserDto(userEntity);
        } else {
            // DB에 있을 시, 기존 User 정보 할당
            userDto = UserDto.userEntityToUserDto(dbUserEntity);
        }

        PrincipalDetails oAuth2UserInfo = PrincipalDetails.builder()
                .userDto(userDto)
                .attributes(attributes)
                .accessToken(userRequest.getAccessToken().getTokenValue())
                .build();

        return oAuth2UserInfo;
    }
}
