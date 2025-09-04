package com.tcr.system.service;

import com.tcr.system.entity.Class;

import java.util.List;

/**
 * 班级服务接口
 */
public interface ClassService {
    
    /**
     * 获取班级列表
     * @return 班级列表
     */
    List<Class> getClassList();
    
    /**
     * 添加班级
     * @param clazz 班级信息
     * @return 是否成功
     */
    boolean addClass(Class clazz);
    
    /**
     * 获取班级详情
     * @param id 班级ID
     * @return 班级详情
     */
    Class getClassDetail(Long id);
} 