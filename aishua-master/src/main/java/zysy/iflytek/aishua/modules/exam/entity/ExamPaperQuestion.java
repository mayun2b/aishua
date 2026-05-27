package zysy.iflytek.aishua.modules.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试实体对象，负责相关业务逻辑与流程处理。
 */
@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long questionId;

    private Integer sort;

    private Integer score;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
