package com.tcr.system.controller;

import com.tcr.system.common.Result;
import com.tcr.system.entity.MaterialType;
import com.tcr.system.service.MaterialTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教学材料类型Controller
 */
@RestController
@RequestMapping("/api/material-type")
public class MaterialTypeController {

    @Autowired
    private MaterialTypeService materialTypeService;

    /**
     * 获取所有材料类型列表
     */
    @GetMapping("/list")
    public Result<List<MaterialType>> list() {
        List<MaterialType> list = materialTypeService.list();
        return Result.success(list);
    }
} 