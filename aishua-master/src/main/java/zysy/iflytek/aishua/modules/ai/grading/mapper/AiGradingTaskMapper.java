package zysy.iflytek.aishua.modules.ai.grading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import zysy.iflytek.aishua.modules.ai.grading.entity.AiGradingTask;

import java.time.LocalDateTime;

/**
 * AI 评分任务数据访问映射。
 */
@Mapper
public interface AiGradingTaskMapper extends BaseMapper<AiGradingTask> {
    @Update("""
            UPDATE ai_grading_task
            SET status = 'PROCESSING',
                locked_at = #{lockedAt},
                started_at = COALESCE(started_at, #{lockedAt}),
                update_time = #{lockedAt}
            WHERE id = #{id}
              AND status = 'PENDING'
              AND deleted = 0
            """)
    int markProcessing(@Param("id") Long id, @Param("lockedAt") LocalDateTime lockedAt);
}
