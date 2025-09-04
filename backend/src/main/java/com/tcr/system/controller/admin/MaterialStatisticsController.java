package com.tcr.system.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.service.MaterialTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教学材料统计分析控制器
 */
@RestController
@RequestMapping("/api/admin/material-statistics")
public class MaterialStatisticsController {

    @Autowired
    private MaterialTaskService materialTaskService;

    /**
     * 获取统计概览数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(
            @RequestParam(required = false) Long materialTypeId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester) {
        
        Map<String, Object> overview = materialTaskService.getStatisticsOverview(materialTypeId, academicYear, semester);
        return Result.success(overview);
    }

    /**
     * 按材料类型统计
     */
    @GetMapping("/by-type")
    public Result<List<Map<String, Object>>> statisticsByType() {
        List<Map<String, Object>> statistics = materialTaskService.statisticsByType();
        return Result.success(statistics);
    }

    /**
     * 按学院统计
     */
    @GetMapping("/by-college")
    public Result<List<Map<String, Object>>> statisticsByCollege() {
        List<Map<String, Object>> statistics = materialTaskService.statisticsByCollege();
        return Result.success(statistics);
    }

    /**
     * 获取未完成任务的教师列表
     */
    @GetMapping("/uncompleted-teachers")
    public Result<IPage<Map<String, Object>>> getUncompletedTeachers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long materialTypeId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester) {
        
        Page<Map<String, Object>> page = new Page<>(current, size);
        IPage<Map<String, Object>> result = materialTaskService.getUncompletedTeachersPage(page, materialTypeId, academicYear, semester);
        return Result.success(result);
    }
} 