package zysy.iflytek.aishua.modules.directory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("directory_tag_relation")
public class DirectoryTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private Long directoryId;

    private Long tagId;

    private Integer relationType;

    private Integer importanceLevel;

    private Integer examFrequency;

    private Integer sort;

    private Integer isEnabled;

    private Integer sourceType;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

