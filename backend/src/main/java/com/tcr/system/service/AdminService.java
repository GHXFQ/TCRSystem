package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.TeacherTitle;
import com.tcr.system.entity.User;
import com.tcr.system.vo.UserStatisticsVO;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 获取用户统计数据
     * @return 用户统计数据
     */
    UserStatisticsVO getUserStatistics();
    
    /**
     * 分页查询教师列表
     * @param page 分页参数
     * @param realName 教师姓名
     * @param username 工号
     * @param titleType 职称类型
     * @return 教师列表
     */
    Page<User> getTeacherList(Page<User> page, String realName, String username, Integer titleType);
    
    /**
     * 获取教师职称列表
     * @return 教师职称列表
     */
    List<TeacherTitle> getTeacherTitles();
    
    /**
     * 添加教师
     * @param teacher 教师信息
     * @return 是否成功
     */
    boolean addTeacher(User teacher);
    
    /**
     * 修改教师
     * @param teacher 教师信息
     * @return 是否成功
     */
    boolean updateTeacher(User teacher);
    
    /**
     * 删除教师
     * @param id 教师ID
     * @return 是否成功
     */
    boolean deleteTeacher(Long id);
    
    /**
     * 修改教师状态
     * @param id 教师ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateTeacherStatus(Long id, Integer status);
    
    /**
     * 设置教师职称
     * @param teacherId 教师ID
     * @param isNormalTeacher 是否普通教师
     * @param isCourseLeader 是否课程负责人
     * @param isMajorLeader 是否专业负责人
     * @param isViceDean 是否副院长
     * @param courseId 课程ID
     * @param majorId 专业ID
     * @param collegeId 学院ID
     * @return 是否成功
     */
    boolean setTeacherTitles(Long teacherId, boolean isNormalTeacher, boolean isCourseLeader, 
                            boolean isMajorLeader, boolean isViceDean, Long courseId, 
                            Long majorId, Long collegeId);
    
    /**
     * 分页查询学生列表
     * @param page 分页参数
     * @param realName 学生姓名
     * @param username 学号
     * @param classId 班级ID
     * @return 学生列表
     */
    Page<User> getStudentList(Page<User> page, String realName, String username, Long classId);
    
    /**
     * 添加学生
     * @param student 学生信息
     * @return 是否成功
     */
    boolean addStudent(User student);
    
    /**
     * 修改学生
     * @param student 学生信息
     * @return 是否成功
     */
    boolean updateStudent(User student);
    
    /**
     * 删除学生
     * @param id 学生ID
     * @return 是否成功
     */
    boolean deleteStudent(Long id);
    
    /**
     * 修改学生状态
     * @param id 学生ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStudentStatus(Long id, Integer status);
    
    /**
     * 学生选课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean selectCourse(Long studentId, Long courseId);
    
    /**
     * 学生退课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean dropCourse(Long studentId, Long courseId);
    
    /**
     * 获取可选课程列表
     * @param studentId 学生ID
     * @return 可选课程列表
     */
    List<Course> getAvailableCourses(Long studentId);
} 