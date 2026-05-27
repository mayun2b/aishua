package zysy.iflytek.aishua.modules.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import zysy.iflytek.aishua.modules.practice.entity.PracticeSession;

import java.time.LocalDateTime;

/**
 * 练习数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface PracticeSessionMapper extends BaseMapper<PracticeSession> {
    @Update("""
            UPDATE practice_session
            SET draft_version = #{nextVersion},
                answered_count = #{answeredCount},
                total_time_cost = #{totalTimeCost}
            WHERE id = #{sessionId}
              AND user_id = #{userId}
              AND status = 1
              AND COALESCE(draft_version, 0) = #{expectedVersion}
            """)
    int updateDraftMetaByVersion(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("expectedVersion") Integer expectedVersion,
            @Param("nextVersion") Integer nextVersion,
            @Param("answeredCount") Integer answeredCount,
            @Param("totalTimeCost") Integer totalTimeCost
    );

    @Update("""
            UPDATE practice_session
            SET answered_count = #{answeredCount},
                correct_count = #{correctCount},
                wrong_count = #{wrongCount},
                total_time_cost = #{totalTimeCost},
                status = #{status},
                ended_at = #{endedAt},
                draft_version = #{nextVersion}
            WHERE id = #{sessionId}
              AND user_id = #{userId}
              AND status = 1
              AND COALESCE(draft_version, 0) = #{expectedVersion}
            """)
    int finishSessionByVersion(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("expectedVersion") Integer expectedVersion,
            @Param("nextVersion") Integer nextVersion,
            @Param("answeredCount") Integer answeredCount,
            @Param("correctCount") Integer correctCount,
            @Param("wrongCount") Integer wrongCount,
            @Param("totalTimeCost") Integer totalTimeCost,
            @Param("status") Integer status,
            @Param("endedAt") LocalDateTime endedAt
    );
}
