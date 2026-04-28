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

@Slf4j
@Service
public class ExamTagServiceImpl implements ExamTagService {
    private final ExamTagMapper examTagMapper;
    private final SubjectMapper subjectMapper;

    public ExamTagServiceImpl(ExamTagMapper examTagMapper, SubjectMapper subjectMapper) {
        this.examTagMapper = examTagMapper;
        this.subjectMapper = subjectMapper;
    }

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

    @Override
    @Transactional
    public void deleteTag(Long id) {
        ExamTag examTag = requireTag(id);
        examTagMapper.deleteById(id);
        log.info("考点标签删除成功，tagId={}", examTag.getId());
    }

    private void applyChanges(ExamTag examTag, ExamTagUpsertDTO examTagUpsertDTO) {
        examTag.setName(examTagUpsertDTO.getName().trim());
        examTag.setSubjectId(examTagUpsertDTO.getSubjectId());
        examTag.setTag(normalizeText(examTagUpsertDTO.getTag()));
    }

    private void ensureUniqueName(Long id, Long subjectId, String name) {
        ExamTag existing = examTagMapper.selectOne(new LambdaQueryWrapper<ExamTag>()
                .eq(ExamTag::getSubjectId, subjectId)
                .eq(ExamTag::getName, name.trim())
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("同一学科下考点名称不能重复", 400);
        }
    }

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

    private ExamTag requireTag(Long id) {
        ExamTag examTag = examTagMapper.selectById(id);
        if (examTag == null) {
            throw new BusinessException("考点标签不存在", 404);
        }
        return examTag;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

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
