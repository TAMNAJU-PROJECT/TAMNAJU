package com.tamnaju.dev.controllers;

import java.time.LocalDate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tamnaju.dev.configs.jwt.TokenDto;
import com.tamnaju.dev.configs.jwt.TokenProvider;
import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.results.user.UserJoinResult;
import com.tamnaju.dev.domains.services.UserService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Controller
@RequestMapping(value = "/")
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private TokenProvider tokenProvider;
    private UserService userService;

    AuthenticationController(AuthenticationManager authenticationManager,
            TokenProvider tokenProvider,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @GetMapping("/login")
    public void getLogin() {
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> postLogin(@Valid @RequestBody UserDto userDto) {
        log.info("1");
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken
                .unauthenticated(userDto.getId(), userDto.getPassword());

        // authenticate() 메소드가 호출될 시,
        // PrincipalUserDetailsService의 loadUserByUsername() 메소드가 작동
        log.info("2");
        Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        log.info("3");
        SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
        TokenDto tokenInfo = tokenProvider.generateToken(authenticationResponse);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(TokenProvider.ACCESS_TOKEN, "Bearer " + tokenInfo.getAccessToken());

        return new ResponseEntity<>(tokenInfo, httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/join")
    public void getJoin() {
    }

    @PostMapping("/join")
    @ResponseBody
    public JSONObject postJoin(@Valid UserDto userDto,
            BindingResult bindingResult,
            @RequestParam(value = "birthStr") String birthStr) {
        JSONObject responseObject = new JSONObject();
        UserJoinResult userJoinResult;
        // UserDto 정규화
        if (bindingResult.hasFieldErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                responseObject.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            userJoinResult = UserJoinResult.FAILURE;
        } else if (birthStr == null || !birthStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            responseObject.put("birthStr", "생년월일을 선택해야 합니다.");
            userJoinResult = UserJoinResult.FAILURE;
        } else {
            userDto.setBirth(LocalDate.parse(birthStr));
            userJoinResult = userService.insertUser(responseObject, userDto);
        }
        responseObject.put("result", userJoinResult.name().toLowerCase());
        return responseObject;
    }
}
