package com.tamnaju.dev.configs.jwt;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.mappers.UserMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenProvider implements InitializingBean {
    public static final String ACCESS_TOKEN = "tamnaju-access";
    public static final String REFRESH_TOKEN = "tamnaju-refresh";
    public static final String AUTHORITIES_KEY = "authorities-key"; // 토큰에 key로 사용될 이름
    public static final int ACCESS_TOKEN_EXPIRED_AT_SECONDS = 60 * 5; // access 토큰을 유지할 시간(초)
    public static final long REFRESH_TOKEN_EXPIRED_AT_SECONDS = 60 * 60; // refresh 토큰을 유지할 시간(초)

    private static final String GRANT_TYPE = "Bearer";
    private final String cipherAccessKey; // base64로 인코딩된 비밀 access key

    private UserMapper userMapper;

    private SecretKey accessKey; // 비밀 access key
    private SecretKey refreshKey; // 비밀 refresh key

    TokenProvider(@Value("${jwt.secret-access}") String cipherAccessKey,
            UserMapper userMapper) {
        this.cipherAccessKey = cipherAccessKey;
        this.userMapper = userMapper;
    }

    // Bean 등록이 끝날 때, secret키를 디코딩해서 key에 할당
    @Override
    public void afterPropertiesSet() {
        byte[] accessKeyBytes = Decoders.BASE64.decode(cipherAccessKey);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);

        byte[] randomRefreshKey = new byte[256 / 8];
        new SecureRandom().nextBytes(randomRefreshKey);
        byte[] refreshKeyBytes = randomRefreshKey;
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
    }

    /**
     * 토큰 생성
     */
    public TokenDto generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        // 토큰의 만료 시간을 설정
        Date accessTokenExpirationIn = new Date(
                (new Date()).getTime() + TokenProvider.ACCESS_TOKEN_EXPIRED_AT_SECONDS * 1000);
        Date refreshTokenExpirationIn = new Date(
                (new Date()).getTime() + TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS * 1000);

        PrincipalDetails principalDetails = ((PrincipalDetails) authentication.getPrincipal());
        UserDto userDto = principalDetails.getUserDto();

        // access token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpirationIn)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();

        // refresh token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpirationIn)
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();

        log.info("[TokenProvider] generateToken()" +
                "\n\tauthorities : " + authorities +
                "\n\tuserDto : " + userDto +
                "\n\taccessToken : " + accessToken +
                "\n\trefreshToken : " + refreshToken);

        return TokenDto.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * access token을 사용해서 신원을 확인
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(accessKey).build()
                .parseClaimsJws(accessToken).getBody();

        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDto userDto = UserDto.userEntityToUserDto(userMapper.findUserById(claims.getSubject()));

        PrincipalDetails principalDetails = PrincipalDetails.builder()
                .userDto(userDto)
                .accessToken(accessToken)
                .build();

        return new UsernamePasswordAuthenticationToken(principalDetails, accessToken, authorities);
    }

    /**
     * Token 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(accessKey).build()
                    .parseClaimsJws(token);

            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("[TokenProvider] validateToken() :\\n" + //
                    "Bad Jwt Token Exception");
        } catch (ExpiredJwtException e) {
            log.info("[TokenProvider] validateToken() :\\n" + //
                    "Expired Jwt Token Exception");
        } catch (UnsupportedJwtException e) {
            log.info("[TokenProvider] validateToken() :\\n" + //
                    "Unsupported Jwt Token Exception");
        } catch (IllegalArgumentException e) {
            log.info("[TokenProvider] validateToken() :\\n" + //
                    "Illegal Argument Exception");
        }

        return false;
    }
}
