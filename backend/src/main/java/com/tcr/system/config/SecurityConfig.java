package com.tcr.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.filter.CorsFilter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CorsFilter corsFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("配置 Spring Security...");
        http
            // 禁用CSRF
            .csrf().disable()
            // 添加CORS过滤器
            .addFilterBefore(corsFilter, ChannelProcessingFilter.class)
            // 基于token，不需要session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 请求权限配置
            .authorizeRequests()
            // 允许所有请求
            .anyRequest().permitAll();
        
        System.out.println("Spring Security 配置完成");
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("创建密码编码器");
        return new BCryptPasswordEncoder();
    }
} 