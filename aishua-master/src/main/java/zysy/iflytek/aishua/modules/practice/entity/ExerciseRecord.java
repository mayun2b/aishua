package zysy.iflytek.aishua.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习实体对象，负责相关业务逻辑与流程处理。
 */
@Data
@TableName("exercise_record")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Long sessionRefId;

    private Integer exerciseMode;

    private String userAnswer;

    private Integer isCorrect;

    private Integer timeCost;

    private LocalDateTime exerciseTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
