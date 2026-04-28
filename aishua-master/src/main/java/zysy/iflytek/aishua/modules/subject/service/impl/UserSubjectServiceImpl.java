package zysy.iflytek.aishua.modules.subject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectCatalogVO;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.subject.mapper.UserSubjectMapper;
import zysy.iflytek.aishua.modules.subject.service.UserSubjectService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserSubjectServiceImpl implements UserSubjectService {
    private final SubjectMapper subjectMapper;
    private final UserSubjectMapper userSubjectMapper;

    public UserSubjectServiceImpl(SubjectMapper subjectMapper, UserSubjectMapper userSubjectMapper) {
        this.subjectMapper = subjectMapper;
        this.userSubjectMapper = userSubjectMapper;
    }

    @Override
    public List<SubjectCatalogVO> listSubjectCatalog(Long userId) {
        List<Subject> subjects = subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getIsEnabled, 1)
                .orderByAsc(Subject::getSort)
                .orderByDesc(Subject::getId));

        Set<Long> joinedSubjectIds = new HashSet<>(userSubjectMapper.selectJoinedSubjectIds(userId));
        return subjects.stream()
                .map(subject -> toCatalogVO(subject, joinedSubjectIds.contains(subject.getId())))
                .toList();
    }

    @Override
    public List<MySubjectVO> listMySubjects(Long userId) {
        return userSubjectMapper.selectMySubjects(userId);
    }

    @Override
    @Transactional
    public MySubjectVO joinSubject(Long userId, Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            throw new BusinessException("学科ID不合法", 400);
        }

        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("学科不存在", 404);
        }
        if (!Integer.valueOf(1).equals(subject.getIsEnabled())) {
            throw new BusinessException("该学科已禁用，暂不能加入", 400);
        }

        userSubjectMapper.upsertJoin(userId, subjectId);
        MySubjectVO mySubjectVO = userSubjectMapper.selectMySubjectBySubjectId(userId, subjectId);
        if (mySubjectVO == null) {
            throw new BusinessException("加入学科失败，请稍后重试", 500);
        }

        log.info("用户加入学科成功，userId={}, subjectId={}", userId, subjectId);
        return mySubjectVO;
    }

    private SubjectCatalogVO toCatalogVO(Subject subject, boolean joined) {
        SubjectCatalogVO subjectCatalogVO = new SubjectCatalogVO();
        subjectCatalogVO.setId(subject.getId());
        subjectCatalogVO.setName(subject.getName());
        subjectCatalogVO.setCode(subject.getCode());
        subjectCatalogVO.setDescription(subject.getDescription());
        subjectCatalogVO.setIcon(subject.getIcon());
        subjectCatalogVO.setQuestionCount(subject.getQuestionCount());
        subjectCatalogVO.setSort(subject.getSort());
        subjectCatalogVO.setIsEnabled(subject.getIsEnabled());
        subjectCatalogVO.setJoined(joined);
        return subjectCatalogVO;
    }
}
