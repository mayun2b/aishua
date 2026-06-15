package zysy.iflytek.aishua.modules.ai.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 视频搜索请求 DTO。
 */
@Data
public class VideoSearchRequest {
    /**
     * 知识点名称（必填）。
     */
    @NotBlank(message = "知识点名称不能为空")
    private String knowledgePoint;

    /**
     * 学科名称（可选）。
     */
    private String subject;

    /**
     * 学科 ID（可选）。
     */
    private Long subjectId;

    /**
     * 年级（可选）。
     */
    private String grade;

    /**
     * 返回结果数量（可选）。
     */
    private Integer limit = 5;
}
