package zysy.iflytek.aishua.modules.ai.service;

import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.ai.entity.vo.LearningAnalysisReportVO;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyTestRequest;

import java.util.Map;

public interface LearningAnalysisReportService {
    LearningAnalysisReportVO saveDifyResult(Long userId, DifyTestRequest request, Map<String, Object> difyResponse);

    LearningAnalysisReportVO getById(Long userId, Long reportId);

    LearningAnalysisReportVO getLatest(Long userId, Long subjectId);

    PageResult<LearningAnalysisReportVO> listHistory(Long userId, Long subjectId, Integer pageNum, Integer pageSize);
}
