package com.tamnaju.dev.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tamnaju.dev.domains.services.UserService;


@Controller
@RequestMapping(value = "/user")
public class UserController {
    private UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

}
