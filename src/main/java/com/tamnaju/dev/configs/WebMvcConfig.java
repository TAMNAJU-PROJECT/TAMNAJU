package com.tamnaju.dev.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 특정 패턴의 자원 경로를 설정
        // TODO SecurityConfig 사용으로 인해, default 설정을 사용하지 못하는 것으로 추측되므로, 해당 코드를 추가하였으나,
        // 타당성을 파악할 필요가 있음
        registry.addResourceHandler("/scripts/**").addResourceLocations("classpath:/static/scripts/");
        registry.addResourceHandler("/stylesheets/**").addResourceLocations("classpath:/static/stylesheets/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
    }
}
