package com.tamnaju.dev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/notice")
public class NoticeController {
    @GetMapping(value = "/list")
    public void getList() {
    }

    @GetMapping(value = "/write")
    public void getWrite() {
    }

    @GetMapping(value = "/read")
    public void getRead() {
    }

    @GetMapping(value = "/modify")
    public void getModify() {
    }
}
