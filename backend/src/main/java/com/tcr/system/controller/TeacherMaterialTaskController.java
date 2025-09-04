package com.tcr.system.controller;

import com.tcr.system.common.Result;
import com.tcr.system.service.MaterialTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师端教学材料任务控制器
 */
@RestController
@RequestMapping("/api/material-task")
public class TeacherMaterialTaskController {

    @Autowired
    private MaterialTaskService materialTaskService;

    /**
     * 获取教师的任务列表
     */
    @GetMapping("/teacher")
    public Result<List<Map<String, Object>>> getTeacherTasks(@RequestParam Long teacherId) {
        List<Map<String, Object>> tasks = materialTaskService.getTeacherTasks(teacherId);
        return Result.success(tasks);
    }

    /**
     * 标记任务已读
     */
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id, @RequestParam Long teacherId) {
        // 实现标记任务通知已读的逻辑
        return Result.success();
    }
} 