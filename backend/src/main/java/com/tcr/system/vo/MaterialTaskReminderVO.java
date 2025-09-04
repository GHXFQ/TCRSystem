package com.tcr.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 教学材料任务提醒VO
 */
@Data
public class MaterialTaskReminderVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
} 