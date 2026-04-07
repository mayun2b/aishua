package zysy.iflytek.aishuai.exam.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zysy.iflytek.aishuai.exam.entity.ExamRecord;
import zysy.iflytek.aishuai.exam.mapper.ExamRecordMapper;
import zysy.iflytek.aishuai.exam.service.ExamRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Override
    public List<ExamRecord> getRecordsByUserId(Long userId) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getStartTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<ExamRecord> getRecordsByUserIdAndMode(Long userId, Integer mode) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamMode, mode)
                .orderByDesc(ExamRecord::getStartTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<ExamRecord> getRecordsByUserIdAndDateRange(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getUserId, userId)
                .ge(ExamRecord::getStartTime, startDate)
                .le(ExamRecord::getStartTime, endDate)
                .orderByDesc(ExamRecord::getStartTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public ExamRecord getRecordById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Long saveRecord(ExamRecord examRecord) {
        save(examRecord);
        return examRecord.getId();
    }
}
