package com.tamnaju.dev.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public HikariDataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc.mariadb://localhost:3306/tamnaju_db");
        dataSource.setUsername("admin");
        dataSource.setPassword("admin1234");
        return dataSource;
    }
}
