package zysy.iflytek.aishua.modules.dify.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Dify 联调接口入参。
 */
@Data
public class DifyTestRequest {
    /**
     * 用户问题文本。
     */
    @NotBlank(message = "query 不能为空")
    private String query;

    /**
     * 学生标识（可选）。不传时后端默认使用当前登录用户 ID。
     */
    private String studentId;

    /**
     * 学科（可选）。
     */
    private String subject;

    /**
     * 学科ID（可选）。
     */
    private Long subjectId;

    /**
     * 年级（可选）。
     */
    private String grade;

    /**
     * 教材版本（可选）。
     */
    private String textbookVersion;
}
