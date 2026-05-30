package zysy.iflytek.aishua.modules.dify.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyTestRequest;
import zysy.iflytek.aishua.modules.dify.service.DifyTestService;

import java.util.Map;

/**
 * Dify 联调入口控制器。
 * 提供最小测试接口，便于验证后端到工作流的打通情况。
 */
@RestController
@RequestMapping("/api/ai")
public class DifyTestController {

    private final DifyTestService difyTestService;

    /**
     * 注入服务依赖。
     */
    public DifyTestController(DifyTestService difyTestService) {
        this.difyTestService = difyTestService;
    }

    /**
     * 触发一次 Dify 工作流调用。
     * query 必填，其他业务字段可选；登录态信息由鉴权拦截器注入。
     */
    @PostMapping("/dify-test")
    public Result<Map<String, Object>> testDify(@Valid @RequestBody DifyTestRequest request) {
        return Result.success(difyTestService.testDify(request));
    }
}
