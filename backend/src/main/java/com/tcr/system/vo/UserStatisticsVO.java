package com.tcr.system.vo;

import lombok.Data;

/**
 * 用户统计VO
 */
@Data
public class UserStatisticsVO {
    /**
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 教师数量
     */
    private Integer teacherCount;
    
    /**
     * 学生数量
     */
    private Integer studentCount;
} 