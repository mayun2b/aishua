package zysy.iflytek.aishua.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_subject_stats")
public class UserSubjectStats {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long subjectId;

    private Integer totalCount;

    private Integer correctCount;

    private Integer wrongCount;

    private BigDecimal correctRate;

    private Integer totalTimeCost;

    private LocalDate lastPracticeDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
