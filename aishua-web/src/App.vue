<template>
  <div id="app">
    <div class="route-progress" :class="{ active: navigationState.pending }"></div>

    <router-view v-slot="{ Component }">
      <AppShell v-if="showAppShell">
        <transition :name="transitionName" mode="out-in">
          <div :key="route.fullPath" class="route-view-shell">
            <component :is="Component" />
          </div>
        </transition>
      </AppShell>

      <transition v-else :name="transitionName" mode="out-in">
        <div :key="route.fullPath" class="route-view-shell">
          <component :is="Component" />
        </div>
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from './components/AppShell.vue'
import { navigationState } from './router/navigationState'

const route = useRoute()
const transitionName = computed(() => route.meta?.transition || 'page-fade')
const showAppShell = computed(() => {
  return Boolean(route.meta?.requiresAuth) && route.meta?.shell !== false
})
</script>

<style>
#app {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Noto Sans SC', 'Source Han Sans SC', 'WenQuanYi Micro Hei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  background: #f4f7fb;
}

.route-progress {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  z-index: 2000;
  pointer-events: none;
  opacity: 0;
}

.route-progress::before {
  content: '';
  display: block;
  width: 30%;
  height: 100%;
  background: linear-gradient(90deg, #17324d 0%, #2563eb 52%, #10b981 100%);
  border-radius: 0 999px 999px 0;
  box-shadow: 0 0 14px rgba(37, 99, 235, 0.45);
  transform: translateX(-120%);
}

.route-progress.active {
  opacity: 1;
}

.route-progress.active::before {
  animation: route-loading 1.25s ease-in-out infinite;
}

@keyframes route-loading {
  0% {
    transform: translateX(-120%);
  }
  100% {
    transform: translateX(420%);
  }
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

.route-view-shell {
  min-width: 0;
  min-height: 100%;
}
</style>
