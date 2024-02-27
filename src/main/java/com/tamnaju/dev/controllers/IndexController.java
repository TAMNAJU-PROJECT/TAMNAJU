package com.tamnaju.dev.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/")
public class IndexController {
    @GetMapping("/")
    public String getIndex(
            @RequestAttribute(value = "authentication", required = false) Authentication authentication,
            Model model) {
        model.addAttribute("authentication", authentication);
        return "index";
    }
}
