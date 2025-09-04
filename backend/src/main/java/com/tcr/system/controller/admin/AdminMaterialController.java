package com.tcr.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Material;
import com.tcr.system.service.MaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员材料控制器
 */
@RestController
@RequestMapping("/api/admin/material")
@RequiredArgsConstructor
@Api(tags = "管理员材料管理")
public class AdminMaterialController {

    private final MaterialService materialService;

    /**
     * 获取材料总数
     */
    @GetMapping("/count")
    @ApiOperation("获取材料总数")
    public Result<Long> count() {
        LambdaQueryWrapper<Material> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Material::getDeleted, 0); // 未删除
        long count = materialService.count(queryWrapper);
        return Result.success(count);
    }
} 