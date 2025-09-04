package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级实体类
 */
@Data
@TableName("class")
public class Class implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 所属专业ID
     */
    private Long majorId;

    /**
     * 入学年份
     */
    private String year;

    /**
     * 班级描述
     */
    private String description;

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
     * 专业名称（非数据库字段）
     */
    @TableField(exist = false)
    private String majorName;
    
    /**
     * 学院名称（非数据库字段）
     */
    @TableField(exist = false)
    private String collegeName;
    
    /**
     * 学生数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer studentCount;
} 