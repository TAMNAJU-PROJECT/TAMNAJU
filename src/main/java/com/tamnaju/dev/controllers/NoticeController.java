package com.tamnaju.dev.controllers;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tamnaju.dev.configs.jwt.TokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public void getWrite(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (request.getCookies() == null) {
            response.sendRedirect("/login");
        } else {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(TokenProvider.ACCESS_TOKEN)) {
                    log.info(cookie.getName());
                    log.info(cookie.getAttribute("isAdmin"));

                    return;
                }
            }
            response.sendRedirect("/");
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
