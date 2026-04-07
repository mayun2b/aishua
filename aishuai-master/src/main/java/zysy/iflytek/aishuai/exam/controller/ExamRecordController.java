package zysy.iflytek.aishuai.exam.controller;

import zysy.iflytek.aishuai.exam.entity.ExamRecord;
import zysy.iflytek.aishuai.exam.entity.ExamRecordQuestion;
import zysy.iflytek.aishuai.exam.service.ExamRecordService;
import zysy.iflytek.aishuai.exam.service.ExamRecordQuestionService;
import zysy.iflytek.aishuai.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/exam/records")
public class ExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;

    @GetMapping("/user/{userId}")
    public Result<List<ExamRecord>> getRecordsByUserId(@PathVariable Long userId) {
        List<ExamRecord> records = examRecordService.getRecordsByUserId(userId);
        return Result.success(records);
    }

    @GetMapping("/user/{userId}/mode/{mode}")
    public Result<List<ExamRecord>> getRecordsByUserIdAndMode(@PathVariable Long userId, @PathVariable Integer mode) {
        List<ExamRecord> records = examRecordService.getRecordsByUserIdAndMode(userId, mode);
        return Result.success(records);
    }

    @GetMapping("/user/{userId}/date")
    public Result<List<ExamRecord>> getRecordsByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<ExamRecord> records = examRecordService.getRecordsByUserIdAndDateRange(userId, startDate, endDate);
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<ExamRecord> getRecordById(@PathVariable Long id) {
        ExamRecord record = examRecordService.getRecordById(id);
        return Result.success(record);
    }

    @GetMapping("/{id}/questions")
    public Result<List<ExamRecordQuestion>> getRecordQuestions(@PathVariable Long id) {
        List<ExamRecordQuestion> questions = examRecordQuestionService.getQuestionsByExamRecordId(id);
        return Result.success(questions);
    }

    @Autowired
    private ExamRecordQuestionService examRecordQuestionService;

    @PostMapping( "/save")
    public Result<Long> saveRecord(@RequestBody Map<String, Object> request) {
        System.out.println("收到保存考试记录请求: " + request);
        try {
            // 提取考试记录数据
            ExamRecord examRecord = new ExamRecord();
            examRecord.setUserId(Long.valueOf(request.get("userId").toString()));
            examRecord.setExamName(request.get("examName").toString());
            examRecord.setExamMode(Integer.valueOf(request.get("examMode").toString()));
            examRecord.setTotalQuestions(Integer.valueOf(request.get("totalQuestions").toString()));
            examRecord.setCorrectQuestions(Integer.valueOf(request.get("correctQuestions").toString()));
            examRecord.setScore(Double.valueOf(request.get("score").toString()));
            examRecord.setDuration(Integer.valueOf(request.get("duration").toString()));
            examRecord.setStartTime(request.get("startTime").toString());
            examRecord.setEndTime(request.get("endTime").toString());
            examRecord.setStatus(Integer.valueOf(request.get("status").toString()));

            // 保存考试记录
            Long examRecordId = examRecordService.saveRecord(examRecord);
            System.out.println("保存考试记录成功，ID: " + examRecordId);

            // 保存考试题目记录
            if (request.containsKey("questions") && examRecordId != null) {
                List<Map<String, Object>> questionList = (List<Map<String, Object>>) request.get("questions");
                List<ExamRecordQuestion> questions = new ArrayList<>();
                
                for (Map<String, Object> q : questionList) {
                    ExamRecordQuestion question = new ExamRecordQuestion();
                    question.setQuestionId(Long.valueOf(q.get("questionId").toString()));
                    question.setUserAnswer(q.get("userAnswer").toString());
                    question.setIsCorrect(Integer.valueOf(q.get("isCorrect").toString()));
                    if (q.containsKey("answerTime")) {
                        question.setAnswerTime(Integer.valueOf(q.get("answerTime").toString()));
                    }
                    questions.add(question);
                }

                if (!questions.isEmpty()) {
                    examRecordQuestionService.saveQuestions(examRecordId, questions);
                    System.out.println("保存考试题目记录成功，数量: " + questions.size());
                }
            }

            return Result.success(examRecordId);
        } catch (Exception e) {
            System.out.println("保存考试记录失败: " + e.getMessage());
            e.printStackTrace();
            return Result.fail("保存考试记录失败: " + e.getMessage());
        }
    }
}
