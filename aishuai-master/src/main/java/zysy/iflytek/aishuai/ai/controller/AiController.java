package zysy.iflytek.aishuai.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.ai.service.QwenAiService;
import zysy.iflytek.aishuai.ai.service.ZhipuAiService;

import java.util.HashMap;
import java.util.Map;

/**
 * AI服务控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private ZhipuAiService zhipuAiService;

    @Autowired
    private QwenAiService qwenAiService;

    /**
     * 生成文本向量（使用智谱AI）
     * @param text 文本内容
     * @return 向量结果
     */
    @PostMapping("/embedding")
    public Map<String, Object> generateEmbedding(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String text = request.get("text");
            if (text == null || text.isEmpty()) {
                result.put("success", false);
                result.put("message", "文本内容不能为空");
                return result;
            }
            
            float[] embedding = zhipuAiService.generateEmbedding(text);
            if (embedding != null) {
                result.put("success", true);
                result.put("embedding", embedding);
                result.put("dimension", embedding.length);
            } else {
                result.put("success", false);
                result.put("message", "向量生成失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "向量生成异常");
        }
        
        return result;
    }

    /**
     * 生成答疑讲解（使用阿里云qwen3.5-flash）
     * @param request 请求参数
     * @return 答疑结果
     */
    @PostMapping("/explain")
    public Map<String, Object> generateExplanation(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String question = request.get("question");
            String context = request.get("context");
            
            if (question == null || question.isEmpty()) {
                result.put("success", false);
                result.put("message", "问题内容不能为空");
                return result;
            }
            
            String explanation = qwenAiService.generateExplanation(question, context);
            if (explanation != null) {
                result.put("success", true);
                result.put("explanation", explanation);
            } else {
                result.put("success", false);
                result.put("message", "答疑生成失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "答疑生成异常");
        }
        
        return result;
    }
}
