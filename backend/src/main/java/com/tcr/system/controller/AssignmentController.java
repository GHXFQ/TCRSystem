package com.tcr.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Assignment;
import com.tcr.system.service.AssignmentService;
import com.tcr.system.service.AssignmentSubmissionService;
import com.tcr.system.vo.AssignmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 作业控制器
 */
@RestController
@RequestMapping("/api/assignment")
@RequiredArgsConstructor
@Api(tags = "作业管理")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService assignmentSubmissionService;

    /**
     * 分页查询教师发布的作业列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询教师发布的作业列表")
    public Result<Page<AssignmentVO>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @RequestAttribute("userId") Long userId) {
        Page<Assignment> page = new Page<>(pageNum, pageSize);
        Page<AssignmentVO> result = assignmentService.getTeacherAssignments(page, userId, courseId, status);
        return Result.success(result);
    }
    
    /**
     * 分页查询教师发布的作业列表 (别名)
     */
    @GetMapping("/teacher/list")
    @ApiOperation("分页查询教师发布的作业列表")
    public Result<Page<AssignmentVO>> teacherList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @RequestAttribute("userId") Long userId) {
        return list(pageNum, pageSize, courseId, status, userId);
    }
    
    /**
     * 分页查询学生的作业列表
     */
    @GetMapping("/student/list")
    @ApiOperation("分页查询学生的作业列表")
    public Result<Page<AssignmentVO>> studentList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @RequestAttribute("userId") Long userId) {
        Page<Assignment> page = new Page<>(pageNum, pageSize);
        Page<AssignmentVO> result = assignmentService.getStudentAssignments(page, userId, courseId, status);
        return Result.success(result);
    }

    /**
     * 获取作业详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取作业详情")
    public Result<AssignmentVO> getById(@ApiParam("作业ID") @PathVariable Long id) {
        AssignmentVO assignment = assignmentService.getAssignmentDetail(id);
        return Result.success(assignment);
    }

    /**
     * 新增作业
     */
    @PostMapping
    @ApiOperation("新增作业")
    public Result<Long> save(@RequestBody Assignment assignment, @RequestAttribute("userId") Long userId) {
        assignment.setTeacherId(userId);
        Long assignmentId = assignmentService.publishAssignment(assignment);
        return Result.success(assignmentId, "发布成功");
    }
    
    /**
     * 发布作业
     */
    @PostMapping("/publish")
    @ApiOperation("发布作业")
    public Result<Long> publish(@RequestBody Assignment assignment, @RequestAttribute("userId") Long userId) {
        assignment.setTeacherId(userId);
        Long assignmentId = assignmentService.publishAssignment(assignment);
        return Result.success(assignmentId, "发布成功");
    }

    /**
     * 修改作业
     */
    @PutMapping
    @ApiOperation("修改作业")
    public Result<Boolean> update(@RequestBody Assignment assignment, @RequestAttribute("userId") Long userId) {
        // 验证是否为作业发布者
        Assignment existAssignment = assignmentService.getById(assignment.getId());
        if (existAssignment != null && !existAssignment.getTeacherId().equals(userId)) {
            return Result.error("无权修改他人发布的作业");
        }
        boolean result = assignmentService.updateById(assignment);
        return Result.success(result, "修改成功");
    }

    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除作业")
    public Result<Boolean> remove(@ApiParam("作业ID") @PathVariable Long id, @RequestAttribute("userId") Long userId) {
        // 验证是否为作业发布者
        Assignment existAssignment = assignmentService.getById(id);
        if (existAssignment != null && !existAssignment.getTeacherId().equals(userId)) {
            return Result.error("无权删除他人发布的作业");
        }
        boolean result = assignmentService.removeById(id);
        return Result.success(result, "删除成功");
    }
    
    /**
     * 更新作业状态
     */
    @PostMapping("/{id}/status/{status}")
    @ApiOperation("更新作业状态")
    public Result<Boolean> updateStatus(
            @ApiParam("作业ID") @PathVariable Long id,
            @ApiParam("状态") @PathVariable Integer status,
            @RequestAttribute("userId") Long userId) {
        // 验证是否为作业发布者
        Assignment existAssignment = assignmentService.getById(id);
        if (existAssignment != null && !existAssignment.getTeacherId().equals(userId)) {
            return Result.error("无权修改他人发布的作业");
        }
        boolean result = assignmentService.updateStatus(id, status);
        return Result.success(result, "状态更新成功");
    }
    
    /**
     * 更新作业状态 (别名路径 - 兼容前端)
     */
    @PutMapping("/status/{id}")
    @ApiOperation("更新作业状态")
    public Result<Boolean> updateStatusAlt(
            @ApiParam("作业ID") @PathVariable Long id,
            @ApiParam("状态") @RequestParam Integer status,
            @RequestAttribute("userId") Long userId) {
        return updateStatus(id, status, userId);
    }
    
    /**
     * 获取作业统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("获取作业统计数据")
    public Result<Map<String, Object>> getStatistics(
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @RequestAttribute("userId") Long userId) {
        Map<String, Object> statistics = assignmentService.getAssignmentStatistics(userId, courseId);
        return Result.success(statistics);
    }
    
    /**
     * 获取作业提交率信息
     */
    @GetMapping("/submission-rate")
    @ApiOperation("获取作业提交率信息")
    public Result<List<Map<String, Object>>> getSubmissionRate(@RequestAttribute("userId") Long userId) {
        List<Map<String, Object>> submissionRate = assignmentService.getSubmissionRateInfo(userId);
        return Result.success(submissionRate);
    }

    /**
     * 提交作业路径 - 匹配前端请求
     */
    @PostMapping("/submit")
    @ApiOperation("提交作业")
    public Result<Long> submitAssignmentByFrontend(
            @ApiParam("作业ID") @RequestParam Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @ApiParam("文件名") @RequestParam(required = false) String fileName,
            @RequestAttribute("userId") Long userId) {
        if (content == null) {
            content = "";
        }
        
        // 如果有文件路径但没有文件，使用AssignmentSubmissionController中的方法处理
        if (file == null && filePath != null && !filePath.isEmpty()) {
            return assignmentSubmissionService.submitWithFilePath(assignmentId, userId, content, filePath, fileName);
        }
        
        Long submissionId = assignmentSubmissionService.submitAssignment(assignmentId, userId, content, file);
        return Result.success(submissionId, "提交成功");
    }

    /**
     * 提交作业 (兼容路径)
     */
    @PostMapping("/{id}/submit")
    @ApiOperation("提交作业")
    public Result<Long> submitAssignment(
            @ApiParam("作业ID") @PathVariable Long id,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        // 重定向到作业提交服务
        Long submissionId = assignmentSubmissionService.submitAssignment(id, userId, content != null ? content : "", file);
        return Result.success(submissionId, "提交成功");
    }
    
    /**
     * 批改作业
     */
    @PostMapping("/grade")
    @ApiOperation("批改作业")
    public Result<Boolean> gradeAssignment(
            @ApiParam("提交ID") @RequestParam Long id, 
            @ApiParam("分数") @RequestParam Integer score, 
            @ApiParam("评语") @RequestParam(required = false) String comment) {
        boolean result = assignmentSubmissionService.gradeSubmission(id, score, comment);
        return Result.success(result, "批改成功");
    }
} 