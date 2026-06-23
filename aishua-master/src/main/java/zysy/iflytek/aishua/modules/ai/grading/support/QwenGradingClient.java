package zysy.iflytek.aishua.modules.ai.grading.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.config.properties.QwenAiProperties;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * 通义千问多模态评分客户端。
 */
@Slf4j
@Component
public class QwenGradingClient {
    private static final String DEFAULT_GRADING_MODEL = "qwen-vl-max";
    private static final int DEFAULT_GRADING_MAX_TOKENS = 800;
    private static final Duration DEFAULT_GRADING_TIMEOUT = Duration.ofSeconds(90);

    private final QwenAiProperties qwenAiProperties;
    private final HttpClient httpClient;

    public QwenGradingClient(QwenAiProperties qwenAiProperties) {
        this.qwenAiProperties = qwenAiProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public GradingRawResult grade(String systemPrompt, String userPrompt, List<ImageInput> images) {
        String apiKey = requireApiKey();
        String chatApi = requireChatApi();
        JSONObject payload = buildPayload(systemPrompt, userPrompt, images);

        HttpRequest request = HttpRequest.newBuilder(URI.create(chatApi))
                .timeout(resolveTimeout())
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toJSONString(), StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String providerMessage = extractProviderErrorMessage(response.body());
                log.warn("调用 AI 评分接口失败: status={}, providerMessage={}", response.statusCode(), providerMessage);
                throw new BusinessException("调用 AI 评分服务失败：" + providerMessage, 502);
            }
            if (!StringUtils.hasText(response.body())) {
                throw new BusinessException("AI 评分服务响应为空", 502);
            }
            JSONObject root = JSON.parseObject(response.body());
            String content = extractAssistantContent(root);
            return new GradingRawResult(content, response.body());
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException exception) {
            log.error("调用 AI 评分接口失败", exception);
            throw new BusinessException("调用 AI 评分服务失败，请稍后重试", 502);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("AI 评分任务被中断", 502);
        } catch (Exception exception) {
            log.error("解析 AI 评分响应失败", exception);
            throw new BusinessException("AI 评分响应解析失败", 502);
        }
    }

    private JSONObject buildPayload(String systemPrompt, String userPrompt, List<ImageInput> images) {
        JSONObject payload = new JSONObject();
        String model = resolveGradingModel();
        payload.put("model", model);
        payload.put("stream", false);
        payload.put("max_tokens", resolveMaxTokens());
        if (model.toLowerCase().startsWith("qwen")) {
            payload.put("enable_thinking", false);
        }

        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");
        payload.put("response_format", responseFormat);

        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        JSONArray content = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("type", "text");
        textPart.put("text", userPrompt);
        content.add(textPart);

        if (images != null) {
            for (ImageInput image : images) {
                if (image == null || !StringUtils.hasText(image.base64Data())) {
                    continue;
                }
                JSONObject imagePart = new JSONObject();
                imagePart.put("type", "image_url");
                JSONObject imageUrl = new JSONObject();
                imageUrl.put("url", "data:" + image.mimeType() + ";base64," + image.base64Data());
                imagePart.put("image_url", imageUrl);
                content.add(imagePart);
            }
        }
        userMessage.put("content", content);
        messages.add(userMessage);
        payload.put("messages", messages);
        return payload;
    }

    private String extractAssistantContent(JSONObject root) {
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("AI 评分服务返回内容为空", 502);
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            throw new BusinessException("AI 评分服务返回内容为空", 502);
        }
        Object content = message.get("content");
        return content == null ? "" : String.valueOf(content);
    }

    private String requireApiKey() {
        String apiKey = qwenAiProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("AI 评分服务未配置访问密钥", 500);
        }
        return apiKey.trim();
    }

    private String requireChatApi() {
        String chatApi = qwenAiProperties.getChatApi();
        if (!StringUtils.hasText(chatApi)) {
            throw new BusinessException("AI 评分服务未配置接口地址", 500);
        }
        return chatApi.trim();
    }

    private String resolveGradingModel() {
        String configured = qwenAiProperties.getGradingModel();
        return StringUtils.hasText(configured) ? configured.trim() : DEFAULT_GRADING_MODEL;
    }

    private int resolveMaxTokens() {
        Integer configured = qwenAiProperties.getGradingMaxTokens();
        if (configured == null || configured <= 0) {
            return DEFAULT_GRADING_MAX_TOKENS;
        }
        return Math.min(Math.max(configured, 200), 4000);
    }

    private Duration resolveTimeout() {
        Duration configured = qwenAiProperties.getGradingTimeout();
        if (configured == null || configured.isNegative() || configured.isZero()) {
            return DEFAULT_GRADING_TIMEOUT;
        }
        return configured;
    }

    private String extractProviderErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "模型服务无响应体";
        }
        try {
            JSONObject root = JSON.parseObject(body);
            JSONObject error = root == null ? null : root.getJSONObject("error");
            if (error != null && StringUtils.hasText(error.getString("message"))) {
                return error.getString("message");
            }
            if (root != null && StringUtils.hasText(root.getString("message"))) {
                return root.getString("message");
            }
            return body;
        } catch (Exception ignored) {
            return body;
        }
    }

    public record ImageInput(String label, String mimeType, String base64Data) {
    }

    public record GradingRawResult(String content, String rawResponse) {
    }
}
