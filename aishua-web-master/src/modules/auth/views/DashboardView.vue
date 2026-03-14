<template>
  <div class="dashboard-container">
    <van-nav-bar
      title="个人中心"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="user-info-card">
      <van-card>
        <template #title>
          <div class="user-header">
            <div class="avatar">
              <van-icon name="user-circle-o" size="48" />
            </div>
            <div class="user-details">
              <h3>{{ username }}</h3>
              <p>{{ isAdmin ? '管理员' : '普通用户' }}</p>
            </div>
          </div>
        </template>
        
        <div class="user-stats">
          <div class="stat-item">
            <van-icon name="bar-chart-o" />
            <span>练习统计</span>
          </div>
          <div class="stat-item">
            <van-icon name="warning-o" />
            <span>错题本</span>
          </div>
          <div class="stat-item">
            <van-icon name="edit" />
            <span>继续练习</span>
          </div>
        </div>
      </van-card>
    </div>
    
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <van-cell-group inset>
        <van-cell 
          title="开始练习" 
          is-link 
          @click="goToExercise"
          icon="edit"
        />
        <van-cell 
          title="开始考试" 
          is-link 
          @click="goToExam"
          icon="clock-o"
        />
        <van-cell 
          title="考试记录" 
          is-link 
          @click="goToExamRecords"
          icon="history-o"
        />
        <van-cell 
          title="查看错题" 
          is-link 
          @click="goToWrongQuestions"
          icon="warning-o"
        />
        <van-cell 
          title="练习统计" 
          is-link 
          @click="goToStats"
          icon="bar-chart-o"
        />
        <van-cell 
          v-if="isAdmin"
          title="用户管理" 
          is-link 
          @click="goToUserManagement"
          icon="manager"
        />
      </van-cell-group>
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
  padding-bottom: 20px;
}

.user-info-card {
  padding: 0 16px 20px 16px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
}

.user-details h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #eee;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #409eff;
}

.stat-item .van-icon {
  font-size: 24px;
}

.quick-actions {
  padding: 0 16px;
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.van-cell {
  border-radius: 12px;
  margin-bottom: 12px;
}

.van-cell:last-child {
  margin-bottom: 0;
}
</style>