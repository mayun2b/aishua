package zysy.iflytek.aishua.modules.subject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectCatalogVO;
import zysy.iflytek.aishua.modules.subject.service.UserSubjectService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserSubjectController {
    private final UserSubjectService userSubjectService;

    public UserSubjectController(UserSubjectService userSubjectService) {
        this.userSubjectService = userSubjectService;
    }

    @GetMapping("/subjects")
    public Result<List<SubjectCatalogVO>> listSubjectCatalog() {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.listSubjectCatalog(userId));
    }

    @GetMapping("/user/subjects")
    public Result<List<MySubjectVO>> listMySubjects() {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.listMySubjects(userId));
    }

    @PostMapping("/user/subjects/{subjectId}/join")
    public Result<MySubjectVO> joinSubject(@PathVariable Long subjectId) {
        Long userId = UserContext.requireUserId();
        return Result.success(userSubjectService.joinSubject(userId, subjectId));
    }
}
