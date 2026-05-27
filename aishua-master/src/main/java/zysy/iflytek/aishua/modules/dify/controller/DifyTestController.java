package zysy.iflytek.aishua.modules.dify.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyTestRequest;
import zysy.iflytek.aishua.modules.dify.service.DifyTestService;

import java.util.Map;

/**
 * 流程编排调试入口。
 */
@RestController
@RequestMapping("/api/ai")
public class DifyTestController {

    private final DifyTestService difyTestService;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public DifyTestController(DifyTestService difyTestService) {
        this.difyTestService = difyTestService;
    }

    /**
     * 触发一次流程编排工作流测试调用。
     */
    @PostMapping("/dify-test")
    public Map<String, Object> testDify(@RequestBody DifyTestRequest request) {
        return difyTestService.testDify(
                request.getQuery(),
                request.getStudentId()
        );
    }
}
