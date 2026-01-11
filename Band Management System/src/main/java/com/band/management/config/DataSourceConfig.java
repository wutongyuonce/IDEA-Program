package com.band.management.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 * 根据用户角色动态切换数据库连接
 * 
 * @author Band Management Team
 */
@Configuration
public class DataSourceConfig {

    /**
     * 管理员数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.admin")
    public DataSource adminDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 乐队用户数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.band")
    public DataSource bandDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 歌迷用户数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.fan")
    public DataSource fanDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 动态数据源
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceContextHolder.DataSourceType.ADMIN.getValue(), adminDataSource());
        targetDataSources.put(DataSourceContextHolder.DataSourceType.BAND.getValue(), bandDataSource());
        targetDataSources.put(DataSourceContextHolder.DataSourceType.FAN.getValue(), fanDataSource());
        
        dynamicDataSource.setTargetDataSources(targetDataSources);
        // 默认使用管理员数据源
        dynamicDataSource.setDefaultTargetDataSource(adminDataSource());
        
        return dynamicDataSource;
    }
}
