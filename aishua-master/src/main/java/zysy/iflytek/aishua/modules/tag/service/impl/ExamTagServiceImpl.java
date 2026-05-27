package zysy.iflytek.aishua.modules.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.entity.dto.ExamTagUpsertDTO;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;
import zysy.iflytek.aishua.modules.tag.service.ExamTagService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签服务实现，负责相关业务逻辑与流程处理。
 */
@Slf4j
@Service
public class ExamTagServiceImpl implements ExamTagService {
    private final ExamTagMapper examTagMapper;
    private final SubjectMapper subjectMapper;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public ExamTagServiceImpl(ExamTagMapper examTagMapper, SubjectMapper subjectMapper) {
        this.examTagMapper = examTagMapper;
        this.subjectMapper = subjectMapper;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamTagVO> listTags(Long subjectId, String keyword) {
        LambdaQueryWrapper<ExamTag> queryWrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            requireSubject(subjectId);
            queryWrapper.eq(ExamTag::getSubjectId, subjectId);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(ExamTag::getName, trimmedKeyword)
                    .or()
                    .like(ExamTag::getTag, trimmedKeyword));
        }
        queryWrapper.orderByAsc(ExamTag::getSubjectId).orderByDesc(ExamTag::getId);

        List<ExamTag> examTags = examTagMapper.selectList(queryWrapper);
        Map<Long, String> subjectNameMap = buildSubjectNameMap(examTags);
        return examTags.stream().map(examTag -> toVO(examTag, subjectNameMap.get(examTag.getSubjectId()))).toList();
    }

    /**
     * 执行创建业务流程并返回结果。
     */
    @Override
    @Transactional
    public ExamTagVO createTag(ExamTagUpsertDTO examTagUpsertDTO) {
        Subject subject = requireSubject(examTagUpsertDTO.getSubjectId());
        ensureUniqueName(null, examTagUpsertDTO.getSubjectId(), examTagUpsertDTO.getName());

        ExamTag examTag = new ExamTag();
        applyChanges(examTag, examTagUpsertDTO);
        examTagMapper.insert(examTag);

        log.info("考点标签创建成功，tagId={}, subjectId={}", examTag.getId(), subject.getId());
        return toVO(requireTag(examTag.getId()), subject.getName());
    }

    /**
     * 执行保存与更新业务流程。
     */
    @Override
    @Transactional
    public ExamTagVO updateTag(Long id, ExamTagUpsertDTO examTagUpsertDTO) {
        ExamTag examTag = requireTag(id);
        Subject subject = requireSubject(examTagUpsertDTO.getSubjectId());
        ensureUniqueName(id, examTagUpsertDTO.getSubjectId(), examTagUpsertDTO.getName());

        applyChanges(examTag, examTagUpsertDTO);
        examTagMapper.updateById(examTag);

        log.info("考点标签更新成功，tagId={}", examTag.getId());
        return toVO(requireTag(id), subject.getName());
    }

    /**
     * 执行删除与清理业务流程。
     */
    @Override
    @Transactional
    public void deleteTag(Long id) {
        ExamTag examTag = requireTag(id);
        examTagMapper.deleteById(id);
        log.info("考点标签删除成功，tagId={}", examTag.getId());
    }

    /**
     * 执行核心业务处理流程。
     */
    private void applyChanges(ExamTag examTag, ExamTagUpsertDTO examTagUpsertDTO) {
        examTag.setName(examTagUpsertDTO.getName().trim());
        examTag.setSubjectId(examTagUpsertDTO.getSubjectId());
        examTag.setTag(normalizeText(examTagUpsertDTO.getTag()));
    }

    /**
     * 执行参数与状态校验。
     */
    private void ensureUniqueName(Long id, Long subjectId, String name) {
        ExamTag existing = examTagMapper.selectOne(new LambdaQueryWrapper<ExamTag>()
                .eq(ExamTag::getSubjectId, subjectId)
                .eq(ExamTag::getName, name.trim())
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("同一学科下考点名称不能重复", 400);
        }
    }

    /**
     * 构建业务处理所需数据。
     */
    private Map<Long, String> buildSubjectNameMap(List<ExamTag> examTags) {
        if (examTags.isEmpty()) {
            return Map.of();
        }
        Set<Long> subjectIds = examTags.stream().map(ExamTag::getSubjectId).collect(Collectors.toSet());
        List<Subject> subjects = subjectMapper.selectBatchIds(subjectIds);
        Map<Long, String> subjectNameMap = new HashMap<>();
        for (Subject subject : subjects) {
            if (subject != null && !Integer.valueOf(1).equals(subject.getDeleted())) {
                subjectNameMap.put(subject.getId(), subject.getName());
            }
        }
        return subjectNameMap;
    }

    /**
     * 执行参数与状态校验。
     */
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

    /**
     * 执行参数与状态校验。
     */
    private ExamTag requireTag(Long id) {
        ExamTag examTag = examTagMapper.selectById(id);
        if (examTag == null) {
            throw new BusinessException("考点标签不存在", 404);
        }
        return examTag;
    }

    /**
     * 解析并转换输入数据。
     */
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 执行核心业务处理流程。
     */
    private ExamTagVO toVO(ExamTag examTag, String subjectName) {
        ExamTagVO examTagVO = new ExamTagVO();
        examTagVO.setId(examTag.getId());
        examTagVO.setName(examTag.getName());
        examTagVO.setSubjectId(examTag.getSubjectId());
        examTagVO.setSubjectName(subjectName);
        examTagVO.setTag(examTag.getTag());
        examTagVO.setCreateTime(examTag.getCreateTime());
        examTagVO.setUpdateTime(examTag.getUpdateTime());
        return examTagVO;
    }
}
