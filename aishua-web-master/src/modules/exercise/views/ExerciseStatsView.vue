<template>
  <div class="exercise-stats">
    <h2>练习统计</h2>
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-number">{{ stats.totalCount || 0 }}</div>
        <div class="stat-label">总练习题数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-number">{{ stats.correctCount || 0 }}</div>
        <div class="stat-label">正确题数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-number">{{ (stats.correctRate * 1 || 0).toFixed(1) }}%</div>
        <div class="stat-label">正确率</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-number">{{ stats.categoryCount || 0 }}</div>
        <div class="stat-label">涉及分类</div>
      </el-card>
    </div>
    
    <div class="stats-chart" v-if="chartData.length > 0">
      <h3>各分类正确率</h3>
      <!-- 这里可以集成图表库，暂时用简单列表展示 -->
      <el-table :data="chartData" style="width: 100%">
        <el-table-column prop="categoryName" label="分类名称" width="180"></el-table-column>
        <el-table-column prop="correctRate" label="正确率">
          <template #default="scope">
            {{ scope && scope.row ? (scope.row.correctRate * 1).toFixed(1) + '%' : '0.0%' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="练习题数" width="120"></el-table-column>
      </el-table>
    </div>
    
    <el-button @click="loadStats" type="primary" :loading="loading">刷新数据</el-button>
  </div>
</template>

<script>
import { exerciseApi } from '@/modules/exercise/api/exercise';

export default {
  name: 'ExerciseStatsView',
  data() {
    return {
      stats: {},
      chartData: [],
      loading: false
    };
  },
  mounted() {
    this.loadStats();
  },
  methods: {
    async loadStats() {
      this.loading = true;
      try {
        const response = await exerciseApi.getUserStats();
        if (response.code === 200) {
          this.stats = response.data;
          // 确保chartData只包含有效的对象
          this.chartData = (response.data.categoryStats || []).filter(item => item !== undefined && item !== null);
        }
      } catch (error) {
        this.$message.error('加载统计数据失败');
        console.error(error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.exercise-stats {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.stats-chart {
  margin: 30px 0;
}

.stats-chart h3 {
  margin-bottom: 20px;
}
</style>