<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <div class="page-header">

      <h1>学习中心</h1>
      <div class="header-right">
        <button class="user-button" @click="goToUserProfile">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ userInfo?.username || '用户' }}
        </button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="dashboard-content">
      <!-- 用户信息卡片 -->
      <div class="user-info-card">
        <div class="user-avatar">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="user-details">
          <h2>{{ userInfo?.username || '用户' }}</h2>
          <p>{{ userInfo?.email || '未设置邮箱' }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-number">{{ stats?.totalQuestions || 0 }}</div>
              <div class="stat-label">总做题数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ (stats?.correctRate || 0).toFixed(1) }}%</div>
              <div class="stat-label">正确率</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats?.learningDays || 0 }}</div>
              <div class="stat-label">学习天数</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快速功能入口 -->
      <div class="quick-actions">
        <h3 class="section-title">快速开始</h3>
        <div class="action-grid">
          <div class="action-card" @click="goToExercise">
            <div class="action-icon exercise-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 18h6v-2H9v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h4>开始练习</h4>
            <p>选择学科进行刷题</p>
          </div>
          
          <div class="action-card" @click="goToExam">
            <div class="action-icon exam-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="12 6 12 12 16 14" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h4>参加考试</h4>
            <p>模拟考试提升能力</p>
          </div>
          
          <div class="action-card" @click="goToWrongQuestions">
            <div class="action-icon wrong-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="8" x2="12" y2="12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="16" x2="12.01" y2="16" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h4>错题重练</h4>
            <p>巩固薄弱知识点</p>
          </div>
          
          <div class="action-card" @click="goToStats">
            <div class="action-icon stats-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="12 8 12 12 15 15" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h4>学习统计</h4>
            <p>查看详细学习数据</p>
          </div>
        </div>
      </div>

      <!-- 学习进度 -->
      <div class="learning-progress">
        <h3 class="section-title">学习进度</h3>
        <div v-if="subjectProgress.length > 0" class="progress-cards">
          <div class="progress-card" v-for="subject in subjectProgress" :key="subject.id">
            <div class="progress-header">
              <h4>{{ subject.name }}</h4>
              <span class="progress-percentage">{{ subject.progress }}%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: subject.progress + '%' }"></div>
            </div>
            <div class="progress-details">
              <span>{{ subject.completed }}/{{ subject.total }} 题</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>您还没有学习任何科目，开始练习吧！</p>
          <button class="start-button" @click="goToExercise">开始练习</button>
        </div>
      </div>

      <!-- 最近学习记录 -->
      <div class="recent-records">
        <h3 class="section-title">最近学习</h3>
        <div class="records-list">
          <div class="record-item" v-for="record in recentRecords" :key="record.id">
            <div class="record-info">
              <h4>{{ record.title }}</h4>
              <p>{{ record.time }}</p>
            </div>
            <div class="record-result">
              <span :class="['result-badge', record.isPass ? 'pass' : 'fail']">
                {{ record.isPass ? '通过' : '未通过' }}
              </span>
            </div>
          </div>
          <div v-if="recentRecords.length === 0" class="empty-state">
            <p>暂无学习记录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserStats, getAllSubjects, getKnowledgePointProgress, getExamRecords } from '../api/exercise';

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter();
    const userInfo = ref(null);
    const stats = ref(null);
    const subjectProgress = ref([]);
    const recentRecords = ref([]);

    // 加载用户信息（从localStorage获取）
    const loadUserInfo = () => {
      try {
        const userId = localStorage.getItem('userId');
        const username = localStorage.getItem('username') || '用户';
        const email = localStorage.getItem('email') || '未设置邮箱';
        
        userInfo.value = {
          id: userId,
          username: username,
          email: email
        };
      } catch (error) {
        console.error('加载用户信息失败:', error);
      }
    };

    // 加载学习统计
    const loadStats = async () => {
      try {
        const response = await getUserStats();
        if (response && response.code === 200) {
          stats.value = response.data;
        }
      } catch (error) {
        console.error('加载学习统计失败:', error);
      }
    };

    // 加载学科进度
    const loadSubjectProgress = async () => {
      try {
        const userId = localStorage.getItem('userId');
        const subjectsResponse = await getAllSubjects();
        
        if (subjectsResponse && subjectsResponse.code === 200) {
          const subjects = subjectsResponse.data || [];
          const progressData = [];
          
          for (const subject of subjects) {
            try {
              const progressResponse = await getKnowledgePointProgress({
                subjectId: subject.id,
                userId: userId ? parseInt(userId) : null
              });
              
              if (progressResponse && progressResponse.code === 200) {
                const points = progressResponse.data || [];
                const totalQuestions = points.reduce((sum, p) => sum + (p.totalQuestions || 0), 0);
                const completedQuestions = points.reduce((sum, p) => sum + (p.completedQuestions || 0), 0);
                const progress = totalQuestions > 0 ? Math.round((completedQuestions / totalQuestions) * 100) : 0;
                
                // 只添加用户学过的科目（有完成的题目）
                if (completedQuestions > 0) {
                  progressData.push({
                    id: subject.id,
                    name: subject.name,
                    progress: progress,
                    completed: completedQuestions,
                    total: totalQuestions
                  });
                }
              }
            } catch (pointError) {
              console.error(`加载学科 ${subject.name} 进度失败:`, pointError);
              // 出错时不添加该学科
            }
          }
          
          subjectProgress.value = progressData;
        }
      } catch (error) {
        console.error('加载学科进度失败:', error);
        subjectProgress.value = [];
      }
    };

    // 加载最近学习记录
    const loadRecentRecords = async () => {
      try {
        const userId = localStorage.getItem('userId');
        if (userId) {
          const response = await getExamRecords(userId);
          if (response && response.code === 200) {
            const records = response.data || [];
            recentRecords.value = records.slice(0, 5).map(record => ({
              id: record.id,
              title: record.title || '练习记录',
              time: record.createTime || new Date().toLocaleString(),
              isPass: record.score >= 60
            }));
          }
        }
      } catch (error) {
        console.error('加载最近学习记录失败:', error);
        recentRecords.value = [];
      }
    };



    const goToUserProfile = () => {
      router.push('/user/profile');
    };

    const goToExercise = () => {
      // 清除本地存储中的学科选择，强制用户重新选择学科
      localStorage.removeItem('selectedSubjectId');
      router.push('/exercise');
    };

    const goToExam = () => {
      router.push('/exam');
    };

    const goToWrongQuestions = () => {
      router.push('/exercise/wrong');
    };

    const goToStats = () => {
      router.push('/exercise/stats');
    };

    onMounted(async () => {
      await loadUserInfo();
      await loadStats();
      await loadSubjectProgress();
      await loadRecentRecords();
    });

    return {
      userInfo,
      stats,
      subjectProgress,
      recentRecords,
      goToUserProfile,
      goToExercise,
      goToExam,
      goToWrongQuestions,
      goToStats
    };
  }
};
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}



.user-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

.user-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 主内容区域 */
.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  display: grid;
  gap: 2rem;
}

/* 用户信息卡片 */
.user-info-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 2rem;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
}

.user-details p {
  margin: 0 0 1.5rem 0;
  color: #6b7280;
}

.user-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #409eff;
}

.stat-label {
  font-size: 0.875rem;
  color: #6b7280;
}

/* 章节标题 */
.section-title {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

/* 快速功能入口 */
.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.action-card:hover {
  border-color: #409eff;
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(64, 158, 255, 0.2);
}

.action-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.exercise-icon {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
}

.exam-icon {
  background: linear-gradient(135deg, #67c23a 0%, #52c41a 100%);
}

.wrong-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #d48929 100%);
}

.stats-icon {
  background: linear-gradient(135deg, #909399 0%, #7a7d82 100%);
}

.action-card h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.action-card p {
  margin: 0;
  color: #6b7280;
  font-size: 0.875rem;
}

/* 学习进度 */
.learning-progress {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.progress-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.progress-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 1.5rem;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.progress-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
}

.progress-percentage {
  font-weight: 600;
  color: #409eff;
}

.progress-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff 0%, #2a7fff 100%);
  transition: width 0.3s ease;
}

.progress-details {
  font-size: 0.875rem;
  color: #6b7280;
}

/* 最近学习记录 */
.recent-records {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.3s;
}

.record-item:hover {
  border-color: #409eff;
  background: #f9fafb;
}

.record-info h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
}

.record-info p {
  margin: 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.result-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.result-badge.pass {
  background: #f0fdf4;
  color: #16a34a;
}

.result-badge.fail {
  background: #fef2f2;
  color: #dc2626;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #6b7280;
}

/* 开始练习按钮 */
.start-button {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  margin-top: 1.5rem;
  transition: all 0.3s;
}

.start-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-content {
    padding: 1rem;
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
  
  .progress-cards {
    grid-template-columns: 1fr;
  }
}
</style>