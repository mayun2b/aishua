package zysy.iflytek.aishua.modules.question.service;

import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.question.entity.dto.QuestionUpsertDTO;
import zysy.iflytek.aishua.modules.question.entity.vo.QuestionVO;

/**
 * 题目服务接口，定义该领域对外能力契约。
 */
public interface QuestionService {
    PageResult<QuestionVO> listQuestions(
            Long subjectId,
            Long directoryId,
            Integer difficulty,
            Integer type,
            String keyword,
            Integer pageNum,
            Integer pageSize
    );

    QuestionVO getQuestionDetail(Long id);

    QuestionVO createQuestion(QuestionUpsertDTO questionUpsertDTO);

    QuestionVO updateQuestion(Long id, QuestionUpsertDTO questionUpsertDTO);

    void deleteQuestion(Long id);
}
