package zysy.iflytek.aishua.modules.tag.controller;

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
import zysy.iflytek.aishua.modules.tag.entity.dto.ExamTagUpsertDTO;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;
import zysy.iflytek.aishua.modules.tag.service.ExamTagService;

import java.util.List;

/**
 * 标签控制器，负责相关业务逻辑与流程处理。
 */
@Validated
@RestController
@RequestMapping("/api/admin/tags")
public class AdminExamTagController {
    private final ExamTagService examTagService;
    private final AdminAccess adminAccess;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public AdminExamTagController(ExamTagService examTagService, AdminAccess adminAccess) {
        this.examTagService = examTagService;
        this.adminAccess = adminAccess;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping
    public Result<List<ExamTagVO>> list(
            @RequestParam(required = false) @Min(value = 1, message = "学科编号不合法") Long subjectId,
            @RequestParam(required = false) String keyword
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examTagService.listTags(subjectId, keyword));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping
    public Result<ExamTagVO> create(@Valid @RequestBody ExamTagUpsertDTO examTagUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examTagService.createTag(examTagUpsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/{id}")
    public Result<ExamTagVO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExamTagUpsertDTO examTagUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examTagService.updateTag(id, examTagUpsertDTO));
    }

    /**
     * 处理删除请求并返回结果。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        examTagService.deleteTag(id);
        // 调用服务层处理业务并封装统一响应。
        return Result.success();
    }
}
