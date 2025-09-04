package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作业提交实体类
 */
@Data
@TableName("assignment_submission")
public class AssignmentSubmission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID
     */
    private Long assignmentId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 提交内容
     */
    private String content;

    /**
     * 文件路径
     */
    private String filePath;

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
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 非数据库字段 - 学生姓名
     */
    @TableField(exist = false)
    private String studentName;
    
    /**
     * 非数据库字段 - 作业标题
     */
    @TableField(exist = false)
    private String assignmentTitle;
    
    /**
     * 非数据库字段 - 课程ID
     */
    @TableField(exist = false)
    private Long courseId;
    
    /**
     * 非数据库字段 - 课程名称
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 非数据库字段 - 文件名
     */
    @TableField(exist = false)
    private String fileName;
} 