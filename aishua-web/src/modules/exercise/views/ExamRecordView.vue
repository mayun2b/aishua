<template>
  <div class="exam-record-container">
    <van-nav-bar
      title="考试记录"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="record-content">
      <h2>考试记录</h2>
      
      <div class="filter-section">
        <van-form>
          <van-field label="时间范围">
            <template #input>
              <van-datetime-picker
                v-model="dateRange"
                type="daterange"
                @confirm="onDateRangeConfirm"
                :min-date="minDate"
                :max-date="maxDate"
                title="选择日期范围"
              />
            </template>
          </van-field>
          <van-field label="考试模式">
            <template #input>
              <van-dropdown-menu>
                <van-dropdown-item 
                  v-model="filterForm.mode" 
                  :options="modeOptions"
                  placeholder="选择模式"
                />
              </van-dropdown-menu>
            </template>
          </van-field>
        </van-form>
        <van-button type="primary" block @click="loadExamRecords" :loading="loading">
          查询
        </van-button>
      </div>
      
      <div class="record-list" v-if="examRecords.length > 0">
        <van-cell-group inset>
          <van-cell 
            v-for="record in examRecords" 
            :key="record.id"
            :title="`${record.examMode} - ${record.date}`"
            :value="`得分: ${record.score}分`"
            is-link
            @click="viewRecordDetail(record)"
          >
            <template #extra>
              <span class="exam-grade" :class="getGradeClass(record.score)">
                {{ getGradeText(record.score) }}
              </span>
            </template>
            <template #default>
              <div class="record-info">
                <p class="record-date">{{ record.date }}</p>
                <p class="record-stats">
                  正确率: {{ Math.round(record.correctRate * 100) }}% | 
                  用时: {{ record.duration }}分钟
                </p>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
        
        <div class="pagination" v-if="total > pageSize">
          <van-pagination
            v-model:current="currentPage"
            :page-size="pageSize"
            :total-items="total"
            :show-page-size="false"
            @change="loadExamRecords"
          />
        </div>
      </div>
      
      <div class="empty-state" v-else-if="!loading">
        <van-icon name="history-o" size="64" color="#c8c9cc" />
        <p>暂无考试记录</p>
        <van-button type="primary" @click="goToExam">开始考试</van-button>
      </div>
      
      <div class="loading" v-else>
        <van-loading>加载中...</van-loading>
      </div>
    </div>
    

  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { getExamRecords, getExamRecordsByMode, getExamRecordsByDateRange } from '../api/exercise';

export default {
  name: 'ExamRecordView',
  setup() {
    const router = useRouter();
    
    // 考试记录数据
    const examRecords = ref([]);
    const loading = ref(false);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    
    // 筛选条件
    const filterForm = reactive({
      mode: null,
      startDate: null,
      endDate: null
    });
    
    const dateRange = ref([]);
    const minDate = new Date('2020-01-01');
    const maxDate = new Date();
    
    // 考试模式选项
    const modeOptions = [
      { text: '基础考试', value: '基础考试' },
      { text: '中级考试', value: '中级考试' },
      { text: '高级考试', value: '高级考试' }
    ];
    

    
    // 加载考试记录
    const loadExamRecords = async () => {
      loading.value = true;
      try {
        // 从localStorage获取用户ID
        const userId = localStorage.getItem('userId');
        
        if (!userId) {
          showToast('请先登录');
          loading.value = false;
          return;
        }
        
        let response;
        
        // 根据筛选条件调用不同的API
        if (filterForm.mode) {
          // 转换模式值：基础考试 -> 1, 中级考试 -> 2, 高级考试 -> 3
          const modeMap = {
            '基础考试': 1,
            '中级考试': 2,
            '高级考试': 3
          };
          const modeValue = modeMap[filterForm.mode] || 1;
          response = await getExamRecordsByMode(parseInt(userId), modeValue);
        } else if (filterForm.startDate && filterForm.endDate) {
          // 格式化日期为YYYY-MM-DD HH:mm:ss
          const startDate = new Date(filterForm.startDate).toISOString().slice(0, 19).replace('T', ' ');
          const endDate = new Date(filterForm.endDate).toISOString().slice(0, 19).replace('T', ' ');
          response = await getExamRecordsByDateRange(parseInt(userId), startDate, endDate);
        } else {
          // 获取所有记录
          response = await getExamRecords(parseInt(userId));
        }
        
        if (response.code === 200) {
          // 转换数据格式以适应前端展示
          examRecords.value = response.data.map(record => ({
            id: record.id,
            examMode: record.examMode === 1 ? '基础考试' : record.examMode === 2 ? '中级考试' : '高级考试',
            date: new Date(record.startTime).toLocaleString('zh-CN'),
            score: record.score,
            correctRate: record.correctQuestions / record.totalQuestions,
            duration: record.duration,
            questionCount: record.totalQuestions,
            correctCount: record.correctQuestions
          }));
          total.value = examRecords.value.length;
        } else {
          showToast('获取考试记录失败');
        }
      } catch (error) {
        console.error('加载考试记录失败:', error);
        showToast('加载考试记录失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 查看记录详情
    const viewRecordDetail = (record) => {
      router.push(`/exercise/records/${record.id}`);
    };
    
    // 日期范围确认
    const onDateRangeConfirm = (value) => {
      if (value && value.length === 2) {
        filterForm.startDate = value[0];
        filterForm.endDate = value[1];
      }
    };
    
    // 获取等级样式
    const getGradeClass = (score) => {
      if (score >= 90) return 'grade-excellent';
      if (score >= 80) return 'grade-good';
      if (score >= 60) return 'grade-pass';
      return 'grade-fail';
    };
    
    // 获取等级文本
    const getGradeText = (score) => {
      if (score >= 90) return '优秀';
      if (score >= 80) return '良好';
      if (score >= 60) return '及格';
      return '不及格';
    };
    
    // 跳转到考试页面
    const goToExam = () => {
      router.push('/exercise/exam');
    };
    
    // 返回上一页
    const goBack = () => {
      router.go(-1);
    };
    
    onMounted(() => {
      loadExamRecords();
    });
    
    return {
      examRecords,
      loading,
      currentPage,
      pageSize,
      total,
      filterForm,
      dateRange,
      minDate,
      maxDate,
      modeOptions,
      loadExamRecords,
      viewRecordDetail,
      onDateRangeConfirm,
      getGradeClass,
      getGradeText,
      goToExam,
      goBack
    };
  }
};
</script>

<style scoped>
.exam-record-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding-bottom: 20px;
}

.record-content {
  padding: 20px;
}

.record-content h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 700;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-section .van-button {
  margin-top: 20px;
  height: 48px;
  font-weight: 600;
}

.record-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.record-info {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}

.record-date {
  margin-bottom: 4px;
}

.record-stats {
  font-size: 12px;
}

.exam-grade {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.grade-excellent {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.grade-good {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.grade-pass {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.grade-fail {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffccc7;
}

.pagination {
  padding: 20px;
  text-align: center;
  background: white;
  margin-top: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
}

.empty-state p {
  margin: 20px 0;
  color: #999;
  font-size: 16px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.exam-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.detail-header .van-icon {
  font-size: 20px;
  color: #999;
  cursor: pointer;
}

.detail-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.detail-info {
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  color: #666;
  font-size: 14px;
}

.info-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.info-item .value.score {
  color: #409eff;
  font-size: 16px;
}

.info-item .value.grade {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.detail-actions .van-button {
  height: 48px;
  font-weight: 600;
}

/* 题目详情样式 */
.questions-section {
  margin: 30px 0;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.questions-section h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  padding: 16px;
  border-radius: 8px;
  background-color: #f9f9f9;
  border-left: 4px solid #ddd;
}

.question-item.correct {
  border-left-color: #52c41a;
  background-color: #f6ffed;
}

.question-item.incorrect {
  border-left-color: #f5222d;
  background-color: #fff1f0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-number {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.question-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.question-status.correct {
  background-color: #52c41a;
  color: white;
}

.question-status.incorrect {
  background-color: #f5222d;
  color: white;
}

.question-content {
  font-size: 14px;
  line-height: 1.5;
}

.user-answer {
  margin: 0 0 8px 0;
  color: #666;
}

.answer-time {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.loading-questions {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.no-questions {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}
</style>