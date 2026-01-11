package com.band.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 乐队管理系统启动类
 * 
 * @author Band Management Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.band.management.mapper")
public class BandManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BandManagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("乐队管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("Druid监控: http://localhost:8080/druid");
        System.out.println("========================================");
    }
}
