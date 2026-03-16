<template>
  <div class="dashboard-container">
    <div class="header-section">
      <h1 class="page-title">个人中心</h1>
      <button class="back-btn" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 11H7.83l5.59-5.59L12 4l-8 8l8 8l1.41-1.41L7.83 13H20v-2z" fill="currentColor"/>
        </svg>
      </button>
    </div>
    
    <div class="user-info-card">
      <div class="card-content">
        <div class="user-header">
          <div class="avatar">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="user-details">
            <h3>{{ username }}</h3>
            <p>{{ isAdmin ? '管理员' : '普通用户' }}</p>
          </div>
        </div>
        
        <div class="user-stats">
          <div class="stat-item" @click="goToStats">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 20V10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 20V4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6 20V14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>练习统计</span>
          </div>
          <div class="stat-item" @click="goToWrongQuestions">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>错题本</span>
          </div>
          <div class="stat-item" @click="goToExercise">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>继续练习</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-grid">
        <div class="action-card" @click="goToExercise">
          <div class="action-icon exercise">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M14 2V8H20" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 13H8" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 17H8" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M10 9H8" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span>开始练习</span>
        </div>
        
        <div class="action-card" @click="goToExam">
          <div class="action-icon exam">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polyline points="12 6 12 12 16 14" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span>开始考试</span>
        </div>
        
        <div class="action-card" @click="goToExamRecords">
          <div class="action-icon records">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span>考试记录</span>
        </div>
        
        <div class="action-card" @click="goToWrongQuestions">
          <div class="action-icon wrong">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="9" x2="12" y2="13" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="17" x2="12.01" y2="17" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span>查看错题</span>
        </div>
        
        <div class="action-card" @click="goToStats">
          <div class="action-icon stats">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 20V10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 20V4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M6 20V14" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span>练习统计</span>
        </div>
        
        <div v-if="isAdmin" class="action-card" @click="goToUserManagement">
          <div class="action-icon users">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="9" cy="7" r="4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span>用户管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter();
    const username = ref('');
    const isAdmin = ref(false);
    
    onMounted(() => {
      username.value = localStorage.getItem('username') || '用户';
      isAdmin.value = localStorage.getItem('isAdmin') === 'true';
    });
    
    const goBack = () => {
      router.go(-1);
    };
    
    const goToExercise = () => {
      router.push('/exercise');
    };
    
    const goToWrongQuestions = () => {
      router.push('/exercise/wrong');
    };
    
    const goToStats = () => {
      router.push('/exercise/stats');
    };
    
    const goToExam = () => {
      router.push('/exercise/exam');
    };
    
    const goToExamRecords = () => {
      router.push('/exercise/records');
    };
    
    const goToUserManagement = () => {
      router.push('/user-management');
    };
    
    return {
      username,
      isAdmin,
      goBack,
      goToExercise,
      goToExam,
      goToExamRecords,
      goToWrongQuestions,
      goToStats,
      goToUserManagement
    };
  }
};
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 2rem;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.back-btn {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  border: none;
  border-radius: 8px;
  padding: 0.75rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.user-info-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 2rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.user-info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-content {
  padding: 2rem;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: transform 0.3s;
}

.avatar:hover {
  transform: scale(1.05);
}

.user-details h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
}

.user-details p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 1.5rem 0;
  border-top: 1px solid #eee;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  color: #409eff;
  cursor: pointer;
  padding: 1rem;
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 1.5rem;
  transition: transform 0.3s;
}

.stat-item:hover .stat-icon {
  transform: scale(1.1);
}

.stat-item span {
  font-weight: 500;
  font-size: 0.9rem;
}

.quick-actions {
  margin-bottom: 2rem;
}

.quick-actions h3 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.action-card:hover .action-icon {
  transform: scale(1.1);
}

.action-icon.exercise {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
}

.action-icon.exam {
  background: linear-gradient(135deg, #67c23a 0%, #409eff 100%);
}

.action-icon.records {
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
}

.action-icon.wrong {
  background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
}

.action-icon.stats {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
}

.action-icon.users {
  background: linear-gradient(135deg, #722ed1 0%, #eb2f96 100%);
}

.action-card span {
  font-weight: 500;
  color: #333;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 1rem;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .card-content {
    padding: 1.5rem;
  }
  
  .user-header {
    gap: 1rem;
  }
  
  .avatar {
    width: 64px;
    height: 64px;
  }
  
  .action-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 1rem;
  }
  
  .action-card {
    padding: 1rem;
  }
  
  .action-icon {
    width: 48px;
    height: 48px;
  }
}
</style>