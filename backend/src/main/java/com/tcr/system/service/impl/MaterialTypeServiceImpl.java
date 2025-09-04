package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.MaterialType;
import com.tcr.system.mapper.MaterialTypeMapper;
import com.tcr.system.service.MaterialTypeService;
import org.springframework.stereotype.Service;

/**
 * 教学材料类型Service实现类
 */
@Service
public class MaterialTypeServiceImpl extends ServiceImpl<MaterialTypeMapper, MaterialType> implements MaterialTypeService {
} 