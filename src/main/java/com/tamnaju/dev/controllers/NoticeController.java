package com.tamnaju.dev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Controller
@RequestMapping(value = "/notice")
public class NoticeController {
    @GetMapping(value = "/list")
    public void getList() {
    }

    @GetMapping(value = "/write")
    public void getWrite(HttpServletRequest request) {
        if (request.getAttribute("role") != null) {
            log.info(request.getAttribute("role").toString());
        }
    }

    @PostMapping(value = "/write")
    public JSONObject postWrite() {
        JSONObject responsObject = new JSONObject();
        return responsObject;
    }

    @GetMapping(value = "/read")
    public void getRead() {
    }

    @GetMapping(value = "/modify")
    public void getModify() {
    }
}
