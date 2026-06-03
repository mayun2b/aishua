<template>
  <div class="command-center">
    <header class="screen-head">
      <div class="head-title">
        <p class="eyebrow">Learning Command Center</p>
        <h1>{{ user?.nickname || '同学' }} · 学情战情大屏</h1>
        <small>更新时间：{{ updateTimeText }}</small>
      </div>

      <div class="head-actions">
        <div class="range-control" aria-label="统计周期">
          <button
            v-for="option in dayOptions"
            :key="option"
            type="button"
            :class="{ active: selectedDays === option }"
            @click="changeDays(option)"
          >
            近 {{ option }} 天
          </button>
        </div>
        <button type="button" class="ghost" @click="loadStats">刷新</button>
        <router-link class="ghost" to="/dashboard">返回看板</router-link>
      </div>
    </header>

    <section class="alert-strip">
      <article class="alert-card danger">
        <span>高风险知识点</span>
        <strong>{{ highRiskCount }}</strong>
        <small>样本充足且正确率低于 60%</small>
      </article>
      <article class="alert-card warn">
        <span>本周下滑预警</span>
        <strong>{{ downTrendCount }}</strong>
        <small>近 7 天更新且正确率低于 70%</small>
      </article>
      <article class="alert-card info">
        <span>样本不足知识点</span>
        <strong>{{ lowSampleCount }}</strong>
        <small>作答量小于 {{ sampleThreshold }} 题</small>
      </article>
      <article class="alert-card good">
        <span>整体趋势</span>
        <strong>{{ weeklyTrendText }}</strong>
        <small>近 7 天 vs 前 7 天正确率变化</small>
      </article>
    </section>

    <section class="kpi-grid">
      <article class="kpi-card">
        <span>整体正确率</span>
        <strong>{{ formatRate(overview.correctRate) }}</strong>
        <small>{{ formatNumber(overview.correctCount) }} / {{ formatNumber(overview.totalCount) }} 题</small>
      </article>
      <article class="kpi-card">
        <span>已掌握占比</span>
        <strong>{{ masteredRatioText }}</strong>
        <small>{{ masteredCount }} / {{ masteryItems.length }} 个知识点达到熟悉及以上</small>
      </article>
      <article class="kpi-card">
        <span>连续学习</span>
        <strong>{{ formatNumber(overview.continuousDays) }} 天</strong>
        <small>历史最高 {{ formatNumber(overview.maxContinuousDays) }} 天</small>
      </article>
      <article class="kpi-card">
        <span>今日练习量</span>
        <strong>{{ formatNumber(overview.todayCount) }} 题</strong>
        <small>今日用时 {{ formatTime(overview.todayTimeCost) }}</small>
      </article>
    </section>

    <section v-if="loading" class="loading-block">正在加载学情大屏数据...</section>

    <template v-else>
      <section class="middle-grid">
        <article class="panel matrix-panel">
          <div class="panel-head">
            <h2>掌握度优先级矩阵</h2>
            <span>右下角 = 高优先补救区</span>
          </div>
          <div ref="matrixChartRef" class="chart-canvas" aria-label="掌握度优先级矩阵"></div>
        </article>

        <article class="panel top-risk-panel">
          <div class="panel-head">
            <h2>重点补救 Top 5</h2>
            <span>按风险分排序</span>
          </div>

          <div v-if="topRiskItems.length" class="risk-list">
            <article v-for="(item, index) in topRiskItems" :key="`${item.subjectId}-${item.tagId}`" class="risk-row">
              <div class="risk-rank">{{ index + 1 }}</div>
              <div class="risk-main">
                <strong>{{ item.tagName || '-' }}</strong>
                <p>{{ item.subjectName || '-' }}</p>
                <div class="risk-meta">
                  <span>正确率 {{ formatRate(item.correctRate) }}</span>
                  <span>样本 {{ item.totalCount || 0 }} 题</span>
                  <span>风险分 {{ item.riskScore }}</span>
                </div>
                <div class="risk-bar">
                  <span :style="{ width: `${Math.min(item.riskScore, 100)}%` }"></span>
                </div>
                <small>{{ item.actionText }}</small>
              </div>
            </article>
          </div>
          <div v-else class="empty-state">暂无可识别的风险知识点</div>
        </article>
      </section>

      <section class="panel trend-panel">
        <div class="panel-head">
          <h2>学习趋势带</h2>
          <span>正确率趋势 + 弱项预警指数（近 {{ selectedDays }} 天）</span>
        </div>
        <div ref="trendChartRef" class="chart-canvas trend-canvas" aria-label="学习趋势图"></div>
      </section>
    </template>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { showToast } from 'vant'
import { useStore } from 'vuex'
import practiceApi from '../../practice/api/practice'

const store = useStore()

const dayOptions = [7, 30, 90]
const selectedDays = ref(30)
const loading = ref(false)
const lastLoadedAt = ref('')
const sampleThreshold = 20

const matrixChartRef = ref(null)
const trendChartRef = ref(null)

let matrixChartInstance = null
let trendChartInstance = null
let resizeObserver = null

const emptyStats = () => ({
  overview: {},
  dailyTrends: [],
  knowledgeMasteries: []
})

const stats = ref(emptyStats())

const user = computed(() => store.getters['auth/currentUser'])
const overview = computed(() => stats.value.overview || {})
const trendItems = computed(() => stats.value.dailyTrends || [])
const masteryItems = computed(() => stats.value.knowledgeMasteries || [])

const matrixItems = computed(() => {
  return masteryItems.value
    .filter((item) => Number(item.totalCount || 0) > 0)
    .map((item) => {
      const totalCount = Number(item.totalCount || 0)
      const correctRate = Number(item.correctRate || 0)
      const masteryLevel = Number(item.masteryLevel || 0)
      const sampleWeight = Math.min(totalCount / sampleThreshold, 1)
      const levelPenalty = Math.max(0, 3 - masteryLevel) * 10
      const riskScore = Math.round(((100 - correctRate) * 0.7 + levelPenalty) * sampleWeight + (sampleWeight < 1 ? 15 : 0))

      let priority = '稳定'
      if (totalCount < sampleThreshold) {
        priority = '样本不足'
      } else if (correctRate < 60) {
        priority = '高危'
      } else if (correctRate < 75) {
        priority = '关注'
      }

      return {
        ...item,
        totalCount,
        correctRate,
        masteryLevel,
        riskScore: Math.min(Math.max(riskScore, 0), 100),
        priority,
        actionText: resolveActionText({ totalCount, correctRate, masteryLevel })
      }
    })
})

const topRiskItems = computed(() => {
  return [...matrixItems.value]
    .sort((left, right) => right.riskScore - left.riskScore)
    .slice(0, 5)
})

const highRiskCount = computed(() => {
  return matrixItems.value.filter((item) => item.totalCount >= sampleThreshold && item.correctRate < 60).length
})

const lowSampleCount = computed(() => {
  return matrixItems.value.filter((item) => item.totalCount < sampleThreshold).length
})

const downTrendCount = computed(() => {
  return matrixItems.value.filter((item) => {
    return item.totalCount >= sampleThreshold
      && item.correctRate < 70
      && isWithinDays(item.updateTime, 7)
  }).length
})

const masteredCount = computed(() => {
  return matrixItems.value.filter((item) => item.masteryLevel >= 2).length
})

const masteredRatioText = computed(() => {
  if (!matrixItems.value.length) {
    return '0%'
  }
  return `${Math.round((masteredCount.value / matrixItems.value.length) * 100)}%`
})

const weeklyTrendDelta = computed(() => {
  const daily = trendItems.value
  if (!daily.length) {
    return 0
  }
  const recent = daily.slice(-7)
  const prev = daily.slice(-14, -7)
  const recentRate = aggregateRate(recent)
  const prevRate = aggregateRate(prev)
  return Math.round((recentRate - prevRate) * 10) / 10
})

const weeklyTrendText = computed(() => {
  const delta = weeklyTrendDelta.value
  if (delta > 0) {
    return `↑ ${delta}%`
  }
  if (delta < 0) {
    return `↓ ${Math.abs(delta)}%`
  }
  return '→ 0%'
})

const updateTimeText = computed(() => {
  if (!lastLoadedAt.value) {
    return '-'
  }
  return formatDateTime(lastLoadedAt.value)
})

const loadStats = async () => {
  loading.value = true
  try {
    const response = await practiceApi.getStats({ days: selectedDays.value })
    stats.value = {
      ...emptyStats(),
      ...(response.data || {})
    }
    lastLoadedAt.value = new Date().toISOString()
  } catch (error) {
    showToast(error.message || '加载学情大屏失败')
  } finally {
    loading.value = false
  }
}

const changeDays = async (days) => {
  if (selectedDays.value === days) {
    return
  }
  selectedDays.value = days
  await loadStats()
  await nextTick()
  renderCharts()
}

const aggregateRate = (items) => {
  if (!items.length) {
    return 0
  }
  const totalCount = items.reduce((sum, item) => sum + Number(item.doCount || 0), 0)
  if (totalCount <= 0) {
    return 0
  }
  const correctCount = items.reduce((sum, item) => sum + Number(item.correctCount || 0), 0)
  return Math.round((correctCount / totalCount) * 1000) / 10
}

const resolveActionText = ({ totalCount, correctRate, masteryLevel }) => {
  if (totalCount < sampleThreshold) {
    return `先补到 ${sampleThreshold} 题，再判断稳定性`
  }
  if (correctRate < 60) {
    return '今天优先做基础题 12 题，做完立即复盘错因'
  }
  if (correctRate < 75 || masteryLevel <= 1) {
    return '安排同类题 8 题，重点练易错步骤'
  }
  return '每周复习 3 题，维持熟练度'
}

const isWithinDays = (value, days) => {
  if (!value) {
    return false
  }
  const targetDate = new Date(value)
  if (Number.isNaN(targetDate.getTime())) {
    return false
  }
  const offset = Date.now() - targetDate.getTime()
  return offset >= 0 && offset <= days * 24 * 60 * 60 * 1000
}

const formatNumber = (value) => Number(value || 0).toLocaleString('zh-CN')

const formatRate = (value) => {
  const rate = Number(value || 0)
  const rounded = Math.round(rate * 10) / 10
  return `${Number.isInteger(rounded) ? rounded.toFixed(0) : rounded.toFixed(1)}%`
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

const formatTime = (value) => {
  const seconds = Number(value || 0)
  if (seconds <= 0) {
    return '0 分钟'
  }
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours} 小时 ${minutes} 分钟`
  }
  return `${Math.max(minutes, 1)} 分钟`
}

const initCharts = () => {
  if (matrixChartRef.value && !matrixChartInstance) {
    matrixChartInstance = echarts.init(matrixChartRef.value)
  }
  if (trendChartRef.value && !trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
  }
  renderCharts()
}

const renderCharts = () => {
  renderMatrixChart()
  renderTrendChart()
}

const renderMatrixChart = () => {
  if (!matrixChartInstance) {
    return
  }

  const points = matrixItems.value
  const xMax = Math.max(sampleThreshold + 10, ...points.map((item) => item.totalCount + 5), 30)
  const option = {
    grid: { left: 56, right: 16, top: 30, bottom: 50 },
    tooltip: {
      trigger: 'item',
      formatter(params) {
        const data = params.data?.raw
        if (!data) {
          return ''
        }
        return `
          <div>
            <strong>${data.tagName || '-'}</strong><br/>
            学科：${data.subjectName || '-'}<br/>
            作答：${data.totalCount} 题<br/>
            正确率：${formatRate(data.correctRate)}<br/>
            风险分：${data.riskScore}<br/>
            建议：${data.actionText}
          </div>
        `
      }
    },
    xAxis: {
      name: '作答样本量（题）',
      min: 0,
      max: xMax,
      splitLine: { show: true, lineStyle: { color: '#253045' } },
      axisLine: { lineStyle: { color: '#4b5b76' } },
      axisLabel: { color: '#d1d9e6' }
    },
    yAxis: {
      name: '正确率（%）',
      min: 0,
      max: 100,
      splitLine: { show: true, lineStyle: { color: '#253045' } },
      axisLine: { lineStyle: { color: '#4b5b76' } },
      axisLabel: { color: '#d1d9e6' }
    },
    series: [
      {
        type: 'scatter',
        data: points.map((item) => ({
          value: [item.totalCount, item.correctRate, item.riskScore],
          raw: item,
          symbolSize: 10 + Math.round(item.riskScore * 0.18),
          itemStyle: {
            color: item.priority === '高危'
              ? '#ef4444'
              : item.priority === '关注'
                ? '#f59e0b'
                : item.priority === '样本不足'
                  ? '#22d3ee'
                  : '#22c55e'
          }
        })),
        markLine: {
          symbol: 'none',
          label: { show: false },
          lineStyle: { type: 'dashed', color: '#94a3b8' },
          data: [{ xAxis: sampleThreshold }, { yAxis: 70 }]
        },
        markArea: {
          itemStyle: {
            color: 'rgba(239, 68, 68, 0.08)'
          },
          data: [[{ xAxis: sampleThreshold, yAxis: 0 }, { xAxis: xMax, yAxis: 70 }]]
        }
      }
    ]
  }

  matrixChartInstance.setOption(option)
}

const renderTrendChart = () => {
  if (!trendChartInstance) {
    return
  }

  const labels = trendItems.value.map((item) => String(item.statDate || '').slice(5))
  const rateSeries = trendItems.value.map((item) => Number(item.correctRate || 0))
  const riskPressureSeries = trendItems.value.map((item) => {
    const doCount = Number(item.doCount || 0)
    const wrongRate = 100 - Number(item.correctRate || 0)
    return Math.round((doCount * wrongRate) / 100)
  })

  const option = {
    grid: { left: 50, right: 56, top: 34, bottom: 36 },
    tooltip: { trigger: 'axis' },
    legend: {
      top: 4,
      textStyle: { color: '#d1d9e6' },
      data: ['正确率', '弱项预警指数']
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: '#4b5b76' } },
      axisLabel: { color: '#d1d9e6' }
    },
    yAxis: [
      {
        type: 'value',
        name: '正确率',
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}%', color: '#d1d9e6' },
        splitLine: { lineStyle: { color: '#253045' } }
      },
      {
        type: 'value',
        name: '预警指数',
        min: 0,
        axisLabel: { color: '#d1d9e6' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '正确率',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: rateSeries,
        lineStyle: { width: 2, color: '#3b82f6' },
        itemStyle: { color: '#60a5fa' },
        areaStyle: { color: 'rgba(59,130,246,0.14)' }
      },
      {
        name: '弱项预警指数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: riskPressureSeries,
        lineStyle: { width: 2, color: '#f59e0b' },
        itemStyle: { color: '#fbbf24' }
      }
    ]
  }

  trendChartInstance.setOption(option)
}

const bindResize = () => {
  resizeObserver = new ResizeObserver(() => {
    matrixChartInstance?.resize()
    trendChartInstance?.resize()
  })

  if (matrixChartRef.value) {
    resizeObserver.observe(matrixChartRef.value)
  }
  if (trendChartRef.value) {
    resizeObserver.observe(trendChartRef.value)
  }
}

const disposeCharts = () => {
  resizeObserver?.disconnect()
  resizeObserver = null
  matrixChartInstance?.dispose()
  trendChartInstance?.dispose()
  matrixChartInstance = null
  trendChartInstance = null
}

watch([matrixItems, trendItems], async () => {
  await nextTick()
  renderCharts()
}, { deep: true })

onMounted(async () => {
  await loadStats()
  await nextTick()
  initCharts()
  bindResize()
})

onBeforeUnmount(() => {
  disposeCharts()
})
</script>

<style scoped>
.command-center {
  min-height: 100vh;
  padding: 20px;
  background:
    radial-gradient(circle at 20% 0%, rgba(59, 130, 246, 0.12), transparent 38%),
    radial-gradient(circle at 100% 10%, rgba(16, 185, 129, 0.1), transparent 30%),
    #0f172a;
  color: #e5edf9;
}

.screen-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 14px;
}

.head-title h1 {
  margin: 6px 0 2px;
  font-size: 28px;
}

.head-title .eyebrow {
  margin: 0;
  color: #8da2c3;
  font-size: 12px;
  text-transform: uppercase;
}

.head-title small {
  color: #93a4bf;
  font-size: 12px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.range-control {
  display: flex;
  gap: 6px;
  background: #111c32;
  border: 1px solid #253045;
  border-radius: 10px;
  padding: 4px;
}

.range-control button,
.ghost {
  height: 34px;
  border-radius: 8px;
  border: 1px solid #2d3b55;
  background: #16233d;
  color: #d9e7ff;
  padding: 0 12px;
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
}

.range-control button {
  border: 0;
  background: transparent;
}

.range-control button.active {
  background: #2563eb;
  color: #fff;
}

.alert-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 10px;
}

.alert-card {
  border-radius: 10px;
  padding: 12px;
  border: 1px solid #2a3a57;
  background: #141f35;
  display: grid;
  gap: 4px;
}

.alert-card span {
  font-size: 12px;
  color: #9eb2cf;
}

.alert-card strong {
  font-size: 26px;
  line-height: 1.1;
}

.alert-card small {
  font-size: 12px;
  color: #8fa4c2;
}

.alert-card.danger strong { color: #fb7185; }
.alert-card.warn strong { color: #fbbf24; }
.alert-card.info strong { color: #22d3ee; }
.alert-card.good strong { color: #34d399; }

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 10px;
}

.kpi-card {
  border: 1px solid #2a3a57;
  border-radius: 10px;
  background: #131f35;
  padding: 12px;
  display: grid;
  gap: 4px;
}

.kpi-card span {
  color: #9bb0cd;
  font-size: 12px;
}

.kpi-card strong {
  font-size: 30px;
  line-height: 1.1;
}

.kpi-card small {
  color: #8ea3c2;
  font-size: 12px;
}

.loading-block,
.empty-state {
  border: 1px dashed #385078;
  border-radius: 10px;
  padding: 36px;
  text-align: center;
  color: #9db0cc;
  background: rgba(19, 31, 53, 0.6);
}

.middle-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(0, 1fr);
  gap: 10px;
}

.panel {
  border: 1px solid #2a3a57;
  border-radius: 10px;
  background: #131f35;
  padding: 12px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.panel-head h2 {
  margin: 0;
  font-size: 16px;
}

.panel-head span {
  color: #90a4c3;
  font-size: 12px;
}

.chart-canvas {
  width: 100%;
  height: 340px;
}

.trend-panel {
  margin-top: 10px;
}

.trend-canvas {
  height: 250px;
}

.risk-list {
  display: grid;
  gap: 8px;
}

.risk-row {
  display: grid;
  grid-template-columns: 30px minmax(0, 1fr);
  gap: 10px;
  border: 1px solid #283a56;
  border-radius: 8px;
  background: #16243b;
  padding: 8px;
}

.risk-rank {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  background: #1d4ed8;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
}

.risk-main {
  display: grid;
  gap: 4px;
}

.risk-main strong {
  font-size: 14px;
  line-height: 1.3;
}

.risk-main p {
  margin: 0;
  color: #91a5c2;
  font-size: 12px;
}

.risk-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: #a0b2cb;
  font-size: 12px;
}

.risk-bar {
  height: 6px;
  border-radius: 999px;
  overflow: hidden;
  background: #2b3a52;
}

.risk-bar span {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #f59e0b 0%, #ef4444 100%);
}

.risk-main small {
  color: #d8e4f7;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .alert-strip,
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .middle-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .command-center {
    padding: 14px;
  }

  .screen-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .alert-strip,
  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .chart-canvas {
    height: 300px;
  }
}
</style>
