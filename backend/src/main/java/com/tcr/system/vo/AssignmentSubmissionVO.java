package com.tcr.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交传输对象
 */
@Data
public class AssignmentSubmissionVO {
    
    /**
     * 提交ID
     */
    private Long id;
    
    /**
     * 作业ID
     */
    private Long assignmentId;
    
    /**
     * 作业标题
     */
    private String assignmentTitle;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 提交内容
     */
    private String content;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 得分
     */
    private Integer score;
    
    /**
     * 教师评语
     */
    private String comment;
    
    /**
     * 状态：0-已提交，1-已批改
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 提交时间是否超过截止日期
     */
    private Boolean isOverdue;
    
    /**
     * 课程名称
     */
    private String courseName;
} 