<template>
  <div class="app-shell">
    <aside
      id="app-shell-sidebar"
      class="shell-sidebar"
      :class="{ open: mobileMenuOpen }"
      :inert="isMobileViewport && !mobileMenuOpen"
      :aria-hidden="isMobileViewport && !mobileMenuOpen ? 'true' : 'false'"
    >
      <div class="brand-block">
        <router-link class="brand-mark" :to="homePath" @click="closeMobileMenu">
          <span>AI</span>
          <strong>刷题</strong>
        </router-link>
        <p>{{ userRoleText }}</p>
      </div>

      <nav class="shell-nav" aria-label="主导航">
        <section v-for="section in navigationSections" :key="section.key" class="nav-section">
          <h2>{{ section.title }}</h2>
          <router-link
            v-for="item in section.items"
            :key="item.key"
            :to="item.to"
            :class="['nav-item', { active: isActive(item) }]"
            @click="closeMobileMenu"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </router-link>
        </section>
      </nav>
    </aside>

    <div v-if="mobileMenuOpen" class="shell-mask" @click="closeMobileMenu"></div>

    <section class="shell-body">
      <header class="shell-topbar">
        <button
          type="button"
          class="menu-button"
          :aria-label="mobileMenuOpen ? '关闭导航' : '打开导航'"
          aria-controls="app-shell-sidebar"
          :aria-expanded="mobileMenuOpen ? 'true' : 'false'"
          @click="toggleMobileMenu"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        <div class="page-context">
          <span>{{ contextLabel }}</span>
          <strong>{{ pageTitle }}</strong>
        </div>

        <div class="user-box">
          <div>
            <strong>{{ userName }}</strong>
            <span>{{ userAccount }}</span>
          </div>
          <button type="button" @click="handleLogout">退出</button>
        </div>
      </header>

      <main class="shell-content">
        <slot></slot>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
  getNavigationSections,
  isNavigationItemActive
} from '../router/navigation'

const route = useRoute()
const router = useRouter()
const store = useStore()

const mobileMenuOpen = ref(false)
const isMobileViewport = ref(false)
const user = computed(() => store.getters['auth/currentUser'] || {})
const isAdmin = computed(() => store.getters['auth/isAdmin'])
const navigationSections = computed(() => getNavigationSections(isAdmin.value))
const homePath = computed(() => (isAdmin.value ? '/admin' : '/dashboard'))
const pageTitle = computed(() => String(route.meta?.title || '工作台'))

const contextLabel = computed(() => {
  if (route.meta?.requiresAdmin || route.path.startsWith('/admin')) {
    return '管理台'
  }
  if (route.path.startsWith('/exercise/exam')) {
    return '考试'
  }
  if (route.path.startsWith('/practice') || route.path.startsWith('/wrong-questions')) {
    return '练习'
  }
  return '学习'
})

const userName = computed(() => user.value.nickname || user.value.username || '用户')
const userAccount = computed(() => user.value.phone || user.value.id || '已登录')
const userRoleText = computed(() => (isAdmin.value ? '管理员工作区' : '学习工作区'))

const isActive = (item) => isNavigationItemActive(route.path, item)

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const handleLogout = async () => {
  await store.dispatch('auth/logout')
  closeMobileMenu()
  router.replace('/login')
}

let mobileMediaQuery

const syncMobileViewport = () => {
  isMobileViewport.value = Boolean(mobileMediaQuery?.matches)
}

watch(
  () => route.fullPath,
  () => closeMobileMenu()
)

onMounted(() => {
  mobileMediaQuery = window.matchMedia('(max-width: 980px)')
  syncMobileViewport()
  if (mobileMediaQuery.addEventListener) {
    mobileMediaQuery.addEventListener('change', syncMobileViewport)
  } else {
    mobileMediaQuery.addListener(syncMobileViewport)
  }
})

onBeforeUnmount(() => {
  if (!mobileMediaQuery) {
    return
  }
  if (mobileMediaQuery.removeEventListener) {
    mobileMediaQuery.removeEventListener('change', syncMobileViewport)
  } else {
    mobileMediaQuery.removeListener(syncMobileViewport)
  }
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 248px minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.12), transparent 28%),
    linear-gradient(135deg, #f6f8fb 0%, #eef4f8 100%);
}

.shell-sidebar {
  position: sticky;
  top: 0;
  z-index: 40;
  height: 100vh;
  padding: 22px 14px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 20px;
  border-right: 1px solid #dbe4ee;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(18px);
}

.brand-block {
  padding: 0 8px 16px;
  border-bottom: 1px solid #e6edf5;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #17324d;
  text-decoration: none;
}

.brand-mark span {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #17324d;
  color: #fff;
  font-weight: 800;
}

.brand-mark strong {
  font-size: 20px;
  letter-spacing: 0;
}

.brand-block p {
  margin: 8px 0 0;
  color: #667085;
  font-size: 13px;
}

.shell-nav {
  overflow-y: auto;
  padding: 0 2px 10px;
}

.nav-section + .nav-section {
  margin-top: 18px;
}

.nav-section h2 {
  margin: 0 8px 8px;
  color: #8a98aa;
  font-size: 12px;
  font-weight: 700;
}

.nav-item {
  min-height: 42px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 8px;
  color: #344054;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.nav-item:hover,
.nav-item:focus-visible {
  background: #eef4ff;
  color: #17324d;
  transform: translateX(2px);
}

.brand-mark:focus-visible,
.nav-item:focus-visible,
.menu-button:focus-visible,
.user-box button:focus-visible {
  outline: 3px solid rgba(37, 99, 235, 0.24);
  outline-offset: 3px;
}

.nav-item.active {
  background: #17324d;
  color: #fff;
  box-shadow: 0 12px 24px rgba(23, 50, 77, 0.18);
}

.nav-icon {
  width: 24px;
  height: 24px;
  border-radius: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(23, 50, 77, 0.08);
  font-size: 13px;
  font-weight: 700;
}

.nav-item.active .nav-icon {
  background: rgba(255, 255, 255, 0.18);
}

.shell-body {
  min-width: 0;
  display: grid;
  grid-template-rows: auto 1fr;
}

.shell-topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  min-height: 68px;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid rgba(219, 228, 238, 0.88);
  background: rgba(246, 248, 251, 0.82);
  backdrop-filter: blur(16px);
}

.page-context {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.page-context span {
  color: #667085;
  font-size: 12px;
}

.page-context strong {
  color: #111827;
  font-size: 18px;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-wrap: balance;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-box div {
  display: grid;
  gap: 2px;
  text-align: right;
}

.user-box strong {
  color: #111827;
  font-size: 14px;
}

.user-box span {
  color: #667085;
  font-size: 12px;
}

.user-box button {
  min-width: 44px;
  min-height: 40px;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  padding: 0 12px;
  background: #fff;
  color: #344054;
  cursor: pointer;
  transition: background-color 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.user-box button:hover {
  border-color: #98a2b3;
  background: #f9fafb;
  transform: translateY(-1px);
}

.shell-content {
  min-width: 0;
}

.menu-button {
  display: none;
  width: 40px;
  height: 40px;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: background-color 0.18s ease, border-color 0.18s ease;
}

.menu-button:hover {
  border-color: #98a2b3;
  background: #f9fafb;
}

.menu-button span {
  display: block;
  width: 18px;
  height: 2px;
  margin: 4px auto;
  border-radius: 999px;
  background: #17324d;
}

.shell-mask {
  display: none;
}

@media (max-width: 980px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .shell-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    width: min(82vw, 300px);
    transform: translateX(-105%);
    transition: transform 0.22s ease;
    box-shadow: 20px 0 44px rgba(15, 23, 42, 0.18);
  }

  .shell-sidebar.open {
    transform: translateX(0);
  }

  .shell-mask {
    position: fixed;
    inset: 0;
    z-index: 35;
    display: block;
    background: rgba(15, 23, 42, 0.34);
  }

  .menu-button {
    display: inline-block;
    flex-shrink: 0;
  }

  .shell-topbar {
    padding: 10px 14px;
  }

  .user-box div {
    display: none;
  }
}

@media (max-width: 560px) {
  .shell-topbar {
    gap: 10px;
  }

  .page-context {
    flex: 1;
  }

  .page-context strong {
    font-size: 16px;
    white-space: normal;
  }

  .user-box button {
    padding: 0 10px;
  }
}
</style>
