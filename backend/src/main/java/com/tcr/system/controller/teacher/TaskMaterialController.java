package com.tcr.system.controller.teacher;

import com.tcr.system.common.Result;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.service.MaterialService;
import com.tcr.system.service.MaterialTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;

@Api(tags = "教师-任务材料提交")
@RestController
@RequestMapping("/api/teacher/task-material")
public class TaskMaterialController {

    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private MaterialTaskService materialTaskService;

    @ApiOperation("提交任务材料")
    @PostMapping("/upload")
    public Result uploadTaskMaterial(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("courseId") Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("type") Integer type,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") Integer semester,
            @RequestParam("materialTypeId") Long materialTypeId,
            @RequestParam("taskId") Long taskId) {
        try {
            // 1. 上传材料
            Long tempMaterialId = materialService.uploadTaskMaterial(
                name, description, courseId, file, userId, type, 
                academicYear, semester, materialTypeId, taskId
            );
            
            return Result.success(tempMaterialId);
        } catch (Exception e) {
            return Result.error("提交任务材料失败：" + e.getMessage());
        }
    }
    
    @ApiOperation("获取任务详情")
    @GetMapping("/task-detail/{taskId}/{courseId}")
    public Result getTaskDetail(@PathVariable Long taskId, @PathVariable Long courseId) {
        try {
            // 先获取任务基本信息
            MaterialTask task = materialTaskService.getTaskById(taskId);
            if (task == null) {
                return Result.error("任务不存在");
            }
            
            // 构建结果
            Map<String, Object> taskDetail = new HashMap<>();
            taskDetail.put("taskId", task.getId());
            taskDetail.put("title", task.getTitle());
            taskDetail.put("description", task.getDescription());
            taskDetail.put("materialTypeId", task.getMaterialTypeId());
            taskDetail.put("materialTypeName", task.getMaterialTypeName());
            taskDetail.put("academicYear", task.getAcademicYear());
            taskDetail.put("semester", task.getSemester());
            taskDetail.put("deadline", task.getDeadline());
            taskDetail.put("courseId", courseId);
            
            return Result.success(taskDetail);
        } catch (Exception e) {
            return Result.error("获取任务详情失败：" + e.getMessage());
        }
    }
} 