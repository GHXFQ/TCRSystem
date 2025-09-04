package com.tcr.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许跨域的头部信息
        config.addAllowedHeader("*");
        // 允许跨域的方法
        config.addAllowedMethod("*");
        // 允许跨域的域名，如果允许所有域名则设置为"*"
        config.addAllowedOriginPattern("*");
        // 是否允许发送Cookie信息
        config.setAllowCredentials(true);
        // 设置预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 添加映射路径，拦截一切请求
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 