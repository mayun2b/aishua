package zysy.iflytek.aishua.modules.ai.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.config.properties.QwenAiProperties;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class QwenChatClient {
    private static final String DEFAULT_CHAT_MODEL = "qwen3.5-flash";

    private final RestTemplate restTemplate;
    private final QwenAiProperties qwenAiProperties;

    public QwenChatClient(RestTemplate restTemplate, QwenAiProperties qwenAiProperties) {
        this.restTemplate = restTemplate;
        this.qwenAiProperties = qwenAiProperties;
    }

    public ChatResult chat(List<ChatMessage> messages, boolean jsonResponse) {
        return chat(messages, jsonResponse, null);
    }

    public ChatResult chat(List<ChatMessage> messages, boolean jsonResponse, Integer maxTokens) {
        String apiKey = qwenAiProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("AI 服务未配置 API Key", 500);
        }
        if (!StringUtils.hasText(qwenAiProperties.getChatApi())) {
            throw new BusinessException("AI 服务未配置聊天接口地址", 500);
        }

        JSONObject payload = new JSONObject();
        String model = resolveChatModel();
        payload.put("model", model);
        payload.put("stream", false);
        if (model.toLowerCase().startsWith("qwen")) {
            // Disable thinking mode to reduce latency.
            payload.put("enable_thinking", false);
        }

        List<ChatMessage> requestMessages = new ArrayList<>(messages);
        if (jsonResponse) {
            boolean hasJsonHint = requestMessages.stream()
                    .anyMatch(message -> StringUtils.hasText(message.content())
                            && message.content().toLowerCase().contains("json"));
            if (!hasJsonHint) {
                requestMessages.add(0, new ChatMessage("system", "Return a valid JSON object only."));
            }
        }

        JSONArray messageArray = new JSONArray();
        for (ChatMessage message : requestMessages) {
            JSONObject item = new JSONObject();
            item.put("role", message.role());
            item.put("content", message.content());
            messageArray.add(item);
        }
        payload.put("messages", messageArray);

        if (jsonResponse) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            payload.put("response_format", responseFormat);
        }
        if (maxTokens != null && maxTokens > 0) {
            payload.put("max_tokens", maxTokens);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<String> entity = new HttpEntity<>(payload.toJSONString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(qwenAiProperties.getChatApi(), entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw new BusinessException("AI 服务响应异常", 502);
            }

            JSONObject root = JSON.parseObject(response.getBody());
            String content = extractAssistantContent(root);
            Usage usage = extractUsage(root);
            return new ChatResult(content, usage, response.getBody());
        } catch (HttpStatusCodeException exception) {
            String providerMessage = extractProviderErrorMessage(exception.getResponseBodyAsString());
            log.warn("调用 AI 聊天接口失败: status={}, providerMessage={}",
                    exception.getStatusCode().value(), providerMessage);
            throw new BusinessException("调用 AI 服务失败: " + providerMessage, 502);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("调用 AI 聊天接口失败", exception);
            throw new BusinessException("调用 AI 服务失败，请稍后重试", 502);
        }
    }

    private String resolveChatModel() {
        String configured = qwenAiProperties.getChatModel();
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        return DEFAULT_CHAT_MODEL;
    }

    private String extractAssistantContent(JSONObject root) {
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("AI 服务返回内容为空", 502);
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (firstChoice == null) {
            throw new BusinessException("AI 服务返回内容为空", 502);
        }
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            throw new BusinessException("AI 服务返回内容为空", 502);
        }

        Object contentObject = message.get("content");
        if (contentObject == null) {
            throw new BusinessException("AI 服务返回内容为空", 502);
        }
        if (contentObject instanceof String content) {
            return content;
        }
        if (contentObject instanceof JSONArray array) {
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item != null && StringUtils.hasText(item.getString("text"))) {
                    parts.add(item.getString("text"));
                }
            }
            if (!parts.isEmpty()) {
                return String.join("\n", parts);
            }
        }
        return String.valueOf(contentObject);
    }

    private Usage extractUsage(JSONObject root) {
        JSONObject usageObject = root.getJSONObject("usage");
        if (usageObject == null) {
            return new Usage(0, 0, 0);
        }
        int promptTokens = usageObject.getIntValue("prompt_tokens");
        int completionTokens = usageObject.getIntValue("completion_tokens");
        int totalTokens = usageObject.getIntValue("total_tokens");
        return new Usage(promptTokens, completionTokens, totalTokens);
    }

    private String extractProviderErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "模型服务无响应体";
        }
        try {
            JSONObject root = JSON.parseObject(body);
            if (root == null) {
                return body;
            }

            JSONObject error = root.getJSONObject("error");
            if (error != null && StringUtils.hasText(error.getString("message"))) {
                return error.getString("message");
            }
            if (StringUtils.hasText(root.getString("message"))) {
                return root.getString("message");
            }
            return body;
        } catch (Exception ignored) {
            return body;
        }
    }

    public record ChatMessage(String role, String content) {
    }

    public record Usage(int promptTokens, int completionTokens, int totalTokens) {
    }

    public record ChatResult(String content, Usage usage, String rawResponse) {
    }
}
