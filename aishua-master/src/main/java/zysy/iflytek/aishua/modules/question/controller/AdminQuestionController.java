package zysy.iflytek.aishua.modules.question.controller;

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
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.common.security.AdminAccess;
import zysy.iflytek.aishua.modules.question.entity.dto.QuestionUpsertDTO;
import zysy.iflytek.aishua.modules.question.entity.vo.QuestionVO;
import zysy.iflytek.aishua.modules.question.service.QuestionService;

import java.util.List;

/**
 * 题目控制器，提供该领域对外接口入口。
 */
@Validated
@RestController
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {
    private final QuestionService questionService;
    private final AdminAccess adminAccess;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public AdminQuestionController(QuestionService questionService, AdminAccess adminAccess) {
        this.questionService = questionService;
        this.adminAccess = adminAccess;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping
    public Result<List<QuestionVO>> list(
            @RequestParam(required = false) @Min(value = 1, message = "学科编号不合法") Long subjectId,
            @RequestParam(required = false) @Min(value = 1, message = "目录编号不合法") Long directoryId,
            @RequestParam(required = false) @Min(value = 1, message = "难度范围为 1-3")
            @Max(value = 3, message = "难度范围为 1-3") Integer difficulty,
            @RequestParam(required = false) @Min(value = 1, message = "题型范围为 1-5")
            @Max(value = 5, message = "题型范围为 1-5") Integer type,
            @RequestParam(required = false) String keyword
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(questionService.listQuestions(subjectId, directoryId, difficulty, type, keyword));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @GetMapping("/{id}")
    public Result<QuestionVO> detail(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(questionService.getQuestionDetail(id));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping
    public Result<QuestionVO> create(@Valid @RequestBody QuestionUpsertDTO questionUpsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(questionService.createQuestion(questionUpsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/{id}")
    public Result<QuestionVO> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpsertDTO questionUpsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(questionService.updateQuestion(id, questionUpsertDTO));
    }

    /**
     * 删除接口入口，负责资源删除与结果返回。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        questionService.deleteQuestion(id);
        // 调用服务层处理业务并封装统一响应。
        return Result.success();
    }
}
