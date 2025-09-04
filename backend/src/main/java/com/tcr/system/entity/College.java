package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学院实体类
 */
@Data
@TableName("college")
public class College implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学院ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学院名称
     */
    private String name;

    /**
     * 学院描述
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
} 