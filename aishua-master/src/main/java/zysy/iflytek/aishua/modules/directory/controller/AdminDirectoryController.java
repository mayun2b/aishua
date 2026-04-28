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

@Validated
@RestController
@RequestMapping("/api/admin/directories")
public class AdminDirectoryController {
    private final DirectoryService directoryService;
    private final AdminAccess adminAccess;

    public AdminDirectoryController(DirectoryService directoryService, AdminAccess adminAccess) {
        this.directoryService = directoryService;
        this.adminAccess = adminAccess;
    }

    @GetMapping("/tree")
    public Result<List<DirectoryTreeVO>> listTree(
            @RequestParam @Min(value = 1, message = "学科ID不合法") Long subjectId
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(directoryService.listTreeBySubject(subjectId));
    }

    @PostMapping
    public Result<DirectoryTreeVO> create(@Valid @RequestBody DirectoryUpsertDTO directoryUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(directoryService.createDirectory(directoryUpsertDTO));
    }

    @PutMapping("/{id}")
    public Result<DirectoryTreeVO> update(
            @PathVariable Long id,
            @Valid @RequestBody DirectoryUpsertDTO directoryUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(directoryService.updateDirectory(id, directoryUpsertDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        directoryService.deleteDirectory(id);
        return Result.success();
    }
}
