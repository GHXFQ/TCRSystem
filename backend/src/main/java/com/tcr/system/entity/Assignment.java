package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作业实体类
 */
@Data
@TableName("assignment")
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 发布教师ID
     */
    private Long teacherId;

    /**
     * 截止日期
     */
    private LocalDateTime deadline;

    /**
     * 状态：0-已关闭，1-进行中，2-已结束
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
     * 非数据库字段 - 教师姓名
     */
    @TableField(exist = false)
    private String teacherName;
    
    /**
     * 非数据库字段 - 课程名称
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 非数据库字段 - 是否已提交
     */
    @TableField(exist = false)
    private Boolean submitted;
    
    /**
     * 非数据库字段 - 提交ID
     */
    @TableField(exist = false)
    private Long submissionId;
    
    /**
     * 非数据库字段 - 得分
     */
    @TableField(exist = false)
    private Integer score;
} 