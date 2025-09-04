package com.tcr.system.controller;

import com.tcr.system.common.Result;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.Assignment;
import com.tcr.system.service.CourseService;
import com.tcr.system.service.StudentCourseService;
import com.tcr.system.service.AssignmentService;
import com.tcr.system.service.AssignmentSubmissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.vo.AssignmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学生控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "学生管理")
public class StudentController {

    private final StudentCourseService studentCourseService;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService assignmentSubmissionService;


    /**
     * 获取学生选课列表
     */
    @GetMapping("/courses")
    @ApiOperation("获取学生选课列表")
    public Result<List<Course>> getCourses(@RequestAttribute("userId") Long userId) {
        System.out.println("获取学生选课列表，userId: " + userId);
        List<Course> courses = studentCourseService.getStudentCourses(userId);
        System.out.println("获取到的课程数量: " + (courses != null ? courses.size() : 0));
        return Result.success(courses);
    }

    /**
     * 获取指定学生的选课列表，通过路径参数
     */
    @GetMapping("/course/{studentId}")
    @ApiOperation("获取指定学生的选课列表")
    public Result<List<Course>> getStudentCourses(@ApiParam("学生ID") @PathVariable Long studentId) {
        List<Course> courses = studentCourseService.getStudentCourses(studentId);
        return Result.success(courses);
    }

    /**
     * 选课
     */
    @PostMapping("/course/select/{courseId}")
    @ApiOperation("选课")
    public Result<Boolean> selectCourse(@RequestAttribute("userId") Long userId, @ApiParam("课程ID") @PathVariable Long courseId) {
        boolean result = studentCourseService.selectCourse(userId, courseId);
        return Result.success(result, "选课成功");
    }

    /**
     * 退课
     */
    @PostMapping("/course/drop/{courseId}")
    @ApiOperation("退课")
    public Result<Boolean> dropCourse(@RequestAttribute("userId") Long userId, @ApiParam("课程ID") @PathVariable Long courseId) {
        boolean result = studentCourseService.dropCourse(userId, courseId);
        return Result.success(result, "退课成功");
    }

    /**
     * 获取学生的作业列表
     */
    @GetMapping("/assignments")
    @ApiOperation("获取学生的作业列表")
    public Result<Page<AssignmentVO>> getStudentAssignments(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @RequestAttribute("userId") Long userId) {
        Page<Assignment> page = new Page<>(pageNum, pageSize);
        Page<AssignmentVO> assignments = assignmentService.getStudentAssignments(page, userId, null, status);
        return Result.success(assignments);
    }

    /**
     * 获取学生在指定课程的作业列表
     */
    @GetMapping("/course/{courseId}/assignments")
    @ApiOperation("获取学生在指定课程的作业列表")
    public Result<Page<AssignmentVO>> getStudentCourseAssignments(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @RequestAttribute("userId") Long userId,
            @ApiParam("课程ID") @PathVariable Long courseId) {
        Page<Assignment> page = new Page<>(pageNum, pageSize);
        Page<AssignmentVO> assignments = assignmentService.getStudentAssignments(page, userId, courseId, status);
        return Result.success(assignments);
    }

    /**
     * 提交作业
     */
    @PostMapping("/assignment/submit")
    @ApiOperation("提交作业")
    public Result<Long> submitAssignment(
            @ApiParam("作业ID") @RequestParam Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        if (content == null) {
            content = "";
        }
        Long submissionId = assignmentSubmissionService.submitAssignment(assignmentId, userId, content, file);
        return Result.success(submissionId, "提交成功");
    }

    /**
     * 提交作业 (带作业ID)
     */
    @PostMapping("/assignment/{assignmentId}/submit")
    @ApiOperation("提交作业")
    public Result<Long> submitAssignmentWithPathVariable(
            @ApiParam("作业ID") @PathVariable Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        if (content == null) {
            content = "";
        }
        Long submissionId = assignmentSubmissionService.submitAssignment(assignmentId, userId, content, file);
        return Result.success(submissionId, "提交成功");
    }
} 