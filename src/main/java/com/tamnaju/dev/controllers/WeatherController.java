package com.tamnaju.dev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jejumap")
public class WeatherController {

    @GetMapping("/weather")
    public void getWeather() {
    }
}