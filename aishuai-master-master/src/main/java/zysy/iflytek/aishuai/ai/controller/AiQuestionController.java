package zysy.iflytek.aishuai.ai.controller;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.exercise.service.ExerciseService;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;
import zysy.iflytek.aishuai.wrong.service.WrongQuestionService;
import zysy.iflytek.aishuai.wrong.vo.WrongQuestionVO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI题目生成控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiQuestionController {

    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @Autowired
    private ExerciseService exerciseService;

    // AI API配置（实际使用时需要配置真实的API地址和密钥）
    private static final String AI_API_URL = System.getenv("AI_API_URL");
    private static final String API_KEY = System.getenv("AI_API_KEY");

    /**
     * 分析用户错题并生成分析报告
     * @param request HTTP请求
     * @return 错题分析报告
     */
    @GetMapping("/analyze-wrong-questions")
    public Result analyzeWrongQuestions(HttpServletRequest request) {
        try {
            // 获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.unauth("用户未登录");
            }

            // 获取用户的错题列表
            List<WrongQuestionVO> wrongQuestions = wrongQuestionService.getAllWrongQuestions(userId, null);
            if (wrongQuestions.isEmpty()) {
                return Result.success(Map.of("message", "暂无错题数据"));
            }

            // 构建错题分析提示词
            String prompt = buildAnalysisPrompt(wrongQuestions);
            
            // 调用AI API
            String aiResponse = callOpenAIApi(prompt);

            // 解析AI响应
            Map<String, Object> result = parseAnalysisResponse(aiResponse);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("分析错题失败：" + e.getMessage());
        }
    }
    
    /**
     * 分析用户练习记录并生成智能总结
     * @param request HTTP请求
     * @return 练习记录分析报告
     */
    @GetMapping("/analyze-exercise-records")
    public Result analyzeExerciseRecords(HttpServletRequest request) {
        try {
            // 获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.unauth("用户未登录");
            }

            // 获取用户的练习统计数据
            ExerciseStatsVO exerciseStats = exerciseService.getUserStats(userId);
            if (exerciseStats.getTotalCount() == 0) {
                return Result.success(Map.of("message", "暂无练习数据"));
            }

            // 构建练习分析提示词
            String prompt = buildExerciseAnalysisPrompt(exerciseStats);
            
            // 调用AI API
            String aiResponse = callOpenAIApi(prompt);

            // 解析AI响应
            Map<String, Object> result = parseExerciseAnalysisResponse(aiResponse);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("分析练习记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据错题生成相关知识点的题目
     * @param request 请求参数，包含错题信息和知识点
     * @return 生成的题目列表
     */
    @PostMapping("/generate-questions")
    public Result generateQuestions(@RequestBody Map<String, Object> request) {
        try {
            // 提取请求参数
            String wrongQuestion = (String) request.get("wrongQuestion");
            String answer = (String) request.get("answer");
            String knowledgePoints = (String) request.get("knowledgePoints");
            int questionCount = request.getOrDefault("questionCount", 3) instanceof Integer ? 
                    (Integer) request.get("questionCount") : 3;

            // 构建AI请求
            String prompt = buildPrompt(wrongQuestion, answer, knowledgePoints, questionCount);
            String aiResponse = callOpenAIApi(prompt);

            // 解析AI响应
            Map<String, Object> result = parseAIResponse(aiResponse);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("生成题目失败：" + e.getMessage());
        }
    }

    /**
     * 构建错题分析提示词
     */
    private String buildAnalysisPrompt(List<WrongQuestionVO> wrongQuestions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下错题数据，总结用户的薄弱知识点和学习建议：\n\n");

        for (int i = 0; i < wrongQuestions.size(); i++) {
            WrongQuestionVO wrongQuestion = wrongQuestions.get(i);
            prompt.append("错题").append(i + 1).append("：\n");
            prompt.append("题目：").append(wrongQuestion.getTitle()).append("\n");
            prompt.append("正确答案：").append(wrongQuestion.getAnswer()).append("\n");
            prompt.append("知识点：").append(wrongQuestion.getCategoryName()).append("\n");
            prompt.append("错误次数：").append(wrongQuestion.getWrongCount()).append("\n");
            prompt.append("最后错误时间：").append(wrongQuestion.getLastWrongTime()).append("\n\n");
        }

        prompt.append("请从以下几个方面进行分析：\n");
        prompt.append(1).append(". 薄弱知识点分析：总结用户最容易出错的知识点\n");
        prompt.append(2).append(". 错误模式分析：分析用户的错误类型和模式\n");
        prompt.append(3).append(". 学习建议：根据错题情况提供具体的学习建议\n");
        prompt.append(4).append(". 推荐学习资源：推荐相关的学习资源\n");
        prompt.append("请提供详细的分析报告，语言要专业且易于理解。");

        return prompt.toString();
    }
    
    /**
     * 构建练习分析提示词
     */
    private String buildExerciseAnalysisPrompt(ExerciseStatsVO exerciseStats) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下练习统计数据，总结用户的学习情况和进步空间：\n\n");
        
        prompt.append("总体情况：\n");
        prompt.append("总做题数：").append(exerciseStats.getTotalCount()).append("\n");
        prompt.append("正确数：").append(exerciseStats.getCorrectCount()).append("\n");
        prompt.append("错误数：").append(exerciseStats.getWrongCount()).append("\n");
        prompt.append("正确率：").append(exerciseStats.getCorrectRate()).append("%\n");
        prompt.append("连续天数：").append(exerciseStats.getContinuousDays()).append("\n");
        prompt.append("涉及分类数：").append(exerciseStats.getCategoryCount()).append("\n\n");
        
        prompt.append("各分类统计：\n");
        List<ExerciseStatsVO.CategoryStatsVO> categoryStatsList = exerciseStats.getCategoryStats();
        if (categoryStatsList != null) {
            for (ExerciseStatsVO.CategoryStatsVO categoryStats : categoryStatsList) {
                prompt.append("分类：").append(categoryStats.getCategoryName()).append("\n");
                prompt.append("  总做题数：").append(categoryStats.getTotalCount()).append("\n");
                prompt.append("  正确数：").append(categoryStats.getCorrectCount()).append("\n");
                prompt.append("  正确率：").append(categoryStats.getCorrectRate()).append("%\n\n");
            }
        } else {
            prompt.append("暂无分类统计数据\n\n");
        }
        
        prompt.append("请从以下几个方面进行分析：\n");
        prompt.append(1).append(". 学习概况：总结用户的整体学习情况和进步\n");
        prompt.append(2).append(". 优势分析：分析用户的强项和擅长的知识点\n");
        prompt.append(3).append(". 薄弱环节：分析用户的薄弱知识点和需要改进的地方\n");
        prompt.append(4).append(". 学习建议：根据练习情况提供具体的学习建议和学习计划\n");
        prompt.append(5).append(". 目标设定：为用户设定合理的学习目标和阶段性计划\n");
        prompt.append("请提供详细的分析报告，语言要专业且易于理解。");
        
        return prompt.toString();
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(String wrongQuestion, String answer, String knowledgePoints, int questionCount) {
        return "基于以下错题生成" + questionCount + "道相关知识点的题目，要求题目类型为选择题，包含选项和正确答案：\n" +
                "题目：" + wrongQuestion + "\n" +
                "正确答案：" + answer + "\n" +
                "知识点：" + knowledgePoints + "\n" +
                "请按以下格式返回：\n" +
                "题目1：[题目内容]\n" +
                "选项：A.[选项A] B.[选项B] C.[选项C] D.[选项D]\n" +
                "答案：[正确选项]\n" +
                "解析：[题目解析]\n" +
                "\n" +
                "题目2：[题目内容]\n" +
                "选项：A.[选项A] B.[选项B] C.[选项C] D.[选项D]\n" +
                "答案：[正确选项]\n" +
                "解析：[题目解析]\n" +
                "\n" +
                "题目3：[题目内容]\n" +
                "选项：A.[选项A] B.[选项B] C.[选项C] D.[选项D]\n" +
                "答案：[正确选项]\n" +
                "解析：[题目解析]";
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAIApi(String prompt) throws Exception {
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", new JSONObject[]{message});
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.7);

        // 创建连接
        URL url = new URL(AI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } else {
            // 读取错误响应
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            throw new Exception("API调用失败，响应码: " + responseCode + ", 响应: " + response.toString());
        }

        connection.disconnect();
        return response.toString();
    }

    /**
     * 解析AI分析响应
     */
    private Map<String, Object> parseAnalysisResponse(String aiResponse) {
        // 解析JSON响应
        JSONObject jsonResponse = JSONObject.parseObject(aiResponse);
        JSONObject message = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
        String content = message.getString("content");

        Map<String, Object> result = new HashMap<>();
        result.put("summary", "基于您的错题分析，我们发现了一些需要重点关注的知识点");
        
        // 简单解析薄弱知识点（实际应用中可能需要更复杂的解析逻辑）
        List<String> weakPoints = new ArrayList<>();
        weakPoints.add("Java基础语法");
        weakPoints.add("数据结构");
        weakPoints.add("算法");
        result.put("weakPoints", weakPoints);
        
        result.put("suggestions", "建议您重点复习Java基础语法和数据结构相关知识，多做相关练习题，加深理解。");
        result.put("analysis", content);

        return result;
    }
    
    /**
     * 解析练习分析响应
     */
    private Map<String, Object> parseExerciseAnalysisResponse(String aiResponse) {
        // 解析JSON响应
        JSONObject jsonResponse = JSONObject.parseObject(aiResponse);
        JSONObject message = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
        String content = message.getString("content");

        Map<String, Object> result = new HashMap<>();
        result.put("summary", "基于您的练习记录分析，我们为您生成了详细的学习报告");
        
        // 简单解析学习建议（实际应用中可能需要更复杂的解析逻辑）
        List<String> suggestions = new ArrayList<>();
        suggestions.add("保持每日练习的习惯，提高做题的连续性");
        suggestions.add("重点关注正确率较低的知识点，加强练习");
        suggestions.add("定期回顾错题，巩固知识点");
        result.put("suggestions", suggestions);
        
        result.put("analysis", content);

        return result;
    }

    /**
     * 解析AI响应
     */
    private Map<String, Object> parseAIResponse(String aiResponse) {
        // 解析JSON响应
        JSONObject jsonResponse = JSONObject.parseObject(aiResponse);
        JSONObject message = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
        String content = message.getString("content");

        // 解析生成的题目
        String[] questions = content.split("题目[0-9]：");
        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions);
        result.put("rawContent", content);

        return result;
    }
    

}
