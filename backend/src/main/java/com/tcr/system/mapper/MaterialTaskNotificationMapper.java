package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.MaterialTaskNotification;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 教学材料任务通知Mapper接口
 */
public interface MaterialTaskNotificationMapper extends BaseMapper<MaterialTaskNotification> {

    /**
     * 分页查询教师的未读通知
     * @param page 分页对象
     * @param teacherId 教师ID
     * @return 未读通知列表
     */
    @Select("SELECT n.*, mt.title as task_title, c.name as course_name " +
            "FROM material_task_notification n " +
            "JOIN material_task mt ON n.task_id = mt.id " +
            "JOIN course c ON n.course_id = c.id " +
            "WHERE n.teacher_id = #{teacherId} AND n.status = 0 AND n.deleted = 0 " +
            "ORDER BY n.create_time DESC")
    IPage<MaterialTaskNotification> selectUnreadNotifications(Page<MaterialTaskNotification> page, @Param("teacherId") Long teacherId);

    /**
     * 查询教师的未读通知数量
     * @param teacherId 教师ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM material_task_notification " +
            "WHERE teacher_id = #{teacherId} AND status = 0 AND deleted = 0")
    int countUnreadNotifications(@Param("teacherId") Long teacherId);

    /**
     * 查询所有教师的未完成任务提醒
     * @param taskId 任务ID
     * @return 未完成任务的教师和课程信息列表
     */
    @Select("SELECT u.id as teacher_id, c.id as course_id " +
            "FROM course c " +
            "JOIN user u ON c.teacher_id = u.id AND u.deleted = 0 AND u.role = 1 " +
            "JOIN material_task mt ON FIND_IN_SET(c.id, mt.course_ids) AND mt.id = #{taskId} AND mt.deleted = 0 " +
            "LEFT JOIN material_task_completion mtc ON mt.id = mtc.task_id AND mtc.teacher_id = u.id AND mtc.course_id = c.id AND mtc.deleted = 0 " +
            "WHERE c.deleted = 0 AND mtc.id IS NULL")
    List<MaterialTaskNotification> selectUncompletedTeachersForNotification(@Param("taskId") Long taskId);
} 