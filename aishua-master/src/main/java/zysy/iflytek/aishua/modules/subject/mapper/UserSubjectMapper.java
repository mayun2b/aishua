package zysy.iflytek.aishua.modules.subject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import zysy.iflytek.aishua.modules.subject.entity.UserSubject;
import zysy.iflytek.aishua.modules.subject.entity.vo.MySubjectVO;

import java.util.List;

@Mapper
public interface UserSubjectMapper extends BaseMapper<UserSubject> {
    @Select("""
            SELECT us.subject_id
            FROM user_subject us
            WHERE us.user_id = #{userId}
              AND us.deleted = 0
            """)
    List<Long> selectJoinedSubjectIds(@Param("userId") Long userId);

    @Select("""
            SELECT us.id AS user_subject_id,
                   us.subject_id,
                   us.status,
                   us.joined_at,
                   us.last_practice_at,
                   s.name,
                   s.code,
                   s.description,
                   s.icon,
                   s.question_count,
                   s.sort,
                   s.is_enabled
            FROM user_subject us
                     INNER JOIN subject s ON s.id = us.subject_id AND s.deleted = 0
            WHERE us.user_id = #{userId}
              AND us.deleted = 0
            ORDER BY us.sort ASC, us.joined_at DESC, us.id DESC
            """)
    List<MySubjectVO> selectMySubjects(@Param("userId") Long userId);

    @Select("""
            SELECT us.id AS user_subject_id,
                   us.subject_id,
                   us.status,
                   us.joined_at,
                   us.last_practice_at,
                   s.name,
                   s.code,
                   s.description,
                   s.icon,
                   s.question_count,
                   s.sort,
                   s.is_enabled
            FROM user_subject us
                     INNER JOIN subject s ON s.id = us.subject_id AND s.deleted = 0
            WHERE us.user_id = #{userId}
              AND us.subject_id = #{subjectId}
              AND us.deleted = 0
            LIMIT 1
            """)
    MySubjectVO selectMySubjectBySubjectId(@Param("userId") Long userId, @Param("subjectId") Long subjectId);

    @Insert("""
            INSERT INTO user_subject (user_id, subject_id, joined_at, status, sort, deleted)
            VALUES (#{userId}, #{subjectId}, NOW(), 1, 0, 0)
            ON DUPLICATE KEY UPDATE
                deleted = 0,
                status = 1,
                update_time = CURRENT_TIMESTAMP
            """)
    int upsertJoin(@Param("userId") Long userId, @Param("subjectId") Long subjectId);
}
