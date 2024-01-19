package com.tamnaju.dev.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.results.UserJoinResult;
import com.tamnaju.dev.domains.services.UserService;

import net.minidev.json.JSONObject;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/join")
    public void getJoin() {
    }

    @PostMapping("/join")
    @ResponseBody
    public JSONObject postJoin(@Validated UserDto userDto,
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

    @GetMapping("/login")
    public void getLogin() {
    }

    // // TODO session 등, 로그인 구현 필요
    // @PostMapping("/login")
    // @ResponseBody
    // public JSONObject postLogin(@Validated UserDto userDto,
    //         BindingResult bindingResult) {
    //     JSONObject responseObject = new JSONObject();
    //     UserLoginResult userLoginResult;
    //     if (bindingResult.hasFieldErrors("email") || bindingResult.hasFieldErrors("password")) {
    //         userLoginResult = UserLoginResult.FAILURE;
    //     } else {
    //         userLoginResult = userService.selectUserByEmailAndPassword(userDto);
    //     }
    //     responseObject.put("result", userLoginResult.name().toLowerCase());
    //     return responseObject;
    // }
}
