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
      <button @click="analyzeRecords" class="analyze-button" :disabled="analyzing">{{ analyzing ? '分析中...' : '智能总结' }}</button>
      <button @click="openAiQuestionDialog" class="ai-question-button" :disabled="generatingAiQuestion || analyzing">{{ generatingAiQuestion || analyzing ? '处理中...' : 'AI出题巩固' }}</button>
    </div>
    
    <!-- AI出题确认对话框 -->
    <div v-if="showAiQuestionDialog" class="dialog-overlay">
      <div class="dialog-content">
        <h3>AI出题巩固</h3>
        
        <!-- AI分析结果展示 -->
        <div v-if="aiAnalysisResult" class="ai-analysis-section">
          <h4>📊 AI分析结果</h4>
          <div class="analysis-summary">
            <p><strong>学习概况：</strong>{{ aiAnalysisResult.summary }}</p>
            <p><strong>薄弱知识点：</strong>{{ aiAnalysisResult.weakPoints.join('、') }}</p>
          </div>
        </div>
        
        <!-- 题目配置选项 -->
        <div class="ai-config-section">
          <h4>⚙️ 题目配置</h4>
          
          <div class="config-item">
            <label>题目数量：</label>
            <select v-model="aiQuestionConfig.questionCount" class="config-select">
              <option v-for="count in questionCountOptions" :key="count" :value="count">{{ count }} 题</option>
            </select>
          </div>
          
          <div class="config-item">
            <label>题目难度：</label>
            <select v-model="aiQuestionConfig.difficulty" class="config-select">
              <option v-for="option in difficultyOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
            </select>
          </div>
        </div>
        
        <p class="dialog-note">根据您的薄弱知识点，AI将为您生成相关题目进行巩固练习。这些题目将单独保存到AI生成题目表中，方便您后期查看和练习。</p>
        
        <div class="dialog-buttons">
          <button @click="showAiQuestionDialog = false" class="cancel-button">取消</button>
          <button @click="generateAiQuestions" class="confirm-button" :disabled="generatingAiQuestion">{{ generatingAiQuestion ? '生成中...' : '确认生成' }}</button>
        </div>
      </div>
    </div>
    
    <div class="analysis-section" v-if="analysisResult">
      <h3>智能总结</h3>
      <div class="analysis-card">
        <div class="analysis-item">
          <h4>学习概况</h4>
          <p>{{ analysisResult.summary }}</p>
        </div>
        
        <div class="analysis-item">
          <h4>学习建议</h4>
          <ul class="suggestions">
            <li v-for="(suggestion, index) in analysisResult.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
        
        <div class="analysis-item">
          <h4>详细分析</h4>
          <div class="analysis-content">{{ analysisResult.analysis }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { exerciseApi, aiApi } from '@/modules/exercise/api/exercise';
import * as echarts from 'echarts';
const { analyzeExerciseRecords, generateAiQuestion } = aiApi;

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
      analyzing: false,
      analysisResult: null,
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
      pathGoal: '',
      // AI出题相关数据
      generatingAiQuestion: false,
      showAiQuestionDialog: false,
      // AI分析结果
      aiAnalysisResult: null,
      // AI题目配置
      aiQuestionConfig: {
        questionCount: 5,
        difficulty: 2 // 1: 简单, 2: 中等, 3: 困难
      },
      // 题目数量选项
      questionCountOptions: [5, 10, 15, 20],
      // 难度选项
      difficultyOptions: [
        { value: 1, label: '简单' },
        { value: 2, label: '中等' },
        { value: 3, label: '困难' }
      ]
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
    },
    async analyzeRecords() {
      console.log('开始分析练习记录');
      this.analyzing = true;
      try {
        // 检查analyzeExerciseRecords是否存在
        console.log('analyzeExerciseRecords类型:', typeof analyzeExerciseRecords);
        if (typeof analyzeExerciseRecords !== 'function') {
          throw new Error('analyzeExerciseRecords function is not defined');
        }
        
        console.log('调用analyzeExerciseRecords');
        const response = await analyzeExerciseRecords();
        
        console.log('获取到响应:', response);
        // 检查响应是否存在
        if (!response) {
          throw new Error('No response received');
        }
        
        if (response.code === 200) {
          console.log('分析成功，结果:', response.data);
          this.analysisResult = response.data;
        } else {
          console.log('分析失败，错误信息:', response.message);
          console.error('分析练习记录失败：' + (response.message || '未知错误'));
        }
      } catch (error) {
        console.error('Error in analyzeRecords:', error);
        // 安全地处理错误对象
        const errorMessage = error.message || error.error || error || '未知错误';
        console.error('分析练习记录失败：' + errorMessage);
      } finally {
        this.analyzing = false;
        console.log('分析完成');
      }
    },
    async openAiQuestionDialog() {
      // 先进行AI分析
      this.analyzing = true;
      try {
        console.log('开始AI分析用户做题记录');
        
        // 调用分析API
        const response = await analyzeExerciseRecords();
        
        if (response.code === 200) {
          console.log('AI分析成功，结果:', response.data);
          // 处理分析结果
          this.aiAnalysisResult = {
            summary: response.data.summary || '分析完成，准备生成针对性题目',
            weakPoints: response.data.weakPoints || this.weakPoints.map(point => point.split(' (')[0])
          };
        } else {
          console.log('AI分析失败，使用默认数据');
          // 使用现有的薄弱知识点数据
          this.aiAnalysisResult = {
            summary: '基于您的练习数据进行分析',
            weakPoints: this.weakPoints.map(point => point.split(' (')[0])
          };
        }
        
        // 打开对话框
        this.showAiQuestionDialog = true;
      } catch (error) {
        console.error('AI分析失败:', error);
        // 使用默认数据
        this.aiAnalysisResult = {
          summary: '基于您的练习数据进行分析',
          weakPoints: this.weakPoints.map(point => point.split(' (')[0])
        };
        // 仍然打开对话框
        this.showAiQuestionDialog = true;
      } finally {
        this.analyzing = false;
      }
    },
    
    // 生成题目标题和内容
    generateQuestionContent(weakPoints, index) {
      const knowledgePoints = weakPoints.split('、');
      const selectedPoint = knowledgePoints[index % knowledgePoints.length];
      
      let title, content;
      
      switch (selectedPoint) {
        case '面向对象编程':
          title = `Java面向对象编程测试题 ${index + 1}`;
          content = `以下关于Java面向对象编程的描述，正确的是：`;
          break;
        case '集合框架':
          title = `Java集合框架测试题 ${index + 1}`;
          content = `关于Java集合框架中的List接口，下列说法错误的是：`;
          break;
        case '异常处理':
          title = `Java异常处理测试题 ${index + 1}`;
          content = `在Java中，以下哪个不是Checked Exception？`;
          break;
        case 'IO流':
          title = `Java IO流测试题 ${index + 1}`;
          content = `关于Java中的IO流，下列说法正确的是：`;
          break;
        case 'Java基础语法':
          title = `Java基础语法测试题 ${index + 1}`;
          content = `以下Java代码片段中，哪个会编译错误？`;
          break;
        default:
          title = `AI生成题目 ${index + 1} - ${selectedPoint}`;
          content = `关于${selectedPoint}的测试题，下列说法正确的是：`;
      }
      
      return { title, content };
    },
    
    // 生成题目选项和正确答案
    generateQuestionOptions(weakPoints, index) {
      const knowledgePoints = weakPoints.split('、');
      const selectedPoint = knowledgePoints[index % knowledgePoints.length];
      
      let options, correctAnswer;
      
      switch (selectedPoint) {
        case '面向对象编程':
          options = [
            { key: 'A', value: 'Java是纯面向对象语言，所有内容都必须在类中定义' },
            { key: 'B', value: '继承是面向对象的三大特征之一' },
            { key: 'C', value: '多态只能通过方法重载实现' },
            { key: 'D', value: '抽象类可以实例化对象' }
          ];
          correctAnswer = 'B';
          break;
        case '集合框架':
          options = [
            { key: 'A', value: 'ArrayList的底层实现是数组' },
            { key: 'B', value: 'LinkedList的查询效率比ArrayList高' },
            { key: 'C', value: 'List接口允许存放重复元素' },
            { key: 'D', value: 'List接口中的元素是有序的' }
          ];
          correctAnswer = 'B';
          break;
        case '异常处理':
          options = [
            { key: 'A', value: 'IOException' },
            { key: 'B', value: 'SQLException' },
            { key: 'C', value: 'RuntimeException' },
            { key: 'D', value: 'ClassNotFoundException' }
          ];
          correctAnswer = 'C';
          break;
        case 'IO流':
          options = [
            { key: 'A', value: '字节流可以处理所有类型的数据' },
            { key: 'B', value: '字符流只能处理文本数据' },
            { key: 'C', value: 'BufferedReader属于字节流' },
            { key: 'D', value: 'FileInputStream是字符流' }
          ];
          correctAnswer = 'B';
          break;
        case 'Java基础语法':
          options = [
            { key: 'A', value: 'int a = 10;' },
            { key: 'B', value: 'String str = "Hello";' },
            { key: 'C', value: 'boolean flag = true;' },
            { key: 'D', value: 'float f = 3.14;' }
          ];
          correctAnswer = 'D';
          break;
        default:
          options = [
            { key: 'A', value: '选项A内容' },
            { key: 'B', value: '选项B内容' },
            { key: 'C', value: '选项C内容' },
            { key: 'D', value: '选项D内容' }
          ];
          correctAnswer = 'B';
      }
      
      return { options, correctAnswer };
    },
    
    // 生成题目解析
    generateQuestionAnalysis(weakPoints, index, difficulty, correctAnswer) {
      const knowledgePoints = weakPoints.split('、');
      const selectedPoint = knowledgePoints[index % knowledgePoints.length];
      
      let analysis;
      
      switch (selectedPoint) {
        case '面向对象编程':
          analysis = `正确答案：${correctAnswer}
解析：Java是面向对象语言，但不是纯面向对象（如基本数据类型不是对象）。继承、封装、多态是面向对象的三大特征。多态可以通过方法重载和方法重写实现。抽象类不能直接实例化对象。`;
          break;
        case '集合框架':
          analysis = `正确答案：${correctAnswer}
解析：ArrayList底层是数组，查询效率高；LinkedList底层是链表，插入删除效率高。List接口允许重复元素且元素有序。`;
          break;
        case '异常处理':
          analysis = `正确答案：${correctAnswer}
解析：Checked Exception是编译时异常，必须显式处理。RuntimeException是运行时异常，属于Unchecked Exception，不需要显式处理。`;
          break;
        case 'IO流':
          analysis = `正确答案：${correctAnswer}
解析：字节流处理二进制数据，字符流处理文本数据。BufferedReader和FileInputStream都属于字节流的包装类。`;
          break;
        case 'Java基础语法':
          analysis = `正确答案：${correctAnswer}
解析：float类型赋值时需要加f或F后缀，如float f = 3.14f; 否则会被视为double类型导致编译错误。`;
          break;
        default:
          analysis = `正确答案：${correctAnswer}
解析：这是关于${selectedPoint}的详细解析，帮助理解相关知识点。`;
      }
      
      return analysis;
    },
    
    async generateAiQuestions() {
      this.generatingAiQuestion = true;
      try {
        // 获取当前用户ID（这里假设用户已登录，实际应该从登录状态中获取）
        const userId = 1; // 临时值，实际应该从store中获取
        
        // 获取薄弱知识点
        const weakPoints = this.aiAnalysisResult.weakPoints.join('、');
        const questionCount = this.aiQuestionConfig.questionCount;
        const difficulty = this.aiQuestionConfig.difficulty;
        
        // 根据用户配置生成题目
        for (let i = 0; i < questionCount; i++) {
          // 根据薄弱知识点生成具体的题目内容
          const questionContent = this.generateQuestionContent(weakPoints, i);
          const questionOptions = this.generateQuestionOptions(weakPoints, i);
          const correctAnswer = questionOptions.correctAnswer;
          const detailedAnalysis = this.generateQuestionAnalysis(weakPoints, i, difficulty, correctAnswer);
          
          const aiQuestion = {
            userId: userId,
            title: questionContent.title,
            content: questionContent.content,
            type: 1, // 单选题
            subjectId: this.selectedSubject ? this.selectedSubject.subjectId : null,
            difficulty: difficulty,
            options: JSON.stringify(questionOptions.options),
            answer: correctAnswer,
            analysis: detailedAnalysis,
            tags: weakPoints,
            generateReason: `基于用户薄弱知识点：${weakPoints}，难度：${this.difficultyOptions.find(opt => opt.value === difficulty).label}`
          };
          
          await generateAiQuestion(aiQuestion);
        }
        
        // 关闭对话框
        this.showAiQuestionDialog = false;
        // 提示用户生成成功
        alert(`成功生成${questionCount}道${this.difficultyOptions.find(opt => opt.value === difficulty).label}难度的AI题目！题目已保存到AI生成题目表中。`);
      } catch (error) {
        console.error('生成AI题目失败:', error);
        alert('生成AI题目失败，请稍后重试。');
      } finally {
        this.generatingAiQuestion = false;
      }
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

.analyze-button {
  padding: 12px 24px;
  background-color: var(--success-color);
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

.analyze-button:hover:not(:disabled) {
  background-color: #85ce61;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.analyze-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(103, 194, 58, 0.3);
}

.analyze-button:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

/* AI出题按钮样式 */
.ai-question-button {
  padding: 12px 24px;
  background-color: #909399;
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

.ai-question-button:hover:not(:disabled) {
  background-color: #a6a9ad;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}

.ai-question-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(144, 147, 153, 0.3);
}

.ai-question-button:disabled {
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

.analysis-card {
  background-color: var(--card-bg);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color-light);
  padding: 24px;
  margin-top: 20px;
}

.analysis-section {
  margin: 32px 0;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background-color: white;
  border-radius: var(--border-radius-md);
  padding: 24px;
  width: 90%;
  max-width: 500px;
  box-shadow: var(--shadow-lg);
}

.dialog-content h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: var(--text-color);
}

.dialog-content p {
  margin-bottom: 24px;
  color: var(--text-color-secondary);
  line-height: 1.6;
}

/* AI分析结果样式 */
.ai-analysis-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--background-color);
  border-radius: var(--border-radius-md);
  border-left: 4px solid var(--primary-color);
}

.ai-analysis-section h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-summary p {
  margin: 8px 0;
  font-size: 14px;
  color: var(--text-color-secondary);
}

.analysis-summary strong {
  color: var(--text-color);
}

/* AI配置选项样式 */
.ai-config-section {
  margin-bottom: 24px;
}

.ai-config-section h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-item {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-item label {
  width: 80px;
  font-size: 14px;
  color: var(--text-color);
  font-weight: 500;
}

.config-select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  font-size: 14px;
  background-color: white;
  color: var(--text-color);
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.config-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.dialog-note {
  font-size: 13px;
  color: var(--text-color-secondary);
  line-height: 1.5;
  background-color: rgba(64, 158, 255, 0.05);
  padding: 12px;
  border-radius: var(--border-radius-sm);
  border-left: 3px solid var(--primary-color);
}

.dialog-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

.cancel-button {
  padding: 10px 20px;
  background-color: #f5f7fa;
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition);
}

.cancel-button:hover {
  background-color: #ecf5ff;
  border-color: var(--primary-color);
}

.confirm-button {
  padding: 10px 20px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition);
}

.confirm-button:hover:not(:disabled) {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.confirm-button:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.analysis-section h3 {
  margin-bottom: 20px;
}

.analysis-item {
  margin-bottom: 20px;
}

.analysis-item h4 {
  margin-bottom: 10px;
  color: #333;
}

.analysis-item p {
  color: #666;
  line-height: 1.6;
}

.suggestions {
  list-style-type: disc;
  padding-left: 20px;
  color: #666;
  line-height: 1.6;
}

.suggestions li {
  margin-bottom: 5px;
}

.analysis-content {
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
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