package com.tcr.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 作业传输对象
 */
@Data
public class AssignmentVO {
    
    /**
     * 作业ID
     */
    private Long id;
    
    /**
     * 作业标题
     */
    private String title;
    
    /**
     * 作业描述
     */
    private String description;
    
    /**
     * 所属课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 发布教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 截止日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    
    /**
     * 状态：0-已关闭，1-进行中，2-已结束
     */
    private Integer status;
    
    /**
     * 已提交学生数量
     */
    private Integer submittedCount;
    
    /**
     * 总学生数量
     */
    private Integer totalStudents;
    
    /**
     * 提交率
     */
    private String submissionRate;
    
    /**
     * 平均分数
     */
    private String avgScore;
    
    /**
     * 是否已提交（针对单个学生）
     */
    private Boolean submitted;
    
    /**
     * 提交状态：0-未提交，1-已提交，2-已批改
     * 注：此字段用于适配前端状态定义
     */
    private Integer submissionStatus;
    
    /**
     * 提交ID（如果已提交）
     */
    private Long submissionId;
    
    /**
     * 分数（如果已批改）
     */
    private Integer score;
    
    /**
     * 提交信息对象，用于前端显示
     * 包含：id, content, filePath, fileName, submitTime, isOverdue, status, score, comment等字段
     */
    private Map<String, Object> submissionInfo;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
} 