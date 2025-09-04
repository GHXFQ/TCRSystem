package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.MaterialReviewRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 教学材料审核记录Mapper接口
 */
@Mapper
public interface MaterialReviewRecordMapper extends BaseMapper<MaterialReviewRecord> {
    
    /**
     * 获取审核记录列表
     * @param reviewId 审核ID
     * @return 审核记录列表
     */
    List<Map<String, Object>> getReviewRecords(@Param("reviewId") Long reviewId);
} 