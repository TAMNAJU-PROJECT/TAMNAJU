package com.tamnaju.dev.configs.jwt;

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
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider implements InitializingBean {
    public static final String AUTHORITIES_KEY = "jwt-authentication"; // 토큰에 key로 사용될 이름
    public static final long ACCESS_TOKEN_EXPIRED_AT_SECONDS = 60 * 5; // access 토큰을 유지할 시간(초)
    public static final long REFRESH_TOKEN_EXPIRED_AT_SECONDS = 60 * 60; // refresh 토큰을 유지할 시간(초)

    private static final String GRANT_TYPE = "Bearer";
    private final String cipherAccessKey; // base64로 인코딩된 비밀 access key
    private final String cipherRefreshKey; // base64로 인코딩된 비밀 refresh key
    private SecretKey accessKey; // 비밀 access key
    private SecretKey refreshKey; // 비밀 refresh key

    TokenProvider(
            @Value("${jwt.secret-access}") String cipherAccessKey,
            @Value("${jwt.secret-refresh}") String cipherRefreshKey) {
        this.cipherAccessKey = cipherAccessKey;
        this.cipherRefreshKey = cipherRefreshKey;
    }

    // Bean 등록이 끝날 때, secret키를 디코딩하여서 key에 할당
    @Override
    public void afterPropertiesSet() {
        byte[] accessKeyBytes = Decoders.BASE64.decode(cipherAccessKey);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);

        byte[] refreshKeyBytes = Decoders.BASE64.decode(cipherRefreshKey);
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
    }

    public TokenInfo generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 토큰의 만료 시간을 설정
        Date accessTokenExpirationIn = new Date(
                (new Date()).getTime() + TokenProvider.ACCESS_TOKEN_EXPIRED_AT_SECONDS * 1000);
        Date refreshTokenExpirationIn = new Date(
                (new Date()).getTime() + TokenProvider.REFRESH_TOKEN_EXPIRED_AT_SECONDS * 1000);

        // access token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(accessKey, SignatureAlgorithm.HS512)
                .setExpiration(accessTokenExpirationIn)
                .compact();

        // refresh token 생성
        String refreshToken = Jwts.builder()
                .signWith(refreshKey, SignatureAlgorithm.HS512)
                .setExpiration(refreshTokenExpirationIn)
                .compact();

        return TokenInfo.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 토큰으로 클레임을 만들고 이를 이용해 유저 객체를 만들어서 최종적으로 authentication 객체를 리턴
    public Authentication getAuthentication(String accessToken) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
    }

    // 토큰의 유효성 검증을 수행
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            // logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            // logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            // logger.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }
}
