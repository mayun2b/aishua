package zysy.iflytek.aishuai.exam.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zysy.iflytek.aishuai.exam.entity.ExamRecordQuestion;
import zysy.iflytek.aishuai.exam.mapper.ExamRecordQuestionMapper;
import zysy.iflytek.aishuai.exam.service.ExamRecordQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamRecordQuestionServiceImpl extends ServiceImpl<ExamRecordQuestionMapper, ExamRecordQuestion> implements ExamRecordQuestionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestions(Long examRecordId, List<ExamRecordQuestion> questions) {
        for (ExamRecordQuestion question : questions) {
            question.setExamRecordId(examRecordId);
        }
        saveBatch(questions);
    }

    @Override
    public List<ExamRecordQuestion> getQuestionsByExamRecordId(Long examRecordId) {
        LambdaQueryWrapper<ExamRecordQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecordQuestion::getExamRecordId, examRecordId);
        return baseMapper.selectList(queryWrapper);
    }
}
