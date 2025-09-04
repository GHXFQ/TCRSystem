package com.tcr.system.service;

import com.tcr.system.entity.Course;

import java.util.List;

/**
 * 学生课程服务接口
 */
public interface StudentCourseService {

    /**
     * 获取学生选课列表
     *
     * @param studentId 学生ID
     * @return 课程列表
     */
    List<Course> getStudentCourses(Long studentId);

    /**
     * 选课
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean selectCourse(Long studentId, Long courseId);

    /**
     * 退课
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean dropCourse(Long studentId, Long courseId);
} 