package zysy.iflytek.aishua.modules.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import zysy.iflytek.aishua.modules.ai.entity.dto.VideoSearchRequest;
import zysy.iflytek.aishua.modules.ai.entity.vo.VideoSearchResultVO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VideoSearchService {

    private static final Logger log = LoggerFactory.getLogger(VideoSearchService.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String[] SEARCH_TEMPLATES = {
            "{grade} {subject} {knowledgePoint} 知识点讲解",
            "{knowledgePoint} {subject} 题型解题技巧",
            "{knowledgePoint} 专题复习 考点总结",
            "{knowledgePoint} 真题解析 例题精讲",
            "{knowledgePoint} 学霸笔记 知识点梳理"
    };
    private static final String BILIBILI_SEARCH_URL = "https://search.bilibili.com/all?keyword={keyword}&order=click";
    private static final String REDIS_KEY_PREFIX = "video:search:";
    private static final String SUBJECT_MAPPING = "语文:chn,数学:math,英语:eng,物理:phys,化学:chem,生物:bio,政治:pol,历史:hist,地理:geo,科学:sci,道德与法治:moral";
    private static final String GRADE_MAPPING = "一年级:g1,二年级:g2,三年级:g3,四年级:g4,五年级:g5,六年级:g6,初一:g7,初二:g8,初三:g9,高一:g10,高二:g11,高三:g12";
    private static final int MAX_RESULT = 5;
    private static final long EMPTY_EXPIRE_SECONDS = 3600;

    public List<VideoSearchResultVO> searchVideos(VideoSearchRequest request) {
        String knowledgePoint = request.getKnowledgePoint() != null ? request.getKnowledgePoint().trim() : "";
        String subject = request.getSubject() != null ? request.getSubject().trim() : "all";
        String grade = request.getGrade() != null ? request.getGrade().trim() : "all";
        int limit = request.getLimit() != null ? Math.min(request.getLimit(), MAX_RESULT) : 3;

        if (knowledgePoint.isEmpty()) {
            return new ArrayList<>();
        }

        String cacheKey = buildCacheKey(subject, grade, knowledgePoint);
        log.info("视频搜索 - 缓存键: {}", cacheKey);

        List<VideoSearchResultVO> cacheResult = getCache(cacheKey);
        if (cacheResult != null) {
            log.info("视频搜索 - 缓存命中！返回 {} 条结果", cacheResult.size());
            return cacheResult.stream().limit(limit).toList();
        }

        log.info("视频搜索 - 缓存未命中，构造新搜索链接");
        List<VideoSearchResultVO> result = buildSearchResult(subject, grade, knowledgePoint);
        saveCache(cacheKey, result);
        log.info("视频搜索 - 结果已缓存，共 {} 条", result.size());
        return result.stream().limit(limit).toList();
    }

    private String buildCacheKey(String subject, String grade, String knowledgePoint) {
        String subjectCode = parseMapping(SUBJECT_MAPPING, subject);
        String gradeCode = parseMapping(GRADE_MAPPING, grade);
        String knowledgeHash = DigestUtils.md5DigestAsHex(knowledgePoint.getBytes(StandardCharsets.UTF_8)).substring(0, 8);

        return REDIS_KEY_PREFIX + String.format("%s:%s:%s", subjectCode, gradeCode, knowledgeHash);
    }

    private String parseMapping(String mappingStr, String key) {
        if (key == null || key.isEmpty()) {
            return "all";
        }
        String[] pairs = mappingStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2 && kv[0].trim().equals(key.trim())) {
                return kv[1].trim();
            }
        }
        return key;
    }

    private List<VideoSearchResultVO> getCache(String cacheKey) {
        try {
            String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cacheValue == null) {
                return null;
            }
            if ("EMPTY".equals(cacheValue)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(cacheValue, new TypeReference<List<VideoSearchResultVO>>() {});
        } catch (Exception exception) {
            return null;
        }
    }

    private void saveCache(String cacheKey, List<VideoSearchResultVO> result) {
        try {
            String cacheValue;
            long expireSeconds;

            if (result.isEmpty()) {
                cacheValue = "EMPTY";
                expireSeconds = EMPTY_EXPIRE_SECONDS;
            } else {
                cacheValue = objectMapper.writeValueAsString(result);
                expireSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                        LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
            }

            stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, expireSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException exception) {
            // ignore
        }
    }

    private List<VideoSearchResultVO> buildSearchResult(String subject, String grade, String knowledgePoint) {
        List<VideoSearchResultVO> result = new ArrayList<>();

        for (String template : SEARCH_TEMPLATES) {
            String keyword = template
                    .replace("{grade}", "all".equals(grade) ? "" : grade)
                    .replace("{subject}", "all".equals(subject) ? "" : subject)
                    .replace("{knowledgePoint}", knowledgePoint)
                    .trim()
                    .replaceAll("\\s+", " ");

            try {
                String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
                String url = BILIBILI_SEARCH_URL.replace("{keyword}", encodedKeyword);
                VideoSearchResultVO vo = new VideoSearchResultVO();
                vo.setKeyword(keyword);
                vo.setTitle(keyword + " - B站教学视频");
                vo.setUrl(url);
                vo.setSource("bilibili");
                vo.setThumbnail("");
                result.add(vo);
            } catch (Exception exception) {
                continue;
            }

            if (result.size() >= MAX_RESULT) {
                break;
            }
        }

        if (result.isEmpty()) {
            try {
                String defaultKeyword = knowledgePoint + " 教学讲解";
                String encodedKeyword = URLEncoder.encode(defaultKeyword, StandardCharsets.UTF_8.name());
                String url = BILIBILI_SEARCH_URL.replace("{keyword}", encodedKeyword);
                VideoSearchResultVO vo = new VideoSearchResultVO();
                vo.setKeyword(defaultKeyword);
                vo.setTitle(defaultKeyword + " - B站教学视频");
                vo.setUrl(url);
                vo.setSource("bilibili");
                result.add(vo);
            } catch (Exception ignored) {}
        }

        return result;
    }
}
