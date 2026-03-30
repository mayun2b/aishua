package zysy.iflytek.aishuai.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.aishuai.ai.entity.AiGeneratedQuestion;

import java.util.List;

/**
 * AI生成题目Service接口
 */
public interface AiGeneratedQuestionService extends IService<AiGeneratedQuestion> {

    /**
     * 根据用户ID获取AI生成的题目列表
     *
     * @param userId 用户ID
     * @return AI生成题目列表
     */
    List<AiGeneratedQuestion> getAiGeneratedQuestionsByUserId(Long userId);

    /**
     * 根据用户ID和学科ID获取AI生成的题目列表
     *
     * @param userId   用户ID
     * @param subjectId 学科ID
     * @return AI生成题目列表
     */
    List<AiGeneratedQuestion> getAiGeneratedQuestionsByUserIdAndSubjectId(Long userId, Long subjectId);

    /**
     * 生成AI题目并保存
     *
     * @param aiGeneratedQuestion AI生成题目对象
     * @return 生成结果
     */
    boolean generateAndSaveAiQuestion(AiGeneratedQuestion aiGeneratedQuestion);

    /**
     * 更新题目练习状态
     *
     * @param id          题目ID
     * @param isPracticed 是否已练习
     * @return 更新结果
     */
    boolean updatePracticeStatus(Long id, Integer isPracticed);
}