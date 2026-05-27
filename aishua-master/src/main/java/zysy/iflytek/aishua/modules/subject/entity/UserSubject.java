package zysy.iflytek.aishua.modules.subject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学科实体对象，负责相关业务逻辑与流程处理。
 */
@Data
@TableName("user_subject")
public class UserSubject {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long subjectId;

    private LocalDateTime joinedAt;

    private Integer status;

    private LocalDateTime lastPracticeAt;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
