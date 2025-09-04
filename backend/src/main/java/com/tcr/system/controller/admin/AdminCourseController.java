package com.tcr.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Course;
import com.tcr.system.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员课程控制器
 */
@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
@Api(tags = "管理员课程管理")
public class AdminCourseController {

    private final CourseService courseService;

    /**
     * 查询所有课程列表（不分页）
     */
    @GetMapping("/list")
    @ApiOperation("查询所有课程列表")
    public Result<List<Course>> list(
            @ApiParam("课程名称") @RequestParam(required = false) String name,
            @ApiParam("课程状态") @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getDeleted, 0); // 未删除
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Course::getName, name);
        }
        
        if (status != null) {
            queryWrapper.eq(Course::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Course::getCreateTime);
        
        List<Course> courses = courseService.list(queryWrapper);
        return Result.success(courses);
    }

    /**
     * 查询所有课程列表（分页）
     */
    @GetMapping("/page")
    @ApiOperation("分页查询所有课程列表")
    public Result<Page<Course>> page(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程名称") @RequestParam(required = false) String name,
            @ApiParam("课程状态") @RequestParam(required = false) Integer status) {
        
        Page<Course> page = new Page<>(pageNum, pageSize);
        Page<Course> result = courseService.page(page, name, status);
        return Result.success(result);
    }

    /**
     * 获取课程总数
     */
    @GetMapping("/count")
    @ApiOperation("获取课程总数")
    public Result<Long> count() {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getDeleted, 0); // 未删除
        long count = courseService.count(queryWrapper);
        return Result.success(count);
    }
} 