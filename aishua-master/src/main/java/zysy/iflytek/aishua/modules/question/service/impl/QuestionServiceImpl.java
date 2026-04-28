package zysy.iflytek.aishua.modules.question.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.entity.QuestionTagRelation;
import zysy.iflytek.aishua.modules.question.entity.dto.QuestionUpsertDTO;
import zysy.iflytek.aishua.modules.question.entity.vo.QuestionTagVO;
import zysy.iflytek.aishua.modules.question.entity.vo.QuestionVO;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;
import zysy.iflytek.aishua.modules.question.mapper.QuestionTagRelationMapper;
import zysy.iflytek.aishua.modules.question.service.QuestionService;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final SubjectMapper subjectMapper;
    private final TextbookDirectoryMapper textbookDirectoryMapper;
    private final ExamTagMapper examTagMapper;

    public QuestionServiceImpl(
            QuestionMapper questionMapper,
            QuestionTagRelationMapper questionTagRelationMapper,
            SubjectMapper subjectMapper,
            TextbookDirectoryMapper textbookDirectoryMapper,
            ExamTagMapper examTagMapper
    ) {
        this.questionMapper = questionMapper;
        this.questionTagRelationMapper = questionTagRelationMapper;
        this.subjectMapper = subjectMapper;
        this.textbookDirectoryMapper = textbookDirectoryMapper;
        this.examTagMapper = examTagMapper;
    }

    @Override
    public List<QuestionVO> listQuestions(Long subjectId, Long directoryId, Integer difficulty, Integer type, String keyword) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            requireSubject(subjectId);
            queryWrapper.eq(Question::getSubjectId, subjectId);
        }
        if (directoryId != null) {
            queryWrapper.eq(Question::getDirectoryId, directoryId);
        }
        if (difficulty != null) {
            queryWrapper.eq(Question::getDifficulty, difficulty);
        }
        if (type != null) {
            queryWrapper.eq(Question::getType, type);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(Question::getTitle, trimmed)
                    .or()
                    .like(Question::getContent, trimmed)
                    .or()
                    .like(Question::getAnswer, trimmed));
        }
        queryWrapper.orderByDesc(Question::getUpdateTime).orderByDesc(Question::getId);
        List<Question> questions = questionMapper.selectList(queryWrapper);
        return buildQuestionVOs(questions);
    }

    @Override
    public QuestionVO getQuestionDetail(Long id) {
        Question question = requireQuestion(id);
        return buildQuestionVOs(List.of(question)).get(0);
    }

    @Override
    @Transactional
    public QuestionVO createQuestion(QuestionUpsertDTO questionUpsertDTO) {
        Subject subject = requireSubject(questionUpsertDTO.getSubjectId());
        TextbookDirectory directory = requireDirectory(questionUpsertDTO.getDirectoryId(), subject.getId());
        List<Long> validTagIds = requireTags(questionUpsertDTO.getTagIds(), subject.getId());
        validateOptions(questionUpsertDTO.getType(), questionUpsertDTO.getOptions());

        Question question = new Question();
        applyChanges(question, questionUpsertDTO, directory);
        question.setCorrectRate(BigDecimal.ZERO);
        question.setDoCount(0);
        questionMapper.insert(question);

        replaceTagRelations(question.getId(), validTagIds);
        syncSubjectQuestionCount(subject.getId());

        log.info("题目创建成功，questionId={}, subjectId={}", question.getId(), subject.getId());
        return getQuestionDetail(question.getId());
    }

    @Override
    @Transactional
    public QuestionVO updateQuestion(Long id, QuestionUpsertDTO questionUpsertDTO) {
        Question question = requireQuestion(id);
        Long oldSubjectId = question.getSubjectId();

        Subject subject = requireSubject(questionUpsertDTO.getSubjectId());
        TextbookDirectory directory = requireDirectory(questionUpsertDTO.getDirectoryId(), subject.getId());
        List<Long> validTagIds = requireTags(questionUpsertDTO.getTagIds(), subject.getId());
        validateOptions(questionUpsertDTO.getType(), questionUpsertDTO.getOptions());

        applyChanges(question, questionUpsertDTO, directory);
        questionMapper.updateById(question);

        replaceTagRelations(question.getId(), validTagIds);
        syncSubjectQuestionCount(subject.getId());
        if (oldSubjectId != null && !oldSubjectId.equals(subject.getId())) {
            syncSubjectQuestionCount(oldSubjectId);
        }

        log.info("题目更新成功，questionId={}", question.getId());
        return getQuestionDetail(question.getId());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = requireQuestion(id);
        questionMapper.deleteById(id);
        questionTagRelationMapper.delete(new LambdaQueryWrapper<QuestionTagRelation>()
                .eq(QuestionTagRelation::getQuestionId, id));
        syncSubjectQuestionCount(question.getSubjectId());
        log.info("题目删除成功，questionId={}", question.getId());
    }

    private void applyChanges(Question question, QuestionUpsertDTO questionUpsertDTO, TextbookDirectory directory) {
        question.setTitle(questionUpsertDTO.getTitle().trim());
        question.setContent(normalizeText(questionUpsertDTO.getContent()));
        question.setType(questionUpsertDTO.getType());
        question.setOptions(normalizeJsonText(questionUpsertDTO.getOptions()));
        question.setAnswer(questionUpsertDTO.getAnswer().trim());
        question.setAnalysis(normalizeText(questionUpsertDTO.getAnalysis()));
        question.setImageUrls(normalizeText(questionUpsertDTO.getImageUrls()));
        question.setImageDesc(normalizeText(questionUpsertDTO.getImageDesc()));
        question.setSubjectId(questionUpsertDTO.getSubjectId());
        question.setDirectoryId(directory == null ? null : directory.getId());
        question.setDifficulty(questionUpsertDTO.getDifficulty());
        if (question.getCorrectRate() == null) {
            question.setCorrectRate(BigDecimal.ZERO);
        }
        if (question.getDoCount() == null) {
            question.setDoCount(0);
        }
    }

    private List<QuestionVO> buildQuestionVOs(List<Question> questions) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> subjectIds = questions.stream()
                .map(Question::getSubjectId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Set<Long> directoryIds = questions.stream()
                .map(Question::getDirectoryId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());

        Map<Long, Subject> subjectMap = subjectIds.isEmpty()
                ? Map.of()
                : subjectMapper.selectBatchIds(subjectIds).stream()
                .filter(subject -> subject != null && !Integer.valueOf(1).equals(subject.getDeleted()))
                .collect(Collectors.toMap(Subject::getId, Function.identity(), (left, right) -> left));

        Map<Long, TextbookDirectory> directoryMap = directoryIds.isEmpty()
                ? Map.of()
                : textbookDirectoryMapper.selectBatchIds(directoryIds).stream()
                .filter(directory -> directory != null && !Integer.valueOf(1).equals(directory.getDeleted()))
                .collect(Collectors.toMap(TextbookDirectory::getId, Function.identity(), (left, right) -> left));

        List<QuestionTagRelation> relations = questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                .in(QuestionTagRelation::getQuestionId, questionIds));
        Set<Long> tagIds = relations.stream().map(QuestionTagRelation::getTagId).collect(Collectors.toSet());
        Map<Long, ExamTag> tagMap = tagIds.isEmpty()
                ? Map.of()
                : examTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> tag != null)
                .collect(Collectors.toMap(ExamTag::getId, Function.identity(), (left, right) -> left));

        Map<Long, List<QuestionTagVO>> questionTagMap = relations.stream()
                .collect(Collectors.groupingBy(
                        QuestionTagRelation::getQuestionId,
                        Collectors.mapping(relation -> {
                            ExamTag tag = tagMap.get(relation.getTagId());
                            if (tag == null) {
                                return null;
                            }
                            QuestionTagVO questionTagVO = new QuestionTagVO();
                            questionTagVO.setId(tag.getId());
                            questionTagVO.setName(tag.getName());
                            return questionTagVO;
                        }, Collectors.toCollection(ArrayList::new))
                ));

        for (List<QuestionTagVO> tags : questionTagMap.values()) {
            tags.removeIf(tag -> tag == null);
            tags.sort((left, right) -> Long.compare(left.getId(), right.getId()));
        }

        List<QuestionVO> result = new ArrayList<>();
        for (Question question : questions) {
            List<QuestionTagVO> tags = questionTagMap.getOrDefault(question.getId(), Collections.emptyList());
            List<Long> questionTagIds = tags.stream().map(QuestionTagVO::getId).toList();
            Subject subject = subjectMap.get(question.getSubjectId());
            TextbookDirectory directory = directoryMap.get(question.getDirectoryId());

            QuestionVO questionVO = new QuestionVO();
            questionVO.setId(question.getId());
            questionVO.setTitle(question.getTitle());
            questionVO.setContent(question.getContent());
            questionVO.setType(question.getType());
            questionVO.setOptions(question.getOptions());
            questionVO.setAnswer(question.getAnswer());
            questionVO.setAnalysis(question.getAnalysis());
            questionVO.setImageUrls(question.getImageUrls());
            questionVO.setImageDesc(question.getImageDesc());
            questionVO.setDirectoryId(question.getDirectoryId());
            questionVO.setDirectoryName(directory == null ? null : directory.getName());
            questionVO.setSubjectId(question.getSubjectId());
            questionVO.setSubjectName(subject == null ? null : subject.getName());
            questionVO.setDifficulty(question.getDifficulty());
            questionVO.setCorrectRate(question.getCorrectRate());
            questionVO.setDoCount(question.getDoCount());
            questionVO.setTagIds(questionTagIds);
            questionVO.setTags(tags);
            questionVO.setCreateTime(question.getCreateTime());
            questionVO.setUpdateTime(question.getUpdateTime());
            result.add(questionVO);
        }
        return result;
    }

    private void replaceTagRelations(Long questionId, List<Long> tagIds) {
        questionTagRelationMapper.delete(new LambdaQueryWrapper<QuestionTagRelation>()
                .eq(QuestionTagRelation::getQuestionId, questionId));
        if (tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            QuestionTagRelation relation = new QuestionTagRelation();
            relation.setQuestionId(questionId);
            relation.setTagId(tagId);
            questionTagRelationMapper.insert(relation);
        }
    }

    private List<Long> requireTags(List<Long> tagIds, Long subjectId) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> uniqueTagIds = new ArrayList<>(new LinkedHashSet<>(tagIds));
        if (uniqueTagIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException("考点标签ID不合法", 400);
        }

        List<ExamTag> tags = examTagMapper.selectBatchIds(uniqueTagIds);
        if (tags.size() != uniqueTagIds.size()) {
            throw new BusinessException("存在无效的考点标签", 400);
        }
        for (ExamTag tag : tags) {
            if (!subjectId.equals(tag.getSubjectId())) {
                throw new BusinessException("题目考点必须属于同一学科", 400);
            }
        }
        return uniqueTagIds;
    }

    private TextbookDirectory requireDirectory(Long directoryId, Long subjectId) {
        if (directoryId == null) {
            return null;
        }
        if (directoryId <= 0) {
            throw new BusinessException("所属目录不合法", 400);
        }
        TextbookDirectory directory = textbookDirectoryMapper.selectById(directoryId);
        if (directory == null || Integer.valueOf(1).equals(directory.getDeleted())) {
            throw new BusinessException("所属目录不存在", 404);
        }
        if (!subjectId.equals(directory.getSubjectId())) {
            throw new BusinessException("题目目录必须与学科一致", 400);
        }
        return directory;
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

    private Question requireQuestion(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("题目ID不合法", 400);
        }
        Question question = questionMapper.selectById(id);
        if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
            throw new BusinessException("题目不存在", 404);
        }
        return question;
    }

    private void validateOptions(Integer type, String options) {
        String normalized = normalizeJsonText(options);
        if (Integer.valueOf(1).equals(type) || Integer.valueOf(2).equals(type)) {
            if (normalized == null) {
                throw new BusinessException("单选/多选题必须填写选项JSON", 400);
            }
            JSONArray jsonArray = parseOptions(normalized);
            if (jsonArray.size() < 2) {
                throw new BusinessException("单选/多选题至少需要 2 个选项", 400);
            }
            return;
        }

        if (normalized != null) {
            parseOptions(normalized);
        }
    }

    private JSONArray parseOptions(String options) {
        try {
            return JSON.parseArray(options);
        } catch (Exception exception) {
            throw new BusinessException("选项JSON格式错误，需为数组结构", 400);
        }
    }

    private void syncSubjectQuestionCount(Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            return;
        }
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            return;
        }
        Long count = questionMapper.selectCount(new LambdaQueryWrapper<Question>()
                .eq(Question::getSubjectId, subjectId));
        subject.setQuestionCount(count == null ? 0 : count.intValue());
        subjectMapper.updateById(subject);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeJsonText(String value) {
        return normalizeText(value);
    }
}
