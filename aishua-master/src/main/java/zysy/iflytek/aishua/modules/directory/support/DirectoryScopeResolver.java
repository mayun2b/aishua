package zysy.iflytek.aishua.modules.directory.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析“当前目录及其所有子目录”的查询范围。
 */
@Component
public class DirectoryScopeResolver {
    private final TextbookDirectoryMapper textbookDirectoryMapper;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public DirectoryScopeResolver(TextbookDirectoryMapper textbookDirectoryMapper) {
        this.textbookDirectoryMapper = textbookDirectoryMapper;
    }

    /**
     * 解析目录筛选范围：返回“当前目录 + 全部子目录”ID 列表。
     */
    public List<Long> resolveSelfAndDescendants(Long directoryId, Long subjectId) {
        if (directoryId == null || directoryId <= 0) {
            throw new BusinessException("目录筛选条件不合法", 400);
        }

        TextbookDirectory root = textbookDirectoryMapper.selectById(directoryId);
        if (root == null || Integer.valueOf(1).equals(root.getDeleted())) {
            throw new BusinessException("目录不存在", 404);
        }
        if (subjectId != null && !subjectId.equals(root.getSubjectId())) {
            throw new BusinessException("目录不属于当前学科", 400);
        }

        List<TextbookDirectory> directories = textbookDirectoryMapper.selectList(new LambdaQueryWrapper<TextbookDirectory>()
                .eq(TextbookDirectory::getSubjectId, root.getSubjectId()));
        if (directories.isEmpty()) {
            return List.of(root.getId());
        }

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (TextbookDirectory directory : directories) {
            Long parentId = directory.getParentId();
            if (parentId == null || parentId <= 0) {
                continue;
            }
            childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(directory.getId());
        }

        List<Long> result = new ArrayList<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(root.getId());
        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            result.add(currentId);
            List<Long> children = childrenMap.getOrDefault(currentId, Collections.emptyList());
            for (Long childId : children) {
                stack.push(childId);
            }
        }
        return result;
    }
}

