package zysy.iflytek.aishuai.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.question.entity.Subject;
import zysy.iflytek.aishuai.question.mapper.SubjectMapper;

import java.util.List;

/**
 * 学科控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/subject")
@Validated
public class SubjectController {
    
    @Autowired
    private SubjectMapper subjectMapper;
    
    /**
     * 获取所有学科
     */
    @GetMapping
    public Result<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectMapper.selectList(null);
        return Result.success(subjects);
    }
    
    /**
     * 根据 ID 查询学科详情
     */
    @GetMapping("/{id}")
    public Result<Subject> getSubjectById(@PathVariable Long id) {
        Subject subject = subjectMapper.selectById(id);
        return Result.success(subject);
    }
    
    /**
     * 创建学科
     */
    @PostMapping
    public Result<Long> createSubject(@RequestBody @Validated Subject subject) {
        subjectMapper.insert(subject);
        return Result.success(subject.getId());
    }
    
    /**
     * 更新学科
     */
    @PutMapping("/{id}")
    public Result<Void> updateSubject(@PathVariable Long id, 
                                       @RequestBody @Validated Subject subject) {
        subject.setId(id);
        subjectMapper.updateById(subject);
        return Result.success(null);
    }
    
    /**
     * 删除学科
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectMapper.deleteById(id);
        return Result.success(null);
    }
}