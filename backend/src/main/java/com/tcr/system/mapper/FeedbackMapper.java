package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈Mapper接口
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
} 