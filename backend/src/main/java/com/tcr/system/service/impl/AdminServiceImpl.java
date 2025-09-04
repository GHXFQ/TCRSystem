package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.StudentCourse;
import com.tcr.system.entity.TeacherTitle;
import com.tcr.system.entity.User;
import com.tcr.system.mapper.CourseMapper;
import com.tcr.system.mapper.StudentCourseMapper;
import com.tcr.system.mapper.TeacherTitleMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.AdminService;
import com.tcr.system.vo.UserStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final TeacherTitleMapper teacherTitleMapper;
    private final CourseMapper courseMapper;
    private final StudentCourseMapper studentCourseMapper;

    @Override
    public UserStatisticsVO getUserStatistics() {
        UserStatisticsVO statistics = new UserStatisticsVO();
        
        // 查询总用户数
        LambdaQueryWrapper<User> totalQuery = Wrappers.lambdaQuery();
        totalQuery.eq(User::getDeleted, 0);
        int totalUsers = userMapper.selectCount(totalQuery).intValue();
        
        // 查询教师数量
        LambdaQueryWrapper<User> teacherQuery = Wrappers.lambdaQuery();
        teacherQuery.eq(User::getDeleted, 0);
        teacherQuery.eq(User::getRole, 1); // 教师角色
        int teacherCount = userMapper.selectCount(teacherQuery).intValue();
        
        // 查询学生数量
        LambdaQueryWrapper<User> studentQuery = Wrappers.lambdaQuery();
        studentQuery.eq(User::getDeleted, 0);
        studentQuery.eq(User::getRole, 0); // 学生角色，修正：学生role=0，不是2
        int studentCount = userMapper.selectCount(studentQuery).intValue();
        
        statistics.setTotalUsers(totalUsers);
        statistics.setTeacherCount(teacherCount);
        statistics.setStudentCount(studentCount);
        
        return statistics;
    }

    @Override
    public Page<User> getTeacherList(Page<User> page, String realName, String username, Integer titleType) {
        return userMapper.getTeacherList(page, realName, username, titleType);
    }

    @Override
    public List<TeacherTitle> getTeacherTitles() {
        LambdaQueryWrapper<TeacherTitle> query = Wrappers.lambdaQuery();
        query.eq(TeacherTitle::getDeleted, 0);
        return teacherTitleMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTeacher(User teacher) {
        // 设置教师角色
        teacher.setRole(1);
        // 设置默认密码
        teacher.setPassword("123456");
        // 设置状态为正常
        teacher.setStatus(1);
        
        return userMapper.insert(teacher) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeacher(User teacher) {
        // 确保是教师角色
        teacher.setRole(1);
        
        // 如果密码为空，则不更新密码字段
        if (teacher.getPassword() == null || teacher.getPassword().trim().isEmpty()) {
            User existingUser = userMapper.selectById(teacher.getId());
            teacher.setPassword(existingUser.getPassword());
        }
        
        return userMapper.updateById(teacher) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacher(Long id) {
        // 逻辑删除教师
        User teacher = new User();
        teacher.setId(id);
        teacher.setDeleted(1);
        
        // 删除教师职称
        LambdaQueryWrapper<TeacherTitle> titleQuery = Wrappers.lambdaQuery();
        titleQuery.eq(TeacherTitle::getTeacherId, id);
        teacherTitleMapper.delete(titleQuery);
        
        return userMapper.updateById(teacher) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeacherStatus(Long id, Integer status) {
        User teacher = new User();
        teacher.setId(id);
        teacher.setStatus(status);
        
        return userMapper.updateById(teacher) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setTeacherTitles(Long teacherId, boolean isNormalTeacher, boolean isCourseLeader, 
                                   boolean isMajorLeader, boolean isViceDean, Long courseId, 
                                   Long majorId, Long collegeId) {
        // 先删除教师现有职称
        LambdaQueryWrapper<TeacherTitle> deleteQuery = Wrappers.lambdaQuery();
        deleteQuery.eq(TeacherTitle::getTeacherId, teacherId);
        teacherTitleMapper.delete(deleteQuery);
        
        List<TeacherTitle> titles = new ArrayList<>();
        
        // 普通教师
        if (isNormalTeacher) {
            TeacherTitle title = new TeacherTitle();
            title.setTeacherId(teacherId);
            title.setTitleType(0); // 普通教师类型为0
            titles.add(title);
        }
        
        // 课程负责人
        if (isCourseLeader && courseId != null) {
            TeacherTitle title = new TeacherTitle();
            title.setTeacherId(teacherId);
            title.setTitleType(1); // 课程负责人类型为1
            title.setCourseId(courseId);
            titles.add(title);
        }
        
        // 专业负责人
        if (isMajorLeader && majorId != null) {
            TeacherTitle title = new TeacherTitle();
            title.setTeacherId(teacherId);
            title.setTitleType(2); // 专业负责人类型为2
            title.setMajorId(majorId);
            titles.add(title);
        }
        
        // 副院长
        if (isViceDean && collegeId != null) {
            TeacherTitle title = new TeacherTitle();
            title.setTeacherId(teacherId);
            title.setTitleType(3); // 副院长类型为3
            title.setCollegeId(collegeId);
            titles.add(title);
        }
        
        // 批量插入职称
        for (TeacherTitle title : titles) {
            teacherTitleMapper.insert(title);
        }
        
        return true;
    }

    @Override
    public Page<User> getStudentList(Page<User> page, String realName, String username, Long classId) {
        return userMapper.getStudentList(page, realName, username, classId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStudent(User student) {
        // 设置学生角色
        student.setRole(0); // 修正：学生role=0，不是2
        // 设置默认密码
        student.setPassword("123456");
        // 设置状态为正常
        student.setStatus(1);
        
        return userMapper.insert(student) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudent(User student) {
        // 确保是学生角色
        student.setRole(0); // 修正：学生role=0，不是2
        
        // 如果密码为空，则不更新密码字段
        if (student.getPassword() == null || student.getPassword().trim().isEmpty()) {
            User existingUser = userMapper.selectById(student.getId());
            student.setPassword(existingUser.getPassword());
        }
        
        return userMapper.updateById(student) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long id) {
        // 逻辑删除学生
        User student = new User();
        student.setId(id);
        student.setDeleted(1);
        
        // 删除学生选课记录
        LambdaQueryWrapper<StudentCourse> courseQuery = Wrappers.lambdaQuery();
        courseQuery.eq(StudentCourse::getStudentId, id);
        studentCourseMapper.delete(courseQuery);
        
        return userMapper.updateById(student) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudentStatus(Long id, Integer status) {
        User student = new User();
        student.setId(id);
        student.setStatus(status);
        
        return userMapper.updateById(student) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean selectCourse(Long studentId, Long courseId) {
        // 检查是否已选课（未删除状态）
        LambdaQueryWrapper<StudentCourse> checkQuery = Wrappers.lambdaQuery();
        checkQuery.eq(StudentCourse::getStudentId, studentId);
        checkQuery.eq(StudentCourse::getCourseId, courseId);
        checkQuery.eq(StudentCourse::getDeleted, 0);
        
        if (studentCourseMapper.selectCount(checkQuery) > 0) {
            return false; // 已选课
        }
        
        try {
            // 首先尝试物理删除这门课程的所有记录（包括逻辑删除的记录）
            // 这样可以确保没有因为唯一约束而导致的插入错误
            studentCourseMapper.deletePhysically(studentId, courseId);
            
            // 插入新的选课记录
            StudentCourse studentCourse = new StudentCourse();
            studentCourse.setStudentId(studentId);
            studentCourse.setCourseId(courseId);
            studentCourse.setDeleted(0);
            
            return studentCourseMapper.insert(studentCourse) > 0;
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("选课失败：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dropCourse(Long studentId, Long courseId) {
        // 查询选课记录
        LambdaQueryWrapper<StudentCourse> query = Wrappers.lambdaQuery();
        query.eq(StudentCourse::getStudentId, studentId);
        query.eq(StudentCourse::getCourseId, courseId);
        query.eq(StudentCourse::getDeleted, 0);
        
        StudentCourse studentCourse = studentCourseMapper.selectOne(query);
        if (studentCourse == null) {
            return false; // 未选课
        }
        
        // 逻辑删除选课记录
        studentCourse.setDeleted(1);
        
        return studentCourseMapper.updateById(studentCourse) > 0;
    }

    @Override
    public List<Course> getAvailableCourses(Long studentId) {
        // 查询所有课程
        LambdaQueryWrapper<Course> courseQuery = Wrappers.lambdaQuery();
        courseQuery.eq(Course::getDeleted, 0);
        List<Course> allCourses = courseMapper.selectList(courseQuery);
        
        // 查询学生已选课程ID
        LambdaQueryWrapper<StudentCourse> selectedQuery = Wrappers.lambdaQuery();
        selectedQuery.eq(StudentCourse::getStudentId, studentId);
        selectedQuery.eq(StudentCourse::getDeleted, 0);
        List<StudentCourse> selectedCourses = studentCourseMapper.selectList(selectedQuery);
        
        // 获取已选课程ID集合
        List<Long> selectedCourseIds = selectedCourses.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());
        
        // 过滤出未选课程
        return allCourses.stream()
                .filter(course -> !selectedCourseIds.contains(course.getId()))
                .collect(Collectors.toList());
    }
} 