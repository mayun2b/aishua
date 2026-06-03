package zysy.iflytek.aishua.modules.directory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.common.security.AdminAccess;
import zysy.iflytek.aishua.modules.directory.entity.dto.DirectoryTagRelationUpsertDTO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTagRelationVO;
import zysy.iflytek.aishua.modules.directory.service.DirectoryTagRelationService;

/**
 * 目录-考点关系管理接口。
 */
@Validated
@RestController
@RequestMapping("/api/admin/directory-tag-relations")
public class AdminDirectoryTagRelationController {
    private final DirectoryTagRelationService relationService;
    private final AdminAccess adminAccess;

    public AdminDirectoryTagRelationController(
            DirectoryTagRelationService relationService,
            AdminAccess adminAccess
    ) {
        this.relationService = relationService;
        this.adminAccess = adminAccess;
    }

    @GetMapping
    public Result<PageResult<DirectoryTagRelationVO>> list(
            @RequestParam(required = false) @Min(value = 1, message = "学科编号不合法") Long subjectId,
            @RequestParam(required = false) @Min(value = 1, message = "目录编号不合法") Long directoryId,
            @RequestParam(required = false) @Min(value = 1, message = "考点编号不合法") Long tagId,
            @RequestParam(required = false) @Min(value = 0, message = "启用状态不合法")
            @Max(value = 1, message = "启用状态不合法") Integer isEnabled,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于 0")
            @Max(value = 100, message = "每页数量不能超过 100") Integer pageSize
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(relationService.listRelations(subjectId, directoryId, tagId, isEnabled, keyword, pageNum, pageSize));
    }

    @PostMapping
    public Result<DirectoryTagRelationVO> create(@Valid @RequestBody DirectoryTagRelationUpsertDTO upsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(relationService.createRelation(upsertDTO));
    }

    @PutMapping("/{id}")
    public Result<DirectoryTagRelationVO> update(
            @PathVariable Long id,
            @Valid @RequestBody DirectoryTagRelationUpsertDTO upsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(relationService.updateRelation(id, upsertDTO));
    }

    @PutMapping("/{id}/enabled")
    public Result<DirectoryTagRelationVO> updateEnabled(
            @PathVariable Long id,
            @RequestParam @Min(value = 0, message = "启用状态不合法")
            @Max(value = 1, message = "启用状态不合法") Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(relationService.updateEnabled(id, enabled));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        relationService.deleteRelation(id);
        return Result.success();
    }
}
