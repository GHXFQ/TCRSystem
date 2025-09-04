package com.tcr.system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.User;
import com.tcr.system.service.CourseService;
import com.tcr.system.service.UserService;
import com.tcr.system.vo.CourseDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员端课程管理控制器
 */
@RestController
@RequestMapping("/api/admin/course-management")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "管理员端课程管理")
public class CourseManagementController {

    private final CourseService courseService;
    private final UserService userService;

    /**
     * 分页查询课程列表（包含详细信息）
     */
    @GetMapping("/list")
    @ApiOperation("分页查询课程列表")
    public Result<Page<Course>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程名称") @RequestParam(required = false) String name,
            @ApiParam("教师ID") @RequestParam(required = false) Long teacherId,
            @ApiParam("课程状态") @RequestParam(required = false) Integer status) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        Page<Course> result = courseService.pageForAdmin(page, name, teacherId, status);
        return Result.success(result);
    }
    
    /**
     * 获取课程详情（包含学生列表）
     */
    @GetMapping("/{id}/detail")
    @ApiOperation("获取课程详情")
    public Result<CourseDetailVO> getDetail(@ApiParam("课程ID") @PathVariable Long id) {
        CourseDetailVO detail = courseService.getCourseDetail(id);
        return Result.success(detail);
    }
    
    /**
     * 获取可选教师列表
     */
    @GetMapping("/available-teachers")
    @ApiOperation("获取可选教师列表")
    public Result<List<User>> getAvailableTeachers() {
        List<User> teachers = userService.getTeacherList();
        return Result.success(teachers);
    }
    
    /**
     * 添加新课程
     */
    @PostMapping
    @ApiOperation("添加新课程")
    public Result<Boolean> add(@ApiParam("课程信息") @RequestBody Course course) {
        boolean result = courseService.save(course);
        return Result.success(result, "添加成功");
    }
    
    /**
     * 更新课程信息
     */
    @PutMapping
    @ApiOperation("更新课程信息")
    public Result<Boolean> update(@ApiParam("课程信息") @RequestBody Course course) {
        boolean result = courseService.updateById(course);
        return Result.success(result, "更新成功");
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除课程")
    public Result<Boolean> delete(@ApiParam("课程ID") @PathVariable Long id) {
        boolean result = courseService.removeById(id);
        return Result.success(result, "删除成功");
    }
    
    /**
     * 修改课程状态
     */
    @PutMapping("/status")
    @ApiOperation("修改课程状态")
    public Result<Boolean> updateStatus(@ApiParam("课程状态信息") @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        
        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        
        boolean result = courseService.updateById(course);
        return Result.success(result, "状态修改成功");
    }
    
    /**
     * 学生选课
     */
    @PostMapping("/{courseId}/student/{studentId}")
    @ApiOperation("学生选课")
    public Result<Boolean> addStudent(
            @ApiParam("课程ID") @PathVariable Long courseId,
            @ApiParam("学生ID") @PathVariable Long studentId) {
        boolean result = courseService.addStudentToCourse(studentId, courseId);
        return Result.success(result, "选课成功");
    }
    
    /**
     * 学生退课
     */
    @DeleteMapping("/{courseId}/student/{studentId}")
    @ApiOperation("学生退课")
    public Result<Boolean> removeStudent(
            @ApiParam("课程ID") @PathVariable Long courseId,
            @ApiParam("学生ID") @PathVariable Long studentId) {
        boolean result = courseService.removeStudentFromCourse(studentId, courseId);
        return Result.success(result, "退课成功");
    }
} 