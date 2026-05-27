package zysy.iflytek.aishua.modules.directory.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class DirectoryTreeVO {
    private Long id;
    private String name;
    private Long subjectId;
    private Long parentId;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<DirectoryTreeVO> children = new ArrayList<>();
}
