package com.tcr.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Class;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.TeacherTitle;
import com.tcr.system.entity.User;
import com.tcr.system.service.AdminService;
import com.tcr.system.service.ClassService;
import com.tcr.system.service.CourseService;
import com.tcr.system.service.MajorService;
import com.tcr.system.vo.UserStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "管理员管理")
public class AdminController {

    private final AdminService adminService;
    private final ClassService classService;
    private final MajorService majorService;
    private final CourseService courseService;

    /**
     * 获取用户统计数据
     */
    @GetMapping("/user/statistics")
    @ApiOperation("获取用户统计数据")
    public Result<UserStatisticsVO> getUserStatistics() {
        UserStatisticsVO statistics = adminService.getUserStatistics();
        return Result.success(statistics);
    }

    /**
     * 分页查询教师列表
     */
    @GetMapping("/teacher/list")
    @ApiOperation("分页查询教师列表")
    public Result<Page<User>> getTeacherList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("教师姓名") @RequestParam(required = false) String realName,
            @ApiParam("工号") @RequestParam(required = false) String username,
            @ApiParam("职称类型") @RequestParam(required = false) Integer titleType) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = adminService.getTeacherList(page, realName, username, titleType);
        return Result.success(result);
    }

    /**
     * 获取教师职称列表
     */
    @GetMapping("/teacher/titles")
    @ApiOperation("获取教师职称列表")
    public Result<List<TeacherTitle>> getTeacherTitles() {
        List<TeacherTitle> titles = adminService.getTeacherTitles();
        return Result.success(titles);
    }

    /**
     * 添加教师
     */
    @PostMapping("/teacher")
    @ApiOperation("添加教师")
    public Result<Boolean> addTeacher(@ApiParam("教师信息") @RequestBody User teacher) {
        boolean result = adminService.addTeacher(teacher);
        return Result.success(result, "添加成功");
    }

    /**
     * 修改教师
     */
    @PutMapping("/teacher")
    @ApiOperation("修改教师")
    public Result<Boolean> updateTeacher(@ApiParam("教师信息") @RequestBody User teacher) {
        boolean result = adminService.updateTeacher(teacher);
        return Result.success(result, "修改成功");
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/teacher/{id}")
    @ApiOperation("删除教师")
    public Result<Boolean> deleteTeacher(@ApiParam("教师ID") @PathVariable Long id) {
        boolean result = adminService.deleteTeacher(id);
        return Result.success(result, "删除成功");
    }

    /**
     * 修改教师状态
     */
    @PutMapping("/teacher/status")
    @ApiOperation("修改教师状态")
    public Result<Boolean> updateTeacherStatus(@ApiParam("教师状态信息") @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        boolean result = adminService.updateTeacherStatus(id, status);
        return Result.success(result, "修改成功");
    }

    /**
     * 设置教师职称
     */
    @PostMapping("/teacher/titles")
    @ApiOperation("设置教师职称")
    public Result<Boolean> setTeacherTitles(@ApiParam("教师职称信息") @RequestBody Map<String, Object> params) {
        Long teacherId = Long.valueOf(params.get("teacherId").toString());
        boolean isNormalTeacher = Boolean.parseBoolean(params.get("isNormalTeacher").toString());
        boolean isCourseLeader = Boolean.parseBoolean(params.get("isCourseLeader").toString());
        boolean isMajorLeader = Boolean.parseBoolean(params.get("isMajorLeader").toString());
        boolean isViceDean = Boolean.parseBoolean(params.get("isViceDean").toString());
        
        Long courseId = params.get("courseId") != null ? Long.valueOf(params.get("courseId").toString()) : null;
        Long majorId = params.get("majorId") != null ? Long.valueOf(params.get("majorId").toString()) : null;
        Long collegeId = params.get("collegeId") != null ? Long.valueOf(params.get("collegeId").toString()) : null;
        
        boolean result = adminService.setTeacherTitles(teacherId, isNormalTeacher, isCourseLeader, isMajorLeader, isViceDean, courseId, majorId, collegeId);
        return Result.success(result, "设置成功");
    }

    /**
     * 分页查询学生列表
     */
    @GetMapping("/student/list")
    @ApiOperation("分页查询学生列表")
    public Result<Page<User>> getStudentList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("学生姓名") @RequestParam(required = false) String realName,
            @ApiParam("学号") @RequestParam(required = false) String username,
            @ApiParam("班级ID") @RequestParam(required = false) Long classId) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = adminService.getStudentList(page, realName, username, classId);
        return Result.success(result);
    }

    /**
     * 添加学生
     */
    @PostMapping("/student")
    @ApiOperation("添加学生")
    public Result<Boolean> addStudent(@ApiParam("学生信息") @RequestBody User student) {
        boolean result = adminService.addStudent(student);
        return Result.success(result, "添加成功");
    }

    /**
     * 修改学生
     */
    @PutMapping("/student")
    @ApiOperation("修改学生")
    public Result<Boolean> updateStudent(@ApiParam("学生信息") @RequestBody User student) {
        boolean result = adminService.updateStudent(student);
        return Result.success(result, "修改成功");
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/student/{id}")
    @ApiOperation("删除学生")
    public Result<Boolean> deleteStudent(@ApiParam("学生ID") @PathVariable Long id) {
        boolean result = adminService.deleteStudent(id);
        return Result.success(result, "删除成功");
    }

    /**
     * 修改学生状态
     */
    @PutMapping("/student/status")
    @ApiOperation("修改学生状态")
    public Result<Boolean> updateStudentStatus(@ApiParam("学生状态信息") @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        boolean result = adminService.updateStudentStatus(id, status);
        return Result.success(result, "修改成功");
    }

    /**
     * 学生选课
     */
    @PostMapping("/student/course/select")
    @ApiOperation("学生选课")
    public Result<Boolean> selectCourse(@ApiParam("选课信息") @RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        boolean result = adminService.selectCourse(studentId, courseId);
        return Result.success(result, "选课成功");
    }

    /**
     * 学生退课
     */
    @PostMapping("/student/course/drop")
    @ApiOperation("学生退课")
    public Result<Boolean> dropCourse(@ApiParam("退课信息") @RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        boolean result = adminService.dropCourse(studentId, courseId);
        return Result.success(result, "退课成功");
    }

    /**
     * 获取可选课程列表
     */
    @GetMapping("/course/available")
    @ApiOperation("获取可选课程列表")
    public Result<List<Course>> getAvailableCourses(@ApiParam("学生ID") @RequestParam Long studentId) {
        List<Course> courses = adminService.getAvailableCourses(studentId);
        return Result.success(courses);
    }

    /**
     * 获取班级列表
     */
    @GetMapping("/class/list")
    @ApiOperation("获取班级列表")
    public Result<List<Class>> getClassList() {
        List<Class> classes = classService.getClassList();
        return Result.success(classes);
    }

    /**
     * 添加班级
     */
    @PostMapping("/class")
    @ApiOperation("添加班级")
    public Result<Boolean> addClass(@ApiParam("班级信息") @RequestBody Class clazz) {
        boolean result = classService.addClass(clazz);
        return Result.success(result, "添加成功");
    }

    /**
     * 获取专业列表
     */
    @GetMapping("/major/list")
    @ApiOperation("获取专业列表")
    public Result<List<com.tcr.system.entity.Major>> getMajorList() {
        List<com.tcr.system.entity.Major> majors = majorService.list();
        return Result.success(majors);
    }

    /**
     * 获取学院列表
     */
    @GetMapping("/college/list")
    @ApiOperation("获取学院列表")
    public Result<List<com.tcr.system.entity.College>> getCollegeList() {
        List<com.tcr.system.entity.College> colleges = majorService.getCollegeList();
        return Result.success(colleges);
    }
} 