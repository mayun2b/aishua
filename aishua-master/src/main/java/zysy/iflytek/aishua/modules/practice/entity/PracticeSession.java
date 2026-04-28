package zysy.iflytek.aishua.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("practice_session")
public class PracticeSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionCode;

    private Long userId;

    private Long subjectId;

    private Integer practiceMode;

    private Integer questionCount;

    private Integer answeredCount;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer totalTimeCost;

    private Integer status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
