package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "培训材料管理", description = "培训材料相关接口")
@RestController
@RequestMapping("/api/training-materials")
public class TrainingMaterialController {

    @Autowired
    private TrainingMaterialService trainingMaterialService;

    @Operation(summary = "获取已发布的培训材料", description = "获取所有已发布状态的培训材料列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取培训材料列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    })
    @GetMapping("/published")
    public List<TrainingMaterial> getPublishedMaterials() {
        return trainingMaterialService.getPublishedMaterials();
    }

    @Operation(summary = "获取指定类型的培训材料", description = "获取指定类型的所有培训材料列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取培训材料列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    })
    @GetMapping("/type/{type}")
    public List<TrainingMaterial> getMaterialsByType(
            @Parameter(description = "培训材料类型（1=文档, 2=视频, 3=测试）", required = true) @PathVariable Integer type) {
        return trainingMaterialService.getMaterialsByType(type);
    }

    @Operation(summary = "创建培训材料", description = "创建新的培训材料", responses = {
            @ApiResponse(responseCode = "200", description = "成功创建培训材料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    })
    @PostMapping
    public TrainingMaterial createMaterial(@RequestBody TrainingMaterial material) {
        boolean success = trainingMaterialService.saveOrUpdate(material);
        return success ? material : null;
    }

    @Operation(summary = "更新培训材料", description = "更新指定ID的培训材料", responses = {
            @ApiResponse(responseCode = "200", description = "成功更新培训材料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    })
    @PutMapping("/{materialId}")
    public TrainingMaterial updateMaterial(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Long materialId,
            @RequestBody TrainingMaterial material) {
        material.setMaterialId(materialId);
        return trainingMaterialService.updateById(material) ? material : null;
    }

    @Operation(summary = "更新培训材料状态", description = "更新指定培训材料的状态（发布/下架等）", responses = {
            @ApiResponse(responseCode = "200", description = "成功更新状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @PutMapping("/{materialId}/status/{status}")
    public boolean updateStatus(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Long materialId,
            @Parameter(description = "状态（0=草稿, 1=发布, 2=下架）", required = true) @PathVariable Integer status) {
        return trainingMaterialService.updateStatus(materialId, status);
    }

    @Operation(summary = "更新通过分数", description = "更新指定培训材料的考试通过分数", responses = {
            @ApiResponse(responseCode = "200", description = "成功更新通过分数", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @PutMapping("/{materialId}/pass-score/{passScore}")
    public boolean updatePassScore(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Long materialId,
            @Parameter(description = "通过分数", required = true) @PathVariable Integer passScore) {
        return trainingMaterialService.updatePassScore(materialId, passScore);
    }

    @Operation(summary = "删除培训材料", description = "删除指定ID的培训材料", responses = {
            @ApiResponse(responseCode = "200", description = "成功删除培训材料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @DeleteMapping("/{materialId}")
    public boolean deleteMaterial(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Long materialId) {
        return trainingMaterialService.removeById(materialId);
    }
}