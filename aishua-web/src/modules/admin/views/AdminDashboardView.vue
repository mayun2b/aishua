<template>
  <div class="admin-dashboard-page">
    <section class="dashboard-card">
      <div class="hero-copy">
        <p class="eyebrow">Admin Console</p>
        <h1>{{ user?.nickname || '管理员' }}，进入管理台</h1>
        <p class="description">
          管理学科、教材目录、考点标签、题库和试卷。全局导航已经放在左侧，这里只保留高频管理入口。
        </p>
      </div>

      <div class="panel-grid">
        <router-link v-for="item in adminLinks" :key="item.to" class="panel" :to="item.to">
          <span>{{ item.kicker }}</span>
          <strong>{{ item.label }}</strong>
          <p>{{ item.desc }}</p>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const user = computed(() => store.getters['auth/currentUser'])

const adminLinks = [
  {
    to: '/admin/subjects',
    kicker: 'Subjects',
    label: '学科管理',
    desc: '维护学科基础信息和学科入口。'
  },
  {
    to: '/admin/directories',
    kicker: 'Directories',
    label: '目录管理',
    desc: '维护教材目录树和章节结构。'
  },
  {
    to: '/admin/tags',
    kicker: 'Knowledge',
    label: '考点标签',
    desc: '管理知识点标签，支撑练习与分析。'
  },
  {
    to: '/admin/directory-tags',
    kicker: 'Mapping',
    label: '目录-考点',
    desc: '维护章节和考点映射，支撑配题和章节练习。'
  },
  {
    to: '/admin/questions',
    kicker: 'Question Bank',
    label: '题库管理',
    desc: '维护题目、答案、解析和题图。'
  },
  {
    to: '/admin/exams',
    kicker: 'Papers',
    label: '试卷管理',
    desc: '配置模拟考试试卷和试题。'
  }
]
</script>

<style scoped>
.admin-dashboard-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: transparent;
}

.dashboard-card {
  max-width: 1120px;
  margin: 0 auto;
  padding: 28px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
}

.hero-copy {
  max-width: 760px;
}

.eyebrow {
  margin: 0;
  color: #667085;
  font-size: 12px;
  letter-spacing: 0;
  text-transform: uppercase;
}

.dashboard-card h1 {
  margin: 10px 0 0;
  color: #111827;
  font-size: 32px;
  line-height: 1.25;
}

.description {
  margin: 14px 0 0;
  color: #5f6f82;
  line-height: 1.7;
}

.panel-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.panel {
  min-height: 156px;
  padding: 18px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #f8fafc;
  display: grid;
  align-content: space-between;
  gap: 10px;
  color: inherit;
  text-decoration: none;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.panel:hover {
  border-color: #b9c9dd;
  box-shadow: 0 16px 28px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.panel span {
  color: #667085;
  font-size: 12px;
}

.panel strong {
  color: #17324d;
  font-size: 20px;
}

.panel p {
  margin: 0;
  color: #5f6f82;
  line-height: 1.6;
}

@media (max-width: 560px) {
  .admin-dashboard-page {
    padding: 20px 14px 36px;
  }

  .dashboard-card {
    padding: 20px;
  }

  .dashboard-card h1 {
    font-size: 26px;
  }
}
</style>
