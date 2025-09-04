package com.tcr.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.service.MaterialReviewService;
import com.tcr.system.vo.MaterialReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 教学材料审核Controller
 */
@Api(tags = "材料审核接口")
@RestController
@RequestMapping("/api/material-review")
@RequiredArgsConstructor
public class MaterialReviewController {

    private final MaterialReviewService materialReviewService;

    /**
     * 提交材料审核
     */
    @PostMapping("/submit")
    public Result<Long> submitReview(@RequestBody MaterialReviewVO vo) {
        Long reviewId = materialReviewService.submitReview(
                vo.getMaterialId(),
                vo.getAcademicYear(),
                vo.getSemester(),
                vo.getMaterialTypeId(),
                vo.getTeacherId(),
                vo.getCourseId()
        );
        return Result.success(reviewId);
    }

    /**
     * 审核材料
     */
    @PostMapping("/review")
    @ApiOperation("审核材料")
    public Result<Boolean> reviewMaterial(@RequestBody MaterialReviewVO vo, HttpServletRequest request) {
        // 从请求中获取用户ID和角色
        Long userId = (Long) request.getAttribute("userId");
        String roleStr = (String) request.getAttribute("role");
        Integer role = roleStr != null ? Integer.parseInt(roleStr) : 0;
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        // 设置审核人ID
        vo.setReviewerId(userId);
        
        // 如果是管理员审核
        if (role == 2) {
            vo.setReviewerTitle(4); // 设置为教务人员职称
        }
        
        boolean success = materialReviewService.reviewMaterial(
                vo.getReviewId(),
                vo.getReviewerId(),
                vo.getReviewerTitle(),
                vo.getResult(),
                vo.getComment()
        );
        
        return Result.success(success);
    }

    /**
     * 分页查询教师提交的材料审核列表
     */
    @GetMapping("/teacher-submit")
    public Result<IPage<Map<String, Object>>> getTeacherSubmitReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long teacherId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer status
    ) {
        Page<Map<String, Object>> page = new Page<>(current, size);
        IPage<Map<String, Object>> result = materialReviewService.getTeacherSubmitReviews(page, teacherId, academicYear, semester, status);
        return Result.success(result);
    }

    /**
     * 分页查询教师待审核的材料列表
     */
    @GetMapping("/teacher-pending")
    public Result<IPage<Map<String, Object>>> getTeacherPendingReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester
    ) {
        Page<Map<String, Object>> page = new Page<>(current, size);
        IPage<Map<String, Object>> result = materialReviewService.getTeacherPendingReviews(page, reviewerId, academicYear, semester);
        return Result.success(result);
    }

    /**
     * 获取材料审核详情
     */
    @ApiOperation("获取审核详情")
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getReviewDetail(@ApiParam("审核ID") @PathVariable Long id) {
        Map<String, Object> detail = materialReviewService.getReviewDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取审核记录列表
     */
    @ApiOperation("获取审核记录")
    @GetMapping("/records/{id}")
    public Result<List<Map<String, Object>>> getReviewRecords(@ApiParam("审核ID") @PathVariable Long id) {
        List<Map<String, Object>> records = materialReviewService.getReviewRecords(id);
        return Result.success(records);
    }

    @ApiOperation("管理员获取审核列表")
    @GetMapping("/admin/list")
    public Result<IPage<Map<String, Object>>> getAdminReviewList(
            @ApiParam("当前页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("学年") @RequestParam(required = false) String academicYear,
            @ApiParam("学期") @RequestParam(required = false) Integer semester,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<Map<String, Object>> page = new Page<>(current, size);
        IPage<Map<String, Object>> result = materialReviewService.getAdminReviewList(page, academicYear, semester, status);
        return Result.success(result);
    }
} 