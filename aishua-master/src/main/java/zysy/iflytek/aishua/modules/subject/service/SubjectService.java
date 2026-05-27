package zysy.iflytek.aishua.modules.subject.service;

import zysy.iflytek.aishua.modules.subject.entity.dto.SubjectUpsertDTO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectVO;

import java.util.List;

/**
 * 学科服务接口，定义该领域对外能力契约。
 */
public interface SubjectService {
    List<SubjectVO> listAdminSubjects(String keyword, Integer enabled);

    SubjectVO createSubject(SubjectUpsertDTO subjectUpsertDTO);

    SubjectVO updateSubject(Long id, SubjectUpsertDTO subjectUpsertDTO);

    SubjectVO updateEnabledStatus(Long id, Integer enabled);

    void deleteSubject(Long id);
}
