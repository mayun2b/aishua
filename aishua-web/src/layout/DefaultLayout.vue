<template>
  <div class="default-layout">
    <!-- 只有在非首页时显示导航栏 -->
    <header v-if="!isHomePage" class="app-header">
      <div class="logo">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 17L12 22L22 17" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12L12 17L22 12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>爱刷题系统</span>
      </div>
      
      <nav class="nav-menu">
        <!-- 用户功能区 -->
        <div class="nav-section">
          <template v-for="item in navConfig.userMenu" :key="item.name">
            <router-link 
              v-if="item.path" 
              :to="item.path" 
              class="nav-link"
            >
              <div v-html="iconMap[item.icon]"></div>
              <span>{{ item.name }}</span>
            </router-link>
            
            <button 
              v-else-if="item.action" 
              @click="item.action === 'goToExercise' && goToExercise()" 
              class="nav-button"
            >
              <div v-html="iconMap[item.icon]"></div>
              <span>{{ item.name }}</span>
            </button>
          </template>
        </div>
        
        <!-- 管理员功能区 -->
        <div v-if="isAdmin" class="nav-section admin-section">
          <template v-for="item in navConfig.adminMenu" :key="item.name">
            <router-link 
              :to="item.path" 
              class="nav-link"
            >
              <div v-html="iconMap[item.icon]"></div>
              <span>{{ item.name }}</span>
            </router-link>
          </template>
        </div>
        <!-- 可以在这里添加更多导航项 -->
      </nav>
      <div class="user-info">
        <div class="user-avatar">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="user-details">
          <span class="user-name">{{ username }}</span>
          <span class="user-role" v-if="isAdmin">管理员</span>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="16 17 21 12 16 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>退出</span>
        </button>
      </div>
    </header>
    <main :class="['main-content', { 'home-content': isHomePage }]">
      <router-view />
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { logout } from '../modules/auth/api/auth';
import { navConfig, iconMap } from '../config/navConfig';

export default {
  name: 'DefaultLayout',
  setup() {
    const username = ref('');
    const isAdmin = ref(false);
    const router = useRouter();

    onMounted(() => {
      username.value = localStorage.getItem('username') || '用户';
      isAdmin.value = localStorage.getItem('isAdmin') === 'true';
    });

    // 判断当前是否在首页
    const isHomePage = computed(() => {
      return router.currentRoute.value.path === '/';
    });

    const handleLogout = () => {
      logout();
      router.push('/login');
    };

    const goToExercise = () => {
      // 清除本地存储中的学科选择，强制用户重新选择学科
      localStorage.removeItem('selectedSubjectId');
      router.push('/exercise');
    };

    return {
      username,
      isAdmin,
      isHomePage,
      handleLogout,
      goToExercise,
      navConfig,
      iconMap
    };
  }
};
</script>

<style scoped>
.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.app-header {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  padding: 0 2rem;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.5rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.3s;
}

.logo:hover {
  transform: translateY(-2px);
}



.nav-menu {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-section {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.admin-section {
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  padding-left: 2rem;
}

.nav-link,
.nav-button {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  transition: all 0.3s;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 1rem;
}

.nav-link::before,
.nav-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  transition: left 0.3s;
}

.nav-link:hover::before,
.nav-link.router-link-active::before,
.nav-button:hover::before {
  left: 0;
}

.nav-link:hover,
.nav-link.router-link-active,
.nav-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
}

.user-role {
  font-size: 12px;
  opacity: 0.8;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* 首页内容样式 */
.home-content {
  padding: 0;
  max-width: none;
  margin: 0;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .app-header {
    padding: 0 1.5rem;
  }
  
  .nav-menu {
    gap: 1rem;
  }
  
  .nav-section {
    gap: 0.75rem;
  }
  
  .admin-section {
    border-left: none;
    padding-left: 0;
  }
  
  .nav-link,
  .nav-button {
    padding: 0.5rem 1rem;
  }
  
  .nav-link span,
  .nav-button span {
    display: none;
  }
  
  .user-details {
    display: none;
  }
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 1rem;
  }
  
  .logo span {
    display: none;
  }
  
  .main-content {
    padding: 1rem;
  }
}
</style>