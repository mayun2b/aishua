package zysy.iflytek.aishua.modules.question.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionUpsertDTO {
    @NotBlank(message = "题干不能为空")
    @Size(max = 500, message = "题干长度不能超过 500")
    private String title;

    @Size(max = 20000, message = "题目内容长度不能超过 20000")
    private String content;

    @NotNull(message = "题型不能为空")
    @Min(value = 1, message = "题型取值范围为 1-5")
    @Max(value = 5, message = "题型取值范围为 1-5")
    private Integer type;

    @Size(max = 10000, message = "选项JSON长度不能超过 10000")
    private String options;

    @NotBlank(message = "标准答案不能为空")
    @Size(max = 10000, message = "标准答案长度不能超过 10000")
    private String answer;

    @Size(max = 20000, message = "题目解析长度不能超过 20000")
    private String analysis;

    @Size(max = 2000, message = "题目图片URL长度不能超过 2000")
    private String imageUrls;

    @Size(max = 5000, message = "图片描述长度不能超过 5000")
    private String imageDesc;

    @NotNull(message = "所属学科不能为空")
    @Min(value = 1, message = "所属学科不合法")
    private Long subjectId;

    @Min(value = 1, message = "所属目录不合法")
    private Long directoryId;

    @NotNull(message = "难度不能为空")
    @Min(value = 1, message = "难度取值范围为 1-3")
    @Max(value = 3, message = "难度取值范围为 1-3")
    private Integer difficulty;

    private List<Long> tagIds;
}
