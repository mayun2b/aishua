package zysy.iflytek.aishua.modules.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
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

    private Long subjectId;

    private Integer difficulty;

    private BigDecimal correctRate;

    private Integer doCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
