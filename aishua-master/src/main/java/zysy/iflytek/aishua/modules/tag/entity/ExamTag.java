package zysy.iflytek.aishua.modules.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体对象，用于持久化字段映射与数据承载。
 */
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

    @TableLogic
    private Integer deleted;
}
