package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频搜索结果项。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoSearchResultVO {
    /**
     * 搜索关键词。
     */
    private String keyword;

    /**
     * 视频标题。
     */
    private String title;

    /**
     * 视频链接（B站搜索结果页）。
     */
    private String url;

    /**
     * 来源平台。
     */
    private String source;

    /**
     * 封面图（可选）。
     */
    private String thumbnail;
}
