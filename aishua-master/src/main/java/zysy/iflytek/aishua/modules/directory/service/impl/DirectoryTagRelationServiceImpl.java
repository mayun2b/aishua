package zysy.iflytek.aishua.modules.directory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.directory.entity.DirectoryTagRelation;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.entity.dto.DirectoryTagRelationUpsertDTO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTagRelationVO;
import zysy.iflytek.aishua.modules.directory.mapper.DirectoryTagRelationMapper;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;
import zysy.iflytek.aishua.modules.directory.service.DirectoryTagRelationService;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 目录-考点关系服务实现。
 */
@Slf4j
@Service
public class DirectoryTagRelationServiceImpl implements DirectoryTagRelationService {
    private final DirectoryTagRelationMapper relationMapper;
    private final SubjectMapper subjectMapper;
    private final TextbookDirectoryMapper directoryMapper;
    private final ExamTagMapper tagMapper;

    public DirectoryTagRelationServiceImpl(
            DirectoryTagRelationMapper relationMapper,
            SubjectMapper subjectMapper,
            TextbookDirectoryMapper directoryMapper,
            ExamTagMapper tagMapper
    ) {
        this.relationMapper = relationMapper;
        this.subjectMapper = subjectMapper;
        this.directoryMapper = directoryMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public PageResult<DirectoryTagRelationVO> listRelations(
            Long subjectId,
            Long directoryId,
            Long tagId,
            Integer isEnabled,
            String keyword,
            Integer pageNum,
            Integer pageSize
    ) {
        validateFilters(subjectId, directoryId, tagId, isEnabled);

        int safePageNum = PageResult.normalizePageNum(pageNum);
        int safePageSize = PageResult.normalizePageSize(pageSize);
        LambdaQueryWrapper<DirectoryTagRelation> countWrapper = buildQuery(subjectId, directoryId, tagId, isEnabled, keyword);
        Long total = relationMapper.selectCount(countWrapper);
        if (total == null || total <= 0) {
            return PageResult.empty(safePageNum, safePageSize);
        }

        LambdaQueryWrapper<DirectoryTagRelation> queryWrapper = buildQuery(subjectId, directoryId, tagId, isEnabled, keyword)
                .orderByAsc(DirectoryTagRelation::getSubjectId)
                .orderByAsc(DirectoryTagRelation::getDirectoryId)
                .orderByAsc(DirectoryTagRelation::getSort)
                .orderByAsc(DirectoryTagRelation::getId)
                .last("LIMIT " + PageResult.offset(safePageNum, safePageSize) + ", " + safePageSize);
        List<DirectoryTagRelation> relations = relationMapper.selectList(queryWrapper);
        return PageResult.of(buildRelationVOs(relations), total, safePageNum, safePageSize);
    }

    @Override
    @Transactional
    public DirectoryTagRelationVO createRelation(DirectoryTagRelationUpsertDTO upsertDTO) {
        validateRelationScope(upsertDTO.getSubjectId(), upsertDTO.getDirectoryId(), upsertDTO.getTagId());
        ensureUniqueRelation(null, upsertDTO.getDirectoryId(), upsertDTO.getTagId());

        DirectoryTagRelation relation = new DirectoryTagRelation();
        applyChanges(relation, upsertDTO);
        relationMapper.insert(relation);

        log.info("目录-考点关系创建成功，relationId={}, directoryId={}, tagId={}",
                relation.getId(), relation.getDirectoryId(), relation.getTagId());
        return getRelationVO(relation.getId());
    }

    @Override
    @Transactional
    public DirectoryTagRelationVO updateRelation(Long id, DirectoryTagRelationUpsertDTO upsertDTO) {
        DirectoryTagRelation relation = requireRelation(id);
        validateRelationScope(upsertDTO.getSubjectId(), upsertDTO.getDirectoryId(), upsertDTO.getTagId());
        ensureUniqueRelation(id, upsertDTO.getDirectoryId(), upsertDTO.getTagId());

        applyChanges(relation, upsertDTO);
        relationMapper.updateById(relation);

        log.info("目录-考点关系更新成功，relationId={}", relation.getId());
        return getRelationVO(relation.getId());
    }

    @Override
    @Transactional
    public DirectoryTagRelationVO updateEnabled(Long id, Integer enabled) {
        DirectoryTagRelation relation = requireRelation(id);
        relation.setIsEnabled(normalizeEnabled(enabled));
        relationMapper.updateById(relation);
        return getRelationVO(id);
    }

    @Override
    @Transactional
    public void deleteRelation(Long id) {
        DirectoryTagRelation relation = requireRelation(id);
        relationMapper.hardDeleteById(id);
        log.info("目录-考点关系删除成功，relationId={}", relation.getId());
    }

    private LambdaQueryWrapper<DirectoryTagRelation> buildQuery(
            Long subjectId,
            Long directoryId,
            Long tagId,
            Integer isEnabled,
            String keyword
    ) {
        LambdaQueryWrapper<DirectoryTagRelation> queryWrapper = new LambdaQueryWrapper<DirectoryTagRelation>()
                .eq(subjectId != null, DirectoryTagRelation::getSubjectId, subjectId)
                .eq(directoryId != null, DirectoryTagRelation::getDirectoryId, directoryId)
                .eq(tagId != null, DirectoryTagRelation::getTagId, tagId)
                .eq(isEnabled != null, DirectoryTagRelation::getIsEnabled, isEnabled);

        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            Set<Long> directoryIds = findDirectoryIds(subjectId, trimmed);
            Set<Long> tagIds = findTagIds(subjectId, trimmed);
            queryWrapper.and(wrapper -> {
                wrapper.like(DirectoryTagRelation::getRemark, trimmed);
                if (!directoryIds.isEmpty()) {
                    wrapper.or().in(DirectoryTagRelation::getDirectoryId, directoryIds);
                }
                if (!tagIds.isEmpty()) {
                    wrapper.or().in(DirectoryTagRelation::getTagId, tagIds);
                }
            });
        }
        return queryWrapper;
    }

    private void validateFilters(Long subjectId, Long directoryId, Long tagId, Integer isEnabled) {
        if (subjectId != null) {
            requireSubject(subjectId);
        }
        if (directoryId != null) {
            TextbookDirectory directory = requireDirectory(directoryId);
            if (subjectId != null && !Objects.equals(directory.getSubjectId(), subjectId)) {
                throw new BusinessException("目录不属于当前学科", 400);
            }
        }
        if (tagId != null) {
            ExamTag tag = requireTag(tagId);
            if (subjectId != null && !Objects.equals(tag.getSubjectId(), subjectId)) {
                throw new BusinessException("考点不属于当前学科", 400);
            }
        }
        if (isEnabled != null && isEnabled != 0 && isEnabled != 1) {
            throw new BusinessException("启用状态不合法", 400);
        }
    }

    private void validateRelationScope(Long subjectId, Long directoryId, Long tagId) {
        requireSubject(subjectId);
        TextbookDirectory directory = requireDirectory(directoryId);
        ExamTag tag = requireTag(tagId);
        if (!Objects.equals(directory.getSubjectId(), subjectId)) {
            throw new BusinessException("目录必须属于所选学科", 400);
        }
        if (!Objects.equals(tag.getSubjectId(), subjectId)) {
            throw new BusinessException("考点必须属于所选学科", 400);
        }
    }

    private void ensureUniqueRelation(Long id, Long directoryId, Long tagId) {
        DirectoryTagRelation existing = relationMapper.selectOne(new LambdaQueryWrapper<DirectoryTagRelation>()
                .eq(DirectoryTagRelation::getDirectoryId, directoryId)
                .eq(DirectoryTagRelation::getTagId, tagId)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), id)) {
            throw new BusinessException("该目录已经绑定过该考点", 400);
        }
    }

    private DirectoryTagRelation requireRelation(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("关系编号不合法", 400);
        }
        DirectoryTagRelation relation = relationMapper.selectById(id);
        if (relation == null || Integer.valueOf(1).equals(relation.getDeleted())) {
            throw new BusinessException("目录-考点关系不存在", 404);
        }
        return relation;
    }

    private Subject requireSubject(Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            throw new BusinessException("学科编号不合法", 400);
        }
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("学科不存在", 404);
        }
        return subject;
    }

    private TextbookDirectory requireDirectory(Long directoryId) {
        if (directoryId == null || directoryId <= 0) {
            throw new BusinessException("目录编号不合法", 400);
        }
        TextbookDirectory directory = directoryMapper.selectById(directoryId);
        if (directory == null || Integer.valueOf(1).equals(directory.getDeleted())) {
            throw new BusinessException("目录不存在", 404);
        }
        return directory;
    }

    private ExamTag requireTag(Long tagId) {
        if (tagId == null || tagId <= 0) {
            throw new BusinessException("考点编号不合法", 400);
        }
        ExamTag tag = tagMapper.selectById(tagId);
        if (tag == null || Integer.valueOf(1).equals(tag.getDeleted())) {
            throw new BusinessException("考点不存在", 404);
        }
        return tag;
    }

    private void applyChanges(DirectoryTagRelation relation, DirectoryTagRelationUpsertDTO upsertDTO) {
        relation.setSubjectId(upsertDTO.getSubjectId());
        relation.setDirectoryId(upsertDTO.getDirectoryId());
        relation.setTagId(upsertDTO.getTagId());
        relation.setRelationType(defaultNumber(upsertDTO.getRelationType(), 1));
        relation.setImportanceLevel(defaultNumber(upsertDTO.getImportanceLevel(), 1));
        relation.setExamFrequency(defaultNumber(upsertDTO.getExamFrequency(), 1));
        relation.setSort(defaultNumber(upsertDTO.getSort(), 0));
        relation.setIsEnabled(normalizeEnabled(upsertDTO.getIsEnabled()));
        relation.setSourceType(defaultNumber(upsertDTO.getSourceType(), 1));
        relation.setRemark(normalizeText(upsertDTO.getRemark()));
    }

    private DirectoryTagRelationVO getRelationVO(Long id) {
        return buildRelationVOs(List.of(requireRelation(id))).get(0);
    }

    private List<DirectoryTagRelationVO> buildRelationVOs(List<DirectoryTagRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Subject> subjectMap = loadMap(
                relations.stream().map(DirectoryTagRelation::getSubjectId).collect(Collectors.toSet()),
                ids -> subjectMapper.selectBatchIds(ids),
                Subject::getId
        );
        Map<Long, TextbookDirectory> directoryMap = loadMap(
                relations.stream().map(DirectoryTagRelation::getDirectoryId).collect(Collectors.toSet()),
                ids -> directoryMapper.selectBatchIds(ids),
                TextbookDirectory::getId
        );
        Map<Long, ExamTag> tagMap = loadMap(
                relations.stream().map(DirectoryTagRelation::getTagId).collect(Collectors.toSet()),
                ids -> tagMapper.selectBatchIds(ids),
                ExamTag::getId
        );

        return relations.stream().map(relation -> {
            Subject subject = subjectMap.get(relation.getSubjectId());
            TextbookDirectory directory = directoryMap.get(relation.getDirectoryId());
            ExamTag tag = tagMap.get(relation.getTagId());

            DirectoryTagRelationVO vo = new DirectoryTagRelationVO();
            vo.setId(relation.getId());
            vo.setSubjectId(relation.getSubjectId());
            vo.setSubjectName(subject == null ? null : subject.getName());
            vo.setDirectoryId(relation.getDirectoryId());
            vo.setDirectoryName(directory == null ? null : directory.getName());
            vo.setTagId(relation.getTagId());
            vo.setTagName(tag == null ? null : tag.getName());
            vo.setTagRemark(tag == null ? null : tag.getTag());
            vo.setRelationType(relation.getRelationType());
            vo.setImportanceLevel(relation.getImportanceLevel());
            vo.setExamFrequency(relation.getExamFrequency());
            vo.setSort(relation.getSort());
            vo.setIsEnabled(relation.getIsEnabled());
            vo.setSourceType(relation.getSourceType());
            vo.setRemark(relation.getRemark());
            vo.setCreateTime(relation.getCreateTime());
            vo.setUpdateTime(relation.getUpdateTime());
            return vo;
        }).toList();
    }

    private <T> Map<Long, T> loadMap(Set<Long> ids, BatchLoader<T> loader, Function<T, Long> idGetter) {
        Set<Long> validIds = ids.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (validIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return loader.load(validIds).stream()
                .filter(item -> item != null)
                .collect(Collectors.toMap(idGetter, Function.identity(), (left, right) -> left));
    }

    private Set<Long> findDirectoryIds(Long subjectId, String keyword) {
        return directoryMapper.selectList(new LambdaQueryWrapper<TextbookDirectory>()
                        .eq(subjectId != null, TextbookDirectory::getSubjectId, subjectId)
                        .like(TextbookDirectory::getName, keyword))
                .stream()
                .map(TextbookDirectory::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> findTagIds(Long subjectId, String keyword) {
        return tagMapper.selectList(new LambdaQueryWrapper<ExamTag>()
                        .eq(subjectId != null, ExamTag::getSubjectId, subjectId)
                        .and(wrapper -> wrapper.like(ExamTag::getName, keyword).or().like(ExamTag::getTag, keyword)))
                .stream()
                .map(ExamTag::getId)
                .collect(Collectors.toSet());
    }

    private int normalizeEnabled(Integer enabled) {
        if (enabled == null) {
            return 1;
        }
        if (enabled != 0 && enabled != 1) {
            throw new BusinessException("启用状态不合法", 400);
        }
        return enabled;
    }

    private int defaultNumber(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @FunctionalInterface
    private interface BatchLoader<T> {
        List<T> load(Set<Long> ids);
    }
}
