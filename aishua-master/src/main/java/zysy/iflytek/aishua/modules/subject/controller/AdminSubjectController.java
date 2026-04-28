package zysy.iflytek.aishua.modules.subject.controller;

import jakarta.validation.Valid;
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
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.common.security.AdminAccess;
import zysy.iflytek.aishua.modules.subject.entity.dto.SubjectUpsertDTO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectVO;
import zysy.iflytek.aishua.modules.subject.service.SubjectService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {
    private final SubjectService subjectService;
    private final AdminAccess adminAccess;

    public AdminSubjectController(SubjectService subjectService, AdminAccess adminAccess) {
        this.subjectService = subjectService;
        this.adminAccess = adminAccess;
    }

    @GetMapping
    public Result<List<SubjectVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        if (enabled != null && !Integer.valueOf(0).equals(enabled) && !Integer.valueOf(1).equals(enabled)) {
            throw new BusinessException("启用状态只能是 0 或 1", 400);
        }
        return Result.success(subjectService.listAdminSubjects(keyword, enabled));
    }

    @PostMapping
    public Result<SubjectVO> create(@Valid @RequestBody SubjectUpsertDTO subjectUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(subjectService.createSubject(subjectUpsertDTO));
    }

    @PutMapping("/{id}")
    public Result<SubjectVO> update(
            @PathVariable Long id,
            @Valid @RequestBody SubjectUpsertDTO subjectUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(subjectService.updateSubject(id, subjectUpsertDTO));
    }

    @PutMapping("/{id}/enabled")
    public Result<SubjectVO> updateEnabled(
            @PathVariable Long id,
            @RequestParam Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(subjectService.updateEnabledStatus(id, enabled));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        subjectService.deleteSubject(id);
        return Result.success();
    }
}
