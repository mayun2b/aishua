<template>
  <div class="default-layout">
    <header class="app-header">
      <div class="logo">爱刷题系统</div>
      <nav class="nav-menu">
        <router-link to="/dashboard">首页</router-link>
        <router-link to="/exercise">刷题练习</router-link>
        <router-link to="/exercise/wrong">错题本</router-link>
        <router-link to="/exercise/stats">练习统计</router-link>
        <!-- 管理员专属导航 -->
        <router-link 
          v-if="isAdmin" 
          to="/exercise/questions"
        >
          题目管理
        </router-link>
        <router-link 
          v-if="isAdmin" 
          to="/exercise/subjects"
        >
          学科管理
        </router-link>
        <router-link 
          v-if="isAdmin" 
          to="/user-management"
        >
          用户管理
        </router-link>
        <!-- 可以在这里添加更多导航项 -->
      </nav>
      <div class="user-info">
        <span>{{ username }}{{ isAdmin ? ' (管理员)' : '' }}</span>
        <button @click="handleLogout" class="logout-btn">退出</button>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { logout } from '../modules/auth/api/auth';

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

    const handleLogout = () => {
      logout();
      router.push('/login');
    };

    return {
      username,
      isAdmin,
      handleLogout
    };
  }
};
</script>

<style scoped>
.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background-color: #409eff;
  color: white;
  padding: 0 2rem;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-menu {
  display: flex;
  gap: 2rem;
}

.nav-menu a {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-menu a:hover,
.nav-menu a.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logout-btn {
  background-color: transparent;
  color: white;
  border: 1px solid white;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.main-content {
  flex: 1;
  padding: 2rem;
}
</style>