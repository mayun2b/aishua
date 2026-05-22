<template>
  <div class="default-layout">
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
        <div class="nav-section">
          <router-link
            v-for="item in navConfig.userMenu"
            :key="item.name"
            :to="item.path"
            class="nav-link"
            :class="{ 'is-active': isActiveMenu(item) }"
          >
            <div v-html="iconMap[item.icon]"></div>
            <span>{{ item.name }}</span>
          </router-link>
        </div>

        <div v-if="isAdmin" class="nav-section admin-section">
          <router-link
            v-for="item in navConfig.adminMenu"
            :key="item.name"
            :to="item.path"
            class="nav-link"
            :class="{ 'is-active': isActiveMenu(item) }"
          >
            <div v-html="iconMap[item.icon]"></div>
            <span>{{ item.name }}</span>
          </router-link>
        </div>
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
          <span v-if="isAdmin" class="user-role">管理员</span>
        </div>
        <button class="logout-btn" @click="handleLogout">
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
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { logout } from '../modules/auth/api/auth';
import { navConfig, iconMap } from '../config/navConfig';

export default {
  name: 'DefaultLayout',
  setup() {
    const username = ref('');
    const isAdmin = ref(false);
    const route = useRoute();
    const router = useRouter();

    onMounted(() => {
      username.value = localStorage.getItem('username') || '用户';
      isAdmin.value = localStorage.getItem('isAdmin') === 'true';
    });

    const isHomePage = computed(() => route.path === '/');

    const isActiveMenu = (item) => {
      const currentRouteName = route.name ? String(route.name) : '';
      if (Array.isArray(item.activeRouteNames) && item.activeRouteNames.length) {
        return item.activeRouteNames.includes(currentRouteName);
      }
      return route.path === item.path;
    };

    const handleLogout = () => {
      logout();
      router.push('/login');
    };

    return {
      username,
      isAdmin,
      isHomePage,
      isActiveMenu,
      handleLogout,
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

.nav-link {
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

.nav-link::before {
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
.nav-link.is-active::before {
  left: 0;
}

.nav-link:hover,
.nav-link.is-active {
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

  .nav-link {
    padding: 0.5rem 1rem;
  }

  .nav-link span {
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
