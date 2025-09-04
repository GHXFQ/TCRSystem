package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.StudentCourse;
import com.tcr.system.entity.User;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.CourseMapper;
import com.tcr.system.mapper.StudentCourseMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.CourseService;
import com.tcr.system.vo.CourseDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final StudentCourseMapper studentCourseMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Course> page(Page<Course> page, String name, Integer status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了课程名称，则按名称模糊查询
        if (StringUtils.hasText(name)) {
            wrapper.like(Course::getName, name);
        }
        
        // 如果指定了课程状态，则按状态查询
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Course::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    public List<Course> getByTeacherId(Long teacherId) {
        // 校验参数
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        // 查询教师的课程
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacherId);
        wrapper.orderByDesc(Course::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public Page<Course> pageByTeacherId(Page<Course> page, Long teacherId, String name, Integer status) {
        // 校验参数
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选指定教师的课程
        wrapper.eq(Course::getTeacherId, teacherId);
        
        // 如果指定了课程名称，则按名称精确查询
        if (StringUtils.hasText(name)) {
            wrapper.eq(Course::getName, name);
        }
        
        // 如果指定了课程状态，则按状态查询
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Course::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    public Course getById(Long id) {
        // 校验参数
        if (id == null) {
            throw new BusinessException("课程ID不能为空");
        }
        
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Course course) {
        // 校验参数
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            throw new BusinessException("课程名称不能为空");
        }
        
        if (course.getTeacherId() == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        return super.save(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Course course) {
        // 校验参数
        if (course.getId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        
        // 检查课程是否存在
        Course existCourse = getById(course.getId());
        if (existCourse == null) {
            throw new BusinessException("课程不存在");
        }
        
        return super.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        // 校验参数
        if (id == null) {
            throw new BusinessException("课程ID不能为空");
        }
        
        // 检查课程是否存在
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // TODO: 删除课程相关的选课记录、材料、反馈等
        
        return super.removeById(id);
    }

    @Override
    public List<Course> getStudentCourses(Long studentId) {
        // 校验参数
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        // 查询学生选课记录
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId);
        wrapper.eq(StudentCourse::getDeleted, 0); // 只查询未删除的记录
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(wrapper);
        
        if (studentCourses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取课程ID列表
        List<Long> courseIds = studentCourses.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());
        
        // 查询课程信息
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.in(Course::getId, courseIds);
        courseWrapper.orderByDesc(Course::getCreateTime);
        
        return list(courseWrapper);
    }

    @Override
    public Page<Course> pageForAdmin(Page<Course> page, String name, Long teacherId, Integer status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        // 条件过滤
        if (StringUtils.hasText(name)) {
            wrapper.like(Course::getName, name);
        }
        
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }
        
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Course::getCreateTime);
        
        // 查询课程
        Page<Course> result = this.page(page, wrapper);
        
        // 设置教师姓名
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            List<Long> teacherIds = result.getRecords().stream()
                    .map(Course::getTeacherId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(User::getId, teacherIds);
            List<User> teachers = userMapper.selectList(userWrapper);
            
            Map<Long, String> teacherMap = teachers.stream()
                    .collect(Collectors.toMap(User::getId, User::getRealName));
            
            result.getRecords().forEach(course -> 
                course.setTeacherName(teacherMap.getOrDefault(course.getTeacherId(), "未知"))
            );
        }
        
        return result;
    }
    
    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        // 查询课程信息
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 查询教师信息
        User teacher = userMapper.selectById(course.getTeacherId());
        
        // 查询选课学生
        LambdaQueryWrapper<StudentCourse> scWrapper = new LambdaQueryWrapper<>();
        scWrapper.eq(StudentCourse::getCourseId, courseId);
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(scWrapper);
        
        List<User> students = new ArrayList<>();
        if (!studentCourses.isEmpty()) {
            List<Long> studentIds = studentCourses.stream()
                    .map(StudentCourse::getStudentId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<User> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.in(User::getId, studentIds);
            students = userMapper.selectList(studentWrapper);
        }
        
        // 组装结果
        CourseDetailVO detail = new CourseDetailVO();
        detail.setCourse(course);
        detail.setTeacher(teacher);
        detail.setStudents(students);
        
        return detail;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStudentToCourse(Long studentId, Long courseId) {
        // 检查课程是否存在
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 检查学生是否存在
        User student = userMapper.selectById(studentId);
        if (student == null || student.getRole() != 0) {
            throw new BusinessException("学生不存在");
        }
        
        // 检查是否已选课
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId);
        if (studentCourseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("学生已选择该课程");
        }
        
        // 添加选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);
        
        return studentCourseMapper.insert(studentCourse) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeStudentFromCourse(Long studentId, Long courseId) {
        // 检查选课记录是否存在
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId);
        
        if (studentCourseMapper.selectCount(wrapper) == 0) {
            throw new BusinessException("学生未选择该课程");
        }
        
        // 删除选课记录
        return studentCourseMapper.delete(wrapper) > 0;
    }
} 