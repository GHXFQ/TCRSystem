package com.tcr.system.vo;

import com.tcr.system.entity.Course;
import com.tcr.system.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 课程详情VO
 */
@Data
public class CourseDetailVO {
    /**
     * 课程信息
     */
    private Course course;
    
    /**
     * 教师信息
     */
    private User teacher;
    
    /**
     * 学生列表
     */
    private List<User> students;
} 