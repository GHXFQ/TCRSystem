package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Assignment;
import com.tcr.system.entity.AssignmentSubmission;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.StudentCourse;
import com.tcr.system.entity.User;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.AssignmentMapper;
import com.tcr.system.mapper.AssignmentSubmissionMapper;
import com.tcr.system.mapper.CourseMapper;
import com.tcr.system.mapper.StudentCourseMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.AssignmentService;
import com.tcr.system.vo.AssignmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作业Service实现类
 */
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {

    private final AssignmentSubmissionMapper submissionMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final StudentCourseMapper studentCourseMapper;

    @Override
    public Page<AssignmentVO> getTeacherAssignments(Page<Assignment> page, Long teacherId, Long courseId, Integer status) {
        // 创建查询条件
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getTeacherId, teacherId);
        
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        
        if (status != null) {
            wrapper.eq(Assignment::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Assignment::getCreateTime);
        
        // 执行查询
        Page<Assignment> assignmentPage = page(page, wrapper);
        
        // 创建返回结果
        Page<AssignmentVO> resultPage = new Page<>();
        BeanUtils.copyProperties(assignmentPage, resultPage, "records");
        
        // 处理记录
        List<AssignmentVO> records = new ArrayList<>();
        if (assignmentPage.getRecords() != null && !assignmentPage.getRecords().isEmpty()) {
            for (Assignment assignment : assignmentPage.getRecords()) {
                AssignmentVO vo = convertToVO(assignment);
                
                // 获取课程信息
                if (assignment.getCourseId() != null) {
                    Course course = courseMapper.selectById(assignment.getCourseId());
                    if (course != null) {
                        vo.setCourseName(course.getName());
                    }
                }
                
                // 获取教师信息
                if (assignment.getTeacherId() != null) {
                    User teacher = userMapper.selectById(assignment.getTeacherId());
                if (teacher != null) {
                        vo.setTeacherName(teacher.getRealName());
                    }
                }
                
                // 计算提交情况
                calculateSubmissionRate(vo);
                
                records.add(vo);
            }
        }
        
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<AssignmentVO> getStudentAssignments(Page<Assignment> page, Long studentId, Long courseId, Integer status) {
        // 创建查询条件
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        } else {
            // 如果没有指定课程，则获取学生选修的所有课程
            List<StudentCourse> studentCourses = studentCourseMapper.selectList(
                    new LambdaQueryWrapper<StudentCourse>()
                            .eq(StudentCourse::getStudentId, studentId)
                            .eq(StudentCourse::getDeleted, 0)
            );
            
            if (studentCourses.isEmpty()) {
                // 学生没有选修任何课程
                Page<AssignmentVO> emptyPage = new Page<>();
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            
            // 获取学生选修的课程ID列表
            List<Long> courseIds = studentCourses.stream()
                    .map(StudentCourse::getCourseId)
                    .collect(Collectors.toList());
            
            wrapper.in(Assignment::getCourseId, courseIds);
        }
        
        if (status != null) {
            wrapper.eq(Assignment::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Assignment::getCreateTime);
        
        // 执行查询
        Page<Assignment> assignmentPage = page(page, wrapper);
        
        // 创建返回结果
        Page<AssignmentVO> resultPage = new Page<>();
        BeanUtils.copyProperties(assignmentPage, resultPage, "records");
        
        // 处理记录
        List<AssignmentVO> records = new ArrayList<>();
        if (assignmentPage.getRecords() != null && !assignmentPage.getRecords().isEmpty()) {
            for (Assignment assignment : assignmentPage.getRecords()) {
                AssignmentVO vo = convertToVO(assignment);
                
                // 获取课程信息
                if (assignment.getCourseId() != null) {
                    Course course = courseMapper.selectById(assignment.getCourseId());
                    if (course != null) {
                        vo.setCourseName(course.getName());
                    }
                }
                
                // 获取教师信息
                if (assignment.getTeacherId() != null) {
                    User teacher = userMapper.selectById(assignment.getTeacherId());
                    if (teacher != null) {
                        vo.setTeacherName(teacher.getRealName());
                    }
                }
                
                // 获取学生是否已提交
                LambdaQueryWrapper<AssignmentSubmission> submissionWrapper = new LambdaQueryWrapper<>();
                submissionWrapper.eq(AssignmentSubmission::getAssignmentId, assignment.getId());
                submissionWrapper.eq(AssignmentSubmission::getStudentId, studentId);
                submissionWrapper.eq(AssignmentSubmission::getDeleted, 0);
                AssignmentSubmission submission = submissionMapper.selectOne(submissionWrapper);
                
                // 创建提交信息对象，适配前端所需的格式
                Map<String, Object> submissionInfo = new HashMap<>();
                
                if (submission != null) {
                    // 适配前端状态：0-未提交, 1-已提交, 2-已批改
                    vo.setSubmitted(true); // 已提交
                    vo.setSubmissionId(submission.getId());
                    
                    // 设置提交信息对象
                    submissionInfo.put("id", submission.getId());
                    submissionInfo.put("content", submission.getContent());
                    submissionInfo.put("filePath", submission.getFilePath());
                    submissionInfo.put("fileName", submission.getFilePath() != null ? 
                        submission.getFilePath().substring(submission.getFilePath().lastIndexOf("/") + 1) : null);
                    submissionInfo.put("submitTime", submission.getCreateTime());
                    
                    // 检查是否逾期提交
                    boolean isOverdue = false;
                    if (assignment.getDeadline() != null && submission.getCreateTime() != null) {
                        isOverdue = submission.getCreateTime().isAfter(assignment.getDeadline());
                    }
                    submissionInfo.put("isOverdue", isOverdue);
                    
                    // 根据后端状态(0-已提交,1-已批改)适配前端状态
                    if (submission.getStatus() != null && submission.getStatus() == 1) {
                        // 已批改(后端status=1)对应前端状态2
                        vo.setScore(submission.getScore());
                        vo.setSubmissionStatus(2); // 前端状态：已批改
                        submissionInfo.put("status", 2); // 前端状态：已批改
                        submissionInfo.put("score", submission.getScore());
                        submissionInfo.put("comment", submission.getComment());
                    } else {
                        // 已提交未批改(后端status=0)对应前端状态1
                        vo.setSubmissionStatus(1); // 前端状态：已提交
                        submissionInfo.put("status", 1); // 前端状态：已提交
                    }
                    
                    vo.setSubmittedCount(1);
                    vo.setTotalStudents(1);
                    vo.setSubmissionRate("100%");
                } else {
                    vo.setSubmitted(false); // 未提交
                    vo.setSubmissionId(null);
                    vo.setScore(null);
                    vo.setSubmissionStatus(0); // 前端状态：未提交
                    submissionInfo.put("status", 0); // 前端状态：未提交
                    vo.setSubmittedCount(0);
                    vo.setTotalStudents(1);
                    vo.setSubmissionRate("0%");
                }
                
                // 将提交信息添加到VO中
                vo.setSubmissionInfo(submissionInfo);
                
                records.add(vo);
            }
        }
        
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishAssignment(Assignment assignment) {
        // 验证参数
        if (assignment.getTitle() == null || assignment.getTitle().trim().isEmpty()) {
            throw new BusinessException("作业标题不能为空");
        }
        
        if (assignment.getCourseId() == null) {
            throw new BusinessException("所属课程不能为空");
        }
        
        if (assignment.getTeacherId() == null) {
            throw new BusinessException("发布教师不能为空");
        }
        
        if (assignment.getDeadline() == null) {
            throw new BusinessException("截止日期不能为空");
        }
        
        // 设置状态
        if (assignment.getStatus() == null) {
            assignment.setStatus(1); // 默认状态为进行中
        }
        
        // 保存
        save(assignment);
        
        return assignment.getId();
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("作业ID不能为空");
        }
        
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        
        Assignment assignment = getById(id);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        
        assignment.setStatus(status);
        
        return updateById(assignment);
    }

    @Override
    public AssignmentVO getAssignmentDetail(Long id) {
        Assignment assignment = getById(id);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        
        AssignmentVO vo = convertToVO(assignment);
        
        // 获取课程信息
        if (assignment.getCourseId() != null) {
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
            }
        }
        
        // 获取教师信息
        if (assignment.getTeacherId() != null) {
            User teacher = userMapper.selectById(assignment.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
        }
        
        // 计算提交情况
        calculateSubmissionRate(vo);
        
        return vo;
    }

    @Override
    public Map<String, Object> getAssignmentStatistics(Long teacherId, Long courseId) {
        Map<String, Object> result = new HashMap<>();
        
        // 创建查询条件
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getTeacherId, teacherId);
        
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        
        // 统计总数
        long totalCount = count(wrapper);
        result.put("totalCount", totalCount);
        
        // 统计进行中的作业数
        wrapper.clear();
        wrapper.eq(Assignment::getTeacherId, teacherId);
        wrapper.eq(Assignment::getStatus, 1);
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        long ongoingCount = count(wrapper);
        result.put("ongoingCount", ongoingCount);
        
        // 统计已结束的作业数
        wrapper.clear();
        wrapper.eq(Assignment::getTeacherId, teacherId);
        wrapper.eq(Assignment::getStatus, 2);
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        long completedCount = count(wrapper);
        result.put("completedCount", completedCount);
        
        // 统计已关闭的作业数
        wrapper.clear();
        wrapper.eq(Assignment::getTeacherId, teacherId);
        wrapper.eq(Assignment::getStatus, 0);
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        long closedCount = count(wrapper);
        result.put("closedCount", closedCount);
        
        // 计算各状态占比
        if (totalCount > 0) {
            result.put("ongoingRate", String.format("%.2f", (double) ongoingCount / totalCount * 100) + "%");
            result.put("completedRate", String.format("%.2f", (double) completedCount / totalCount * 100) + "%");
            result.put("closedRate", String.format("%.2f", (double) closedCount / totalCount * 100) + "%");
        } else {
            result.put("ongoingRate", "0%");
            result.put("completedRate", "0%");
            result.put("closedRate", "0%");
        }
        
        // 获取各课程的作业数量
        if (courseId == null) {
            // 这里简化处理，直接查询课程信息
            List<Map<String, Object>> courseAssignmentCount = new ArrayList<>();
            
            // 查询教师所有作业对应的课程
            LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<>();
            assignmentWrapper.eq(Assignment::getTeacherId, teacherId);
            assignmentWrapper.select(Assignment::getCourseId);
            assignmentWrapper.groupBy(Assignment::getCourseId);
            
            List<Assignment> courseAssignments = list(assignmentWrapper);
            for (Assignment assignment : courseAssignments) {
                Long cId = assignment.getCourseId();
                if (cId != null) {
                    // 查询课程信息
                    Course course = courseMapper.selectById(cId);
                    if (course != null) {
                        // 查询该课程的作业数量
                        LambdaQueryWrapper<Assignment> countWrapper = new LambdaQueryWrapper<>();
                        countWrapper.eq(Assignment::getTeacherId, teacherId);
                        countWrapper.eq(Assignment::getCourseId, cId);
                        long count = count(countWrapper);
                        
                        Map<String, Object> item = new HashMap<>();
                        item.put("courseId", cId);
                        item.put("courseName", course.getName());
                        item.put("count", count);
                        courseAssignmentCount.add(item);
                    }
                }
            }
            
            result.put("courseAssignmentCount", courseAssignmentCount);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getSubmissionRateInfo(Long teacherId) {
        // 获取教师发布的所有作业
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getTeacherId, teacherId);
        wrapper.orderByDesc(Assignment::getCreateTime);
        List<Assignment> assignments = list(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Assignment assignment : assignments) {
            Map<String, Object> item = new HashMap<>();
            item.put("assignmentId", assignment.getId());
            item.put("title", assignment.getTitle());
            
            // 获取课程信息
            if (assignment.getCourseId() != null) {
                Course course = courseMapper.selectById(assignment.getCourseId());
                if (course != null) {
                    item.put("courseName", course.getName());
                }
            }
            
            // 计算提交率
            // 获取选课学生数量
            LambdaQueryWrapper<StudentCourse> studentCourseWrapper = new LambdaQueryWrapper<>();
            studentCourseWrapper.eq(StudentCourse::getCourseId, assignment.getCourseId());
            studentCourseWrapper.eq(StudentCourse::getDeleted, 0);
            long totalStudents = studentCourseMapper.selectCount(studentCourseWrapper);
            
            // 获取已提交作业的学生数量
            LambdaQueryWrapper<AssignmentSubmission> submissionWrapper = new LambdaQueryWrapper<>();
            submissionWrapper.eq(AssignmentSubmission::getAssignmentId, assignment.getId());
            submissionWrapper.eq(AssignmentSubmission::getDeleted, 0);
            long submittedCount = submissionMapper.selectCount(submissionWrapper);
            
            item.put("totalStudents", totalStudents);
            item.put("submittedCount", submittedCount);
            
            // 计算提交率
            if (totalStudents > 0) {
                double rate = (double) submittedCount / totalStudents * 100;
                item.put("submissionRate", String.format("%.2f", rate) + "%");
            } else {
                item.put("submissionRate", "0%");
            }
            
            // 获取已批改数量
            submissionWrapper.clear();
            submissionWrapper.eq(AssignmentSubmission::getAssignmentId, assignment.getId());
            submissionWrapper.eq(AssignmentSubmission::getStatus, 1); // 已批改
            submissionWrapper.eq(AssignmentSubmission::getDeleted, 0);
            long gradedCount = submissionMapper.selectCount(submissionWrapper);
            
            item.put("gradedCount", gradedCount);
            
            // 计算批改率
            if (submittedCount > 0) {
                double rate = (double) gradedCount / submittedCount * 100;
                item.put("gradedRate", String.format("%.2f", rate) + "%");
            } else {
                item.put("gradedRate", "0%");
            }
            
            // 获取平均分
            // 查询平均分
            LambdaQueryWrapper<AssignmentSubmission> avgScoreWrapper = new LambdaQueryWrapper<>();
            avgScoreWrapper.eq(AssignmentSubmission::getAssignmentId, assignment.getId());
            avgScoreWrapper.eq(AssignmentSubmission::getStatus, 1); // 只统计已批改的
            avgScoreWrapper.eq(AssignmentSubmission::getDeleted, 0);
            avgScoreWrapper.isNotNull(AssignmentSubmission::getScore);
            
            List<AssignmentSubmission> scoredSubmissions = submissionMapper.selectList(avgScoreWrapper);
            if (!scoredSubmissions.isEmpty()) {
                double totalScore = scoredSubmissions.stream()
                        .mapToInt(sub -> sub.getScore() != null ? sub.getScore() : 0)
                        .sum();
                double avgScore = totalScore / scoredSubmissions.size();
                item.put("avgScore", String.format("%.1f", avgScore));
            } else {
                item.put("avgScore", "--");
            }
            
            // 设置截止日期
            item.put("deadline", assignment.getDeadline() != null ? 
                    assignment.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "--");
            
            item.put("status", assignment.getStatus());
            
            result.add(item);
        }
        
        return result;
    }

    @Override
    public void autoUpdateAssignmentStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有进行中且截止日期已过的作业
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getStatus, 1); // 进行中
        wrapper.lt(Assignment::getDeadline, now); // 截止日期已过
        
        List<Assignment> assignments = list(wrapper);
        
        for (Assignment assignment : assignments) {
            assignment.setStatus(2); // 设置为已结束
            updateById(assignment);
        }
    }

    /**
     * 将实体转换为VO
     */
    private AssignmentVO convertToVO(Assignment assignment) {
        AssignmentVO vo = new AssignmentVO();
        BeanUtils.copyProperties(assignment, vo);
        
        // 设置默认值
        vo.setSubmitted(false);
        vo.setSubmissionId(null);
        vo.setScore(null);
        vo.setSubmissionStatus(0); // 默认为未提交
        vo.setSubmittedCount(0);
        vo.setTotalStudents(0);
        vo.setSubmissionRate("0%");
        
        // 初始化空的提交信息对象
        Map<String, Object> emptySubmissionInfo = new HashMap<>();
        emptySubmissionInfo.put("status", 0); // 默认未提交
        vo.setSubmissionInfo(emptySubmissionInfo);
        
        return vo;
    }

    /**
     * 计算提交率
     */
    private void calculateSubmissionRate(AssignmentVO vo) {
        // 获取选课学生数量
        LambdaQueryWrapper<StudentCourse> studentCourseWrapper = new LambdaQueryWrapper<>();
        studentCourseWrapper.eq(StudentCourse::getCourseId, vo.getCourseId());
        studentCourseWrapper.eq(StudentCourse::getDeleted, 0);
        long totalStudents = studentCourseMapper.selectCount(studentCourseWrapper);
        
        // 获取已提交作业的学生数量
        LambdaQueryWrapper<AssignmentSubmission> submissionWrapper = new LambdaQueryWrapper<>();
        submissionWrapper.eq(AssignmentSubmission::getAssignmentId, vo.getId());
        submissionWrapper.eq(AssignmentSubmission::getDeleted, 0);
        long submittedCount = submissionMapper.selectCount(submissionWrapper);
        
        // 获取已批改的作业数量
        LambdaQueryWrapper<AssignmentSubmission> gradedWrapper = new LambdaQueryWrapper<>();
        gradedWrapper.eq(AssignmentSubmission::getAssignmentId, vo.getId());
        gradedWrapper.eq(AssignmentSubmission::getStatus, 1); // 已批改
        gradedWrapper.eq(AssignmentSubmission::getDeleted, 0);
        long gradedCount = submissionMapper.selectCount(gradedWrapper);
        
        // 计算平均分
        Double avgScore = null;
        if (gradedCount > 0) {
            LambdaQueryWrapper<AssignmentSubmission> avgScoreWrapper = new LambdaQueryWrapper<>();
            avgScoreWrapper.eq(AssignmentSubmission::getAssignmentId, vo.getId());
            avgScoreWrapper.eq(AssignmentSubmission::getStatus, 1); // 只统计已批改的
            avgScoreWrapper.eq(AssignmentSubmission::getDeleted, 0);
            avgScoreWrapper.isNotNull(AssignmentSubmission::getScore);
            
            List<AssignmentSubmission> gradedSubmissions = submissionMapper.selectList(avgScoreWrapper);
            if (!gradedSubmissions.isEmpty()) {
                int totalScore = gradedSubmissions.stream()
                        .mapToInt(submission -> submission.getScore() != null ? submission.getScore() : 0)
                        .sum();
                avgScore = (double) totalScore / gradedSubmissions.size();
            }
        }
        
        vo.setTotalStudents((int)totalStudents);
        vo.setSubmittedCount((int)submittedCount); // 这个字段就是提交数
        
        // 计算提交率
        if (totalStudents > 0) {
            double rate = (double) submittedCount / totalStudents * 100;
            vo.setSubmissionRate(String.format("%.2f", rate) + "%");
        } else {
            vo.setSubmissionRate("0%");
        }
        
        // 设置平均分
        if (avgScore != null) {
            vo.setAvgScore(String.format("%.1f", avgScore));
        } else {
            vo.setAvgScore("暂无");
        }
    }
} 