package com.band.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 * 禁用默认的安全配置，使用自定义的Session认证
 * 
 * @author Band Management Team
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为我们使用Session认证）
            .csrf().disable()
            // 允许所有请求
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
            // 禁用HTTP Basic认证
            .httpBasic().disable()
            // 禁用表单登录
            .formLogin().disable()
            // 禁用登出
            .logout().disable();
        
        return http.build();
    }
}
