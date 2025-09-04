package com.tcr.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.vo.MaterialTaskProgressVO;
import com.tcr.system.vo.MaterialTaskReminderVO;

import java.util.List;
import java.util.Map;

/**
 * 教学材料任务服务接口
 */
public interface MaterialTaskService extends IService<MaterialTask> {

    /**
     * 分页查询任务列表
     * @param page 分页对象
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @param status 状态
     * @param title 任务标题（模糊查询）
     * @return 分页结果
     */
    IPage<MaterialTask> getTaskPage(Page<MaterialTask> page, Long materialTypeId, String academicYear, Integer semester, Integer status, String title);

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    MaterialTask getTaskById(Long id);

    /**
     * 添加任务
     * @param task 任务信息
     * @return 是否成功
     */
    boolean addTask(MaterialTask task);

    /**
     * 更新任务
     * @param task 任务信息
     * @return 是否成功
     */
    boolean updateTask(MaterialTask task);

    /**
     * 删除任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean deleteTask(Long id);

    /**
     * 批量删除任务
     * @param ids 任务ID列表
     * @return 是否成功
     */
    boolean deleteTaskBatch(List<Long> ids);

    /**
     * 发布任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean publishTask(Long id);

    /**
     * 批量发布任务
     * @param ids 任务ID列表
     * @return 是否成功
     */
    boolean publishTaskBatch(List<Long> ids);

    /**
     * 获取任务完成情况
     * @param id 任务ID
     * @return 任务完成情况
     */
    MaterialTaskProgressVO getTaskProgress(Long id);

    /**
     * 提醒未完成任务的教师
     * @param taskId 任务ID
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean remindTeacher(Long taskId, Long teacherId);

    /**
     * 批量提醒未完成任务的教师
     * @param reminders 提醒信息列表
     * @return 是否成功
     */
    boolean remindTeacherBatch(List<MaterialTaskReminderVO> reminders);

    /**
     * 获取最近的任务
     * @param limit 限制数量
     * @return 任务列表
     */
    List<MaterialTask> getRecentTasks(int limit);

    /**
     * 获取教师的任务列表
     * @param teacherId 教师ID
     * @return 任务列表，包含完成状态
     */
    List<Map<String, Object>> getTeacherTasks(Long teacherId);

    /**
     * 获取统计概览数据
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 统计概览数据
     */
    Map<String, Object> getStatisticsOverview(Long materialTypeId, String academicYear, Integer semester);

    /**
     * 按材料类型统计
     * @return 统计结果
     */
    List<Map<String, Object>> statisticsByType();

    /**
     * 按学院统计
     * @return 统计结果
     */
    List<Map<String, Object>> statisticsByCollege();

    /**
     * 获取未完成任务的教师列表
     * @param page 分页对象
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 未完成任务的教师信息列表
     */
    IPage<Map<String, Object>> getUncompletedTeachersPage(Page<Map<String, Object>> page, Long materialTypeId, String academicYear, Integer semester);
} 