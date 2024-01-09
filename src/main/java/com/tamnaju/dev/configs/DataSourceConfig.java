package com.tamnaju.dev.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.Resource;

@Configuration
public class DataSourceConfig {
    @Bean
    public HikariDataSource dataSource() {
        // 연결된 DB의 상세 정보 등록
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc.mariadb://localhost:3306/tamnaju_db");
        dataSource.setUsername("admin");
        dataSource.setPassword("admin1234");
        return dataSource;
    }

    @Bean
    private Resource[] resolveMapperLocations() {
        try {
            // MyBatis XML 파일이 위치한 경로를 설정
            return (Resource[]) new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:/mappers/*.xml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
