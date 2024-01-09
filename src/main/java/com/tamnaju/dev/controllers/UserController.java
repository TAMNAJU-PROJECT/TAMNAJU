package com.tamnaju.dev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.results.UserJoinResult;
import com.tamnaju.dev.domains.services.UserService;

import jakarta.validation.Valid;
import net.minidev.json.JSONObject;

@Controller(value = "/user")
public class UserController {
    private UserService userService;

    @GetMapping("/join")
    public String getJoin() {
        return "user/join";
    }

    @PostMapping("/join")
    @ResponseBody
    public JSONObject postJoin(@Valid UserDto userDto, BindingResult bindingResult) {
        JSONObject responseObject = new JSONObject();
        UserJoinResult userJoinResult;
        if (bindingResult.hasFieldErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                responseObject.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            userJoinResult = UserJoinResult.FAILURE;
        } else {
            userJoinResult = userService.insertUserDto(responseObject, userDto);
        }
        responseObject.put("result", userJoinResult.name().toLowerCase());
        return responseObject;
    }
}
