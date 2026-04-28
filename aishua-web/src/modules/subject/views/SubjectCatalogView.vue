<template>
  <div class="catalog-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">学科中心</p>
        <h1>选择你要学习的学科</h1>
        <p class="description">
          这里展示当前可学习的全部学科。加入后会进入“我的学科”，后续练习、统计等能力都会基于你的学科列表展开。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/my-subjects">我的学科</router-link>
      </div>
    </section>

    <section class="summary-card">
      <span>可加入学科：{{ subjects.length }}</span>
      <span>已加入学科：{{ joinedCount }}</span>
    </section>

    <section class="list-card">
      <div v-if="loading" class="empty-state">正在加载学科列表...</div>
      <div v-else-if="!subjects.length" class="empty-state">当前暂无可加入学科</div>
      <div v-else class="subject-grid">
        <article v-for="subject in pagedSubjects" :key="subject.id" class="subject-item">
          <div class="subject-head">
            <strong>{{ subject.name }}</strong>
            <span>{{ subject.code }}</span>
          </div>

          <p class="meta">
            题量：{{ subject.questionCount ?? 0 }} |
            排序：{{ subject.sort ?? 0 }}
          </p>
          <p class="description-text">{{ subject.description || '暂无学科描述' }}</p>

          <button
            type="button"
            :disabled="subject.joined || joiningId === subject.id"
            @click="joinSubject(subject)"
          >
            {{ subject.joined ? '已加入' : joiningId === subject.id ? '加入中...' : '加入学科' }}
          </button>
        </article>
      </div>
      <BasePagination v-if="!loading && subjects.length" v-model="currentPage" :total="subjects.length" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import { showToast } from 'vant'
import subjectApi from '../api/subject'

const loading = ref(false)
const joiningId = ref(null)
const subjects = ref([])
const { currentPage, pagedItems: pagedSubjects } = useClientPagination(subjects)

const joinedCount = computed(() => subjects.value.filter((subject) => subject.joined).length)

const loadSubjects = async () => {
  loading.value = true
  try {
    const response = await subjectApi.listCatalog()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科列表失败')
  } finally {
    loading.value = false
  }
}

const joinSubject = async (subject) => {
  if (subject.joined) {
    return
  }

  joiningId.value = subject.id
  try {
    await subjectApi.join(subject.id)
    showToast(`已加入学科：${subject.name}`)
    await loadSubjects()
  } catch (error) {
    showToast(error.message || '加入学科失败')
  } finally {
    joiningId.value = null
  }
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped>
.catalog-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f4f0e8 0%, #dcecf6 100%);
}

.hero-card,
.summary-card,
.list-card {
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

.summary-card {
  margin-top: 18px;
  padding: 18px 26px;
  display: flex;
  gap: 28px;
  color: #17324d;
  font-weight: 600;
}

.list-card {
  margin-top: 18px;
  padding: 24px;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.subject-item {
  padding: 18px;
  border: 1px solid #dce5ef;
  border-radius: 18px;
  background: #f8fbff;
  display: grid;
  gap: 10px;
}

.subject-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.subject-head strong {
  color: #17324d;
  font-size: 18px;
}

.subject-head span {
  color: #6c7a8d;
  font-size: 13px;
}

.meta {
  margin: 0;
  color: #5e6d7c;
  font-size: 13px;
}

.description-text {
  margin: 0;
  color: #4d6074;
  line-height: 1.6;
  min-height: 50px;
}

.empty-state {
  padding: 36px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

button,
.ghost {
  border: 0;
  border-radius: 14px;
  padding: 11px 16px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
}

button {
  background: #17324d;
  color: #fff;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.ghost {
  background: #e5edf6;
  color: #17324d;
}

@media (max-width: 768px) {
  .catalog-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .summary-card,
  .list-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }

  .summary-card {
    gap: 12px;
    flex-direction: column;
  }
}
</style>
