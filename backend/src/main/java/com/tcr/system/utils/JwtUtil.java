package com.tcr.system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            String username = getClaimFromToken(token, Claims::getSubject);
            System.out.println("从token中获取用户名: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("获取用户名时发生异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Long userId = getClaimFromToken(token, claims -> claims.get("userId", Long.class));
            System.out.println("从token中获取用户ID: " + userId);
            return userId;
        } catch (Exception e) {
            System.out.println("获取用户ID时发生异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取指定的claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中获取所有的claims
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            System.out.println("解析token: " + token);
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            System.out.println("解析token成功，claims: " + claims);
            return claims;
        } catch (ExpiredJwtException e) {
            System.out.println("Token已过期: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            System.out.println("不支持的Token: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println("Token格式错误: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            System.out.println("Token签名验证失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("解析Token时发生异常: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean isExpired = expiration.before(new Date());
            System.out.println("Token过期时间: " + expiration + ", 是否已过期: " + isExpired);
            return isExpired;
        } catch (Exception e) {
            System.out.println("检查Token是否过期时发生异常: " + e.getMessage());
            return true;
        }
    }

    /**
     * 生成token
     */
    public String generateToken(String username, Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        String token = doGenerateToken(claims, username);
        System.out.println("生成token: " + token);
        return token;
    }

    /**
     * 生成token的具体实现
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration);

        System.out.println("生成token，subject: " + subject + ", 过期时间: " + expirationDate);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            boolean isValid = (tokenUsername.equals(username) && !isTokenExpired(token));
            System.out.println("验证token，token中的用户名: " + tokenUsername + ", 传入的用户名: " + username + ", 是否有效: " + isValid);
            return isValid;
        } catch (Exception e) {
            System.out.println("验证token时发生异常: " + e.getMessage());
            return false;
        }
    }
} 