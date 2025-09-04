package com.tcr.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Feedback;
import com.tcr.system.service.FeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈控制器
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "反馈管理")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 分页查询反馈列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询反馈列表")
    public Result<Page<Feedback>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        Page<Feedback> result = feedbackService.page(page, courseId);
        return Result.success(result);
    }

    /**
     * 添加反馈
     */
    @PostMapping("/add")
    @ApiOperation("添加反馈")
    public Result<Boolean> add(@ApiParam("反馈信息") @RequestBody Feedback feedback, @RequestAttribute("userId") Long userId) {
        feedback.setUserId(userId);
        boolean result = feedbackService.save(feedback);
        return Result.success(result, "反馈提交成功");
    }

    /**
     * 回复反馈
     */
    @PostMapping("/reply")
    @ApiOperation("回复反馈")
    public Result<Boolean> reply(@ApiParam("反馈信息") @RequestBody Feedback feedback, @RequestAttribute("userId") Long userId) {
        feedback.setUserId(userId);
        boolean result = feedbackService.save(feedback);
        return Result.success(result, "回复提交成功");
    }

    /**
     * 删除反馈
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除反馈")
    public Result<Boolean> delete(@ApiParam("反馈ID") @PathVariable Long id, @RequestAttribute("userId") Long userId, @RequestAttribute("role") Integer role) {
        boolean result = feedbackService.remove(id, userId, role);
        return Result.success(result, "删除成功");
    }
} 