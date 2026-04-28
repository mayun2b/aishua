package zysy.iflytek.aishua.modules.subject.service;

import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;
import zysy.iflytek.aishua.modules.subject.entity.vo.SubjectCatalogVO;

import java.util.List;

public interface UserSubjectService {
    List<SubjectCatalogVO> listSubjectCatalog(Long userId);

    List<MySubjectVO> listMySubjects(Long userId);

    MySubjectVO joinSubject(Long userId, Long subjectId);
}
