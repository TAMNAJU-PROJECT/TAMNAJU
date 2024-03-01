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

import com.tamnaju.dev.domains.dtos.NoticeDto;
import com.tamnaju.dev.domains.services.NoticeService;
import com.tamnaju.dev.utilities.CustomAuthChecker;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Controller
@RequestMapping(value = "/notice")
public class NoticeController {
    @Autowired
    private CustomAuthChecker authChecker;
    @Autowired
    private NoticeService noticeService;

    @GetMapping(value = "/list")
    public void getList(@RequestAttribute(value = "authentication", required = false) Authentication authentication,
            Model model,
            NoticeDto noticeDto) throws IOException {
        authChecker.applyAuthentication(authentication, model);
        NoticeDto[] noticeDtos = noticeService.selectNotices(noticeDto);
        model.addAttribute("noticeDtos", noticeDtos);
    }

    @GetMapping(value = "/write")
    public void getWrite(@RequestAttribute(value = "authentication", required = false) Authentication authentication,
            Model model,
            HttpServletResponse response) throws IOException {
        if (authChecker.isAdmin(authentication)) {
            log.error("[NoticeController] getWrite()\n\tNot Admin");
            response.sendRedirect("/");
        }
        authChecker.applyAuthentication(authentication, model);
    }

    @PostMapping(value = "/write")
    public JSONObject postWrite(
            @RequestAttribute(value = "authentication", required = false) Authentication authentication,
            HttpServletResponse response) throws IOException {
        if (authChecker.isAdmin(authentication)) {
            log.warn("[NoticeController] postWrite()\n\tNot Admin");
            response.sendRedirect("/");
            return null;
        }
        JSONObject responsObject = new JSONObject();
        // TODO 공지사항을 db에 추가하는 로직 필요
        return responsObject;
    }

    @GetMapping(value = "/read")
    public void getRead(@RequestAttribute(value = "authentication", required = false) Authentication authentication) {
    }

    @GetMapping(value = "/modify")
    public void getModify(@RequestAttribute(value = "authentication", required = false) Authentication authentication) {
    }
}
