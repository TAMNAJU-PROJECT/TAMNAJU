package com.tamnaju.dev.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tamnaju.dev.utilities.CustomAuthChecker;

@Controller
@RequestMapping(value = "/")
public class IndexController {
    @Autowired
    private CustomAuthChecker authChecker;

    @GetMapping("/")
    public String getIndex(
            @RequestAttribute(value = "authentication", required = false) Authentication authentication,
            Model model) throws IOException {
        authChecker.applyAuthentication(authentication, model);
        return "index";
    }
}
