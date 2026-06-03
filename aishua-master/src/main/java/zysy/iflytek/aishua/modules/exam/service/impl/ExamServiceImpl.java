package zysy.iflytek.aishua.modules.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.entity.DirectoryTagRelation;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;
import zysy.iflytek.aishua.modules.directory.mapper.DirectoryTagRelationMapper;
import zysy.iflytek.aishua.modules.directory.support.DirectoryScopeResolver;
import zysy.iflytek.aishua.modules.directory.service.DirectoryService;
import zysy.iflytek.aishua.modules.exam.entity.ExamPaper;
import zysy.iflytek.aishua.modules.exam.entity.ExamPaperQuestion;
import zysy.iflytek.aishua.modules.exam.entity.ExamRecord;
import zysy.iflytek.aishua.modules.exam.entity.ExamRecordQuestion;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperQuestionAssignDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperQuestionItemDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperUpsertDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamStartDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamSubmitAnswerItemDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamSubmitDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.LegacyExamRecordQuestionDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.LegacyExamRecordSaveDTO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamAvailableQuestionPageVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamAvailableQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamDirectoryTagVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamQuestionItemVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordSummaryVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamStartVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamSubmitResultVO;
import zysy.iflytek.aishua.modules.exam.mapper.ExamPaperMapper;
import zysy.iflytek.aishua.modules.exam.mapper.ExamPaperQuestionMapper;
import zysy.iflytek.aishua.modules.exam.mapper.ExamRecordMapper;
import zysy.iflytek.aishua.modules.exam.mapper.ExamRecordQuestionMapper;
import zysy.iflytek.aishua.modules.exam.service.ExamService;
import zysy.iflytek.aishua.modules.practice.support.AnswerJudgeSupport;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.entity.QuestionTagRelation;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;
import zysy.iflytek.aishua.modules.question.mapper.QuestionTagRelationMapper;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;
import zysy.iflytek.aishua.modules.user.entity.User;
import zysy.iflytek.aishua.modules.user.mapper.UserMapper;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考试服务实现，负责该领域业务流程编排。
 */
@Slf4j
@Service
public class ExamServiceImpl implements ExamService {
    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;
    private static final int RECORD_STATUS_ONGOING = 1;
    private static final int RECORD_STATUS_FINISHED = 2;
    private static final int DEFAULT_TOTAL_SCORE = 100;
    private static final int EXAM_MODE_PAPER = 1;

    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamRecordQuestionMapper examRecordQuestionMapper;
    private final SubjectMapper subjectMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final TextbookDirectoryMapper textbookDirectoryMapper;
    private final DirectoryTagRelationMapper directoryTagRelationMapper;
    private final ExamTagMapper examTagMapper;
    private final UserMapper userMapper;
    private final DirectoryService directoryService;
    private final DirectoryScopeResolver directoryScopeResolver;
    private final AnswerJudgeSupport answerJudgeSupport;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public ExamServiceImpl(
            ExamPaperMapper examPaperMapper,
            ExamPaperQuestionMapper examPaperQuestionMapper,
            ExamRecordMapper examRecordMapper,
            ExamRecordQuestionMapper examRecordQuestionMapper,
            SubjectMapper subjectMapper,
            QuestionMapper questionMapper,
            QuestionTagRelationMapper questionTagRelationMapper,
            TextbookDirectoryMapper textbookDirectoryMapper,
            DirectoryTagRelationMapper directoryTagRelationMapper,
            ExamTagMapper examTagMapper,
            UserMapper userMapper,
            DirectoryService directoryService,
            DirectoryScopeResolver directoryScopeResolver,
            AnswerJudgeSupport answerJudgeSupport
    ) {
        this.examPaperMapper = examPaperMapper;
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.examRecordMapper = examRecordMapper;
        this.examRecordQuestionMapper = examRecordQuestionMapper;
        this.subjectMapper = subjectMapper;
        this.questionMapper = questionMapper;
        this.questionTagRelationMapper = questionTagRelationMapper;
        this.textbookDirectoryMapper = textbookDirectoryMapper;
        this.directoryTagRelationMapper = directoryTagRelationMapper;
        this.examTagMapper = examTagMapper;
        this.userMapper = userMapper;
        this.directoryService = directoryService;
        this.directoryScopeResolver = directoryScopeResolver;
        this.answerJudgeSupport = answerJudgeSupport;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamPaperVO> listAdminPapers(Long subjectId, Integer status, String keyword) {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<ExamPaper>()
                .orderByDesc(ExamPaper::getUpdateTime)
                .orderByDesc(ExamPaper::getId);
        if (subjectId != null) {
            wrapper.eq(ExamPaper::getSubjectId, subjectId);
        }
        if (status != null) {
            wrapper.eq(ExamPaper::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ExamPaper::getPaperName, keyword.trim());
        }

        List<ExamPaper> papers = examPaperMapper.selectList(wrapper);
        return toPaperVOList(papers);
    }

    /**
     * 执行创建业务流程并返回结果。
     */
    @Override
    @Transactional
    public ExamPaperVO createPaper(AdminExamPaperUpsertDTO upsertDTO) {
        Subject subject = requireSubject(upsertDTO.getSubjectId());
        ensureUniquePaperName(null, subject.getId(), upsertDTO.getPaperName());

        ExamPaper examPaper = new ExamPaper();
        examPaper.setPaperName(upsertDTO.getPaperName().trim());
        examPaper.setSubjectId(subject.getId());
        examPaper.setDuration(upsertDTO.getDuration());
        examPaper.setStatus(upsertDTO.getStatus());
        // 该字段表示创建试卷时配置的目标总分。
        examPaper.setTotalScore(upsertDTO.getTotalScore());
        examPaper.setTotalQuestions(0);
        examPaperMapper.insert(examPaper);

        log.info("试卷创建成功，paperId={}, subjectId={}", examPaper.getId(), subject.getId());
        return toPaperVO(requirePaper(examPaper.getId()), subject.getName());
    }

    /**
     * 执行保存与更新业务流程。
     */
    @Override
    @Transactional
    public ExamPaperVO updatePaper(Long id, AdminExamPaperUpsertDTO upsertDTO) {
        ExamPaper existing = requirePaper(id);
        Subject subject = requireSubject(upsertDTO.getSubjectId());
        ensureUniquePaperName(id, subject.getId(), upsertDTO.getPaperName());
        if (!Objects.equals(existing.getSubjectId(), subject.getId())) {
            Long relationCount = examPaperQuestionMapper.selectCount(new LambdaQueryWrapper<ExamPaperQuestion>()
                    .eq(ExamPaperQuestion::getPaperId, existing.getId()));
            if (relationCount != null && relationCount > 0) {
                throw new BusinessException("试卷已配置题目，不允许直接修改学科", 400);
            }
        }

        existing.setPaperName(upsertDTO.getPaperName().trim());
        existing.setSubjectId(subject.getId());
        existing.setDuration(upsertDTO.getDuration());
        existing.setTotalScore(upsertDTO.getTotalScore());
        existing.setStatus(upsertDTO.getStatus());
        examPaperMapper.updateById(existing);

        log.info("试卷更新成功，paperId={}", id);
        return toPaperVO(requirePaper(id), subject.getName());
    }

    /**
     * 执行保存与更新业务流程。
     */
    @Override
    @Transactional
    public ExamPaperVO updatePaperEnabled(Long id, Integer enabled) {
        if (!isDisabled(enabled) && !isEnabled(enabled)) {
            throw new BusinessException("状态只能是 0 或 1", 400);
        }

        ExamPaper paper = requirePaper(id);
        paper.setStatus(enabled);
        examPaperMapper.updateById(paper);

        Subject subject = subjectMapper.selectById(paper.getSubjectId());
        String subjectName = subject == null ? null : subject.getName();
        return toPaperVO(paper, subjectName);
    }

    /**
     * 执行删除与清理业务流程。
     */
    @Override
    @Transactional
    public void deletePaper(Long id) {
        requirePaper(id);
        examPaperQuestionMapper.delete(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, id));
        examPaperMapper.deleteById(id);
        log.info("试卷删除成功，paperId={}", id);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamPaperQuestionVO> listPaperQuestions(Long paperId) {
        ExamPaper paper = requirePaper(paperId);
        List<ExamPaperQuestion> relations = listPaperQuestionRelations(paper.getId());
        return buildPaperQuestionVOList(relations);
    }

    /**
     * 执行核心业务处理流程。
     */
    @Override
    @Transactional
    public List<ExamPaperQuestionVO> assignPaperQuestions(Long paperId, AdminExamPaperQuestionAssignDTO assignDTO) {
        ExamPaper paper = requirePaper(paperId);
        List<AdminExamPaperQuestionItemDTO> items = assignDTO.getQuestions();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("试卷题目不能为空", 400);
        }
        if (items.size() > 300) {
            throw new BusinessException("单张试卷题目不能超过 300 道", 400);
        }

        Map<Long, AdminExamPaperQuestionItemDTO> uniqueItemMap = new LinkedHashMap<>();
        Set<Integer> uniqueSortSet = new LinkedHashSet<>();
        for (AdminExamPaperQuestionItemDTO item : items) {
            Long questionId = item.getQuestionId();
            if (uniqueItemMap.containsKey(questionId)) {
                throw new BusinessException("试卷题目存在重复", 400);
            }
            if (!uniqueSortSet.add(item.getSort())) {
                throw new BusinessException("题目排序存在重复", 400);
            }
            uniqueItemMap.put(questionId, item);
        }

        List<Long> questionIds = new ArrayList<>(uniqueItemMap.keySet());
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        if (questions.size() != questionIds.size()) {
            throw new BusinessException("存在无效题目", 400);
        }

        for (Question question : questions) {
            if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
                throw new BusinessException("存在已删除题目", 400);
            }
            if (!Objects.equals(paper.getSubjectId(), question.getSubjectId())) {
                throw new BusinessException("试卷题目必须与试卷学科一致", 400);
            }
        }

        examPaperQuestionMapper.delete(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, paper.getId()));

        for (AdminExamPaperQuestionItemDTO item : items) {
            ExamPaperQuestion relation = new ExamPaperQuestion();
            relation.setPaperId(paper.getId());
            relation.setQuestionId(item.getQuestionId());
            relation.setSort(item.getSort());
            relation.setScore(item.getScore());
            examPaperQuestionMapper.insert(relation);
        }

        // 保留试卷目标总分配置，此处仅同步已选题目数量。
        paper.setTotalQuestions(items.size());
        examPaperMapper.updateById(paper);

        log.info("试卷题目配置成功，paperId={}, questionCount={}", paper.getId(), items.size());
        return buildPaperQuestionVOList(listPaperQuestionRelations(paper.getId()));
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<DirectoryTreeVO> listPaperDirectories(Long paperId) {
        ExamPaper paper = requirePaper(paperId);
        return directoryService.listTreeBySubject(paper.getSubjectId());
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamDirectoryTagVO> listPaperDirectoryTags(Long paperId, Long directoryId) {
        ExamPaper paper = requirePaper(paperId);
        if (directoryId == null || directoryId <= 0) {
            throw new BusinessException("目录编号不合法", 400);
        }

        List<Long> directoryScopeIds = directoryScopeResolver.resolveSelfAndDescendants(directoryId, paper.getSubjectId());
        List<DirectoryTagRelation> relations = directoryTagRelationMapper.selectList(new LambdaQueryWrapper<DirectoryTagRelation>()
                .eq(DirectoryTagRelation::getSubjectId, paper.getSubjectId())
                .eq(DirectoryTagRelation::getDirectoryId, directoryId)
                .eq(DirectoryTagRelation::getIsEnabled, 1)
                .orderByAsc(DirectoryTagRelation::getSort)
                .orderByAsc(DirectoryTagRelation::getId));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = relations.stream().map(DirectoryTagRelation::getTagId).filter(Objects::nonNull).distinct().toList();
        Map<Long, ExamTag> tagMap = examTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> tag != null && !Integer.valueOf(1).equals(tag.getDeleted()))
                .collect(Collectors.toMap(ExamTag::getId, Function.identity(), (left, right) -> left));
        if (tagMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<Question> scopeQuestions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getSubjectId, paper.getSubjectId())
                .in(Question::getDirectoryId, directoryScopeIds));
        Set<Long> scopeQuestionIds = scopeQuestions.stream().map(Question::getId).collect(Collectors.toSet());
        Map<Long, Integer> tagQuestionCountMap = Collections.emptyMap();
        if (!scopeQuestionIds.isEmpty()) {
            List<QuestionTagRelation> scopeRelations = questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                    .in(QuestionTagRelation::getTagId, tagMap.keySet())
                    .in(QuestionTagRelation::getQuestionId, scopeQuestionIds));
            Map<Long, Set<Long>> tagQuestionIdSetMap = new HashMap<>();
            for (QuestionTagRelation relation : scopeRelations) {
                if (relation.getTagId() == null || relation.getQuestionId() == null) {
                    continue;
                }
                tagQuestionIdSetMap
                        .computeIfAbsent(relation.getTagId(), key -> new LinkedHashSet<>())
                        .add(relation.getQuestionId());
            }
            tagQuestionCountMap = new HashMap<>();
            for (Map.Entry<Long, Set<Long>> entry : tagQuestionIdSetMap.entrySet()) {
                tagQuestionCountMap.put(entry.getKey(), entry.getValue().size());
            }
        }

        List<ExamDirectoryTagVO> result = new ArrayList<>();
        for (DirectoryTagRelation relation : relations) {
            ExamTag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            ExamDirectoryTagVO item = new ExamDirectoryTagVO();
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
     * 执行查询业务流程并返回结果。
     */
    @Override
    public ExamAvailableQuestionPageVO listPaperAvailableQuestions(
            Long paperId,
            Long directoryId,
            String tagIds,
            Integer type,
            Integer difficulty,
            String keyword,
            Integer page,
            Integer pageSize
    ) {
        ExamPaper paper = requirePaper(paperId);
        int safePage = page == null || page <= 0 ? 1 : page;
        int safePageSize = pageSize == null || pageSize <= 0 ? 10 : Math.min(pageSize, 100);

        List<Long> directoryFilterIds = directoryId == null
                ? Collections.emptyList()
                : directoryScopeResolver.resolveSelfAndDescendants(directoryId, paper.getSubjectId());

        List<Long> requestedTagIds = parseTagIds(tagIds);
        if (!requestedTagIds.isEmpty()) {
            validateTagScope(requestedTagIds, paper.getSubjectId());
        }

        long total = countAvailableQuestions(
                paper,
                directoryFilterIds,
                requestedTagIds,
                type,
                difficulty,
                keyword
        );
        if (total <= 0) {
            return emptyAvailableQuestionPage(safePage, safePageSize);
        }

        int offset = (safePage - 1) * safePageSize;
        List<Question> questions = queryAvailableQuestions(
                paper,
                directoryFilterIds,
                requestedTagIds,
                type,
                difficulty,
                keyword,
                offset,
                safePageSize
        );

        ExamAvailableQuestionPageVO pageVO = new ExamAvailableQuestionPageVO();
        pageVO.setTotal(total);
        pageVO.setPage(safePage);
        pageVO.setPageSize(safePageSize);
        pageVO.setRecords(buildAvailableQuestionVOList(paper.getId(), questions));
        return pageVO;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public PageResult<ExamRecordSummaryVO> listAdminRecords(
            Long subjectId,
            Long userId,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Integer pageNum,
            Integer pageSize
    ) {
        int safePageNum = PageResult.normalizePageNum(pageNum);
        int safePageSize = PageResult.normalizePageSize(pageSize);
        Long total = examRecordMapper.selectCount(buildRecordFilter(subjectId, userId, keyword, null, startDate, endDate));
        if (total == null || total <= 0) {
            return PageResult.empty(safePageNum, safePageSize);
        }

        LambdaQueryWrapper<ExamRecord> wrapper = buildRecordFilter(subjectId, userId, keyword, null, startDate, endDate)
                .orderByDesc(ExamRecord::getStartTime)
                .orderByDesc(ExamRecord::getId)
                .last("LIMIT " + PageResult.offset(safePageNum, safePageSize) + ", " + safePageSize);
        List<ExamRecord> records = examRecordMapper.selectList(wrapper);
        return PageResult.of(toRecordSummaryVOList(records), total, safePageNum, safePageSize);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public ExamRecordSummaryVO getAdminRecord(Long recordId) {
        ExamRecord record = requireRecord(recordId);
        return toRecordSummaryVO(record, loadSubjectNameMap(List.of(record)).get(record.getSubjectId()), loadUserMap(List.of(record)).get(record.getUserId()));
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamRecordQuestionVO> getAdminRecordQuestions(Long recordId) {
        ExamRecord record = requireRecord(recordId);
        return buildRecordQuestionVOList(record);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamPaperVO> listAvailablePapers(Long subjectId) {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getStatus, STATUS_ENABLED)
                .orderByDesc(ExamPaper::getUpdateTime)
                .orderByDesc(ExamPaper::getId);
        if (subjectId != null) {
            wrapper.eq(ExamPaper::getSubjectId, subjectId);
        }
        List<ExamPaper> papers = examPaperMapper.selectList(wrapper);
        return toPaperVOList(papers);
    }

    /**
     * 执行创建业务流程并返回结果。
     */
    @Override
    @Transactional
    public ExamStartVO startExam(Long userId, ExamStartDTO startDTO) {
        ExamPaper paper = requireEnabledPaper(startDTO.getPaperId());
        List<ExamPaperQuestion> relations = listPaperQuestionRelations(paper.getId());
        if (relations.isEmpty()) {
            throw new BusinessException("该试卷尚未配置题目", 400);
        }

        List<Long> questionIds = relations.stream().map(ExamPaperQuestion::getQuestionId).toList();
        Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds).stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
        if (questionMap.size() != questionIds.size()) {
            throw new BusinessException("试卷存在无效题目，请联系老师重新配置", 400);
        }

        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setSubjectId(paper.getSubjectId());
        record.setExamName(paper.getPaperName());
        record.setExamMode(EXAM_MODE_PAPER);
        record.setTotalQuestions(relations.size());
        record.setCorrectQuestions(0);
        record.setScore(0d);
        record.setDuration(0);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(RECORD_STATUS_ONGOING);
        examRecordMapper.insert(record);

        for (ExamPaperQuestion relation : relations) {
            ExamRecordQuestion detail = new ExamRecordQuestion();
            detail.setExamRecordId(record.getId());
            detail.setQuestionId(relation.getQuestionId());
            detail.setUserAnswer(null);
            detail.setIsCorrect(0);
            detail.setAnswerTime(0);
            examRecordQuestionMapper.insert(detail);
        }

        Subject subject = subjectMapper.selectById(paper.getSubjectId());
        ExamStartVO startVO = new ExamStartVO();
        startVO.setExamRecordId(record.getId());
        startVO.setPaperId(paper.getId());
        startVO.setPaperName(paper.getPaperName());
        startVO.setSubjectId(paper.getSubjectId());
        startVO.setSubjectName(subject == null ? null : subject.getName());
        startVO.setTotalQuestions(record.getTotalQuestions());
        startVO.setTotalScore(paper.getTotalScore());
        startVO.setDuration(paper.getDuration());
        startVO.setStatus(record.getStatus());
        startVO.setStartTime(record.getStartTime());

        List<ExamQuestionItemVO> items = new ArrayList<>();
        for (ExamPaperQuestion relation : relations) {
            Question question = questionMap.get(relation.getQuestionId());
            ExamQuestionItemVO itemVO = new ExamQuestionItemVO();
            itemVO.setQuestionId(question.getId());
            itemVO.setTitle(question.getTitle());
            itemVO.setContent(question.getContent());
            itemVO.setType(question.getType());
            itemVO.setOptions(question.getOptions());
            itemVO.setImageUrls(question.getImageUrls());
            itemVO.setImageDesc(question.getImageDesc());
            itemVO.setDifficulty(question.getDifficulty());
            itemVO.setSort(relation.getSort());
            itemVO.setScore(relation.getScore());
            items.add(itemVO);
        }
        items.sort(Comparator.comparing(ExamQuestionItemVO::getSort).thenComparing(ExamQuestionItemVO::getQuestionId));
        startVO.setQuestions(items);

        log.info("考试开始成功，userId={}, paperId={}, recordId={}", userId, paper.getId(), record.getId());
        return startVO;
    }

    /**
     * 执行保存与更新业务流程。
     */
    @Override
    @Transactional
    public ExamSubmitResultVO submitExam(Long userId, Long recordId, ExamSubmitDTO submitDTO) {
        ExamRecord record = requireUserRecord(userId, recordId);
        if (!Objects.equals(record.getStatus(), RECORD_STATUS_ONGOING)) {
            throw new BusinessException("该考试已提交，请勿重复提交", 400);
        }

        List<ExamRecordQuestion> details = listRecordQuestionRelations(record.getId());
        if (details.isEmpty()) {
            throw new BusinessException("考试题目不存在", 404);
        }

        Map<Long, ExamSubmitAnswerItemDTO> answerMap = new LinkedHashMap<>();
        for (ExamSubmitAnswerItemDTO item : submitDTO.getAnswers()) {
            answerMap.put(item.getQuestionId(), item);
        }

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(details.stream().map(ExamRecordQuestion::getQuestionId).toList()).stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        int correctCount = 0;
        for (ExamRecordQuestion detail : details) {
            ExamSubmitAnswerItemDTO answerItem = answerMap.get(detail.getQuestionId());
            String userAnswer = answerItem == null ? null : normalizeText(answerItem.getUserAnswer());
            int answerTime = answerItem == null ? 0 : normalizeNonNegative(answerItem.getAnswerTime());

            Question question = questionMap.get(detail.getQuestionId());
            boolean isCorrect = question != null && answerJudgeSupport.isCorrect(question.getType(), question.getAnswer(), userAnswer);

            detail.setUserAnswer(userAnswer);
            detail.setIsCorrect(isCorrect ? 1 : 0);
            detail.setAnswerTime(answerTime);
            examRecordQuestionMapper.updateById(detail);

            if (isCorrect) {
                correctCount++;
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        int duration = submitDTO.getDuration() != null
                ? normalizeNonNegative(submitDTO.getDuration())
                : calculateDurationMinutes(record.getStartTime(), endTime);
        int totalScore = resolvePaperTotalScore(record.getSubjectId(), record.getExamName());
        double score = calculateScore(correctCount, record.getTotalQuestions(), totalScore);

        record.setCorrectQuestions(correctCount);
        record.setScore(score);
        record.setDuration(duration);
        record.setEndTime(endTime);
        record.setStatus(RECORD_STATUS_FINISHED);
        examRecordMapper.updateById(record);

        ExamSubmitResultVO submitResultVO = new ExamSubmitResultVO();
        submitResultVO.setExamRecordId(record.getId());
        submitResultVO.setTotalQuestions(record.getTotalQuestions());
        submitResultVO.setCorrectQuestions(correctCount);
        submitResultVO.setScore(score);
        submitResultVO.setDuration(duration);
        submitResultVO.setStartTime(record.getStartTime());
        submitResultVO.setEndTime(endTime);
        submitResultVO.setStatus(record.getStatus());
        return submitResultVO;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public PageResult<ExamRecordSummaryVO> listMyRecords(Long userId, Long subjectId, Integer status, Integer pageNum, Integer pageSize) {
        int safePageNum = PageResult.normalizePageNum(pageNum);
        int safePageSize = PageResult.normalizePageSize(pageSize);
        LambdaQueryWrapper<ExamRecord> countWrapper = new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(subjectId != null, ExamRecord::getSubjectId, subjectId)
                .eq(status != null, ExamRecord::getStatus, status);
        Long total = examRecordMapper.selectCount(countWrapper);
        if (total == null || total <= 0) {
            return PageResult.empty(safePageNum, safePageSize);
        }

        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(subjectId != null, ExamRecord::getSubjectId, subjectId)
                .eq(status != null, ExamRecord::getStatus, status)
                .orderByDesc(ExamRecord::getStartTime)
                .orderByDesc(ExamRecord::getId)
                .last("LIMIT " + PageResult.offset(safePageNum, safePageSize) + ", " + safePageSize));
        return PageResult.of(toRecordSummaryVOList(records), total, safePageNum, safePageSize);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public ExamRecordSummaryVO getMyRecord(Long userId, Long recordId) {
        ExamRecord record = requireUserRecord(userId, recordId);
        return toRecordSummaryVO(record, loadSubjectNameMap(List.of(record)).get(record.getSubjectId()), loadUserMap(List.of(record)).get(record.getUserId()));
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamRecordQuestionVO> listMyRecordQuestions(Long userId, Long recordId) {
        ExamRecord record = requireUserRecord(userId, recordId);
        return buildRecordQuestionVOList(record);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamRecordSummaryVO> listUserRecordsByUserId(Long currentUserId, Long userId) {
        assertSelfOrAdmin(currentUserId, userId);
        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getStartTime)
                .orderByDesc(ExamRecord::getId));
        return toRecordSummaryVOList(records);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamRecordSummaryVO> listUserRecordsByMode(Long currentUserId, Long userId, Integer mode) {
        assertSelfOrAdmin(currentUserId, userId);
        if (mode == null || mode <= 0) {
            throw new BusinessException("考试模式不合法", 400);
        }
        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamMode, mode)
                .orderByDesc(ExamRecord::getStartTime)
                .orderByDesc(ExamRecord::getId));
        return toRecordSummaryVOList(records);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<ExamRecordSummaryVO> listUserRecordsByDateRange(Long currentUserId, Long userId, LocalDate startDate, LocalDate endDate) {
        assertSelfOrAdmin(currentUserId, userId);
        List<ExamRecord> records = examRecordMapper.selectList(buildRecordFilter(null, userId, null, null, startDate, endDate)
                .orderByDesc(ExamRecord::getStartTime)
                .orderByDesc(ExamRecord::getId));
        return toRecordSummaryVOList(records);
    }

    /**
     * 执行保存与更新业务流程。
     */
    @Override
    @Transactional
    public ExamRecordSummaryVO saveLegacyRecord(Long currentUserId, LegacyExamRecordSaveDTO saveDTO) {
        Long targetUserId = saveDTO.getUserId() == null ? currentUserId : saveDTO.getUserId();
        assertSelfOrAdmin(currentUserId, targetUserId);

        List<LegacyExamRecordQuestionDTO> questionItems = saveDTO.getQuestions();
        if (questionItems == null || questionItems.isEmpty()) {
            throw new BusinessException("题目明细不能为空", 400);
        }

        Set<Long> questionIdSet = questionItems.stream()
                .map(LegacyExamRecordQuestionDTO::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Question> questions = questionMapper.selectBatchIds(new ArrayList<>(questionIdSet));
        if (questions.size() != questionIdSet.size()) {
            throw new BusinessException("存在无效题目", 400);
        }

        Map<Long, Question> questionMap = questions.stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
        if (questionMap.size() != questionIdSet.size()) {
            throw new BusinessException("存在已删除题目", 400);
        }

        Long subjectId = resolveSubjectId(questionMap.values());
        LocalDateTime startTime = saveDTO.getStartTime() == null ? LocalDateTime.now() : saveDTO.getStartTime();
        LocalDateTime endTime = saveDTO.getEndTime() == null ? LocalDateTime.now() : saveDTO.getEndTime();

        ExamRecord record = new ExamRecord();
        record.setUserId(targetUserId);
        record.setSubjectId(subjectId);
        record.setExamName(saveDTO.getExamName().trim());
        record.setExamMode(saveDTO.getExamMode());
        record.setTotalQuestions(Math.max(saveDTO.getTotalQuestions(), questionItems.size()));
        record.setCorrectQuestions(0);
        record.setScore(0d);
        record.setDuration(saveDTO.getDuration() == null
                ? calculateDurationMinutes(startTime, endTime)
                : normalizeNonNegative(saveDTO.getDuration()));
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setStatus(RECORD_STATUS_FINISHED);
        examRecordMapper.insert(record);

        int correctCount = 0;
        for (LegacyExamRecordQuestionDTO item : questionItems) {
            Question question = questionMap.get(item.getQuestionId());
            String userAnswer = normalizeText(item.getUserAnswer());
            boolean isCorrect = answerJudgeSupport.isCorrect(question.getType(), question.getAnswer(), userAnswer);

            ExamRecordQuestion detail = new ExamRecordQuestion();
            detail.setExamRecordId(record.getId());
            detail.setQuestionId(item.getQuestionId());
            detail.setUserAnswer(userAnswer);
            detail.setIsCorrect(isCorrect ? 1 : 0);
            detail.setAnswerTime(normalizeNonNegative(item.getAnswerTime()));
            examRecordQuestionMapper.insert(detail);

            if (isCorrect) {
                correctCount++;
            }
        }

        double score = saveDTO.getScore() == null
                ? calculateScore(correctCount, record.getTotalQuestions(), DEFAULT_TOTAL_SCORE)
                : Math.max(0d, roundTwoDecimals(saveDTO.getScore()));

        record.setCorrectQuestions(correctCount);
        record.setScore(score);
        examRecordMapper.updateById(record);

        log.info("兼容考试记录保存成功，recordId={}, userId={}", record.getId(), targetUserId);
        return toRecordSummaryVO(record, loadSubjectNameMap(List.of(record)).get(record.getSubjectId()), loadUserMap(List.of(record)).get(record.getUserId()));
    }

    /**
     * 计算并返回处理结果。
     */
    private long countAvailableQuestions(
            ExamPaper paper,
            List<Long> directoryFilterIds,
            List<Long> tagFilterIds,
            Integer type,
            Integer difficulty,
            String keyword
    ) {
        LambdaQueryWrapper<Question> countWrapper = buildAvailableQuestionQuery(
                paper,
                directoryFilterIds,
                tagFilterIds,
                type,
                difficulty,
                keyword
        );
        Long total = questionMapper.selectCount(countWrapper);
        return total == null ? 0L : total;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    private List<Question> queryAvailableQuestions(
            ExamPaper paper,
            List<Long> directoryFilterIds,
            List<Long> tagFilterIds,
            Integer type,
            Integer difficulty,
            String keyword,
            int offset,
            int pageSize
    ) {
        LambdaQueryWrapper<Question> queryWrapper = buildAvailableQuestionQuery(
                paper,
                directoryFilterIds,
                tagFilterIds,
                type,
                difficulty,
                keyword
        );
        queryWrapper.orderByDesc(Question::getUpdateTime)
                .orderByDesc(Question::getId)
                .last("limit " + offset + ", " + pageSize);
        return questionMapper.selectList(queryWrapper);
    }

    /**
     * 构建业务处理所需数据。
     */
    private LambdaQueryWrapper<Question> buildAvailableQuestionQuery(
            ExamPaper paper,
            List<Long> directoryFilterIds,
            List<Long> tagFilterIds,
            Integer type,
            Integer difficulty,
            String keyword
    ) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getSubjectId, paper.getSubjectId());

        if (!directoryFilterIds.isEmpty()) {
            wrapper.in(Question::getDirectoryId, directoryFilterIds);
        }
        if (!tagFilterIds.isEmpty()) {
            String csvTagIds = tagFilterIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            // 使用数据库侧标签筛选，前端只需传递标签编号列表并渲染分页结果。
            wrapper.inSql(Question::getId,
                    "SELECT DISTINCT question_id FROM question_tag_relation WHERE tag_id IN (" + csvTagIds + ")");
        }
        if (type != null) {
            wrapper.eq(Question::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(inner -> inner.like(Question::getTitle, trimmedKeyword)
                    .or()
                    .like(Question::getContent, trimmedKeyword));
        }
        return wrapper;
    }

    /**
     * 解析并转换输入数据。
     */
    private List<Long> parseTagIds(String rawTagIds) {
        if (rawTagIds == null || rawTagIds.isBlank()) {
            return Collections.emptyList();
        }
        LinkedHashSet<Long> parsed = new LinkedHashSet<>();
        String[] parts = rawTagIds.split(",");
        for (String part : parts) {
            String trimmed = part == null ? "" : part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            long id;
            try {
                id = Long.parseLong(trimmed);
            } catch (NumberFormatException exception) {
                throw new BusinessException("考点编号不合法", 400);
            }
            if (id <= 0) {
                throw new BusinessException("考点编号不合法", 400);
            }
            parsed.add(id);
        }
        return new ArrayList<>(parsed);
    }

    /**
     * 执行参数与状态校验。
     */
    private void validateTagScope(List<Long> tagIds, Long subjectId) {
        List<ExamTag> tags = examTagMapper.selectBatchIds(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException("存在无效考点", 400);
        }
        for (ExamTag tag : tags) {
            if (tag == null || Integer.valueOf(1).equals(tag.getDeleted())) {
                throw new BusinessException("存在无效考点", 400);
            }
            if (!Objects.equals(subjectId, tag.getSubjectId())) {
                throw new BusinessException("考点不属于当前试卷学科", 400);
            }
        }
    }

    /**
     * 构建业务处理所需数据。
     */
    private List<ExamAvailableQuestionVO> buildAvailableQuestionVOList(Long paperId, List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> directoryIds = questions.stream()
                .map(Question::getDirectoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, TextbookDirectory> directoryMap = directoryIds.isEmpty()
                ? Collections.emptyMap()
                : textbookDirectoryMapper.selectBatchIds(directoryIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TextbookDirectory::getId, Function.identity(), (left, right) -> left));

        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
        List<QuestionTagRelation> tagRelations = questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                .in(QuestionTagRelation::getQuestionId, questionIds));
        Map<Long, List<Long>> questionTagMap = tagRelations.stream()
                .collect(Collectors.groupingBy(
                        QuestionTagRelation::getQuestionId,
                        Collectors.mapping(QuestionTagRelation::getTagId, Collectors.toList())
                ));
        for (List<Long> tags : questionTagMap.values()) {
            tags.sort(Long::compareTo);
        }

        Set<Long> selectedQuestionIds = examPaperQuestionMapper.selectList(new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paperId)
                        .in(ExamPaperQuestion::getQuestionId, questionIds))
                .stream()
                .map(ExamPaperQuestion::getQuestionId)
                .collect(Collectors.toSet());

        List<ExamAvailableQuestionVO> result = new ArrayList<>();
        for (Question question : questions) {
            TextbookDirectory directory = directoryMap.get(question.getDirectoryId());
            ExamAvailableQuestionVO item = new ExamAvailableQuestionVO();
            item.setId(question.getId());
            item.setTitle(question.getTitle());
            item.setType(question.getType());
            item.setDifficulty(question.getDifficulty());
            item.setDirectoryId(question.getDirectoryId());
            item.setDirectoryName(directory == null ? null : directory.getName());
            item.setTagIds(questionTagMap.getOrDefault(question.getId(), Collections.emptyList()));
            item.setSelected(selectedQuestionIds.contains(question.getId()));
            result.add(item);
        }
        return result;
    }

    /**
     * 执行核心业务处理流程。
     */
    private ExamAvailableQuestionPageVO emptyAvailableQuestionPage(int page, int pageSize) {
        ExamAvailableQuestionPageVO pageVO = new ExamAvailableQuestionPageVO();
        pageVO.setTotal(0L);
        pageVO.setPage(page);
        pageVO.setPageSize(pageSize);
        pageVO.setRecords(Collections.emptyList());
        return pageVO;
    }

    /**
     * 执行核心业务处理流程。
     */
    private List<ExamPaperVO> toPaperVOList(List<ExamPaper> papers) {
        if (papers == null || papers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> subjectNameMap = papers.stream()
                .map(ExamPaper::getSubjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ids -> {
                    if (ids.isEmpty()) {
                        return Collections.<Long, String>emptyMap();
                    }
                    return subjectMapper.selectBatchIds(ids).stream()
                            .filter(subject -> subject != null && !Integer.valueOf(1).equals(subject.getDeleted()))
                            .collect(Collectors.toMap(Subject::getId, Subject::getName, (left, right) -> left));
                }));

        return papers.stream()
                .map(paper -> toPaperVO(paper, subjectNameMap.get(paper.getSubjectId())))
                .toList();
    }

    /**
     * 执行核心业务处理流程。
     */
    private ExamPaperVO toPaperVO(ExamPaper paper, String subjectName) {
        ExamPaperVO paperVO = new ExamPaperVO();
        paperVO.setId(paper.getId());
        paperVO.setPaperName(paper.getPaperName());
        paperVO.setSubjectId(paper.getSubjectId());
        paperVO.setSubjectName(subjectName);
        paperVO.setTotalQuestions(paper.getTotalQuestions());
        paperVO.setTotalScore(paper.getTotalScore());
        paperVO.setDuration(paper.getDuration());
        paperVO.setStatus(paper.getStatus());
        paperVO.setCreateTime(paper.getCreateTime());
        paperVO.setUpdateTime(paper.getUpdateTime());
        return paperVO;
    }

    /**
     * 构建业务处理所需数据。
     */
    private List<ExamPaperQuestionVO> buildPaperQuestionVOList(List<ExamPaperQuestion> relations) {
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(relations.stream().map(ExamPaperQuestion::getQuestionId).toList()).stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        List<ExamPaperQuestionVO> result = new ArrayList<>();
        for (ExamPaperQuestion relation : relations) {
            Question question = questionMap.get(relation.getQuestionId());
            if (question == null) {
                continue;
            }
            ExamPaperQuestionVO questionVO = new ExamPaperQuestionVO();
            questionVO.setQuestionId(question.getId());
            questionVO.setTitle(question.getTitle());
            questionVO.setType(question.getType());
            questionVO.setDifficulty(question.getDifficulty());
            questionVO.setOptions(question.getOptions());
            questionVO.setAnswer(question.getAnswer());
            questionVO.setAnalysis(question.getAnalysis());
            questionVO.setSort(relation.getSort());
            questionVO.setScore(relation.getScore());
            result.add(questionVO);
        }
        result.sort(Comparator.comparing(ExamPaperQuestionVO::getSort).thenComparing(ExamPaperQuestionVO::getQuestionId));
        return result;
    }

    /**
     * 执行核心业务处理流程。
     */
    private List<ExamRecordSummaryVO> toRecordSummaryVOList(List<ExamRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> subjectNameMap = loadSubjectNameMap(records);
        Map<Long, User> userMap = loadUserMap(records);

        List<ExamRecordSummaryVO> result = new ArrayList<>();
        for (ExamRecord record : records) {
            result.add(toRecordSummaryVO(record, subjectNameMap.get(record.getSubjectId()), userMap.get(record.getUserId())));
        }
        return result;
    }

    /**
     * 执行核心业务处理流程。
     */
    private Map<Long, String> loadSubjectNameMap(List<ExamRecord> records) {
        Set<Long> subjectIds = records.stream()
                .map(ExamRecord::getSubjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (subjectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return subjectMapper.selectBatchIds(subjectIds).stream()
                .filter(subject -> subject != null && !Integer.valueOf(1).equals(subject.getDeleted()))
                .collect(Collectors.toMap(Subject::getId, Subject::getName, (left, right) -> left));
    }

    /**
     * 执行核心业务处理流程。
     */
    private Map<Long, User> loadUserMap(List<ExamRecord> records) {
        Set<Long> userIds = records.stream()
                .map(ExamRecord::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, User> userMap = new HashMap<>();
        for (User user : userMapper.selectBatchIds(userIds)) {
            if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
                continue;
            }
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    private ExamRecordSummaryVO toRecordSummaryVO(ExamRecord record, String subjectName, User user) {
        ExamRecordSummaryVO summaryVO = new ExamRecordSummaryVO();
        summaryVO.setId(record.getId());
        summaryVO.setUserId(record.getUserId());
        summaryVO.setUserNickname(user == null ? null : user.getNickname());
        summaryVO.setUserPhone(user == null ? null : user.getPhone());
        summaryVO.setSubjectId(record.getSubjectId());
        summaryVO.setSubjectName(subjectName);
        summaryVO.setExamName(record.getExamName());
        summaryVO.setExamMode(record.getExamMode());
        summaryVO.setTotalQuestions(record.getTotalQuestions());
        summaryVO.setCorrectQuestions(record.getCorrectQuestions());
        summaryVO.setScore(record.getScore());
        summaryVO.setDuration(record.getDuration());
        summaryVO.setStartTime(record.getStartTime());
        summaryVO.setEndTime(record.getEndTime());
        summaryVO.setStatus(record.getStatus());
        return summaryVO;
    }

    private List<ExamRecordQuestionVO> buildRecordQuestionVOList(ExamRecord record) {
        List<ExamRecordQuestion> details = listRecordQuestionRelations(record.getId());
        if (details.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(details.stream().map(ExamRecordQuestion::getQuestionId).toList()).stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        List<ExamRecordQuestionVO> result = new ArrayList<>();
        for (ExamRecordQuestion detail : details) {
            Question question = questionMap.get(detail.getQuestionId());
            if (question == null) {
                continue;
            }
            ExamRecordQuestionVO questionVO = new ExamRecordQuestionVO();
            questionVO.setId(detail.getId());
            questionVO.setQuestionId(detail.getQuestionId());
            questionVO.setTitle(question.getTitle());
            questionVO.setType(question.getType());
            questionVO.setDifficulty(question.getDifficulty());
            questionVO.setImageUrls(question.getImageUrls());
            questionVO.setImageDesc(question.getImageDesc());
            questionVO.setOptions(question.getOptions());
            questionVO.setStandardAnswer(question.getAnswer());
            questionVO.setUserAnswer(detail.getUserAnswer());
            questionVO.setIsCorrect(detail.getIsCorrect());
            questionVO.setAnalysis(question.getAnalysis());
            questionVO.setAnswerTime(detail.getAnswerTime());
            result.add(questionVO);
        }
        return result;
    }

    /**
     * 构建业务处理所需数据。
     */
    private LambdaQueryWrapper<ExamRecord> buildRecordFilter(
            Long subjectId,
            Long userId,
            String keyword,
            Integer mode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(ExamRecord::getSubjectId, subjectId);
        }
        if (userId != null) {
            wrapper.eq(ExamRecord::getUserId, userId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ExamRecord::getExamName, keyword.trim());
        }
        if (mode != null) {
            wrapper.eq(ExamRecord::getExamMode, mode);
        }
        if (startDate != null) {
            wrapper.ge(ExamRecord::getStartTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(ExamRecord::getStartTime, endDate.plusDays(1).atStartOfDay());
        }
        return wrapper;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    private List<ExamPaperQuestion> listPaperQuestionRelations(Long paperId) {
        return examPaperQuestionMapper.selectList(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, paperId)
                .orderByAsc(ExamPaperQuestion::getSort)
                .orderByAsc(ExamPaperQuestion::getId));
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    private List<ExamRecordQuestion> listRecordQuestionRelations(Long recordId) {
        return examRecordQuestionMapper.selectList(new LambdaQueryWrapper<ExamRecordQuestion>()
                .eq(ExamRecordQuestion::getExamRecordId, recordId)
                .orderByAsc(ExamRecordQuestion::getId));
    }

    /**
     * 执行参数与状态校验。
     */
    private ExamPaper requirePaper(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("试卷编号不合法", 400);
        }

        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper == null || Integer.valueOf(1).equals(paper.getDeleted())) {
            throw new BusinessException("试卷不存在", 404);
        }
        return paper;
    }

    /**
     * 执行参数与状态校验。
     */
    private ExamPaper requireEnabledPaper(Long id) {
        ExamPaper paper = requirePaper(id);
        if (!isEnabled(paper.getStatus())) {
            throw new BusinessException("该试卷当前未启用", 400);
        }
        return paper;
    }

    /**
     * 执行参数与状态校验。
     */
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

    /**
     * 执行参数与状态校验。
     */
    private ExamRecord requireRecord(Long recordId) {
        if (recordId == null || recordId <= 0) {
            throw new BusinessException("考试记录编号不合法", 400);
        }

        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || Integer.valueOf(1).equals(record.getDeleted())) {
            throw new BusinessException("考试记录不存在", 404);
        }
        return record;
    }

    /**
     * 执行参数与状态校验。
     */
    private ExamRecord requireUserRecord(Long userId, Long recordId) {
        ExamRecord record = requireRecord(recordId);
        if (!Objects.equals(record.getUserId(), userId)) {
            throw new BusinessException("无权访问该考试记录", 403);
        }
        return record;
    }

    /**
     * 执行参数与状态校验。
     */
    private void ensureUniquePaperName(Long paperId, Long subjectId, String paperName) {
        String normalizedName = paperName == null ? "" : paperName.trim();
        if (normalizedName.isEmpty()) {
            throw new BusinessException("试卷名称不能为空", 400);
        }

        ExamPaper existing = examPaperMapper.selectOne(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getSubjectId, subjectId)
                .eq(ExamPaper::getPaperName, normalizedName)
                .last("limit 1"));
        if (existing == null) {
            return;
        }
        if (paperId != null && paperId.equals(existing.getId())) {
            return;
        }
        throw new BusinessException("同一学科下试卷名称不能重复", 400);
    }

    /**
     * 解析并转换输入数据。
     */
    private int resolvePaperTotalScore(Long subjectId, String examName) {
        if (subjectId == null || examName == null || examName.isBlank()) {
            return DEFAULT_TOTAL_SCORE;
        }

        ExamPaper paper = examPaperMapper.selectOne(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getSubjectId, subjectId)
                .eq(ExamPaper::getPaperName, examName.trim())
                .orderByDesc(ExamPaper::getUpdateTime)
                .last("limit 1"));
        if (paper == null || paper.getTotalScore() == null || paper.getTotalScore() <= 0) {
            return DEFAULT_TOTAL_SCORE;
        }
        return paper.getTotalScore();
    }

    /**
     * 解析并转换输入数据。
     */
    private Long resolveSubjectId(Iterable<Question> questions) {
        Long subjectId = null;
        for (Question question : questions) {
            if (subjectId == null) {
                subjectId = question.getSubjectId();
                continue;
            }
            if (!Objects.equals(subjectId, question.getSubjectId())) {
                return null;
            }
        }
        return subjectId;
    }

    /**
     * 执行核心业务处理流程。
     */
    private void assertSelfOrAdmin(Long currentUserId, Long targetUserId) {
        if (Objects.equals(currentUserId, targetUserId)) {
            return;
        }

        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || Integer.valueOf(1).equals(currentUser.getDeleted())) {
            throw new BusinessException("用户不存在", 404);
        }
        if (!Integer.valueOf(1).equals(currentUser.getIsAdmin())) {
            throw new BusinessException("无权访问其他用户的考试记录", 403);
        }
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
     * 解析并转换输入数据。
     */
    private int normalizeNonNegative(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    /**
     * 计算并返回处理结果。
     */
    private int calculateDurationMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        long seconds = Duration.between(start, end).getSeconds();
        return (int) Math.round(seconds / 60.0);
    }

    /**
     * 计算并返回处理结果。
     */
    private double calculateScore(int correctCount, int totalQuestions, int totalScore) {
        if (totalQuestions <= 0 || totalScore <= 0) {
            return 0d;
        }
        double raw = ((double) correctCount / totalQuestions) * totalScore;
        return roundTwoDecimals(raw);
    }

    /**
     * 执行核心业务处理流程。
     */
    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isEnabled(Integer status) {
        return Objects.equals(status, STATUS_ENABLED);
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isDisabled(Integer status) {
        return Objects.equals(status, STATUS_DISABLED);
    }
}
