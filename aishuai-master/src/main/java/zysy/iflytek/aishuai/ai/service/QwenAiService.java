package zysy.iflytek.aishuai.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import zysy.iflytek.aishuai.ai.config.AiConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云qwen3.5-flash服务类（用于答疑讲解）
 */
@Service
@Slf4j
public class QwenAiService {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 生成答疑讲解
     * @param question 问题内容
     * @param context 上下文信息（用户学习情况等）
     * @return 答疑内容
     */
    public String generateExplanation(String question, String context) {
        try {
            AiConfig.QwenConfig qwenConfig = aiConfig.getQwen();
            
            // 构建个性化prompt
            String prompt = buildPrompt(question, context);
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", qwenConfig.getModel());
            
            JSONObject input = new JSONObject();
            input.put("prompt", prompt);
            input.put("max_tokens", 1000);
            input.put("temperature", 0.7);
            input.put("top_p", 0.9);
            
            requestBody.put("input", input);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + qwenConfig.getApiKey());

            HttpEntity<String> request = new HttpEntity<>(requestBody.toJSONString(), headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    qwenConfig.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONObject output = responseBody.getJSONObject("output");
                if (output != null && output.containsKey("text")) {
                    return output.getString("text");
                }
            }

            log.error("阿里云qwen3.5-flash答疑生成失败: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("阿里云qwen3.5-flash答疑生成异常", e);
            return null;
        }
    }

    /**
     * 构建个性化prompt
     * @param question 问题内容
     * @param context 上下文信息
     * @return 构建好的prompt
     */
    private String buildPrompt(String question, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一位专业的教育专家，擅长根据用户的学习情况提供个性化的答疑讲解。\n\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("用户学习情况：\n");
            prompt.append(context);
            prompt.append("\n\n");
        }
        
        prompt.append("请针对以下问题提供详细、清晰的解答：\n");
        prompt.append(question);
        prompt.append("\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 解释要通俗易懂，适合学生理解\n");
        prompt.append("2. 提供详细的解题思路和步骤\n");
        prompt.append("3. 指出关键知识点和易错点\n");
        prompt.append("4. 如果有多种解法，尽量提供多种思路\n");
        prompt.append("5. 语言要友好、鼓励性的");
        
        return prompt.toString();
    }
}
