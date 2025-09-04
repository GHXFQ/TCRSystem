package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String teacherName;

    /**
     * 课程封面
     */
    private String cover;

    /**
     * 上课时间
     */
    private String classTime;

    /**
     * 上课地点
     */
    private String classLocation;

    /**
     * 结课时间
     */
    private LocalDateTime endTime;

    /**
     * 结课形式：0-考试，1-大作业
     */
    private Integer endForm;

    /**
     * 结课地点（考试地点）
     */
    private String endLocation;

    /**
     * 状态：0-未开始，1-进行中，2-已结束
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
} 