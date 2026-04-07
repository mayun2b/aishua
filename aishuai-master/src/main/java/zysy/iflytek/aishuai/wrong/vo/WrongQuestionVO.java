package zysy.iflytek.aishuai.wrong.vo;

import lombok.Data;
import zysy.iflytek.aishuai.question.entity.Question;

import java.time.LocalDateTime;

/**
 * 错题 VO
 */
@Data
public class WrongQuestionVO {
    private Long id; // 题目 ID
    private String title; // 题目标题
    private String content; // 题目内容
    private Integer type; // 题目类型
    private String options; // 选项
    private String answer; // 正确答案
    private String analysis; // 解析
    private Integer difficulty; // 难度
    private Double correctRate; // 正确率
    private Integer wrongCount; // 做错次数
    private LocalDateTime lastWrongTime; // 最近做错时间
    private Integer masterStatus; // 掌握状态
    private String categoryName; // 分类名称
    private String tags; // 知识点标签

    // 从 Question 和 WrongQuestion 构建
    public static WrongQuestionVO fromQuestionAndWrongQuestion(Question question, Integer wrongCount, LocalDateTime lastWrongTime, Integer masterStatus, String categoryName) {
        WrongQuestionVO vo = new WrongQuestionVO();
        vo.setId(question.getId());
        vo.setTitle(question.getTitle());
        vo.setContent(question.getContent());
        vo.setType(question.getType());
        vo.setOptions(question.getOptions());
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setDifficulty(question.getDifficulty());
        vo.setCorrectRate(question.getCorrectRate());
        vo.setWrongCount(wrongCount);
        vo.setLastWrongTime(lastWrongTime);
        vo.setMasterStatus(masterStatus);
        vo.setCategoryName(categoryName);
        vo.setTags(question.getTags());
        return vo;
    }
}