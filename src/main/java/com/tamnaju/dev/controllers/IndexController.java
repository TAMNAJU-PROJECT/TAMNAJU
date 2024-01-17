package com.tamnaju.dev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/")
public class IndexController {
    // TODO ModelAndView가 아닌 String 타입을 반환함
    // 적절한 판단인지 검토 필요
    @GetMapping("/")
    public String getIndex() {
        return "mainPage/index";
    }
}
