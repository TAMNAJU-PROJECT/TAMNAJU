package com.tamnaju.dev.controllers;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tamnaju.dev.domains.dtos.UserDto;
import com.tamnaju.dev.domains.results.user.UserJoinResult;
import com.tamnaju.dev.domains.services.UserService;
import com.tamnaju.dev.utilities.CustomAuthChecker;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.minidev.json.JSONObject;

@Controller
@RequestMapping(value = "/")
public class AuthenticationController {
    @Autowired
    private CustomAuthChecker authChecker;
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public void getLogin() {
    }

    @GetMapping("/logout")
    public void getLogout() {
    }

    @GetMapping("/join")
    public void getJoin(@RequestAttribute(value = "authentication", required = false) Authentication authentication,
            Model model) throws IOException {
        authChecker.applyAuthentication(authentication, model);
    }

    @PostMapping("/join")
    @ResponseBody
    public JSONObject postJoin(
            @RequestAttribute(value = "authentication", required = false) Authentication authentication,
            Model model,
            HttpServletResponse response,
            @Valid UserDto userDto,
            BindingResult bindingResult,
            @RequestParam(value = "birthStr") String birthStr) throws IOException {
        if (authentication != null) {
            response.sendRedirect("/");
            return null;
        }
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
