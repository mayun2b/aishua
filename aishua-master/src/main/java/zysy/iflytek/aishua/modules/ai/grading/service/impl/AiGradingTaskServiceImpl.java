package zysy.iflytek.aishua.modules.ai.grading.service.impl;

import org.springframework.stereotype.Service;
import zysy.iflytek.aishua.modules.ai.grading.entity.AiGradingTask;
import zysy.iflytek.aishua.modules.ai.grading.mapper.AiGradingTaskMapper;
import zysy.iflytek.aishua.modules.ai.grading.service.AiGradingTaskService;

import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.BIZ_TYPE_EXAM;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.BIZ_TYPE_PRACTICE;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_PENDING;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.TRIGGER_SUBMIT;

/**
 * AI 评分任务服务实现。
 */
@Service
public class AiGradingTaskServiceImpl implements AiGradingTaskService {
    private final AiGradingTaskMapper aiGradingTaskMapper;

    public AiGradingTaskServiceImpl(AiGradingTaskMapper aiGradingTaskMapper) {
        this.aiGradingTaskMapper = aiGradingTaskMapper;
    }

    @Override
    public AiGradingTask createPracticeTask(Long exerciseRecordId, Long questionId, Long userId) {
        return createTask(BIZ_TYPE_PRACTICE, exerciseRecordId, questionId, userId, TRIGGER_SUBMIT);
    }

    @Override
    public AiGradingTask createExamTask(Long examRecordQuestionId, Long questionId, Long userId) {
        return createTask(BIZ_TYPE_EXAM, examRecordQuestionId, questionId, userId, TRIGGER_SUBMIT);
    }

    private AiGradingTask createTask(String bizType, Long bizRecordId, Long questionId, Long userId, String triggerSource) {
        AiGradingTask task = new AiGradingTask();
        task.setBizType(bizType);
        task.setBizRecordId(bizRecordId);
        task.setQuestionId(questionId);
        task.setUserId(userId);
        task.setStatus(STATUS_PENDING);
        task.setTriggerSource(triggerSource);
        task.setRetryCount(0);
        task.setMaxRetryCount(0);
        aiGradingTaskMapper.insert(task);
        return task;
    }
}
