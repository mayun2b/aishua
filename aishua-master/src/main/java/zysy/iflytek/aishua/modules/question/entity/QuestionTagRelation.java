package zysy.iflytek.aishua.modules.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question_tag_relation")
public class QuestionTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long tagId;
}
