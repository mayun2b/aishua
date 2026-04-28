package zysy.iflytek.aishua.modules.question.service;

import zysy.iflytek.aishua.modules.question.entity.dto.QuestionUpsertDTO;
import zysy.iflytek.aishua.modules.question.entity.vo.QuestionVO;

import java.util.List;

public interface QuestionService {
    List<QuestionVO> listQuestions(Long subjectId, Long directoryId, Integer difficulty, Integer type, String keyword);

    QuestionVO getQuestionDetail(Long id);

    QuestionVO createQuestion(QuestionUpsertDTO questionUpsertDTO);

    QuestionVO updateQuestion(Long id, QuestionUpsertDTO questionUpsertDTO);

    void deleteQuestion(Long id);
}
