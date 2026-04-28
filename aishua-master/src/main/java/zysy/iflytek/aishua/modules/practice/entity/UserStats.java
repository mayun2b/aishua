package zysy.iflytek.aishua.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_stats")
public class UserStats {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer totalCount;

    private Integer correctCount;

    private Integer wrongCount;

    private BigDecimal correctRate;

    private LocalDate lastExerciseDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
