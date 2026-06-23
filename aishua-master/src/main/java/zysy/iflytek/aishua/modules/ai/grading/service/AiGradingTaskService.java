package zysy.iflytek.aishua.modules.ai.grading.service;

import zysy.iflytek.aishua.modules.ai.grading.entity.AiGradingTask;

/**
 * AI 评分任务服务。
 */
public interface AiGradingTaskService {
    AiGradingTask createPracticeTask(Long exerciseRecordId, Long questionId, Long userId);

    AiGradingTask createExamTask(Long examRecordQuestionId, Long questionId, Long userId);
}
