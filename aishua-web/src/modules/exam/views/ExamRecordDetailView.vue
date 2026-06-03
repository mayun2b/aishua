<template>
  <div class="exam-detail-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Exam Detail</p>
        <h1>{{ record?.examName || '考试详情' }}</h1>
        <p class="description">
          展示本次考试每道题的作答结果、标准答案与解析，便于复盘错题与知识点。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/exercise/exam/records">返回记录列表</router-link>
        <router-link class="ghost" to="/exercise/exam">继续考试</router-link>
      </div>
    </section>

    <section v-if="loading" class="state-card">正在加载考试详情...</section>

    <template v-else-if="record">
      <section class="summary-card">
        <div class="summary-grid">
          <div>
            <span>学科</span>
            <strong>{{ record.subjectName || '-' }}</strong>
          </div>
          <div>
            <span>考试得分</span>
            <strong>{{ formatScore(record.score) }} 分</strong>
          </div>
          <div>
            <span>答对题数</span>
            <strong>{{ record.correctQuestions || 0 }} / {{ record.totalQuestions || 0 }}</strong>
          </div>
          <div>
            <span>考试时长</span>
            <strong>{{ formatDurationMinutes(record.duration) }}</strong>
          </div>
          <div>
            <span>开考时间</span>
            <strong>{{ formatDateTime(record.startTime) }}</strong>
          </div>
          <div>
            <span>交卷时间</span>
            <strong>{{ formatDateTime(record.endTime) }}</strong>
          </div>
        </div>
      </section>

      <section class="question-card">
        <div class="table-head">
          <h2>题目明细</h2>
          <span>{{ questions.length }} 题</span>
        </div>

        <div v-if="!questions.length" class="empty-state">暂无可展示的答题明细</div>
        <article v-for="(item, index) in questions" :key="item.id || `${item.questionId}-${index}`" class="question-item">
          <header class="question-head">
            <div>
              <span class="question-index">第 {{ index + 1 }} 题</span>
              <h3>{{ item.title || '-' }}</h3>
            </div>
            <div class="head-tags">
              <span class="pill">{{ resolveTypeLabel(item.type) }}</span>
              <span class="pill">{{ resolveDifficultyLabel(item.difficulty) }}</span>
              <span :class="['result-pill', Number(item.isCorrect) === 1 ? 'done' : 'wrong']">
                {{ Number(item.isCorrect) === 1 ? '回答正确' : '回答错误' }}
              </span>
            </div>
          </header>

          <QuestionImageList
            v-if="item.imageUrls"
            :image-urls="item.imageUrls"
            :image-desc="item.imageDesc"
            :annotation-object-names="resolveImageAnnotationObjectNames(item.userAnswer)"
            readonly
          />

          <div v-if="isChoiceQuestion(item.type) && parseQuestionOptions(item.options).length" class="option-list">
            <div
              v-for="option in parseQuestionOptions(item.options)"
              :key="option.optionKey"
              :class="[
                'option-row',
                isSelected(option.optionKey, item.userAnswer) ? 'selected' : '',
                isSelected(option.optionKey, item.standardAnswer) ? 'correct' : '',
                isWrongSelected(option.optionKey, item.userAnswer, item.standardAnswer) ? 'wrong' : ''
              ]"
            >
              <span class="option-key">{{ option.optionKey }}</span>
              <span>{{ option.optionText }}</span>
            </div>
          </div>


          <div class="answer-meta">
            <p><strong>你的答案：</strong>{{ formatAnswerDisplay(item.userAnswer) }}</p>
            <p><strong>标准答案：</strong>{{ formatAnswerDisplay(item.standardAnswer) }}</p>
            <p><strong>本题用时：</strong>{{ item.answerTime || 0 }} 秒</p>
          </div>

          <p v-if="item.analysis" class="analysis"><strong>解析：</strong>{{ item.analysis }}</p>
        </article>
      </section>
    </template>

    <section v-else class="state-card">
      <p>考试记录不存在或无权限访问。</p>
      <router-link class="ghost-link" to="/exercise/exam/records">返回记录列表</router-link>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import QuestionImageList from '@/components/QuestionImageList.vue'
import { parseImageAnnotationPayload } from '../../common/utils/imageAnnotationAnswer'
import examApi from '../api/exam'
import {
  formatAnswerDisplay,
  formatDateTime,
  formatDurationMinutes,
  formatScore,
  parseQuestionOptions,
  resolveDifficultyLabel,
  resolveTypeLabel,
  toAnswerTokens
} from '../utils/examHelpers'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const record = ref(null)
const questions = ref([])

const recordId = Number(route.params.recordId || 0)

const isChoiceQuestion = (type) => Number(type) === 1 || Number(type) === 2 || Number(type) === 3

const isSelected = (optionKey, answer) => {
  const tokens = toAnswerTokens(answer)
  return tokens.includes(String(optionKey || '').toUpperCase())
}

const isWrongSelected = (optionKey, userAnswer, standardAnswer) => {
  return isSelected(optionKey, userAnswer) && !isSelected(optionKey, standardAnswer)
}

const resolveImageAnnotationObjectNames = (userAnswer) => {
  const { annotations } = parseImageAnnotationPayload(userAnswer)
  return annotations.reduce((result, item) => {
    result[item.imageObjectName] = item.annotationObjectName
    return result
  }, {})
}

const loadDetail = async () => {
  if (!recordId) {
    showToast('考试记录不存在')
    router.replace('/exercise/exam/records')
    return
  }

  loading.value = true
  try {
    const [summaryRes, questionRes] = await Promise.all([
      examApi.getMyRecord(recordId),
      examApi.listMyRecordQuestions(recordId)
    ])

    if (!summaryRes?.data) {
      showToast('考试记录不存在')
      router.replace('/exercise/exam/records')
      return
    }

    if (Number(summaryRes.data.status) !== 2) {
      showToast('考试尚未提交，暂不支持查看复盘详情')
      router.replace('/exercise/exam/records')
      return
    }

    record.value = summaryRes.data
    questions.value = questionRes.data || []
  } catch (error) {
    showToast(error.message || '加载考试详情失败')
    router.replace('/exercise/exam/records')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.exam-detail-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #edf3ff 0%, #e8f7f3 100%);
}

.hero-card,
.summary-card,
.question-card,
.state-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(29, 57, 84, 0.12);
}

.hero-card {
  padding: 30px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  color: #78879a;
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
  margin: 12px 0 0;
  max-width: 760px;
  color: #607184;
  line-height: 1.75;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-self: flex-start;
}

.summary-card,
.question-card {
  margin-top: 18px;
  padding: 24px;
}

.state-card {
  margin-top: 18px;
  padding: 30px;
  text-align: center;
  color: #607184;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
}

.summary-grid div {
  background: #f4f8ff;
  border-radius: 12px;
  padding: 12px;
  display: grid;
  gap: 4px;
}

.summary-grid span {
  color: #607184;
  font-size: 12px;
}

.summary-grid strong {
  color: #17324d;
  font-size: 16px;
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-head h2 {
  margin: 0;
  color: #17324d;
}

.table-head span {
  color: #6f7f93;
  font-size: 14px;
}

.question-item {
  margin-top: 16px;
  border: 1px solid #d8e2ef;
  border-radius: 16px;
  padding: 16px;
}

.question-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.question-index {
  color: #6f8194;
  font-size: 12px;
}

.question-head h3 {
  margin: 6px 0 0;
  color: #17324d;
  line-height: 1.7;
}

.head-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.pill {
  border-radius: 999px;
  padding: 4px 9px;
  background: #f4f8ff;
  color: #4c6077;
  font-size: 12px;
}

.result-pill {
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 12px;
  font-weight: 600;
}

.result-pill.done {
  color: #2d8a44;
  background: #e7f8ed;
}

.result-pill.wrong {
  color: #bf2f2f;
  background: #feecec;
}

.option-list {
  margin-top: 14px;
  display: grid;
  gap: 8px;
}

.option-row {
  border: 1px solid #d8e2ef;
  border-radius: 10px;
  padding: 10px;
  color: #17324d;
  display: flex;
  gap: 10px;
}

.option-row.selected {
  background: #edf5ff;
}

.option-row.correct {
  border-color: #76b08a;
  background: #effaf2;
}

.option-row.wrong {
  border-color: #d88888;
  background: #fff0f0;
}

.option-key {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #e2ebf7;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.answer-meta {
  margin-top: 12px;
  color: #435b74;
  line-height: 1.8;
}

.answer-meta p {
  margin: 0;
}

.analysis {
  margin-top: 10px;
  color: #4f6680;
  line-height: 1.7;
}

button,
.ghost,
.ghost-link {
  border: 0;
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

.ghost,
.ghost-link {
  background: rgba(23, 50, 77, 0.08);
  color: #17324d;
}

.empty-state {
  margin-top: 14px;
  border: 1px dashed #cfdae8;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  color: #63768b;
}

@media (max-width: 768px) {
  .exam-detail-page {
    padding: 20px 14px 32px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>


