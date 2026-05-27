package zysy.iflytek.aishua.modules.directory.controller;

import jakarta.validation.Valid;
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
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.common.security.AdminAccess;
import zysy.iflytek.aishua.modules.directory.entity.dto.DirectoryUpsertDTO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;
import zysy.iflytek.aishua.modules.directory.service.DirectoryService;

import java.util.List;

/**
 * 目录控制器，负责相关业务逻辑与流程处理。
 */
@Validated
@RestController
@RequestMapping("/api/admin/directories")
public class AdminDirectoryController {
    private final DirectoryService directoryService;
    private final AdminAccess adminAccess;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public AdminDirectoryController(DirectoryService directoryService, AdminAccess adminAccess) {
        this.directoryService = directoryService;
        this.adminAccess = adminAccess;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/tree")
    public Result<List<DirectoryTreeVO>> listTree(
            @RequestParam @Min(value = 1, message = "学科编号不合法") Long subjectId
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(directoryService.listTreeBySubject(subjectId));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping
    public Result<DirectoryTreeVO> create(@Valid @RequestBody DirectoryUpsertDTO directoryUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(directoryService.createDirectory(directoryUpsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/{id}")
    public Result<DirectoryTreeVO> update(
            @PathVariable Long id,
            @Valid @RequestBody DirectoryUpsertDTO directoryUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(directoryService.updateDirectory(id, directoryUpsertDTO));
    }

    /**
     * 处理删除请求并返回结果。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        directoryService.deleteDirectory(id);
        // 调用服务层处理业务并封装统一响应。
        return Result.success();
    }
}
