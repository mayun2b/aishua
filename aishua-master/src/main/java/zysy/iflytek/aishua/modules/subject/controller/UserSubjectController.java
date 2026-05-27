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

/**
 * 学科控制器，负责相关业务逻辑与流程处理。
 */
@RestController
@RequestMapping("/api")
public class UserSubjectController {
    private final UserSubjectService userSubjectService;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public UserSubjectController(UserSubjectService userSubjectService) {
        this.userSubjectService = userSubjectService;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/subjects")
    public Result<List<SubjectCatalogVO>> listSubjectCatalog() {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userSubjectService.listSubjectCatalog(userId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/user/subjects")
    public Result<List<MySubjectVO>> listMySubjects() {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userSubjectService.listMySubjects(userId));
    }

    /**
     * 处理业务请求并返回结果。
     */
    @PostMapping("/user/subjects/{subjectId}/join")
    public Result<MySubjectVO> joinSubject(@PathVariable Long subjectId) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userSubjectService.joinSubject(userId, subjectId));
    }
}
