package zysy.iflytek.aishua.modules.subject.controller;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectCatalogVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectDirectoryTagVO;
import zysy.iflytek.aishua.modules.subject.service.UserSubjectService;

import java.util.List;

/**
 * 学科控制器，负责用户学科相关查询与加入流程。
 */
@Validated
@RestController
@RequestMapping("/api")
public class UserSubjectController {
    private final UserSubjectService userSubjectService;

    /**
     * 构造方法，注入依赖服务。
     */
    public UserSubjectController(UserSubjectService userSubjectService) {
        this.userSubjectService = userSubjectService;
    }

    /**
     * 获取可加入的学科目录。
     */
    @GetMapping("/subjects")
    public Result<List<SubjectCatalogVO>> listSubjectCatalog() {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.listSubjectCatalog(userId));
    }

    /**
     * 获取当前用户已加入的学科列表。
     */
    @GetMapping("/user/subjects")
    public Result<List<MySubjectVO>> listMySubjects() {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.listMySubjects(userId));
    }

    /**
     * 加入指定学科。
     */
    @PostMapping("/user/subjects/{subjectId}/join")
    public Result<MySubjectVO> joinSubject(@PathVariable Long subjectId) {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.joinSubject(userId, subjectId));
    }

    /**
     * 获取用户在指定学科下可见的目录树。
     */
    @GetMapping("/user/subjects/{subjectId}/directories")
    public Result<List<DirectoryTreeVO>> listSubjectDirectories(
            @PathVariable @Min(value = 1, message = "学科编号不合法") Long subjectId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.listSubjectDirectories(userId, subjectId));
    }

    /**
     * 获取指定目录下的考点列表（含题量统计）。
     */
    @GetMapping("/user/subjects/{subjectId}/directories/{directoryId}/tags")
    public Result<List<SubjectDirectoryTagVO>> listSubjectDirectoryTags(
            @PathVariable @Min(value = 1, message = "学科编号不合法") Long subjectId,
            @PathVariable @Min(value = 1, message = "目录编号不合法") Long directoryId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.listSubjectDirectoryTags(userId, subjectId, directoryId));
    }
}

