package zysy.iflytek.aishua.modules.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_tag")
public class ExamTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long subjectId;

    private String tag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
