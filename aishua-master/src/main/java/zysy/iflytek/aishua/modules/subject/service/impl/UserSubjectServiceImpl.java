package zysy.iflytek.aishua.modules.subject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.directory.entity.DirectoryTagRelation;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;
import zysy.iflytek.aishua.modules.directory.mapper.DirectoryTagRelationMapper;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;
import zysy.iflytek.aishua.modules.directory.service.DirectoryService;
import zysy.iflytek.aishua.modules.directory.support.DirectoryScopeResolver;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.entity.QuestionTagRelation;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;
import zysy.iflytek.aishua.modules.question.mapper.QuestionTagRelationMapper;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.entity.UserSubject;
import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectCatalogVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectDirectoryTagVO;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.subject.mapper.UserSubjectMapper;
import zysy.iflytek.aishua.modules.subject.service.UserSubjectService;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学科服务实现，负责用户学科相关业务逻辑与流程处理。
 */
@Slf4j
@Service
public class UserSubjectServiceImpl implements UserSubjectService {
    private final SubjectMapper subjectMapper;
    private final UserSubjectMapper userSubjectMapper;
    private final DirectoryService directoryService;
    private final TextbookDirectoryMapper textbookDirectoryMapper;
    private final DirectoryTagRelationMapper directoryTagRelationMapper;
    private final DirectoryScopeResolver directoryScopeResolver;
    private final ExamTagMapper examTagMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public UserSubjectServiceImpl(
            SubjectMapper subjectMapper,
            UserSubjectMapper userSubjectMapper,
            DirectoryService directoryService,
            TextbookDirectoryMapper textbookDirectoryMapper,
            DirectoryTagRelationMapper directoryTagRelationMapper,
            DirectoryScopeResolver directoryScopeResolver,
            ExamTagMapper examTagMapper,
            QuestionMapper questionMapper,
            QuestionTagRelationMapper questionTagRelationMapper
    ) {
        this.subjectMapper = subjectMapper;
        this.userSubjectMapper = userSubjectMapper;
        this.directoryService = directoryService;
        this.textbookDirectoryMapper = textbookDirectoryMapper;
        this.directoryTagRelationMapper = directoryTagRelationMapper;
        this.directoryScopeResolver = directoryScopeResolver;
        this.examTagMapper = examTagMapper;
        this.questionMapper = questionMapper;
        this.questionTagRelationMapper = questionTagRelationMapper;
    }

    /**
     * 查询可加入的学科目录，并附带当前用户是否已加入标识。
     */
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

    /**
     * 查询当前用户已加入学科。
     */
    @Override
    public List<MySubjectVO> listMySubjects(Long userId) {
        return userSubjectMapper.selectMySubjects(userId);
    }

    /**
     * 用户加入学科。
     */
    @Override
    @Transactional
    public MySubjectVO joinSubject(Long userId, Long subjectId) {
        Subject subject = requireSubjectForJoin(subjectId);

        userSubjectMapper.upsertJoin(userId, subjectId);
        MySubjectVO mySubjectVO = userSubjectMapper.selectMySubjectBySubjectId(userId, subjectId);
        if (mySubjectVO == null) {
            throw new BusinessException("加入学科失败，请稍后重试", 500);
        }

        log.info("用户加入学科成功，userId={}, subjectId={}", userId, subject.getId());
        return mySubjectVO;
    }

    /**
     * 查询用户在指定学科下可见的目录树。
     */
    @Override
    public List<DirectoryTreeVO> listSubjectDirectories(Long userId, Long subjectId) {
        Subject subject = requireJoinedSubject(userId, subjectId);
        return directoryService.listTreeBySubject(subject.getId());
    }

    /**
     * 查询指定目录下的考点列表，并统计该目录范围（含子目录）内每个考点的题量。
     */
    @Override
    public List<SubjectDirectoryTagVO> listSubjectDirectoryTags(Long userId, Long subjectId, Long directoryId) {
        Subject subject = requireJoinedSubject(userId, subjectId);
        validateDirectoryInSubject(directoryId, subject.getId());

        List<DirectoryTagRelation> relations = directoryTagRelationMapper.selectList(new LambdaQueryWrapper<DirectoryTagRelation>()
                .eq(DirectoryTagRelation::getSubjectId, subject.getId())
                .eq(DirectoryTagRelation::getDirectoryId, directoryId)
                .eq(DirectoryTagRelation::getIsEnabled, 1)
                .orderByAsc(DirectoryTagRelation::getSort)
                .orderByAsc(DirectoryTagRelation::getId));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = relations.stream()
                .map(DirectoryTagRelation::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ExamTag> tagMap = examTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> tag != null && !Integer.valueOf(1).equals(tag.getDeleted()))
                .filter(tag -> subject.getId().equals(tag.getSubjectId()))
                .collect(Collectors.toMap(ExamTag::getId, Function.identity(), (left, right) -> left));
        if (tagMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> directoryScopeIds = directoryScopeResolver.resolveSelfAndDescendants(directoryId, subject.getId());
        Map<Long, Integer> tagQuestionCountMap = buildTagQuestionCountMap(subject.getId(), directoryScopeIds, tagMap.keySet());

        List<SubjectDirectoryTagVO> result = new ArrayList<>();
        for (DirectoryTagRelation relation : relations) {
            ExamTag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            SubjectDirectoryTagVO item = new SubjectDirectoryTagVO();
            item.setTagId(tag.getId());
            item.setTagName(tag.getName());
            item.setRelationType(relation.getRelationType());
            item.setImportanceLevel(relation.getImportanceLevel());
            item.setExamFrequency(relation.getExamFrequency());
            item.setSort(relation.getSort());
            item.setQuestionCount(tagQuestionCountMap.getOrDefault(tag.getId(), 0));
            result.add(item);
        }
        return result;
    }

    /**
     * 校验学科是否存在且可加入。
     */
    private Subject requireSubjectForJoin(Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            throw new BusinessException("学科编号不合法", 400);
        }

        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("学科不存在", 404);
        }
        if (!Integer.valueOf(1).equals(subject.getIsEnabled())) {
            throw new BusinessException("该学科已禁用，暂不能加入", 400);
        }
        return subject;
    }

    /**
     * 校验用户是否已加入学科。
     */
    private Subject requireJoinedSubject(Long userId, Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            throw new BusinessException("学科编号不合法", 400);
        }

        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("学科不存在", 404);
        }

        UserSubject userSubject = userSubjectMapper.selectOne(new LambdaQueryWrapper<UserSubject>()
                .eq(UserSubject::getUserId, userId)
                .eq(UserSubject::getSubjectId, subjectId)
                .last("LIMIT 1"));
        if (userSubject == null || Integer.valueOf(1).equals(userSubject.getDeleted())) {
            throw new BusinessException("请先加入该学科后再查看目录", 400);
        }
        return subject;
    }

    /**
     * 校验目录是否存在且属于指定学科。
     */
    private void validateDirectoryInSubject(Long directoryId, Long subjectId) {
        if (directoryId == null || directoryId <= 0) {
            throw new BusinessException("目录编号不合法", 400);
        }

        TextbookDirectory directory = textbookDirectoryMapper.selectById(directoryId);
        if (directory == null || Integer.valueOf(1).equals(directory.getDeleted())) {
            throw new BusinessException("目录不存在", 404);
        }
        if (!subjectId.equals(directory.getSubjectId())) {
            throw new BusinessException("目录不属于当前学科", 400);
        }
    }

    /**
     * 统计目录范围内各考点关联的去重题量。
     */
    private Map<Long, Integer> buildTagQuestionCountMap(Long subjectId, List<Long> directoryScopeIds, Set<Long> tagIds) {
        if (directoryScopeIds == null || directoryScopeIds.isEmpty() || tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Question> scopeQuestions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getSubjectId, subjectId)
                .in(Question::getDirectoryId, directoryScopeIds));
        if (scopeQuestions.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> questionIds = scopeQuestions.stream()
                .map(Question::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<QuestionTagRelation> scopeRelations = questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                .in(QuestionTagRelation::getTagId, tagIds)
                .in(QuestionTagRelation::getQuestionId, questionIds));
        if (scopeRelations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Set<Long>> tagQuestionIdSetMap = new HashMap<>();
        for (QuestionTagRelation relation : scopeRelations) {
            if (relation.getTagId() == null || relation.getQuestionId() == null) {
                continue;
            }
            tagQuestionIdSetMap
                    .computeIfAbsent(relation.getTagId(), key -> new LinkedHashSet<>())
                    .add(relation.getQuestionId());
        }

        Map<Long, Integer> tagQuestionCountMap = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : tagQuestionIdSetMap.entrySet()) {
            tagQuestionCountMap.put(entry.getKey(), entry.getValue().size());
        }
        return tagQuestionCountMap;
    }

    /**
     * 学科目录对象转换。
     */
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
