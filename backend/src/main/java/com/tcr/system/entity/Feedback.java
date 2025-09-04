package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈实体类
 */
@Data
@TableName("feedback")
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 父反馈ID，用于回复
     */
    private Long parentId;

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
     * 用户名，非数据库字段
     */
    @TableField(exist = false)
    private String userName;
    
    /**
     * 用户头像，非数据库字段
     */
    @TableField(exist = false)
    private String userAvatar;
    
    /**
     * 课程名称，非数据库字段
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 回复列表，非数据库字段
     */
    @TableField(exist = false)
    private List<Feedback> children;
} 