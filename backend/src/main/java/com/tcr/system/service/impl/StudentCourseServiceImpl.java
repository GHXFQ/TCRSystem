package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.StudentCourse;
import com.tcr.system.entity.User;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.StudentCourseMapper;
import com.tcr.system.service.CourseService;
import com.tcr.system.service.StudentCourseService;
import com.tcr.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生课程服务实现类
 */
@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse> implements StudentCourseService {

    private final CourseService courseService;
    private final UserService userService;

    @Override
    public List<Course> getStudentCourses(Long studentId) {
        // 查询学生选课记录
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId);
        List<StudentCourse> studentCourses = list(wrapper);
        
        // 获取课程ID列表
        List<Long> courseIds = studentCourses.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());
        
        // 如果没有选课记录，返回空列表
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询课程信息
        List<Course> courses = courseService.listByIds(courseIds);
        
        // 如果没有课程，返回空列表
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有教师ID
        List<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询教师信息
        Map<Long, String> teacherNameMap = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            for (Long teacherId : teacherIds) {
                User teacher = userService.getById(teacherId);
                if (teacher != null) {
                    teacherNameMap.put(teacher.getId(), teacher.getRealName());
                }
            }
        }
        
        // 设置教师名称
        for (Course course : courses) {
            if (course.getTeacherId() != null) {
                course.setTeacherName(teacherNameMap.getOrDefault(course.getTeacherId(), "未知"));
            } else {
                course.setTeacherName("未知");
            }
        }
        
        return courses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean selectCourse(Long studentId, Long courseId) {
        // 校验参数
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }
        
        // 检查课程是否存在
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        try {
            // 先尝试直接删除可能存在的记录（包括已逻辑删除的记录）
            // 使用原生SQL执行物理删除
            baseMapper.deletePhysically(studentId, courseId);
            
            // 创建选课记录
            StudentCourse studentCourse = new StudentCourse();
            studentCourse.setStudentId(studentId);
            studentCourse.setCourseId(courseId);
            
            return save(studentCourse);
        } catch (Exception e) {
            // 检查是否已经选过该课程（未被逻辑删除的）
            LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentCourse::getStudentId, studentId);
            wrapper.eq(StudentCourse::getCourseId, courseId);
            
            if (count(wrapper) > 0) {
                throw new BusinessException("已经选过该课程");
            } else {
                throw e; // 如果是其他错误，则继续抛出
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dropCourse(Long studentId, Long courseId) {
        // 校验参数
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }
        
        // 检查是否已选课
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId);
        wrapper.eq(StudentCourse::getCourseId, courseId);
        
        // 获取选课记录
        StudentCourse studentCourse = getOne(wrapper);
        if (studentCourse == null) {
            throw new BusinessException("未选该课程");
        }
        
        // 删除选课记录
        return removeById(studentCourse.getId());
    }
} 