package com.tcr.system.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.service.MaterialTaskService;
import com.tcr.system.vo.MaterialTaskProgressVO;
import com.tcr.system.vo.MaterialTaskReminderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教学材料任务管理控制器
 */
@RestController
@RequestMapping("/api/admin/material-task")
public class MaterialTaskController {

    @Autowired
    private MaterialTaskService materialTaskService;

    /**
     * 分页查询任务列表
     */
    @GetMapping("/list")
    public Result<IPage<MaterialTask>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long materialTypeId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String title) {
        
        Page<MaterialTask> page = new Page<>(current, size);
        IPage<MaterialTask> result = materialTaskService.getTaskPage(page, materialTypeId, academicYear, semester, status, title);
        return Result.success(result);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<MaterialTask> getById(@PathVariable Long id) {
        MaterialTask task = materialTaskService.getTaskById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }

    /**
     * 添加任务
     */
    @PostMapping
    public Result<Void> add(@RequestBody MaterialTask task) {
        boolean success = materialTaskService.addTask(task);
        return success ? Result.success() : Result.error("添加任务失败");
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MaterialTask task) {
        task.setId(id);
        boolean success = materialTaskService.updateTask(task);
        return success ? Result.success() : Result.error("更新任务失败");
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = materialTaskService.deleteTask(id);
        return success ? Result.success() : Result.error("删除任务失败");
    }

    /**
     * 批量删除任务
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody Map<String, List<Long>> params) {
        List<Long> taskIds = params.get("taskIds");
        if (taskIds == null || taskIds.isEmpty()) {
            return Result.error("请选择要删除的任务");
        }
        boolean success = materialTaskService.deleteTaskBatch(taskIds);
        return success ? Result.success() : Result.error("批量删除任务失败");
    }

    /**
     * 发布任务
     */
    @PutMapping("/publish/{id}")
    public Result<Void> publish(@PathVariable Long id) {
        boolean success = materialTaskService.publishTask(id);
        return success ? Result.success() : Result.error("发布任务失败");
    }

    /**
     * 批量发布任务
     */
    @PutMapping("/publish-batch")
    public Result<Void> publishBatch(@RequestBody Map<String, List<Long>> params) {
        List<Long> taskIds = params.get("taskIds");
        if (taskIds == null || taskIds.isEmpty()) {
            return Result.error("请选择要发布的任务");
        }
        boolean success = materialTaskService.publishTaskBatch(taskIds);
        return success ? Result.success() : Result.error("批量发布任务失败");
    }

    /**
     * 获取任务完成情况
     */
    @GetMapping("/progress/{id}")
    public Result<MaterialTaskProgressVO> getProgress(@PathVariable Long id) {
        MaterialTaskProgressVO progress = materialTaskService.getTaskProgress(id);
        return Result.success(progress);
    }

    /**
     * 提醒未完成任务的教师
     */
    @PostMapping("/remind")
    public Result<Void> remind(@RequestBody MaterialTaskReminderVO reminder) {
        boolean success = materialTaskService.remindTeacher(reminder.getTaskId(), reminder.getTeacherId());
        return success ? Result.success() : Result.error("发送提醒失败");
    }

    /**
     * 批量提醒未完成任务的教师
     */
    @PostMapping("/remind-batch")
    public Result<Void> remindBatch(@RequestBody Map<String, List<MaterialTaskReminderVO>> params) {
        List<MaterialTaskReminderVO> reminders = params.get("reminders");
        if (reminders == null || reminders.isEmpty()) {
            return Result.error("请选择要提醒的教师");
        }
        boolean success = materialTaskService.remindTeacherBatch(reminders);
        return success ? Result.success() : Result.error("批量发送提醒失败");
    }

    /**
     * 获取最近的任务
     */
    @GetMapping("/recent")
    public Result<List<MaterialTask>> getRecentTasks() {
        List<MaterialTask> tasks = materialTaskService.getRecentTasks(5);
        return Result.success(tasks);
    }
} 