package zysy.iflytek.aishuai.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.aishuai.exam.entity.ExamRecord;

import java.util.List;

public interface ExamRecordService extends IService<ExamRecord> {

    List<ExamRecord> getRecordsByUserId(Long userId);

    List<ExamRecord> getRecordsByUserIdAndMode(Long userId, Integer mode);

    List<ExamRecord> getRecordsByUserIdAndDateRange(Long userId, String startDate, String endDate);

    ExamRecord getRecordById(Long id);

    Long saveRecord(ExamRecord examRecord);
}
