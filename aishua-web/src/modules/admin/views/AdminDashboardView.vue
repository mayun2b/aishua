<template>
  <div class="dashboard-page">
    <section class="dashboard-card">
      <div>
        <p class="eyebrow">管理控制台</p>
        <h1>{{ user?.nickname || '管理员' }}，欢迎进入管理台</h1>
        <p class="description">
          第一阶段先完成管理员认证与后台入口。后续学科、题库、目录、统计等模块会继续在这里扩展。
        </p>
      </div>

      <div class="panel-grid">
        <article class="panel">
          <span>认证</span>
          <strong>统一登录令牌校验</strong>
          <p>前后端共用同一套登录态与角色标记，管理端权限控制不再只依赖前端判断。</p>
        </article>
        <article class="panel">
          <span>权限</span>
          <strong>管理员角色分流</strong>
          <p>统一登录接口返回角色信息，登录后自动分流到用户工作台或管理控制台。</p>
        </article>
        <article class="panel">
          <span>下一步</span>
          <strong>后台能力持续扩展</strong>
          <p>后续会继续接入学科、目录、考点、题库等管理模块。</p>
        </article>
      </div>

      <div class="action-row">
        <router-link class="ghost" to="/">返回首页</router-link>
        <router-link class="ghost" to="/dashboard">查看用户台</router-link>
        <router-link class="ghost" to="/admin/subjects">学科管理</router-link>
        <router-link class="ghost" to="/admin/directories">目录树管理</router-link>
        <router-link class="ghost" to="/admin/tags">考点标签管理</router-link>
        <router-link class="ghost" to="/admin/questions">题库管理</router-link>
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

const handleLogout = () => {
  store.dispatch('auth/logout')
  router.push('/login')
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 40px 24px;
  background: linear-gradient(135deg, #efe8ff 0%, #ddece4 100%);
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
  max-width: 640px;
  color: #5e6d7c;
  line-height: 1.7;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-top: 28px;
}

.panel {
  padding: 20px;
  border-radius: 20px;
  background: #f2f5fb;
  display: grid;
  gap: 8px;
}

.panel span {
  color: #6d7891;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.panel strong {
  color: #17324d;
  font-size: 20px;
}

.panel p {
  margin: 0;
  color: #5e6d7c;
  line-height: 1.6;
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
  background: #4e3a8a;
  color: #fff;
}

.action-row .ghost {
  background: #e8eaf7;
  color: #17324d;
}
</style>
