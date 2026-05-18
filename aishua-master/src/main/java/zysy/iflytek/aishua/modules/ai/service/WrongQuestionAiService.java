package zysy.iflytek.aishua.modules.ai.service;

import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiAnalysisRequestDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiAnalysisVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatSessionVO;

import java.util.List;

public interface WrongQuestionAiService {
    WrongQuestionAiAnalysisVO analyzeWrongQuestion(Long userId, Long wrongQuestionId, WrongQuestionAiAnalysisRequestDTO requestDTO);

    WrongQuestionAiAnalysisVO getLatestAnalysis(Long userId, Long wrongQuestionId);

    WrongQuestionAiChatSessionVO getLatestChatSession(Long userId, Long wrongQuestionId);

    WrongQuestionAiChatSessionVO createChatSession(Long userId, Long wrongQuestionId, WrongQuestionAiCreateSessionDTO requestDTO);

    List<WrongQuestionAiChatMessageVO> listChatMessages(Long userId, Long wrongQuestionId, Long sessionId);

    WrongQuestionAiChatMessageVO sendChatMessage(Long userId, Long wrongQuestionId, Long sessionId, WrongQuestionAiSendMessageDTO requestDTO);

    WrongQuestionAiChatSessionVO closeChatSession(Long userId, Long wrongQuestionId, Long sessionId);
}
