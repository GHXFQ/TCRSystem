package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.College;
import com.tcr.system.entity.Major;

import java.util.List;

/**
 * 专业Service接口
 */
public interface MajorService extends IService<Major> {
    
    /**
     * 获取学院列表
     * @return 学院列表
     */
    List<College> getCollegeList();
} 