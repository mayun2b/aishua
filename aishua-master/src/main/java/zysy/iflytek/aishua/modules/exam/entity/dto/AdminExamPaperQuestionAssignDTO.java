package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 考试数据传输对象，用于接口入参封装。
 */
@Data
public class AdminExamPaperQuestionAssignDTO {
    @NotEmpty(message = "试卷题目不能为空")
    @Valid
    private List<AdminExamPaperQuestionItemDTO> questions;
}
