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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class QwenChatClient {
    private static final String DEFAULT_CHAT_MODEL = "qwen3.5-flash";
    private static final Duration STREAM_CONNECT_TIMEOUT = Duration.ofSeconds(8);
    private static final Duration STREAM_REQUEST_TIMEOUT = Duration.ofSeconds(80);

    private final RestTemplate restTemplate;
    private final QwenAiProperties qwenAiProperties;
    private final HttpClient httpClient;

    public QwenChatClient(RestTemplate restTemplate, QwenAiProperties qwenAiProperties) {
        this.restTemplate = restTemplate;
        this.qwenAiProperties = qwenAiProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(STREAM_CONNECT_TIMEOUT)
                .build();
    }

    public ChatResult chat(List<ChatMessage> messages, boolean jsonResponse) {
        return chat(messages, jsonResponse, null);
    }

    public ChatResult chat(List<ChatMessage> messages, boolean jsonResponse, Integer maxTokens) {
        String apiKey = requireApiKey();
        String chatApi = requireChatApi();
        JSONObject payload = buildPayload(messages, jsonResponse, maxTokens, false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<String> entity = new HttpEntity<>(payload.toJSONString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(chatApi, entity, String.class);
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

    public ChatResult chatStream(
            List<ChatMessage> messages,
            boolean jsonResponse,
            Integer maxTokens,
            Consumer<String> chunkConsumer
    ) {
        String apiKey = requireApiKey();
        String chatApi = requireChatApi();
        JSONObject payload = buildPayload(messages, jsonResponse, maxTokens, true);

        HttpRequest request = HttpRequest.newBuilder(URI.create(chatApi))
                .timeout(STREAM_REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-DashScope-SSE", "enable")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toJSONString(), StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = readBodyAsString(response.body());
                String providerMessage = extractProviderErrorMessage(errorBody);
                log.warn("调用 AI 流式接口失败: status={}, providerMessage={}", response.statusCode(), providerMessage);
                throw new BusinessException("调用 AI 服务失败: " + providerMessage, 502);
            }

            StringBuilder fullContent = new StringBuilder();
            StringBuilder rawResponse = new StringBuilder();
            Usage usage;
            try (InputStream stream = response.body()) {
                usage = consumeStreamingBody(stream, chunkConsumer, fullContent, rawResponse);
            }
            return new ChatResult(fullContent.toString(), usage, rawResponse.toString());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("调用 AI 流式接口失败", exception);
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

    private String requireApiKey() {
        String apiKey = qwenAiProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("AI 服务未配置 API Key", 500);
        }
        return apiKey;
    }

    private String requireChatApi() {
        String chatApi = qwenAiProperties.getChatApi();
        if (!StringUtils.hasText(chatApi)) {
            throw new BusinessException("AI 服务未配置聊天接口地址", 500);
        }
        return chatApi;
    }

    private JSONObject buildPayload(List<ChatMessage> messages, boolean jsonResponse, Integer maxTokens, boolean stream) {
        JSONObject payload = new JSONObject();
        String model = resolveChatModel();
        payload.put("model", model);
        payload.put("stream", stream);
        if (stream) {
            JSONObject streamOptions = new JSONObject();
            streamOptions.put("include_usage", true);
            payload.put("stream_options", streamOptions);
        }
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
        return payload;
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
        return extractContentText(contentObject);
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

    private Usage consumeStreamingBody(
            InputStream stream,
            Consumer<String> chunkConsumer,
            StringBuilder fullContent,
            StringBuilder rawResponse
    ) throws IOException {
        int promptTokens = 0;
        int completionTokens = 0;
        int totalTokens = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("data:")) {
                    continue;
                }
                String data = trimmed.substring("data:".length()).trim();
                if (!StringUtils.hasText(data)) {
                    continue;
                }
                if ("[DONE]".equals(data)) {
                    break;
                }
                rawResponse.append(data).append('\n');

                JSONObject chunk;
                try {
                    chunk = JSON.parseObject(data);
                } catch (Exception parseException) {
                    log.debug("Ignore non-JSON stream chunk: {}", data);
                    continue;
                }
                if (chunk == null) {
                    continue;
                }

                Usage usage = extractUsage(chunk);
                if (usage.totalTokens() > 0) {
                    promptTokens = usage.promptTokens();
                    completionTokens = usage.completionTokens();
                    totalTokens = usage.totalTokens();
                }

                String chunkContent = extractDeltaContent(chunk);
                if (!StringUtils.hasText(chunkContent)) {
                    continue;
                }
                String delta = resolveStreamDelta(chunkContent, fullContent.toString());
                if (!StringUtils.hasText(delta)) {
                    continue;
                }
                fullContent.append(delta);
                if (chunkConsumer != null) {
                    chunkConsumer.accept(delta);
                }
            }
        }

        return new Usage(promptTokens, completionTokens, totalTokens);
    }

    private String extractDeltaContent(JSONObject chunk) {
        JSONArray choices = chunk.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (firstChoice == null) {
            return "";
        }
        JSONObject delta = firstChoice.getJSONObject("delta");
        if (delta != null && delta.containsKey("content")) {
            return extractContentText(delta.get("content"));
        }
        JSONObject message = firstChoice.getJSONObject("message");
        if (message != null && message.containsKey("content")) {
            return extractContentText(message.get("content"));
        }
        return "";
    }

    private String extractContentText(Object contentObject) {
        if (contentObject == null) {
            return "";
        }
        if (contentObject instanceof String content) {
            return content;
        }
        if (contentObject instanceof JSONArray array) {
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                if (StringUtils.hasText(item.getString("text"))) {
                    parts.add(item.getString("text"));
                    continue;
                }
                if (StringUtils.hasText(item.getString("content"))) {
                    parts.add(item.getString("content"));
                }
            }
            if (!parts.isEmpty()) {
                return String.join("\n", parts);
            }
        }
        return String.valueOf(contentObject);
    }

    private String resolveStreamDelta(String incoming, String currentContent) {
        if (!StringUtils.hasText(incoming)) {
            return "";
        }
        if (!StringUtils.hasText(currentContent)) {
            return incoming;
        }
        if (incoming.startsWith(currentContent)) {
            return incoming.substring(currentContent.length());
        }
        if (currentContent.endsWith(incoming)) {
            return "";
        }
        return incoming;
    }

    private String readBodyAsString(InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (InputStream in = stream) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return "";
        }
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
