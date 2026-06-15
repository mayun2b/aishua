package zysy.iflytek.aishua.modules.ai.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.ai.entity.dto.VideoSearchRequest;
import zysy.iflytek.aishua.modules.ai.entity.vo.VideoSearchResultVO;
import zysy.iflytek.aishua.modules.ai.service.VideoSearchService;

import java.util.List;

/**
 * 视频搜索控制器。
 */
@RestController
@RequestMapping("/api/ai")
public class VideoSearchController {

    private final VideoSearchService videoSearchService;

    public VideoSearchController(VideoSearchService videoSearchService) {
        this.videoSearchService = videoSearchService;
    }

    /**
     * 搜索薄弱知识点相关的教学视频。
     */
    @PostMapping("/video-search")
    public Result<List<VideoSearchResultVO>> searchVideos(@Valid @RequestBody VideoSearchRequest request) {
        List<VideoSearchResultVO> result = videoSearchService.searchVideos(request);
        return Result.success(result);
    }
}
