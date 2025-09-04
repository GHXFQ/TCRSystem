package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tcr.system.entity.Class;
import com.tcr.system.mapper.ClassMapper;
import com.tcr.system.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 班级服务实现类
 */
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;

    @Override
    public List<Class> getClassList() {
        return classMapper.getClassList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addClass(Class clazz) {
        // 设置默认值
        clazz.setDeleted(0);
        
        return classMapper.insert(clazz) > 0;
    }

    @Override
    public Class getClassDetail(Long id) {
        return classMapper.getClassDetail(id);
    }
} 