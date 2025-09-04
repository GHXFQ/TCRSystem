package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.College;
import com.tcr.system.entity.Major;
import com.tcr.system.mapper.CollegeMapper;
import com.tcr.system.mapper.MajorMapper;
import com.tcr.system.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专业Service实现类
 */
@Service
@RequiredArgsConstructor
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
    
    private final CollegeMapper collegeMapper;
    
    @Override
    public List<College> getCollegeList() {
        // 查询所有未删除的学院
        LambdaQueryWrapper<College> query = Wrappers.lambdaQuery();
        query.eq(College::getDeleted, 0);
        return collegeMapper.selectList(query);
    }
} 