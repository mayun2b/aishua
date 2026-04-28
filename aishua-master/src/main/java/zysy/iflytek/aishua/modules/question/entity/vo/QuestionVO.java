package zysy.iflytek.aishua.modules.question.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private String options;
    private String answer;
    private String analysis;
    private String imageUrls;
    private String imageDesc;
    private Long directoryId;
    private String directoryName;
    private Long subjectId;
    private String subjectName;
    private Integer difficulty;
    private BigDecimal correctRate;
    private Integer doCount;
    private List<Long> tagIds;
    private List<QuestionTagVO> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
