package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教学材料任务完成记录实体类
 */
@Data
@TableName("material_task_completion")
public class MaterialTaskCompletion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
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
     * 材料ID
     */
    private Long materialId;

    /**
     * 完成时间
     */
    private LocalDateTime completionTime;

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
     * 非数据库字段 - 材料名称
     */
    @TableField(exist = false)
    private String materialName;

    /**
     * 非数据库字段 - 任务标题
     */
    @TableField(exist = false)
    private String taskTitle;
} 