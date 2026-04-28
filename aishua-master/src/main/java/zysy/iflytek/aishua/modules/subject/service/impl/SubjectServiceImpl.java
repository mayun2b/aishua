package zysy.iflytek.aishua.modules.subject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.entity.dto.SubjectUpsertDTO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectVO;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.subject.service.SubjectService;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class SubjectServiceImpl implements SubjectService {
    private final SubjectMapper subjectMapper;

    public SubjectServiceImpl(SubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    @Override
    public List<SubjectVO> listAdminSubjects(String keyword, Integer enabled) {
        LambdaQueryWrapper<Subject> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Subject::getName, keyword.trim())
                    .or()
                    .like(Subject::getCode, keyword.trim()));
        }
        if (enabled != null) {
            queryWrapper.eq(Subject::getIsEnabled, enabled);
        }
        queryWrapper.orderByAsc(Subject::getSort).orderByDesc(Subject::getId);
        return subjectMapper.selectList(queryWrapper).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public SubjectVO createSubject(SubjectUpsertDTO subjectUpsertDTO) {
        validateEnabled(subjectUpsertDTO.getIsEnabled());
        ensureCodeUnique(null, subjectUpsertDTO.getCode());

        Subject subject = new Subject();
        applyChanges(subject, subjectUpsertDTO);
        subject.setQuestionCount(0);
        subjectMapper.insert(subject);

        log.info("学科创建成功，subjectId={}", subject.getId());
        return toVO(subject);
    }

    @Override
    @Transactional
    public SubjectVO updateSubject(Long id, SubjectUpsertDTO subjectUpsertDTO) {
        validateEnabled(subjectUpsertDTO.getIsEnabled());
        Subject subject = requireSubject(id);
        ensureCodeUnique(id, subjectUpsertDTO.getCode());

        applyChanges(subject, subjectUpsertDTO);
        subjectMapper.updateById(subject);

        log.info("学科更新成功，subjectId={}", subject.getId());
        return toVO(subjectMapper.selectById(id));
    }

    @Override
    @Transactional
    public SubjectVO updateEnabledStatus(Long id, Integer enabled) {
        validateEnabled(enabled);
        Subject subject = requireSubject(id);
        subject.setIsEnabled(enabled);
        subjectMapper.updateById(subject);

        log.info("学科启停状态更新成功，subjectId={}, enabled={}", subject.getId(), enabled);
        return toVO(subjectMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = requireSubject(id);
        // 与 SQL 设计一致：deleted 字段启用逻辑删除
        subjectMapper.deleteById(id);
        log.info("学科删除成功，subjectId={}", subject.getId());
    }

    private void applyChanges(Subject subject, SubjectUpsertDTO subjectUpsertDTO) {
        subject.setName(subjectUpsertDTO.getName().trim());
        subject.setCode(subjectUpsertDTO.getCode().trim().toUpperCase(Locale.ROOT));
        subject.setDescription(normalizeText(subjectUpsertDTO.getDescription()));
        subject.setIcon(normalizeText(subjectUpsertDTO.getIcon()));
        subject.setSort(subjectUpsertDTO.getSort());
        subject.setIsEnabled(subjectUpsertDTO.getIsEnabled());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void ensureCodeUnique(Long id, String code) {
        Subject existing = subjectMapper.selectOne(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getCode, code.trim().toUpperCase(Locale.ROOT))
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("学科编码已存在", 400);
        }
    }

    private Subject requireSubject(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("学科不存在", 404);
        }
        return subject;
    }

    private void validateEnabled(Integer enabled) {
        if (!Integer.valueOf(0).equals(enabled) && !Integer.valueOf(1).equals(enabled)) {
            throw new BusinessException("启用状态只能是 0 或 1", 400);
        }
    }

    private SubjectVO toVO(Subject subject) {
        SubjectVO subjectVO = new SubjectVO();
        subjectVO.setId(subject.getId());
        subjectVO.setName(subject.getName());
        subjectVO.setCode(subject.getCode());
        subjectVO.setDescription(subject.getDescription());
        subjectVO.setIcon(subject.getIcon());
        subjectVO.setQuestionCount(subject.getQuestionCount());
        subjectVO.setSort(subject.getSort());
        subjectVO.setIsEnabled(subject.getIsEnabled());
        subjectVO.setCreateTime(subject.getCreateTime());
        subjectVO.setUpdateTime(subject.getUpdateTime());
        return subjectVO;
    }
}
