package zysy.iflytek.aishua.modules.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题目实体对象，用于持久化字段映射与数据承载。
 */
@Data
@TableName("question_tag_relation")
public class QuestionTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long tagId;
}
