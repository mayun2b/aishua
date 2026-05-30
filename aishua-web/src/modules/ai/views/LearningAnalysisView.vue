<template>
  <div class="analysis-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Learning Analysis</p>
        <h1>学情分析</h1>
        <p class="description">
          选择学科后，AI 会基于你的问题给出学习情况分析、薄弱点提示和可执行建议。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/my-subjects">我的学科</router-link>
      </div>
    </section>

    <section class="form-card">
      <div class="form-grid">
        <label>
          <span>学科</span>
          <select v-model="form.subjectId">
            <option value="">请选择学科</option>
            <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
              {{ subject.name }}{{ subject.code ? `（${subject.code}）` : '' }}
            </option>
          </select>
        </label>

        <label>
          <span>年级（可选）</span>
          <input v-model="form.grade" type="text" placeholder="例如：高一">
        </label>

        <label>
          <span>教材版本（可选）</span>
          <input v-model="form.textbookVersion" type="text" placeholder="例如：人教版">
        </label>
      </div>

      <label class="query-field">
        <span>分析问题</span>
        <textarea
          v-model="form.query"
          rows="6"
          placeholder="例如：请结合我在该学科的最近练习情况，给出掌握程度评估、薄弱点和下周学习计划。"
        />
      </label>

      <div class="form-actions">
        <button type="button" class="ghost" @click="fillTemplateQuery">生成推荐问题</button>
        <button type="button" class="ghost" @click="resetForm">重置</button>
        <button type="button" :disabled="loading" @click="runAnalysis">
          {{ loading ? '分析中...' : '开始分析' }}
        </button>
      </div>
    </section>

    <section class="result-card">
      <div class="result-head">
        <h2>分析结果</h2>
        <span>{{ analysisAt || '尚未分析' }}</span>
      </div>

      <div v-if="loading" class="empty-state">正在进行学情分析，请稍候...</div>
      <div v-else-if="!resultPayload" class="empty-state">请先选择学科并提交分析问题。</div>
      <div v-else class="result-content">
        <article v-if="summaryText" class="summary-card">
          <h3>AI 总结</h3>
          <p>{{ summaryText }}</p>
        </article>

        <article v-if="outputItems.length" class="output-card">
          <h3>结构化输出</h3>
          <div class="output-grid">
            <div v-for="item in outputItems" :key="item.label" class="output-item">
              <strong>{{ item.label }}</strong>
              <p>{{ item.value }}</p>
            </div>
          </div>
        </article>

        <details class="raw-card">
          <summary>查看原始响应 JSON</summary>
          <pre>{{ prettyResult }}</pre>
        </details>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import learningAnalysisApi from '../api/learningAnalysis'
import subjectApi from '../../subject/api/subject'

const route = useRoute()

const loading = ref(false)
const subjects = ref([])
const resultPayload = ref(null)
const analysisAt = ref('')

const form = reactive({
  subjectId: '',
  grade: '',
  textbookVersion: '',
  query: ''
})

const selectedSubject = computed(() => {
  return subjects.value.find((item) => String(item.subjectId) === String(form.subjectId)) || null
})

const outputObject = computed(() => {
  const payload = resultPayload.value || {}
  return payload?.data?.outputs || payload?.outputs || null
})

const summaryText = computed(() => {
  const outputs = outputObject.value || {}
  const candidates = [
    outputs.summary,
    outputs.analysis,
    outputs.answer,
    outputs.result,
    resultPayload.value?.answer,
    resultPayload.value?.message
  ]
  for (const candidate of candidates) {
    const normalized = normalizeOutputValue(candidate)
    if (normalized) {
      return normalized
    }
  }
  return ''
})

const outputItems = computed(() => {
  const outputs = outputObject.value
  if (!outputs || typeof outputs !== 'object' || Array.isArray(outputs)) {
    return []
  }
  return Object.entries(outputs)
    .filter(([key]) => !['summary', 'analysis', 'answer', 'result'].includes(key))
    .map(([key, value]) => ({
      label: key,
      value: normalizeOutputValue(value) || '-'
    }))
})

const prettyResult = computed(() => {
  if (!resultPayload.value) {
    return ''
  }
  return JSON.stringify(resultPayload.value, null, 2)
})

const parseGradeFromSubjectName = (subjectName) => {
  if (!subjectName) {
    return ''
  }
  const gradePrefixes = ['高一', '高二', '高三', '初一', '初二', '初三', '小一', '小二', '小三', '小四', '小五', '小六']
  const matchedPrefix = gradePrefixes.find((prefix) => String(subjectName).startsWith(prefix))
  return matchedPrefix || ''
}

const buildTemplateQuery = (subjectName) => {
  return `请围绕我在${subjectName || '该学科'}的学习情况，输出一份学情分析，包含：1）当前掌握程度；2）薄弱知识点；3）下一阶段三条可执行学习建议；4）建议练习顺序。`
}

const normalizeNullableText = (value) => {
  if (value == null) {
    return undefined
  }
  const text = String(value).trim()
  return text ? text : undefined
}

const normalizeNullableSubjectId = (value) => {
  if (value == null || value === '') {
    return undefined
  }
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined
}

const normalizeOutputValue = (value) => {
  if (value == null) {
    return ''
  }
  if (typeof value === 'string') {
    return value.trim()
  }
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }
  if (Array.isArray(value)) {
    return value.map((item) => normalizeOutputValue(item)).filter(Boolean).join('；')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}

const resolveRouteSubjectId = () => {
  return route.query.subjectId ? String(route.query.subjectId) : ''
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const fillTemplateQuery = () => {
  if (!selectedSubject.value) {
    showToast('请先选择学科')
    return
  }
  form.query = buildTemplateQuery(selectedSubject.value.name)
}

const resetForm = () => {
  form.grade = parseGradeFromSubjectName(selectedSubject.value?.name)
  form.textbookVersion = ''
  form.query = selectedSubject.value ? buildTemplateQuery(selectedSubject.value.name) : ''
}

const runAnalysis = async () => {
  if (!selectedSubject.value) {
    showToast('请先选择学科')
    return
  }
  if (!form.query.trim()) {
    showToast('请输入分析问题')
    return
  }

  loading.value = true
  try {
    const response = await learningAnalysisApi.run({
      query: form.query.trim(),
      subject: selectedSubject.value.name,
      subjectId: normalizeNullableSubjectId(form.subjectId),
      grade: normalizeNullableText(form.grade),
      textbookVersion: normalizeNullableText(form.textbookVersion)
    })
    resultPayload.value = response.data || null
    analysisAt.value = new Date().toLocaleString('zh-CN', { hour12: false })
    showToast('学情分析完成')
  } catch (error) {
    showToast(error.message || '学情分析失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => selectedSubject.value,
  (subject) => {
    if (!subject) {
      return
    }
    if (!form.grade) {
      form.grade = parseGradeFromSubjectName(subject.name)
    }
    if (!form.query.trim()) {
      form.query = buildTemplateQuery(subject.name)
    }
  }
)

watch(
  () => route.query.subjectId,
  () => {
    const subjectId = resolveRouteSubjectId()
    if (subjectId) {
      form.subjectId = subjectId
    }
  }
)

onMounted(async () => {
  await loadSubjects()
  const subjectId = resolveRouteSubjectId()
  if (subjectId) {
    form.subjectId = subjectId
  }
})
</script>

<style scoped>
.analysis-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f3efe8 0%, #dbe7f8 100%);
}

.hero-card,
.form-card,
.result-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.94);
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

.hero-actions,
.form-actions {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.form-card,
.result-card {
  margin-top: 18px;
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.form-grid label,
.query-field {
  display: grid;
  gap: 8px;
}

.form-grid span,
.query-field span {
  color: #17324d;
  font-size: 14px;
  font-weight: 600;
}

.form-grid select,
.form-grid input,
.query-field textarea {
  width: 100%;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  padding: 12px 14px;
  box-sizing: border-box;
  font-size: 14px;
  color: #17324d;
  background: #fff;
}

.query-field {
  margin-top: 16px;
}

.query-field textarea {
  resize: vertical;
  min-height: 140px;
  line-height: 1.6;
}

.form-actions {
  margin-top: 16px;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.result-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.result-head span {
  color: #6c7a8d;
  font-size: 13px;
}

.empty-state {
  margin-top: 16px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.result-content {
  margin-top: 16px;
  display: grid;
  gap: 14px;
}

.summary-card,
.output-card,
.raw-card {
  padding: 18px;
  border-radius: 18px;
  background: #f7fbff;
}

.summary-card h3,
.output-card h3 {
  margin: 0;
  color: #17324d;
  font-size: 18px;
}

.summary-card p {
  margin: 10px 0 0;
  color: #334b64;
  line-height: 1.8;
  white-space: pre-wrap;
}

.output-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.output-item {
  padding: 12px;
  border-radius: 12px;
  background: #edf4fb;
}

.output-item strong {
  display: block;
  color: #17324d;
  font-size: 13px;
}

.output-item p {
  margin: 8px 0 0;
  color: #42596f;
  line-height: 1.6;
  white-space: pre-wrap;
}

.raw-card summary {
  cursor: pointer;
  color: #17324d;
  font-weight: 600;
}

.raw-card pre {
  margin: 12px 0 0;
  padding: 12px;
  border-radius: 12px;
  background: #101923;
  color: #d7e3f0;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
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
  opacity: 0.65;
  cursor: not-allowed;
}

.ghost {
  background: #e5edf6;
  color: #17324d;
}

@media (max-width: 768px) {
  .analysis-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .form-card,
  .result-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
