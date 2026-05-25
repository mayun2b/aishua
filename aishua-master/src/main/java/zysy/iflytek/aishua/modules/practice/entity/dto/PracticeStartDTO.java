package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PracticeStartDTO {
    @NotNull(message = "subjectId is required")
    private Long subjectId;

    @Min(value = 1, message = "practiceMode must be between 1 and 4")
    @Max(value = 4, message = "practiceMode must be between 1 and 4")
    private Integer practiceMode = 1;

    @Min(value = 1, message = "questionCount must be >= 1")
    @Max(value = 50, message = "questionCount must be <= 50")
    private Integer questionCount = 10;

    private List<Long> tagIds;
}
