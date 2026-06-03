<template>
  <div class="not-found-page">
    <section class="not-found-card">
      <p class="eyebrow">404</p>
      <h1>页面未找到</h1>
      <p class="description">
        你访问的地址可能已变更，或者链接有误。你可以返回上一页，或回到常用入口继续操作。
      </p>

      <div class="actions">
        <button type="button" class="ghost-button" @click="goBack">返回上一页</button>
        <router-link :to="fallbackPath" class="solid-link">{{ fallbackLabel }}</router-link>
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

const fallbackPath = computed(() => {
  if (!store.getters['auth/isAuthenticated']) {
    return '/'
  }
  return store.getters['auth/isAdmin'] ? '/admin' : '/dashboard'
})

const fallbackLabel = computed(() => {
  return store.getters['auth/isAuthenticated'] ? '回到工作台' : '回到首页'
})

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push(fallbackPath.value)
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 16% 18%, rgba(15, 92, 77, 0.16), transparent 36%),
    radial-gradient(circle at 82% 20%, rgba(30, 64, 175, 0.15), transparent 38%),
    linear-gradient(135deg, #f4f7fb 0%, #e7eef7 100%);
}

.not-found-card {
  width: min(560px, 100%);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 26px 72px rgba(30, 41, 59, 0.15);
  padding: 40px 34px;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #64748b;
}

.not-found-card h1 {
  margin: 10px 0 0;
  font-size: 34px;
  color: #0f172a;
}

.description {
  margin: 16px 0 0;
  color: #475569;
  line-height: 1.7;
}

.actions {
  margin-top: 28px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.ghost-button,
.solid-link {
  border: 0;
  min-height: 44px;
  border-radius: 12px;
  padding: 0 16px;
  font-size: 14px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.ghost-button {
  background: #e7edf6;
  color: #0f172a;
}

.solid-link {
  background: #17324d;
  color: #fff;
}

@media (max-width: 560px) {
  .not-found-card {
    padding: 30px 22px;
    border-radius: 20px;
  }

  .not-found-card h1 {
    font-size: 28px;
  }

  .ghost-button,
  .solid-link {
    width: 100%;
  }
}
</style>
