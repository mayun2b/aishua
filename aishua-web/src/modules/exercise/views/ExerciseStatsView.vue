<template>
  <div class="exercise-stats">
    <h2>练习统计</h2>
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-number">{{ stats.totalCount || 0 }}</div>
        <div class="stat-label">总练习题数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ stats.correctCount || 0 }}</div>
        <div class="stat-label">正确题数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ (stats.correctRate * 1 || 0).toFixed(1) }}%</div>
        <div class="stat-label">正确率</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ stats.categoryCount || 0 }}</div>
        <div class="stat-label">涉及分类</div>
      </div>
    </div>
    
    <!-- 正确率变化趋势图表 -->
    <div class="chart-section">
      <h3>正确率变化趋势</h3>
      <div id="correctRateChart" ref="correctRateChart" class="chart-container"></div>
    </div>
    
    <!-- 个性化学习建议 -->
    <div class="suggestion-section">
      <h3>个性化学习建议</h3>
      <div class="suggestion-card">
        <div class="suggestion-item">
          <h4>📈 学习概况</h4>
          <p>{{ learningSummary }}</p>
        </div>
        
        <div class="suggestion-item">
          <h4>⚠️ 需要重点关注的知识点</h4>
          <ul class="weak-points-list">
            <li v-for="(point, index) in weakPoints" :key="index">
              {{ point }}
            </li>
          </ul>
        </div>
        
        <div class="suggestion-item">
          <h4>💡 学习建议</h4>
          <ul class="suggestions-list">
            <li v-for="(suggestion, index) in learningSuggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 最优学习路径推荐 -->
    <div class="learning-path-section">
      <h3>最优学习路径推荐</h3>
      <div class="learning-path-card">
        <div class="learning-path-item">
          <h4>🎯 推荐学习路径</h4>
          <div class="path-steps">
            <div 
              v-for="(step, index) in learningPath" 
              :key="index" 
              class="path-step"
              :class="{ 'current-step': index === currentStep }"
            >
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">
                <div class="step-title">{{ step.title }}</div>
                <div class="step-description">{{ step.description }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="learning-path-item">
          <h4>📊 路径目标</h4>
          <p>{{ pathGoal }}</p>
        </div>
      </div>
    </div>
    
    <div class="subject-section">
      <h3>练习科目</h3>
      <div class="subject-list">
        <div 
          v-for="subject in subjects" 
          :key="subject.subjectId" 
          class="subject-card"
          @click="selectSubject(subject)"
          :class="{ active: selectedSubject && selectedSubject.subjectId === subject.subjectId }"
        >
          <div class="subject-name">{{ subject.subjectName }}</div>
          <div class="subject-info">
            <span class="subject-count">{{ subject.totalCount || 0 }} 题</span>
            <span class="subject-rate">{{ (subject.correctRate * 1 || 0).toFixed(1) }}%</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="category-section" v-if="selectedSubject">
      <h3>{{ selectedSubject.subjectName }} - 知识点进度</h3>
      <div class="back-button">
        <button @click="selectedSubject = null" class="back-button">返回科目列表</button>
      </div>
      <div class="stats-chart" v-if="selectedSubjectStats.length > 0">
        <!-- 这里可以集成图表库，暂时用简单列表展示 -->
        <div class="category-list">
          <div 
            v-for="(item, index) in selectedSubjectStats" 
            :key="index" 
            class="category-item"
            :class="{ 'weak-point': (item.correctRate * 1 || 0) < 40 }"
          >
            <div class="category-name">{{ item.categoryName }}</div>
            <div class="category-details">
              <div class="progress-container">
                <div 
                  class="progress-bar" 
                  :style="{ width: (item.correctRate * 1 || 0) + '%' }"
                  :class="{ 
                    'low-rate': (item.correctRate * 1 || 0) < 40,
                    'medium-rate': (item.correctRate * 1 || 0) >= 40 && (item.correctRate * 1 || 0) < 70,
                    'high-rate': (item.correctRate * 1 || 0) >= 70
                  }"
                ></div>
              </div>
              <div class="category-stats">
                <span 
                  class="category-rate"
                  :class="{ 
                    'low-rate': (item.correctRate * 1 || 0) < 40,
                    'medium-rate': (item.correctRate * 1 || 0) >= 40 && (item.correctRate * 1 || 0) < 70,
                    'high-rate': (item.correctRate * 1 || 0) >= 70
                  }"
                >{{ (item.correctRate * 1).toFixed(1) }}%</span>
                <span class="category-count">{{ item.totalCount }} 题</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>该科目暂无练习数据</p>
      </div>
    </div>
    
    <div class="action-buttons">
      <button @click="loadStats" class="refresh-button" :disabled="loading">{{ loading ? '加载中...' : '刷新数据' }}</button>
    </div>
  </div>
</template>

<script>
import { exerciseApi } from '@/modules/exercise/api/exercise';
import * as echarts from 'echarts';

export default {
  name: 'ExerciseStatsView',
  props: {
    subjectId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      stats: {},
      chartData: [],
      subjects: [],
      selectedSubject: null,
      selectedSubjectStats: [],
      loading: false,
      correctRateChart: null,
      // 当前选中学科ID
      currentSubjectId: '',
      // 正确率变化数据（从后端获取）
      correctRateData: {
        dates: [],
        rates: []
      },
      // 学习建议相关数据
      learningSummary: '',
      weakPoints: [],
      learningSuggestions: [],
      // 学习路径相关数据
      learningPath: [],
      currentStep: 0,
      pathGoal: ''
    };
  },
  mounted() {
    // 检查本地存储中是否有选中学科
    const savedSubjectId = localStorage.getItem('selectedSubjectId');
    if (savedSubjectId) {
      this.currentSubjectId = savedSubjectId;
    } else if (this.subjectId) {
      this.currentSubjectId = this.subjectId;
    }
    this.loadStats();
    this.$nextTick(() => {
      this.initChart();
    });
  },
  methods: {
    async loadStats() {
      this.loading = true;
      try {
        const params = {};
        const currentSubjectId = this.currentSubjectId;
        console.log('当前学科ID:', currentSubjectId);
        
        if (currentSubjectId) {
          params.subjectId = currentSubjectId;
        }
        
        const response = await exerciseApi.getUserStats(params);
        if (response.code === 200) {
          this.stats = response.data;
          console.log('后端返回的完整数据:', response.data);
          console.log('分类统计数据:', response.data.categoryStats);
          
          // 后端已经按学科筛选，直接使用返回的数据
          this.chartData = (response.data.categoryStats || []).filter(item => item !== undefined && item !== null);
          this.subjects = response.data.subjectStats || [];
          
          // 如果有学科数据，设置选中的学科
          if (this.subjects.length > 0) {
            this.selectedSubject = this.subjects[0];
          } else {
            this.selectedSubject = null;
          }
          
          console.log('过滤后的分类数据:', this.chartData);
          console.log('科目统计数据:', this.subjects);
        }
        // 更新图表
        this.$nextTick(() => {
          this.updateChart();
        });
        
        // 生成个性化学习建议
        this.generateLearningSuggestions();
        
        // 生成学习路径
        this.generateLearningPath();
      } catch (error) {
        console.error('加载统计数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    // 初始化图表
    initChart() {
      const chartDom = this.$refs.correctRateChart;
      if (!chartDom) return;
      
      this.correctRateChart = echarts.init(chartDom);
      this.updateChart();
    },
    // 更新图表数据
    updateChart() {
      if (!this.correctRateChart) return;
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}%'
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
          data: this.correctRateData.dates,
          axisLabel: {
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          name: '正确率(%)',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '正确率',
            type: 'line',
            smooth: true,
            data: this.correctRateData.rates,
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
          }
        ]
      };
      
      this.correctRateChart.setOption(option);
    },
    // 生成个性化学习建议
    generateLearningSuggestions() {
      // 分析用户学习概况
      const correctRate = this.stats.correctRate * 1 || 0;
      const totalCount = this.stats.totalCount || 0;
      
      if (totalCount === 0) {
        this.learningSummary = '您还没有开始练习，建议您尽快开始刷题，积累练习数据。';
        this.weakPoints = [];
        this.learningSuggestions = [
          '开始练习，积累基础数据',
          '选择一个科目开始学习',
          '制定合理的学习计划'
        ];
        return;
      }
      
      // 生成学习概况
      if (correctRate < 40) {
        this.learningSummary = `您目前共练习了${totalCount}道题，正确率为${correctRate.toFixed(1)}%，处于基础阶段，需要重点加强基础知识学习。`;
      } else if (correctRate < 70) {
        this.learningSummary = `您目前共练习了${totalCount}道题，正确率为${correctRate.toFixed(1)}%，处于提升阶段，需要针对性地加强薄弱知识点。`;
      } else {
        this.learningSummary = `您目前共练习了${totalCount}道题，正确率为${correctRate.toFixed(1)}%，处于进阶阶段，可以挑战更高难度的题目。`;
      }
      
      // 使用后端返回的薄弱知识点数据
      const weakPoints = [];
      if (this.stats.weakPointStats && this.stats.weakPointStats.length > 0) {
        this.stats.weakPointStats.forEach(item => {
          weakPoints.push(`${item.categoryName} (正确率: ${(item.correctRate * 1 || 0).toFixed(1)}%)`);
        });
      }
      
      this.weakPoints = weakPoints.length > 0 ? weakPoints : ['暂无明显薄弱知识点，继续保持！'];
      
      // 生成学习建议
      const suggestions = [];
      
      if (correctRate < 40) {
        suggestions.push('重点复习基础知识，理解概念原理');
        suggestions.push('多做基础题，巩固知识点');
        suggestions.push('查看错题解析，理解错误原因');
      } else if (correctRate < 70) {
        suggestions.push('针对性练习薄弱知识点');
        suggestions.push('总结解题方法和技巧');
        suggestions.push('定期回顾错题，避免重复错误');
      } else {
        suggestions.push('挑战更高难度的题目');
        suggestions.push('总结解题思路，提高解题效率');
        suggestions.push('尝试模拟考试，提高应试能力');
      }
      
      // 如果有薄弱知识点，添加针对性建议
      if (weakPoints.length > 0) {
        suggestions.push(`重点复习以下知识点：${weakPoints.map(p => p.split(' ')[0]).join('、')}`);
      }
      
      this.learningSuggestions = suggestions;
    },
    // 生成学习路径
    generateLearningPath() {
      const correctRate = this.stats.correctRate * 1 || 0;
      const totalCount = this.stats.totalCount || 0;
      
      if (totalCount === 0) {
        this.learningPath = [
          {
            title: '开始基础学习',
            description: '选择一个科目开始学习，熟悉基础知识'
          },
          {
            title: '练习基础题目',
            description: '完成基础题目练习，巩固知识点'
          },
          {
            title: '定期复习',
            description: '定期回顾已学内容，强化记忆'
          }
        ];
        this.pathGoal = '建立基础知识体系，完成100道基础题练习';
        return;
      }
      
      if (correctRate < 40) {
        // 基础阶段学习路径
        this.learningPath = [
          {
            title: '复习基础知识',
            description: '重新学习薄弱知识点的基础概念'
          },
          {
            title: '基础题专项练习',
            description: '针对薄弱知识点进行基础题练习'
          },
          {
            title: '错题分析',
            description: '详细分析错题原因，总结解题方法'
          },
          {
            title: '阶段性测试',
            description: '完成基础知识点测试，检验学习效果'
          }
        ];
        this.pathGoal = '将薄弱知识点的正确率提升至60%以上';
      } else if (correctRate < 70) {
        // 提升阶段学习路径
        this.learningPath = [
          {
            title: '强化薄弱知识点',
            description: '针对薄弱知识点进行专项练习'
          },
          {
            title: '综合练习',
            description: '进行跨知识点的综合练习'
          },
          {
            title: '总结解题技巧',
            description: '总结解题方法和技巧，提高解题效率'
          },
          {
            title: '模拟考试',
            description: '进行模拟考试，适应考试节奏'
          }
        ];
        this.pathGoal = '将整体正确率提升至80%以上';
      } else {
        // 进阶阶段学习路径
        this.learningPath = [
          {
            title: '挑战难题',
            description: '练习高难度题目，提升解题能力'
          },
          {
            title: '知识点拓展',
            description: '学习相关拓展知识点，丰富知识体系'
          },
          {
            title: '综合复习',
            description: '全面复习所有知识点，查漏补缺'
          },
          {
            title: '实战模拟',
            description: '进行全真模拟考试，提升应试能力'
          }
        ];
        this.pathGoal = '保持高正确率，提升解题速度和准确率';
      }
    },
    selectSubject(subject) {
      this.selectedSubject = subject;
      console.log('选择的科目:', subject);
      console.log('科目ID:', subject.subjectId);
      console.log('可用的分类数据数量:', this.chartData.length);
      
      // 过滤出该科目的知识点数据，添加类型转换确保比较正确
      this.selectedSubjectStats = this.chartData.filter(item => {
        const match = parseInt(item.subjectId) === parseInt(subject.subjectId);
        console.log('比较:', item.subjectId, '===', subject.subjectId, '->', match);
        return match;
      });
      
      console.log('过滤后的知识点数据数量:', this.selectedSubjectStats.length);
      console.log('过滤后的知识点数据:', this.selectedSubjectStats);
      
      // 如果没有数据，显示空状态提示
      console.log('过滤后的知识点数据数量:', this.selectedSubjectStats.length);
    },
    getSubjectStats(subjectId) {
      // 从后端返回的科目统计数据中查找
      const subjectStats = this.subjects.find(item => item.subjectId === subjectId);
      if (subjectStats) {
        return subjectStats;
      }
      return { totalCount: 0, correctCount: 0, correctRate: 0 };
    }
  }
};
</script>

<style scoped>
/* 现代化设计变量 */
:root {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --text-color: #303133;
  --text-color-secondary: #606266;
  --border-color: #dcdfe6;
  --border-color-light: #e4e7ed;
  --background-color: #f5f7fa;
  --card-bg: #ffffff;
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);
  --border-radius-sm: 4px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.exercise-stats {
  padding: 24px;
  background-color: var(--card-bg);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-md);
  margin: 24px 0;
  animation: fadeIn 0.5s ease-in-out;
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 24px;
  border-left: 4px solid var(--primary-color);
  padding-left: 12px;
}

h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 16px;
}

h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 12px;
}

p {
  color: var(--text-color-secondary);
  line-height: 1.6;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

/* 图表样式 */
.chart-section {
  margin: 32px 0;
}

.chart-section h3 {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 320px;
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color-light);
}

/* 个性化学习建议样式 */
.suggestion-section {
  margin: 32px 0;
}

.suggestion-card {
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color-light);
  padding: 24px;
}

.suggestion-item {
  margin-bottom: 24px;
}

.suggestion-item:last-child {
  margin-bottom: 0;
}

.suggestion-item h4 {
  display: flex;
  align-items: center;
  gap: 8px;
}

.weak-points-list,
.suggestions-list {
  list-style-type: none;
  padding-left: 0;
  color: var(--text-color-secondary);
  line-height: 1.6;
}

.weak-points-list li,
.suggestions-list li {
  margin-bottom: 12px;
  padding-left: 20px;
  position: relative;
}

.weak-points-list li::before,
.suggestions-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--primary-color);
  font-weight: bold;
}

.weak-points-list li {
  color: var(--danger-color);
}

/* 学习路径样式 */
.learning-path-section {
  margin: 32px 0;
}

.learning-path-card {
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color-light);
  padding: 24px;
}

.learning-path-item {
  margin-bottom: 24px;
}

.learning-path-item:last-child {
  margin-bottom: 0;
}

.learning-path-item h4 {
  display: flex;
  align-items: center;
  gap: 8px;
}

.path-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.path-step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background-color: var(--background-color);
  border-radius: var(--border-radius-md);
  transition: var(--transition);
  border-left: 3px solid transparent;
}

.path-step:hover {
  background-color: rgba(64, 158, 255, 0.05);
  transform: translateX(8px);
  border-left-color: var(--primary-color);
}

.path-step.current-step {
  background-color: rgba(64, 158, 255, 0.1);
  border-left-color: var(--primary-color);
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
  font-size: 14px;
}

.step-content {
  flex: 1;
}

.step-title {
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 6px;
  font-size: 15px;
}

.step-description {
  color: var(--text-color-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.stat-card {
  text-align: center;
  padding: 24px;
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color-light);
  transition: var(--transition);
  animation: slideUp 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, var(--primary-color), rgba(64, 158, 255, 0.6));
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

/* 卡片上移动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--text-color-secondary);
  font-weight: 500;
}

.subject-section {
  margin: 32px 0;
}

.subject-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.subject-card {
  cursor: pointer;
  transition: var(--transition);
  border: 1px solid var(--border-color-light);
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: 24px;
  animation: slideUp 0.5s ease-out;
  position: relative;
}

.subject-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.subject-card:hover::after {
  transform: scaleX(1);
}

.subject-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.subject-card.active {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
  background-color: rgba(64, 158, 255, 0.05);
}

.subject-card.active::after {
  transform: scaleX(1);
}

.subject-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 12px;
  line-height: 1.3;
}

.subject-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  align-items: center;
}

.subject-count {
  color: var(--primary-color);
  font-weight: 500;
}

.subject-rate {
  color: var(--success-color);
  font-weight: 600;
  font-size: 15px;
}

.category-section {
  margin: 32px 0;
  padding: 24px;
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color-light);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-color-secondary);
  font-size: 16px;
}

.empty-state p {
  margin: 0;
}

.action-buttons {
  margin: 32px 0;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.refresh-button {
  padding: 12px 24px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-button:hover:not(:disabled) {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.refresh-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.refresh-button:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}



.back-button {
  padding: 10px 20px;
  background-color: var(--text-color-secondary);
  color: white;
  border: none;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 20px;
  transition: var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.back-button:hover {
  background-color: #a6a9ad;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}



.category-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  transition: background-color 0.2s ease;
}

.category-item:hover {
  background-color: #f5f7fa;
}

.category-item:last-child {
  border-bottom: none;
}

/* 薄弱知识点样式 */
.category-item.weak-point {
  background-color: #fff1f0;
  border-left: 4px solid #f56c6c;
}

.category-item.weak-point:hover {
  background-color: #ffe8e6;
}

.category-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  flex: 1;
  margin-right: 20px;
}

.category-details {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 进度条样式 */
.progress-container {
  width: 100%;
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

/* 根据正确率显示不同颜色 */
.progress-bar.low-rate {
  background-color: #f56c6c; /* 红色 - 低正确率 */
}

.progress-bar.medium-rate {
  background-color: #e6a23c; /* 黄色 - 中等正确率 */
}

.progress-bar.high-rate {
  background-color: #67c23a; /* 绿色 - 高正确率 */
}

.category-stats {
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: flex-end;
}

.category-rate {
  font-size: 14px;
  font-weight: 600;
}

/* 根据正确率显示不同颜色 */
.category-rate.low-rate {
  color: #f56c6c;
}

.category-rate.medium-rate {
  color: #e6a23c;
}

.category-rate.high-rate {
  color: #67c23a;
}

.category-count {
  font-size: 14px;
  color: #666;
}
</style>