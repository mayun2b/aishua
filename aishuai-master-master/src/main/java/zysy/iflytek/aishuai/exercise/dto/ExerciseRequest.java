package zysy.iflytek.aishuai.exercise.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 答题请求 DTO
 */
@Data
public class ExerciseRequest {
    @NotNull(message = "题目 ID 不能为空")
    private Long questionId; // 题目 ID
    
    private String userAnswer; // 用户答案
    
    private Integer timeCost; // 耗时（秒）
    
    private Integer exerciseMode = 1; // 练习模式：1-顺序，2-随机，3-错题，4-限时
    
    private String sessionId; // 会话 ID
}
