package zysy.iflytek.aishua.modules.ai.service;

import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatSessionVO;

import java.util.List;
import java.util.function.Consumer;

public interface PracticeQuestionAiService {
    PracticeQuestionAiChatSessionVO getLatestChatSession(Long userId, Long practiceSessionId, Long questionId);

    PracticeQuestionAiChatSessionVO createChatSession(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            PracticeQuestionAiCreateSessionDTO requestDTO
    );

    List<PracticeQuestionAiChatMessageVO> listChatMessages(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId
    );

    PracticeQuestionAiChatMessageVO sendChatMessage(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId,
            PracticeQuestionAiSendMessageDTO requestDTO
    );

    PracticeQuestionAiChatMessageVO streamChatMessage(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId,
            PracticeQuestionAiSendMessageDTO requestDTO,
            Consumer<String> chunkConsumer
    );

    PracticeQuestionAiChatSessionVO closeChatSession(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId
    );
}
