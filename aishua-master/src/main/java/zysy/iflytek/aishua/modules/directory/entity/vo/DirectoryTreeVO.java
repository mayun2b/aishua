package zysy.iflytek.aishua.modules.directory.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录视图对象，用于接口出参封装。
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
