package com.tcr.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 教学课程资源管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.tcr.system.mapper")
public class TcrSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcrSystemApplication.class, args);
    }

} 