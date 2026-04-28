package zysy.iflytek.aishua.modules.directory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.entity.dto.DirectoryUpsertDTO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;
import zysy.iflytek.aishua.modules.directory.service.DirectoryService;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DirectoryServiceImpl implements DirectoryService {
    private final TextbookDirectoryMapper textbookDirectoryMapper;
    private final SubjectMapper subjectMapper;

    public DirectoryServiceImpl(TextbookDirectoryMapper textbookDirectoryMapper, SubjectMapper subjectMapper) {
        this.textbookDirectoryMapper = textbookDirectoryMapper;
        this.subjectMapper = subjectMapper;
    }

    @Override
    public List<DirectoryTreeVO> listTreeBySubject(Long subjectId) {
        requireSubject(subjectId);
        List<TextbookDirectory> directories = textbookDirectoryMapper.selectList(new LambdaQueryWrapper<TextbookDirectory>()
                .eq(TextbookDirectory::getSubjectId, subjectId)
                .orderByAsc(TextbookDirectory::getSort)
                .orderByAsc(TextbookDirectory::getId));
        return buildTree(directories);
    }

    @Override
    @Transactional
    public DirectoryTreeVO createDirectory(DirectoryUpsertDTO directoryUpsertDTO) {
        Subject subject = requireSubject(directoryUpsertDTO.getSubjectId());
        Long parentId = normalizeParentId(directoryUpsertDTO.getParentId());
        if (parentId > 0) {
            requireParentInSubject(parentId, subject.getId());
        }

        TextbookDirectory directory = new TextbookDirectory();
        directory.setName(directoryUpsertDTO.getName().trim());
        directory.setSubjectId(subject.getId());
        directory.setParentId(parentId);
        directory.setSort(directoryUpsertDTO.getSort());
        textbookDirectoryMapper.insert(directory);

        log.info("教材目录创建成功，directoryId={}, subjectId={}", directory.getId(), subject.getId());
        return toVO(requireDirectory(directory.getId()));
    }

    @Override
    @Transactional
    public DirectoryTreeVO updateDirectory(Long id, DirectoryUpsertDTO directoryUpsertDTO) {
        TextbookDirectory directory = requireDirectory(id);
        if (!directory.getSubjectId().equals(directoryUpsertDTO.getSubjectId())) {
            throw new BusinessException("目录所属学科不允许直接修改，请新建后迁移", 400);
        }
        requireSubject(directory.getSubjectId());

        Long parentId = normalizeParentId(directoryUpsertDTO.getParentId());
        if (parentId.equals(id)) {
            throw new BusinessException("父目录不能选择当前目录", 400);
        }
        if (parentId > 0) {
            requireParentInSubject(parentId, directory.getSubjectId());
            if (wouldCreateCycle(id, parentId)) {
                throw new BusinessException("父目录选择不合法，不能形成循环层级", 400);
            }
        }

        directory.setName(directoryUpsertDTO.getName().trim());
        directory.setParentId(parentId);
        directory.setSort(directoryUpsertDTO.getSort());
        textbookDirectoryMapper.updateById(directory);

        log.info("教材目录更新成功，directoryId={}", directory.getId());
        return toVO(requireDirectory(id));
    }

    @Override
    @Transactional
    public void deleteDirectory(Long id) {
        TextbookDirectory directory = requireDirectory(id);
        Long childCount = textbookDirectoryMapper.selectCount(new LambdaQueryWrapper<TextbookDirectory>()
                .eq(TextbookDirectory::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该目录下仍有子目录，无法删除", 400);
        }
        textbookDirectoryMapper.deleteById(id);
        log.info("教材目录删除成功，directoryId={}", directory.getId());
    }

    private List<DirectoryTreeVO> buildTree(List<TextbookDirectory> directories) {
        Map<Long, DirectoryTreeVO> nodeMap = new LinkedHashMap<>();
        for (TextbookDirectory directory : directories) {
            nodeMap.put(directory.getId(), toVO(directory));
        }

        List<DirectoryTreeVO> roots = new ArrayList<>();
        for (TextbookDirectory directory : directories) {
            DirectoryTreeVO current = nodeMap.get(directory.getId());
            Long parentId = directory.getParentId();
            if (parentId == null || parentId <= 0 || !nodeMap.containsKey(parentId)) {
                roots.add(current);
                continue;
            }
            nodeMap.get(parentId).getChildren().add(current);
        }
        return roots;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null || parentId <= 0 ? 0L : parentId;
    }

    private boolean wouldCreateCycle(Long currentId, Long parentId) {
        Long cursor = parentId;
        while (cursor != null && cursor > 0) {
            if (cursor.equals(currentId)) {
                return true;
            }
            TextbookDirectory parent = textbookDirectoryMapper.selectById(cursor);
            if (parent == null || Integer.valueOf(1).equals(parent.getDeleted())) {
                return false;
            }
            cursor = normalizeParentId(parent.getParentId());
        }
        return false;
    }

    private Subject requireSubject(Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            throw new BusinessException("所属学科不合法", 400);
        }
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("所属学科不存在", 404);
        }
        return subject;
    }

    private TextbookDirectory requireDirectory(Long id) {
        TextbookDirectory directory = textbookDirectoryMapper.selectById(id);
        if (directory == null || Integer.valueOf(1).equals(directory.getDeleted())) {
            throw new BusinessException("目录不存在", 404);
        }
        return directory;
    }

    private TextbookDirectory requireParentInSubject(Long parentId, Long subjectId) {
        TextbookDirectory parent = requireDirectory(parentId);
        if (!parent.getSubjectId().equals(subjectId)) {
            throw new BusinessException("父目录必须与当前目录属于同一学科", 400);
        }
        return parent;
    }

    private DirectoryTreeVO toVO(TextbookDirectory directory) {
        DirectoryTreeVO directoryTreeVO = new DirectoryTreeVO();
        directoryTreeVO.setId(directory.getId());
        directoryTreeVO.setName(directory.getName());
        directoryTreeVO.setSubjectId(directory.getSubjectId());
        directoryTreeVO.setParentId(directory.getParentId());
        directoryTreeVO.setSort(directory.getSort());
        directoryTreeVO.setCreateTime(directory.getCreateTime());
        directoryTreeVO.setUpdateTime(directory.getUpdateTime());
        return directoryTreeVO;
    }
}
