package zysy.iflytek.aishua.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_knowledge_mastery")
public class UserKnowledgeMastery {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tagId;

    private Long subjectId;

    private Integer totalCount;

    private Integer correctCount;

    private BigDecimal correctRate;

    private Integer wrongCount;

    private Integer masteryLevel;

    private LocalDateTime updateTime;
}
