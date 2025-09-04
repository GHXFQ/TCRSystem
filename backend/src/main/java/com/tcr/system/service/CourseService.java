package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.Course;

import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {

    /**
     * 分页查询课程列表
     *
     * @param page 分页参数
     * @param name 课程名称
     * @param status 课程状态
     * @return 分页结果
     */
    Page<Course> page(Page<Course> page, String name, Integer status);

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程信息
     */
    Course getById(Long id);

    /**
     * 根据教师ID查询课程列表
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> getByTeacherId(Long teacherId);

    /**
     * 根据教师ID分页查询课程列表
     *
     * @param page 分页参数
     * @param teacherId 教师ID
     * @param name 课程名称
     * @param status 课程状态
     * @return 分页结果
     */
    Page<Course> pageByTeacherId(Page<Course> page, Long teacherId, String name, Integer status);

    /**
     * 新增课程
     *
     * @param course 课程信息
     * @return 是否成功
     */
    boolean save(Course course);

    /**
     * 更新课程
     *
     * @param course 课程信息
     * @return 是否成功
     */
    boolean updateById(Course course);

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    boolean removeById(Long id);

    /**
     * 根据学生ID查询课程列表
     *
     * @param studentId 学生ID
     * @return 课程列表
     */
    List<Course> getStudentCourses(Long studentId);

    /**
     * 管理员端分页查询课程列表
     *
     * @param page 分页参数
     * @param name 课程名称
     * @param teacherId 教师ID
     * @param status 课程状态
     * @return 分页结果
     */
    Page<Course> pageForAdmin(Page<Course> page, String name, Long teacherId, Integer status);

    /**
     * 获取课程详情（包含学生列表）
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    com.tcr.system.vo.CourseDetailVO getCourseDetail(Long courseId);

    /**
     * 将学生添加到课程
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean addStudentToCourse(Long studentId, Long courseId);

    /**
     * 将学生从课程中移除
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean removeStudentFromCourse(Long studentId, Long courseId);
} 