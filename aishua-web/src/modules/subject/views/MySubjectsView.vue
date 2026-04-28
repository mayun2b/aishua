<template>
  <div class="my-subject-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">我的学科</p>
        <h1>{{ greeting }}</h1>
        <p class="description">
          这里展示你已加入的学科。练习闭环会从这些学科里发起，所以可以直接在列表中进入练习。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/subjects">去加入学科</router-link>
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>已加入 {{ subjects.length }} 个学科</h2>
      </div>

      <div v-if="loading" class="empty-state">正在加载我的学科...</div>
      <div v-else-if="!subjects.length" class="empty-state">
        你还没有加入任何学科，先去“学科列表”选择一个学习方向。
      </div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>学科</th>
              <th>题量</th>
              <th>状态</th>
              <th>加入时间</th>
              <th>最近练习</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="subject in pagedSubjects" :key="subject.userSubjectId">
              <td>
                <div class="subject-name">
                  <strong>{{ subject.name }}</strong>
                  <span>{{ subject.code }}</span>
                </div>
              </td>
              <td>{{ subject.questionCount ?? 0 }}</td>
              <td>
                <span class="status-chip" :class="resolveStatusClass(subject)">
                  {{ resolveStatusText(subject) }}
                </span>
              </td>
              <td>{{ formatDateTime(subject.joinedAt) }}</td>
              <td>{{ formatDateTime(subject.lastPracticeAt) }}</td>
              <td>
                <router-link class="practice-link" :to="`/practice?subjectId=${subject.subjectId}`">
                  开始练习
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination v-if="!loading && subjects.length" v-model="currentPage" :total="subjects.length" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import subjectApi from '../api/subject'

const store = useStore()
const loading = ref(false)
const subjects = ref([])
const { currentPage, pagedItems: pagedSubjects } = useClientPagination(subjects)

const user = computed(() => store.getters['auth/currentUser'])
const greeting = computed(() => `${user.value?.nickname || '同学'}，这是你的学科学习清单`)

const loadMySubjects = async () => {
  loading.value = true
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载我的学科失败')
  } finally {
    loading.value = false
  }
}

const resolveStatusText = (subject) => {
  if (subject.isEnabled !== 1) {
    return '学科已停用'
  }
  return subject.status === 1 ? '学习中' : '已暂停'
}

const resolveStatusClass = (subject) => {
  if (subject.isEnabled !== 1) {
    return 'disabled'
  }
  return subject.status === 1 ? 'active' : 'paused'
}

const formatDateTime = (value) => {
  if (!value) {
    return '暂无记录'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  loadMySubjects()
})
</script>

<style scoped>
.my-subject-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f0f6eb 0%, #e0eaf8 100%);
}

.hero-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.93);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card {
  padding: 30px;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  color: #7a8793;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 12px 0 0;
  color: #17324d;
  font-size: 34px;
}

.description {
  margin: 14px 0 0;
  max-width: 760px;
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.table-card {
  margin-top: 18px;
  padding: 24px;
}

.table-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.table-wrap {
  margin-top: 18px;
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 860px;
}

th,
td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid #e5edf5;
}

th {
  color: #6c7a8d;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.subject-name {
  display: grid;
  gap: 6px;
}

.subject-name strong {
  color: #17324d;
}

.subject-name span {
  color: #5e6d7c;
  font-size: 13px;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
}

.status-chip.active {
  background: #ddf5e9;
  color: #0f7a43;
}

.status-chip.paused {
  background: #fff4e5;
  color: #b54708;
}

.status-chip.disabled {
  background: #fce7e7;
  color: #b42318;
}

.empty-state {
  margin-top: 18px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.ghost {
  border: 0;
  border-radius: 14px;
  padding: 11px 16px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  background: #e5edf6;
  color: #17324d;
}

.practice-link {
  display: inline-flex;
  align-items: center;
  border-radius: 12px;
  padding: 8px 12px;
  background: #17324d;
  color: #fff;
  text-decoration: none;
  font-size: 13px;
}

@media (max-width: 768px) {
  .my-subject-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .table-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
