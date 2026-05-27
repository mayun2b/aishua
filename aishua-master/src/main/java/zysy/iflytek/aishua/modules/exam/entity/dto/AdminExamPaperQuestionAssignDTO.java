package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 考试数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class AdminExamPaperQuestionAssignDTO {
    @NotEmpty(message = "试卷题目不能为空")
    @Valid
    private List<AdminExamPaperQuestionItemDTO> questions;
}
