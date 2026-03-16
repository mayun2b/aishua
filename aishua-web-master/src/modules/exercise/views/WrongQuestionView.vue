<template>
  <div class="wrong-question-container">
    <div class="header">
      <h2>我的错题本</h2>
      <div class="stats">
        <span>错题总数: {{ wrongCount }}</span>
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="questions.length === 0" class="empty">
      <p>暂无错题，加油练习吧！</p>
    </div>
    
    <div v-else class="question-list">
      <div 
        v-for="question in questions" 
        :key="question.id"
        class="question-item"
      >
        <div class="question-header">
          <div>
            <span class="question-number">第{{ question.id }}题</span>
            <span class="wrong-info">
              做错次数: {{ question.wrongCount || 1 }}
              <span v-if="question.lastWrongTime"> | 最近做错: {{ formatDate(question.lastWrongTime) }}</span>
            </span>
          </div>
          <div class="question-actions">
            <button @click="markAsMastered(question.id)" class="btn mastered">
              标记为已掌握
            </button>
            <button @click="removeFromWrong(question.id)" class="btn remove">
              移除错题
            </button>
          </div>
        </div>
        
        <div class="question-content">
          <h4>{{ question.title }}</h4>
          <div v-if="question.content" class="question-desc">
            {{ question.content }}
          </div>
          
          <div class="options" v-if="question.type <= 3">
            <div 
              v-for="(option, key) in parseOptions(question.options)" 
              :key="key"
              class="option"
            >
              {{ key }}. {{ option }}
            </div>
          </div>
          
          <div v-if="question.type === 4" class="fill-blank">
            <p>填空题答案: {{ question.answer }}</p>
          </div>
          
          <div v-if="question.type === 5" class="short-answer">
            <p>简答题参考答案: {{ question.answer }}</p>
          </div>
        </div>
        
        <div class="analysis" v-if="question.analysis">
          <strong>解析:</strong> {{ question.analysis }}
        </div>
      </div>
    </div>
    
    <div v-if="questions.length > 0" class="pagination">
      <button 
        @click="prevPage" 
        :disabled="currentPage === 1"
        class="btn"
      >
        上一页
      </button>
      <span>第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <button 
        @click="nextPage" 
        :disabled="currentPage === totalPages"
        class="btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script>
import { getWrongQuestions, getWrongCount, markAsMastered, removeFromWrong } from '../api/exercise';
import { showToast } from 'vant';

export default {
  name: 'WrongQuestionView',
  data() {
    return {
      questions: [],
      wrongCount: 0,
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalPages: 1
    };
  },
  async mounted() {
    console.log('WrongQuestionView mounted');
    const token = localStorage.getItem('token');
    console.log('Token from localStorage:', token);
    await this.loadWrongCount();
    await this.loadWrongQuestions();
  },
  methods: {
    async loadWrongCount() {
      try {
        console.log('Loading wrong count...');
        const response = await getWrongCount();
        console.log('Wrong count response:', response);
        this.wrongCount = response.data;
        this.totalPages = Math.ceil(this.wrongCount / this.pageSize);
      } catch (error) {
        console.error('获取错题数量失败:', error);
        showToast('获取错题数量失败');
      }
    },
    
    async loadWrongQuestions() {
      this.loading = true;
      try {
        console.log('Loading wrong questions...');
        const response = await getWrongQuestions(this.currentPage, this.pageSize);
        console.log('Wrong questions response:', response);
        this.questions = response.data.records || [];
        this.totalPages = Math.ceil(response.data.total / this.pageSize);
      } catch (error) {
        console.error('获取错题列表失败:', error);
        showToast('获取错题列表失败');
      } finally {
        this.loading = false;
      }
    },
    
    async markAsMastered(questionId) {
      try {
        await markAsMastered(questionId);
        showToast('标记为已掌握成功');
        await this.loadWrongQuestions();
        await this.loadWrongCount();
      } catch (error) {
        console.error('标记为已掌握失败:', error);
        showToast('标记失败');
      }
    },
    
    async removeFromWrong(questionId) {
      try {
        await removeFromWrong(questionId);
        showToast('移除错题成功');
        // 移除成功后，只更新本地数据，不重新加载
        this.questions = this.questions.filter(q => q.id !== questionId);
        this.wrongCount = Math.max(0, this.wrongCount - 1);
        this.totalPages = Math.ceil(this.wrongCount / this.pageSize);
        // 如果当前页没有数据且不是第一页，跳转到上一页
        if (this.questions.length === 0 && this.currentPage > 1) {
          this.currentPage--;
          // 不重新加载数据，只更新本地状态
        }
      } catch (error) {
        console.error('移除错题失败:', error);
        showToast('移除失败');
      }
    },
    
    async prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        await this.loadWrongQuestions();
      }
    },
    
    async nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        await this.loadWrongQuestions();
      }
    },
    
    parseOptions(optionsStr) {
      if (!optionsStr) return {};
      try {
        const options = JSON.parse(optionsStr);
        // 检查是否是数组格式的选项
        if (Array.isArray(options)) {
          const result = {};
          options.forEach((option) => {
            if (option && typeof option === 'object') {
              if (option.key && option.value) {
                result[option.key] = option.value;
              } else if (option.label && option.value) {
                result[option.label] = option.value;
              }
            }
          });
          return result;
        }
        // 保持原有的对象格式处理
        return options;
      } catch (error) {
        console.error('解析选项失败:', error);
        return {};
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString();
    }
  }
};
</script>

<style scoped>
.wrong-question-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.header h2 {
  margin: 0;
  color: #333;
}

.stats {
  color: #666;
  font-size: 14px;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #666;
}

.question-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #fff;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
  color: #e74c3c;
  margin-right: 15px;
}

.wrong-info {
  font-size: 12px;
  color: #666;
  margin-left: 10px;
}

.question-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.mastered {
  background-color: #27ae60;
  color: white;
}

.remove {
  background-color: #e74c3c;
  color: white;
}

.question-content h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.question-desc {
  margin-bottom: 15px;
  color: #666;
  line-height: 1.5;
}

.options {
  margin-bottom: 15px;
}

.option {
  padding: 5px 0;
  color: #555;
}

.fill-blank, .short-answer {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 14px;
}

.analysis {
  background-color: #e8f4fd;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.pagination span {
  color: #666;
}
</style>