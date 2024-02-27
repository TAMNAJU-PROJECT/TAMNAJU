package com.tamnaju.dev.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.results.user.UserJoinResult;
import com.tamnaju.dev.domains.services.UserService;

import jakarta.validation.Valid;
import net.minidev.json.JSONObject;

@Controller
@RequestMapping(value = "/")
public class AuthenticationController {
    private UserService userService;

    AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public void getLogin() {
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
            userJoinResult = userService.insertUser(userDto);
        }
        responseObject.put("result", userJoinResult.name().toLowerCase());
        return responseObject;
    }
}
