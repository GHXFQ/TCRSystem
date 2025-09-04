package com.tcr.system.controller;

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
 * 课程控制器
 */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "课程管理")
public class CourseController {

    private final CourseService courseService;

    /**
     * 分页查询课程列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询课程列表")
    public Result<Page<Course>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程名称") @RequestParam(required = false) String name,
            @ApiParam("课程状态") @RequestParam(required = false) Integer status) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        Page<Course> result = courseService.page(page, name, status);
        return Result.success(result);
    }

    /**
     * 根据ID查询课程
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询课程")
    public Result<Course> getById(@ApiParam("课程ID") @PathVariable Long id) {
        Course course = courseService.getById(id);
        return Result.success(course);
    }

    /**
     * 根据教师ID查询课程列表
     */
    @GetMapping("/teacher/{teacherId}")
    @ApiOperation("根据教师ID查询课程列表")
    public Result<List<Course>> getByTeacherId(@ApiParam("教师ID") @PathVariable Long teacherId) {
        List<Course> courses = courseService.getByTeacherId(teacherId);
        return Result.success(courses);
    }

    /**
     * 根据教师ID分页查询课程列表（带筛选功能）
     */
    @GetMapping("/teacher/{teacherId}/page")
    @ApiOperation("根据教师ID分页查询课程列表（带筛选功能）")
    public Result<Page<Course>> getByTeacherIdWithPage(
            @ApiParam("教师ID") @PathVariable Long teacherId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程名称") @RequestParam(required = false) String name,
            @ApiParam("课程状态") @RequestParam(required = false) Integer status) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        Page<Course> result = courseService.pageByTeacherId(page, teacherId, name, status);
        return Result.success(result);
    }

    /**
     * 根据学生ID查询课程列表
     */
    @GetMapping("/student/{studentId}")
    @ApiOperation("根据学生ID查询课程列表")
    public Result<List<Course>> getByStudentId(@ApiParam("学生ID") @PathVariable Long studentId) {
        List<Course> courses = courseService.getStudentCourses(studentId);
        return Result.success(courses);
    }

    /**
     * 添加课程
     */
    @PostMapping
    @ApiOperation("添加课程")
    public Result<Boolean> add(@ApiParam("课程信息") @RequestBody Course course, @RequestAttribute("userId") Long userId) {
        // 设置教师ID为当前登录用户ID
        course.setTeacherId(userId);
        boolean result = courseService.save(course);
        return Result.success(result, "添加成功");
    }

    /**
     * 修改课程
     */
    @PutMapping
    @ApiOperation("修改课程")
    public Result<Boolean> update(@ApiParam("课程信息") @RequestBody Course course, @RequestAttribute("userId") Long userId, @RequestAttribute("role") Integer role) {
        // 如果不是管理员，只能修改自己的课程
        if (role != 2) {
            Course existCourse = courseService.getById(course.getId());
            if (existCourse == null || !existCourse.getTeacherId().equals(userId)) {
                return Result.error("无权修改该课程");
            }
        }
        boolean result = courseService.updateById(course);
        return Result.success(result, "修改成功");
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除课程")
    public Result<Boolean> delete(@ApiParam("课程ID") @PathVariable Long id, @RequestAttribute("userId") Long userId, @RequestAttribute("role") Integer role) {
        // 如果不是管理员，只能删除自己的课程
        if (role != 2) {
            Course course = courseService.getById(id);
            if (course == null || !course.getTeacherId().equals(userId)) {
                return Result.error("无权删除该课程");
            }
        }
        boolean result = courseService.removeById(id);
        return Result.success(result, "删除成功");
    }
} 