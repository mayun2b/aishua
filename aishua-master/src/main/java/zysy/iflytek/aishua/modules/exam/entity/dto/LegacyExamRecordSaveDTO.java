package zysy.iflytek.aishua.modules.exam.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试数据传输对象，用于接口入参封装。
 */
@Data
public class LegacyExamRecordSaveDTO {
    @Min(value = 1, message = "用户编号不合法")
    private Long userId;

    @NotBlank(message = "考试名称不能为空")
    @Size(max = 100, message = "考试名称长度不能超过 100")
    private String examName;

    @NotNull(message = "考试模式不能为空")
    @Min(value = 1, message = "考试模式取值不合法")
    @Max(value = 9, message = "考试模式取值不合法")
    private Integer examMode;

    @NotNull(message = "总题数不能为空")
    @Min(value = 1, message = "总题数必须大于 0")
    private Integer totalQuestions;

    @Min(value = 0, message = "正确题数不能小于 0")
    private Integer correctQuestions;

    @DecimalMin(value = "0", message = "得分不能小于 0")
    private Double score;

    @Min(value = 0, message = "考试时长不能小于 0")
    private Integer duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Min(value = 1, message = "状态取值不合法")
    @Max(value = 2, message = "状态取值不合法")
    private Integer status;

    @NotEmpty(message = "题目明细不能为空")
    @Valid
    private List<LegacyExamRecordQuestionDTO> questions;
}
