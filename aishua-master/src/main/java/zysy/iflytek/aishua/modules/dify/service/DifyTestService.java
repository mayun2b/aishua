package zysy.iflytek.aishua.modules.dify.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.dify.client.DifyClient;

import java.util.Map;

/**
 * 流程编排测试服务，负责入参校验与客户端调用。
 */
@Service
public class DifyTestService {

    private final DifyClient difyClient;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public DifyTestService(DifyClient difyClient) {
        this.difyClient = difyClient;
    }

    /**
     * 定义业务能力接口。
     */
    public Map<String, Object> testDify(String query, String studentId) {
        if (!StringUtils.hasText(query)) {
            throw new BusinessException("查询内容不能为空", 400);
        }
        return difyClient.runWorkflow(query.trim(), studentId);
    }
}
