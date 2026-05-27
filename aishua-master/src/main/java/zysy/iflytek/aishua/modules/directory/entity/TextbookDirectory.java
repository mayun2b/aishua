package zysy.iflytek.aishua.modules.directory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 目录实体对象，负责相关业务逻辑与流程处理。
 */
@Data
@TableName("textbook_directory")
public class TextbookDirectory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long subjectId;

    private Long parentId;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
