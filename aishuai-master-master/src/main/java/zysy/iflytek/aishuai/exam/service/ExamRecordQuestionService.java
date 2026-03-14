package zysy.iflytek.aishuai.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.aishuai.exam.entity.ExamRecordQuestion;

import java.util.List;

public interface ExamRecordQuestionService extends IService<ExamRecordQuestion> {

    void saveQuestions(Long examRecordId, List<ExamRecordQuestion> questions);

    List<ExamRecordQuestion> getQuestionsByExamRecordId(Long examRecordId);
}
