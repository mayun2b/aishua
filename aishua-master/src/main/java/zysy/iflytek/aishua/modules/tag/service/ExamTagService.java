package zysy.iflytek.aishua.modules.tag.service;

import zysy.iflytek.aishua.modules.tag.entity.dto.ExamTagUpsertDTO;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;

import java.util.List;

public interface ExamTagService {
    List<ExamTagVO> listTags(Long subjectId, String keyword);

    ExamTagVO createTag(ExamTagUpsertDTO examTagUpsertDTO);

    ExamTagVO updateTag(Long id, ExamTagUpsertDTO examTagUpsertDTO);

    void deleteTag(Long id);
}
