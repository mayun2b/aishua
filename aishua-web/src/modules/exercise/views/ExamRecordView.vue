<template>
  <div class="exam-record-container">
    <van-nav-bar
      title="考试记录"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="record-content">
      <h2>考试记录</h2>
      
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { getExamRecords } from '../api/exercise';

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
        
        // 获取所有记录
        const response = await getExamRecords(parseInt(userId));
        
        if (response.code === 200) {
          // 转换数据格式以适应前端展示并按时间降序排列
          examRecords.value = response.data.map(record => ({
            id: record.id,
            examMode: record.examMode === 1 ? '基础考试' : record.examMode === 2 ? '中级考试' : '高级考试',
            date: new Date(record.startTime).toLocaleString('zh-CN'),
            score: record.score,
            correctRate: record.correctQuestions / record.totalQuestions,
            duration: record.duration,
            questionCount: record.totalQuestions,
            correctCount: record.correctQuestions,
            startTime: new Date(record.startTime).getTime() // 用于排序
          })).sort((a, b) => b.startTime - a.startTime); // 按时间降序排列
          
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
      router.push(`/exercise/records/detail/${record.id}`);
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
      loadExamRecords,
      viewRecordDetail,
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
</style>