package zysy.iflytek.aishua.modules.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.ai.entity.WrongQuestionAiChatSession;

/**
 * 智能问答数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface WrongQuestionAiChatSessionMapper extends BaseMapper<WrongQuestionAiChatSession> {
}
