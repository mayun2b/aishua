package zysy.iflytek.aishuai.question.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.question.dto.QuestionCreateDTO;
import zysy.iflytek.aishuai.question.service.QuestionService;

/**
 * 题目测试控制器（用于快速生成测试数据）
 */
@Slf4j
@RestController
@RequestMapping("/api/test/question")
public class QuestionTestController {
    
    @Autowired
    private QuestionService questionService;
    
    /**
     * 初始化测试题目
     */
    @PostMapping("/init")
    public Result<Void> initTestData() {
        // 创建单选题
        QuestionCreateDTO dto1 = new QuestionCreateDTO();
        dto1.setTitle("Java 中，以下哪个关键字用于定义接口？");
        dto1.setType(1); // 单选
        dto1.setCategoryId(8L); // 数据结构
        dto1.setDifficulty(1); // 简单
        dto1.setOptions("{\"A\":\"class\",\"B\":\"interface\",\"C\":\"extends\",\"D\":\"implements\"}");
        dto1.setAnswer("B");
        dto1.setAnalysis("在 Java 中，使用 interface 关键字来定义接口。class 用于定义类，extends 用于继承，implements 用于实现接口。");
        
        questionService.createQuestion(dto1);
        
        // 创建判断题
        QuestionCreateDTO dto2 = new QuestionCreateDTO();
        dto2.setTitle("HashMap 是线程安全的集合类。");
        dto2.setType(3); // 判断
        dto2.setCategoryId(8L); // 数据结构
        dto2.setDifficulty(1); // 简单
        dto2.setAnswer("false");
        dto2.setAnalysis("HashMap 不是线程安全的。如果需要线程安全的 Map，可以使用 ConcurrentHashMap 或 Collections.synchronizedMap。");
        
        questionService.createQuestion(dto2);
        
        // 创建多选题
        QuestionCreateDTO dto3 = new QuestionCreateDTO();
        dto3.setTitle("以下哪些是 Java 的访问修饰符？（多选）");
        dto3.setType(2); // 多选
        dto3.setCategoryId(8L); // 数据结构
        dto3.setDifficulty(2); // 中等
        dto3.setOptions("{\"A\":\"public\",\"B\":\"private\",\"C\":\"protected\",\"D\":\"static\"}");
        dto3.setAnswer("ABC");
        dto3.setAnalysis("Java 的访问修饰符包括 public、private、protected 和默认（不写）。static 是修饰符但不是访问修饰符。");
        
        questionService.createQuestion(dto3);
        
        log.info("初始化测试题目完成");
        return Result.success(null);
    }
}
