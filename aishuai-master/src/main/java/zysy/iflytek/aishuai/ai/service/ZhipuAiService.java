package zysy.iflytek.aishuai.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
 * 智谱AI服务类（用于向量生成）
 */
@Service
@Slf4j
public class ZhipuAiService {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 生成文本向量
     * @param text 文本内容
     * @return 向量数组
     */
    public float[] generateEmbedding(String text) {
        try {
            AiConfig.ZhipuConfig zhipuConfig = aiConfig.getZhipu();
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", zhipuConfig.getModel());
            
            List<JSONObject> input = new ArrayList<>();
            JSONObject inputItem = new JSONObject();
            inputItem.put("text", text);
            input.add(inputItem);
            requestBody.put("input", input);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + zhipuConfig.getApiKey());

            HttpEntity<String> request = new HttpEntity<>(requestBody.toJSONString(), headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    zhipuConfig.getEmbeddingApi(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONObject data = responseBody.getJSONObject("data");
                if (data != null && data.containsKey("embedding")) {
                    JSONArray embeddingArray = data.getJSONArray("embedding");
                    float[] embedding = new float[embeddingArray.size()];
                    for (int i = 0; i < embeddingArray.size(); i++) {
                        embedding[i] = embeddingArray.getFloatValue(i);
                    }
                    return embedding;
                }
            }

            log.error("智谱AI向量生成失败: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("智谱AI向量生成异常", e);
            return null;
        }
    }
}
