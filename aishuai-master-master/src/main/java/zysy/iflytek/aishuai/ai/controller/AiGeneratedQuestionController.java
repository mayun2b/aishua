package zysy.iflytek.aishuai.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.ai.entity.AiGeneratedQuestion;
import zysy.iflytek.aishuai.ai.service.AiGeneratedQuestionService;
import zysy.iflytek.aishuai.common.result.Result;

import java.util.List;

/**
 * AI生成题目控制器
 */
@RestController
@RequestMapping("/api/ai/question")
public class AiGeneratedQuestionController {

    @Autowired
    private AiGeneratedQuestionService aiGeneratedQuestionService;

    /**
     * 获取用户的AI生成题目列表
     *
     * @param userId 用户ID
     * @return AI生成题目列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<AiGeneratedQuestion>> getAiGeneratedQuestionsByUserId(@PathVariable Long userId) {
        List<AiGeneratedQuestion> questions = aiGeneratedQuestionService.getAiGeneratedQuestionsByUserId(userId);
        return Result.success(questions);
    }

    /**
     * 根据学科获取用户的AI生成题目列表
     *
     * @param userId    用户ID
     * @param subjectId 学科ID
     * @return AI生成题目列表
     */
    @GetMapping("/user/{userId}/subject/{subjectId}")
    public Result<List<AiGeneratedQuestion>> getAiGeneratedQuestionsBySubject(@PathVariable Long userId, @PathVariable Long subjectId) {
        List<AiGeneratedQuestion> questions = aiGeneratedQuestionService.getAiGeneratedQuestionsByUserIdAndSubjectId(userId, subjectId);
        return Result.success(questions);
    }

    /**
     * 生成AI题目
     *
     * @param aiGeneratedQuestion AI生成题目对象
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<Boolean> generateAiQuestion(@RequestBody AiGeneratedQuestion aiGeneratedQuestion) {
        boolean result = aiGeneratedQuestionService.generateAndSaveAiQuestion(aiGeneratedQuestion);
        return Result.success(result);
    }

    /**
     * 更新题目练习状态
     *
     * @param id          题目ID
     * @param isPracticed 是否已练习
     * @return 更新结果
     */
    @PutMapping("/{id}/practice")
    public Result<Boolean> updatePracticeStatus(@PathVariable Long id, @RequestParam Integer isPracticed) {
        boolean result = aiGeneratedQuestionService.updatePracticeStatus(id, isPracticed);
        return Result.success(result);
    }
}