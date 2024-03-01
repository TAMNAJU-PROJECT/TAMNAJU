package com.tamnaju.dev.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tamnaju.dev.domains.services.UserService;
import com.tamnaju.dev.utilities.CustomAuthChecker;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Controller
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private CustomAuthChecker authChecker;
    @Autowired
    private UserService userService;

    @GetMapping("/my-info")
    public void getMyInfo(
            @RequestAttribute(value = "athentication", required = false) Authentication authentication,
            Model model,
            HttpServletResponse response) throws IOException {
        if (authentication == null) {
            log.warn("[UserController] getMyInfo()\n\tNot Login");
            response.sendRedirect("/");
            return;
        }
        authChecker.applyAuthentication(authentication, model);
    }

    @PostMapping("/my-info")
    public JSONObject postMyInfo(
            @RequestAttribute(value = "athentication", required = false) Authentication authentication,
            Model model,
            HttpServletResponse response) {
        // TODO 회원정보 수정 로직
        return null;
    }
}
