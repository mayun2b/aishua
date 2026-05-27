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

/**
 * 学科控制器，负责相关业务逻辑与流程处理。
 */
@Validated
@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {
    private final SubjectService subjectService;
    private final AdminAccess adminAccess;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public AdminSubjectController(SubjectService subjectService, AdminAccess adminAccess) {
        this.subjectService = subjectService;
        this.adminAccess = adminAccess;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping
    public Result<List<SubjectVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        if (enabled != null && !Integer.valueOf(0).equals(enabled) && !Integer.valueOf(1).equals(enabled)) {
            throw new BusinessException("启用状态只能是 0 或 1", 400);
        }
        // 调用服务层处理业务并封装统一响应。
        return Result.success(subjectService.listAdminSubjects(keyword, enabled));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping
    public Result<SubjectVO> create(@Valid @RequestBody SubjectUpsertDTO subjectUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(subjectService.createSubject(subjectUpsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/{id}")
    public Result<SubjectVO> update(
            @PathVariable Long id,
            @Valid @RequestBody SubjectUpsertDTO subjectUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(subjectService.updateSubject(id, subjectUpsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/{id}/enabled")
    public Result<SubjectVO> updateEnabled(
            @PathVariable Long id,
            @RequestParam Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(subjectService.updateEnabledStatus(id, enabled));
    }

    /**
     * 处理删除请求并返回结果。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        subjectService.deleteSubject(id);
        // 调用服务层处理业务并封装统一响应。
        return Result.success();
    }
}
