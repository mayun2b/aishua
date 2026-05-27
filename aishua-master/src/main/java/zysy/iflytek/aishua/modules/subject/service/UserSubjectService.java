package zysy.iflytek.aishua.modules.subject.service;

import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectCatalogVO;

import java.util.List;

/**
 * 学科服务接口，定义该领域对外能力契约。
 */
public interface UserSubjectService {
    List<SubjectCatalogVO> listSubjectCatalog(Long userId);

    List<MySubjectVO> listMySubjects(Long userId);

    MySubjectVO joinSubject(Long userId, Long subjectId);
}
