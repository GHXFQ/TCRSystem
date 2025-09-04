package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专业实体类
 */
@Data
@TableName("major")
public class Major implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专业名称
     */
    private String name;

    /**
     * 专业描述
     */
    private String description;

    /**
     * 所属学院ID
     */
    private Long collegeId;

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