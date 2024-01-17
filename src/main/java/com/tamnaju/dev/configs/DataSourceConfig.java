package com.tamnaju.dev.configs;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@MapperScan("com.tamnaju.dev.domains.mappers")
public class DataSourceConfig {
    @Autowired
    ApplicationContext applicationContext;
    
    @Bean
    public HikariDataSource dataSource() {
        // 연결된 DB의 상세 정보 등록
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mariadb://localhost:3306/tamnaju_db");
        dataSource.setUsername("admin");
        dataSource.setPassword("admin1234");
        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setMapperLocations(applicationContext.getResources("classpath:mappers/*.xml"));
        return sessionFactory.getObject();
    }
}
