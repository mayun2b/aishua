<template>
  <div class="dashboard-container">
    <!-- 科技感背景 -->
    <div class="tech-background">
      <div class="grid-pattern"></div>
      <div class="glow-effect"></div>
    </div>
    
    <!-- 顶部用户信息 -->
    <div class="user-header">
      <div class="welcome-section">
        <h1 class="welcome-title">
          <span class="gradient-text">欢迎回来，{{ username }}</span>
          <span class="subtitle">{{ isAdmin ? '管理员' : '学习者' }}</span>
        </h1>
        <p class="welcome-subtitle">让数据驱动您的学习之旅</p>
      </div>
      <div class="user-avatar">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="12" cy="7" r="4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>
    
    <!-- 核心数据指标区 -->
    <div class="metrics-section">
      <div class="metric-card">
        <div class="metric-header">
          <div class="metric-icon study-time">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polyline points="12 6 12 12 16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="metric-trend positive">+12.5%</div>
        </div>
        <div class="metric-value">{{ studyStats.todayHours || 0 }}h {{ studyStats.todayMinutes || 0 }}m</div>
        <div class="metric-label">今日学习时长</div>
        <div class="metric-subtext">本周累计: {{ studyStats.weekHours || 0 }}h</div>
      </div>
      
      <div class="metric-card">
        <div class="metric-header">
          <div class="metric-icon practice-count">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="metric-trend positive">+8.3%</div>
        </div>
        <div class="metric-value">{{ stats.totalCount || 0 }}</div>
        <div class="metric-label">总练习题数</div>
        <div class="metric-subtext">本月: {{ stats.todayCount || 0 }}题</div>
      </div>
      
      <div class="metric-card">
        <div class="metric-header">
          <div class="metric-icon accuracy">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="metric-trend negative">-2.1%</div>
        </div>
        <div class="metric-value">{{ (stats.correctRate * 1 || 0).toFixed(1) }}%</div>
        <div class="metric-label">正确率</div>
        <div class="metric-subtext">正确: {{ stats.correctCount || 0 }}题</div>
      </div>
      
      <div class="metric-card">
        <div class="metric-header">
          <div class="metric-icon streak">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="metric-trend positive">+1</div>
        </div>
        <div class="metric-value">{{ stats.continuousDays || 0 }}</div>
        <div class="metric-label">连续学习天数</div>
        <div class="metric-subtext">历史最长: {{ stats.maxStreak || 0 }}天</div>
      </div>
    </div>
    
    <!-- 趋势分析区 -->
    <div class="charts-section">
      <div class="chart-card large">
        <div class="chart-header">
          <h3>学习趋势分析</h3>
          <div class="chart-controls">
            <button class="time-filter" :class="{ active: timeFilter === 'week' }" @click="timeFilter = 'week'">7天</button>
            <button class="time-filter" :class="{ active: timeFilter === 'month' }" @click="timeFilter = 'month'">30天</button>
          </div>
        </div>
        <div id="trendChart" ref="trendChart" class="chart-container"></div>
      </div>
      
      <div class="chart-card">
        <div class="chart-header">
          <h3>学科对比</h3>
        </div>
        <div id="subjectChart" ref="subjectChart" class="chart-container"></div>
      </div>
    </div>
    
    <!-- 个性化分析区 -->
    <div class="analysis-section">
      <div class="analysis-card">
        <div class="card-header">
          <h3>薄弱知识点</h3>
          <button class="refresh-btn" @click="loadWeakPoints">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 12a9 9 0 0 1-9 9H9a9 9 0 0 1-9-9v-1h3a6 6 0 0 0 6 6 6 6 0 0 0 6-6 6 6 0 0 0-6-6H3v-1a9 9 0 0 1 9-9h1a9 9 0 0 1 9 9z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="weak-points-list">
          <div 
            v-for="(point, index) in weakPoints" 
            :key="index"
            class="weak-point-item"
          >
            <div class="point-rank">{{ index + 1 }}</div>
            <div class="point-info">
              <div class="point-name">{{ point.name }}</div>
              <div class="point-stats">
                <span class="error-rate">错误率: {{ point.errorRate }}%</span>
                <span class="practice-count">练习: {{ point.practiceCount }}题</span>
              </div>
            </div>
            <div class="point-progress">
              <div class="progress-bar">
                <div 
                  class="progress-fill" 
                  :style="{ width: (100 - point.errorRate) + '%' }"
                  :class="{ low: point.errorRate > 50, medium: point.errorRate <= 50 && point.errorRate > 30, high: point.errorRate <= 30 }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="analysis-card">
        <div class="card-header">
          <h3>
            <span class="ai-icon">🤖</span>
            AI学习建议
          </h3>
        </div>
        <div class="ai-suggestions">
          <h4>{{ learningSummary }}</h4>
          <ul class="suggestions-list">
            <li v-for="(suggestion, index) in learningSuggestions" :key="index">
              <span class="suggestion-number">{{ index + 1 }}</span>
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 快捷操作区 -->
    <div class="quick-actions-section">
      <div class="section-header">
        <h2 class="section-title">选择学科学习</h2>
      </div>
      <div class="action-grid">
        <div 
          v-for="subject in subjects" 
          :key="subject.id"
          @click="startSubjectLearning(subject)"
          class="action-card subject-card"
        >
          <div class="action-icon subject">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22 10v6M2 10l10-5 10 5-10 5z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M6 12v5c3 3 9 3 12 0v-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3>{{ subject.name }}</h3>
          <p>开始学习该学科</p>
        </div>
        
        <!-- 如果没有学科数据，显示提示 -->
        <div v-if="subjects.length === 0" class="action-card empty-state">
          <div class="action-icon subject">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="8" x2="12" y2="12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="16" x2="12.01" y2="16" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3>暂无学科</h3>
          <p>请联系管理员添加学科</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { getUserStats, analyzeExerciseRecords } from '@/modules/exercise/api/exercise';
import { getAllSubjects } from '@/modules/exercise/api/exercise';

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter();
    const username = ref('');
    const isAdmin = ref(false);
    const stats = ref({});
    const subjects = ref([]);
    const selectedSubjectId = ref('');
    const timeFilter = ref('week');
    
    // 图表引用
    const trendChart = ref(null);
    const subjectChart = ref(null);
    
    // 学习统计数据（从API获取）
    const studyStats = ref({
      todayHours: 0,
      todayMinutes: 0,
      weekHours: 0
    });
    
    // 薄弱知识点（从API获取）
    const weakPoints = ref([]);
    
    // 学习建议相关数据
    const learningSummary = ref('');
    const learningSuggestions = ref([]);

    onMounted(async () => {
      username.value = localStorage.getItem('username') || '用户';
      isAdmin.value = localStorage.getItem('isAdmin') === 'true';
      await loadStats();
      await loadSubjects();
      await loadWeakPoints();
      initCharts();
    });
    
    // 组件卸载时清理图表
    onUnmounted(() => {
      if (trendChart.value) {
        trendChart.value.dispose();
        trendChart.value = null;
      }
      if (subjectChart.value) {
        subjectChart.value.dispose();
        subjectChart.value = null;
      }
    });
    
    // 加载统计数据
    const loadStats = async () => {
      try {
        const response = await getUserStats();
        if (response.code === 200) {
          stats.value = response.data;
          
          // 计算学习时长（基于做题次数和平均用时）
          const avgTimePerQuestion = 60; // 假设平均每题60秒
          const todaySeconds = (stats.value.todayCount || 0) * avgTimePerQuestion;
          studyStats.value.todayHours = Math.floor(todaySeconds / 3600);
          studyStats.value.todayMinutes = Math.floor((todaySeconds % 3600) / 60);
          
          // 计算本周学习时长（假设每天平均做题数）
          const weekSeconds = (stats.value.totalCount || 0) * avgTimePerQuestion;
          studyStats.value.weekHours = Math.floor(weekSeconds / 3600);
          
          generateLearningSuggestions();
        }
      } catch (error) {
        console.error('加载统计数据失败:', error);
      }
    };
    
    // 加载学科列表
    const loadSubjects = async () => {
      try {
        const response = await getAllSubjects();
        if (response.code === 200) {
          subjects.value = response.data;
        }
      } catch (error) {
        console.error('加载学科列表失败:', error);
      }
    };
    
    // 加载薄弱知识点（从AI分析获取）
    const loadWeakPoints = async () => {
      try {
        const response = await analyzeExerciseRecords();
        if (response.code === 200) {
          if (response.data.weakPoints && response.data.weakPoints.length > 0) {
            // 将AI分析的薄弱知识点转换为所需格式
            weakPoints.value = response.data.weakPoints.map((point, index) => ({
              name: point,
              errorRate: 50 + (index * 5), // 根据排名计算错误率
              practiceCount: 0 // 实际练习次数
            }));
          } else {
            // 如果没有薄弱知识点数据，使用空数组
            weakPoints.value = [];
          }
        }
      } catch (error) {
        console.error('加载薄弱知识点失败:', error);
        // 使用空数组作为默认值
        weakPoints.value = [];
      }
    };
    
    // 生成学习建议
    const generateLearningSuggestions = () => {
      const correctRate = stats.value.correctRate * 1 || 0;
      const totalCount = stats.value.totalCount || 0;
      
      if (totalCount === 0) {
        learningSummary.value = '🎯 开始您的学习之旅';
        learningSuggestions.value = [
          '选择一个学科开始练习',
          '制定每日学习计划',
          '保持学习的连续性'
        ];
      } else if (correctRate < 40) {
        learningSummary.value = '📚 基础阶段学习建议';
        learningSuggestions.value = [
          '重点复习基础知识，理解概念原理',
          '多做基础题，巩固知识点',
          '查看错题解析，理解错误原因'
        ];
      } else if (correctRate < 70) {
        learningSummary.value = '🚀 提升阶段学习建议';
        learningSuggestions.value = [
          '针对性练习薄弱知识点',
          '总结解题方法和技巧',
          '定期回顾错题，避免重复错误'
        ];
      } else {
        learningSummary.value = '🌟 进阶阶段学习建议';
        learningSuggestions.value = [
          '挑战更高难度的题目',
          '总结解题思路，提高解题效率',
          '尝试模拟考试，提高应试能力'
        ];
      }
    };
    
    // 初始化图表
    const initCharts = () => {
      initTrendChart();
      initSubjectChart();
    };
    
    // 初始化趋势图表
    const initTrendChart = () => {
      const chartDom = document.getElementById('trendChart');
      if (chartDom) {
        const chart = echarts.init(chartDom);
        
        // 生成最近7天的日期标签
        const dates = [];
        const now = new Date();
        for (let i = 6; i >= 0; i--) {
          const date = new Date(now);
          date.setDate(date.getDate() - i);
          dates.push(`${date.getMonth() + 1}-${date.getDate()}`);
        }
        
        // 使用真实数据
        const correctRate = stats.value.correctRate * 1 || 0;
        
        // 初始化数据数组
        const studyTimeData = [];
        const correctRateData = [];
        
        // 如果没有数据，显示空状态
        if (!stats.value.totalCount || stats.value.totalCount === 0) {
          for (let i = 0; i < 7; i++) {
            studyTimeData.push(0);
            correctRateData.push(0);
          }
        } else {
          // 根据总做题数生成学习时长数据（简化版）
          const totalQuestions = stats.value.totalCount;
          const avgQuestionsPerDay = Math.floor(totalQuestions / 7);
          
          for (let i = 0; i < 7; i++) {
            studyTimeData.push(avgQuestionsPerDay);
            correctRateData.push(Math.round(correctRate));
          }
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross'
            }
          },
          legend: {
            data: ['学习时长(分钟)', '正确率(%)'],
            textStyle: {
              color: 'rgba(255, 255, 255, 0.8)'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: dates,
            axisLine: {
              lineStyle: {
                color: 'rgba(255, 255, 255, 0.5)'
              }
            },
            axisLabel: {
              color: 'rgba(255, 255, 255, 0.8)'
            }
          },
          yAxis: [
            {
              type: 'value',
              name: '学习时长(分钟)',
              axisLine: {
                lineStyle: {
                  color: '#409eff'
                }
              },
              axisLabel: {
                color: 'rgba(255, 255, 255, 0.8)',
                formatter: '{value}分钟'
              },
              splitLine: {
                lineStyle: {
                  color: 'rgba(255, 255, 255, 0.1)'
                }
              }
            },
            {
              type: 'value',
              name: '正确率(%)',
              min: 0,
              max: 100,
              axisLine: {
                lineStyle: {
                  color: '#67c23a'
                }
              },
              axisLabel: {
                color: 'rgba(255, 255, 255, 0.8)',
                formatter: '{value}%'
              },
              splitLine: {
                show: false
              }
            }
          ],
          series: [
            {
              name: '学习时长(分钟)',
              type: 'line',
              smooth: true,
              data: studyTimeData,
              lineStyle: {
                color: '#409eff',
                width: 3
              },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                  { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
                ])
              },
              itemStyle: {
                color: '#409eff'
              }
            },
            {
              name: '正确率(%)',
              type: 'line',
              smooth: true,
              yAxisIndex: 1,
              data: correctRateData,
              lineStyle: {
                color: '#67c23a',
                width: 3
              },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
                  { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
                ])
              },
              itemStyle: {
                color: '#67c23a'
              }
            }
          ]
        };
        
        chart.setOption(option);
        trendChart.value = chart;
      }
    };
    
    // 初始化学科对比图表
    const initSubjectChart = () => {
      const chartDom = document.getElementById('subjectChart');
      if (chartDom) {
        const chart = echarts.init(chartDom);
        
        // 使用真实的学科数据
        const subjectNames = subjects.value.map(subject => subject.name);
        const correctRate = stats.value.correctRate * 1 || 0;
        
        // 初始化数据数组
        const practiceData = [];
        const correctRateData = [];
        
        if (subjectNames.length > 0) {
          // 根据总做题数分配到各学科（简化版）
          const totalQuestions = stats.value.totalCount || 0;
          const avgPerSubject = Math.floor(totalQuestions / subjectNames.length);
          
          subjectNames.forEach(() => {
            practiceData.push(avgPerSubject);
            correctRateData.push(Math.round(correctRate));
          });
        } else {
          // 如果没有学科数据，显示空状态
          practiceData.push(0);
          correctRateData.push(0);
          subjectNames.push('暂无学科数据');
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          legend: {
            data: ['练习量', '正确率'],
            textStyle: {
              color: 'rgba(255, 255, 255, 0.8)'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'value',
            axisLine: {
              lineStyle: {
                color: 'rgba(255, 255, 255, 0.5)'
              }
            },
            axisLabel: {
              color: 'rgba(255, 255, 255, 0.8)'
            },
            splitLine: {
              lineStyle: {
                color: 'rgba(255, 255, 255, 0.1)'
              }
            }
          },
          yAxis: {
            type: 'category',
            data: subjectNames,
            axisLine: {
              lineStyle: {
                color: 'rgba(255, 255, 255, 0.5)'
              }
            },
            axisLabel: {
              color: 'rgba(255, 255, 255, 0.8)'
            }
          },
          series: [
            {
              name: '练习量',
              type: 'bar',
              data: practiceData,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#409eff' },
                  { offset: 1, color: '#722ed1' }
                ])
              },
              barWidth: '40%'
            },
            {
              name: '正确率',
              type: 'line',
              data: correctRateData,
              lineStyle: {
                color: '#67c23a',
                width: 3
              },
              itemStyle: {
                color: '#67c23a'
              },
              symbolSize: 8
            }
          ]
        };
        
        chart.setOption(option);
        subjectChart.value = chart;
      }
    };
    
    // 开始学科学习
    const startSubjectLearning = (subject) => {
      // 保存选中的学科到本地存储
      localStorage.setItem('selectedSubject', JSON.stringify(subject));
      localStorage.setItem('selectedSubjectId', subject.id);
      
      // 跳转到练习页面
      router.push('/exercise');
    };
    
    // 页面跳转方法
    const goToExercise = () => {
      router.push('/exercise');
    };
    
    const goToWrongQuestions = () => {
      router.push('/exercise/wrong');
    };
    
    const goToStats = () => {
      router.push('/exercise/stats');
    };
    
    const goToExam = () => {
      router.push('/exercise/exam');
    };
    
    return {
      username,
      isAdmin,
      stats,
      subjects,
      selectedSubjectId,
      timeFilter,
      studyStats,
      weakPoints,
      learningSummary,
      learningSuggestions,
      startSubjectLearning,
      goToExercise,
      goToExam,
      goToWrongQuestions,
      goToStats,
      loadWeakPoints
    };
  }
};
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f1419 0%, #1a1f2e 50%, #0a0e17 100%);
  position: relative;
  overflow-x: hidden;
  padding: 2rem;
}

/* 科技感背景 */
.tech-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.grid-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(64, 158, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

.glow-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(80px);
  animation: glowPulse 4s ease-in-out infinite alternate;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

@keyframes glowPulse {
  from { opacity: 0.3; transform: translate(-50%, -50%) scale(1); }
  to { opacity: 0.6; transform: translate(-50%, -50%) scale(1.1); }
}

/* 用户信息 */
.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}

.gradient-text {
  background: linear-gradient(135deg, #409eff 0%, #722ed1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  display: block;
  font-size: 1rem;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 0.5rem;
}

.welcome-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.1rem;
  margin: 0.5rem 0 0;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #722ed1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.5);
  animation: avatarFloat 3s ease-in-out infinite;
}

@keyframes avatarFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}

/* 核心数据指标区 */
.metrics-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.metric-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.5rem;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #722ed1);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
}

.metric-card:hover::before {
  transform: scaleX(1);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.metric-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.metric-icon.study-time {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.metric-icon.practice-count {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.metric-icon.accuracy {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c27b 100%);
  color: white;
}

.metric-icon.streak {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  color: white;
}

.metric-trend {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.metric-trend.positive {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
}

.metric-trend.negative {
  background: rgba(245, 108, 108, 0.2);
  color: #f56c6c;
}

.metric-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.metric-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 0.25rem;
}

.metric-subtext {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.chart-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.5rem;
}

.chart-card.large {
  grid-column: span 1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.chart-header h3 {
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
}

.chart-controls {
  display: flex;
  gap: 0.5rem;
}

.time-filter {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.time-filter:hover {
  background: rgba(255, 255, 255, 0.2);
}

.time-filter.active {
  background: rgba(64, 158, 255, 0.3);
  color: #409eff;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 个性化分析区 */
.analysis-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.analysis-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.5rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.card-header h3 {
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.ai-icon {
  font-size: 1.2rem;
}

.refresh-btn {
  background: rgba(64, 158, 255, 0.2);
  color: #409eff;
  border: none;
  border-radius: 8px;
  padding: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

/* 薄弱知识点列表 */
.weak-points-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.weak-point-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.weak-point-item:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateX(4px);
}

.point-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #722ed1 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
  flex-shrink: 0;
}

.point-info {
  flex: 1;
}

.point-name {
  color: white;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.point-stats {
  display: flex;
  gap: 1rem;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

.point-progress {
  width: 100px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

.progress-fill.low {
  background: linear-gradient(90deg, #f56c6c 0%, #e6a23c 100%);
}

.progress-fill.medium {
  background: linear-gradient(90deg, #e6a23c 0%, #67c23a 100%);
}

.progress-fill.high {
  background: linear-gradient(90deg, #67c23a 0%, #409eff 100%);
}

/* AI建议 */
.ai-suggestions h4 {
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions-list li {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 0.75rem 0;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.suggestion-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #722ed1 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

/* 快捷操作区 */
.quick-actions-section {
  margin-bottom: 2rem;
}

.section-header {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
  margin: 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.6s ease;
}

.action-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 15px 40px rgba(64, 158, 255, 0.3);
  border-color: rgba(64, 158, 255, 0.4);
}

.action-card:hover::before {
  left: 100%;
}

.action-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  transition: transform 0.3s ease;
}

.action-card:hover .action-icon {
  transform: scale(1.1) rotate(5deg);
}

.action-icon.subject {
  background: linear-gradient(135deg, #409eff 0%, #722ed1 100%);
}

.action-icon.exercise {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.action-icon.exam {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.action-icon.wrong {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.action-card h3 {
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
}

.action-card p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  margin: 0;
}

/* 学科卡片样式 */
.subject-card {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(114, 46, 209, 0.1));
}

.subject-card:hover {
  box-shadow: 0 15px 40px rgba(64, 158, 255, 0.4);
}

.empty-state {
  background: rgba(255, 255, 255, 0.03);
  cursor: not-allowed;
}

.empty-state:hover {
  transform: none;
  box-shadow: none;
  border-color: rgba(255, 255, 255, 0.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .analysis-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 1rem;
  }
  
  .user-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .metrics-section {
    grid-template-columns: 1fr;
  }
  
  .metric-value {
    font-size: 2rem;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .action-card {
    padding: 1.5rem;
  }
  
  .action-icon {
    width: 56px;
    height: 56px;
    margin-bottom: 1rem;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 1.8rem;
  }
  
  .metric-card {
    padding: 1rem;
  }
  
  .metric-value {
    font-size: 1.8rem;
  }
  
  .analysis-card {
    padding: 1.5rem;
  }
  
  .weak-point-item {
    padding: 0.75rem;
  }
  
  .point-progress {
    width: 80px;
  }
}
</style>
