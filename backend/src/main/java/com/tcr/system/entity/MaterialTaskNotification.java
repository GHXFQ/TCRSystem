package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教学材料任务通知实体类
 */
@Data
@TableName("material_task_notification")
public class MaterialTaskNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 状态：0-未读，1-已读
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
     * 非数据库字段 - 任务标题
     */
    @TableField(exist = false)
    private String taskTitle;
} 