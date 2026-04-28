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

@Validated
@RestController
@RequestMapping("/api/admin/tags")
public class AdminExamTagController {
    private final ExamTagService examTagService;
    private final AdminAccess adminAccess;

    public AdminExamTagController(ExamTagService examTagService, AdminAccess adminAccess) {
        this.examTagService = examTagService;
        this.adminAccess = adminAccess;
    }

    @GetMapping
    public Result<List<ExamTagVO>> list(
            @RequestParam(required = false) @Min(value = 1, message = "学科ID不合法") Long subjectId,
            @RequestParam(required = false) String keyword
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examTagService.listTags(subjectId, keyword));
    }

    @PostMapping
    public Result<ExamTagVO> create(@Valid @RequestBody ExamTagUpsertDTO examTagUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examTagService.createTag(examTagUpsertDTO));
    }

    @PutMapping("/{id}")
    public Result<ExamTagVO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExamTagUpsertDTO examTagUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examTagService.updateTag(id, examTagUpsertDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        examTagService.deleteTag(id);
        return Result.success();
    }
}
