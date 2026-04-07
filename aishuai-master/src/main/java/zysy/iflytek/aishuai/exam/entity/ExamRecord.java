package zysy.iflytek.aishuai.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String examName;

    private Integer examMode;

    private Integer totalQuestions;

    private Integer correctQuestions;

    private Double score;

    private Integer duration;

    private String startTime;

    private String endTime;

    private Integer status;

    private String createTime;
}
