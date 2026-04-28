<template>
  <div class="dashboard-page">
    <section class="dashboard-card">
      <div>
        <p class="eyebrow">User Dashboard</p>
        <h1>{{ greeting }}</h1>
        <p class="description">
          你可以从这里进入学科、练习、练习记录和错题记录页面，完整查看自己的刷题过程和结果沉淀。
        </p>
      </div>

      <div class="profile-grid">
        <div class="profile-item">
          <span>用户 ID</span>
          <strong>{{ user?.id || '-' }}</strong>
        </div>
        <div class="profile-item">
          <span>手机号</span>
          <strong>{{ user?.phone || '-' }}</strong>
        </div>
        <div class="profile-item">
          <span>昵称</span>
          <strong>{{ user?.nickname || '-' }}</strong>
        </div>
        <div class="profile-item">
          <span>角色</span>
          <strong>{{ user?.isAdmin === 1 ? '管理员' : '普通用户' }}</strong>
        </div>
      </div>

      <div class="action-row">
        <router-link class="ghost" to="/">返回首页</router-link>
        <router-link class="ghost" to="/subjects">学科列表</router-link>
        <router-link class="ghost" to="/my-subjects">我的学科</router-link>
        <router-link class="ghost" to="/practice">开始练习</router-link>
        <router-link class="ghost" to="/practice-records">练习记录</router-link>
        <router-link class="ghost" to="/wrong-questions">错题记录</router-link>
        <router-link v-if="user?.isAdmin === 1" class="ghost" to="/admin">进入管理台</router-link>
        <button @click="handleLogout">退出登录</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

const user = computed(() => store.getters['auth/currentUser'])
const greeting = computed(() => `${user.value?.nickname || '同学'}，欢迎回来`)

const handleLogout = () => {
  store.dispatch('auth/logout')
  router.push('/login')
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 40px 24px;
  background: linear-gradient(135deg, #f6f2e9 0%, #dce8f4 100%);
}

.dashboard-card {
  max-width: 920px;
  margin: 0 auto;
  padding: 36px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.14);
}

.eyebrow {
  margin: 0;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #7a8793;
  font-size: 12px;
}

.dashboard-card h1 {
  margin: 12px 0 0;
  font-size: 36px;
  color: #17324d;
}

.description {
  margin: 16px 0 0;
  max-width: 680px;
  color: #5e6d7c;
  line-height: 1.7;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-top: 28px;
}

.profile-item {
  padding: 18px;
  border-radius: 18px;
  background: #edf4fb;
  display: grid;
  gap: 6px;
}

.profile-item span {
  color: #5e6d7c;
  font-size: 13px;
}

.profile-item strong {
  color: #17324d;
  font-size: 18px;
}

.action-row {
  margin-top: 28px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-row button,
.action-row .ghost {
  border: 0;
  border-radius: 14px;
  padding: 12px 18px;
  font-size: 15px;
  text-decoration: none;
  cursor: pointer;
}

.action-row button {
  background: #17324d;
  color: #fff;
}

.action-row .ghost {
  background: #e4edf6;
  color: #17324d;
}
</style>
