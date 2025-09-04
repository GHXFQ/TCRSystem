package com.tcr.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 教学材料任务完成情况VO
 */
@Data
public class MaterialTaskProgressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总教师数
     */
    private Integer totalCount;

    /**
     * 已完成教师数
     */
    private Integer completedCount;

    /**
     * 已完成教师列表
     */
    private List<Map<String, Object>> completedTeachers;

    /**
     * 未完成教师列表
     */
    private List<Map<String, Object>> uncompletedTeachers;
} 