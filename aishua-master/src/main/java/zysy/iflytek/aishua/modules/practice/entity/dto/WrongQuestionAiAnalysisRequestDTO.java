package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WrongQuestionAiAnalysisRequestDTO {
    @Size(max = 500, message = "补充说明长度不能超过500字符")
    private String extraInstruction;
}
