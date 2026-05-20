package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminExamPaperQuestionAssignDTO {
    @NotEmpty(message = "试卷题目不能为空")
    @Valid
    private List<AdminExamPaperQuestionItemDTO> questions;
}
