<template>
  <div class="question-management">
    <h1>题目管理</h1>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <button class="btn-primary" @click="addQuestion">
        <i class="icon-plus"></i> 添加题目
      </button>
    </div>
    
    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <div class="filter-item">
        <label>学科：</label>
        <select v-model="searchParams.subjectId">
          <option value="">全部</option>
          <option v-for="subject in subjects" :key="subject.id" :value="subject.id">
            {{ subject.name }}
          </option>
        </select>
      </div>
      
      <div class="filter-item">
        <label>分类：</label>
        <select v-model="searchParams.categoryId">
          <option value="">全部</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>
      
      <div class="filter-item">
        <label>题型：</label>
        <select v-model="searchParams.type">
          <option value="">全部</option>
          <option value="1">单选题</option>
          <option value="2">多选题</option>
          <option value="3">判断题</option>
          <option value="4">填空题</option>
          <option value="5">简答题</option>
        </select>
      </div>
      
      <div class="filter-item">
        <label>难度：</label>
        <select v-model="searchParams.difficulty">
          <option value="">全部</option>
          <option value="1">简单</option>
          <option value="2">中等</option>
          <option value="3">困难</option>
        </select>
      </div>
      
      <div class="filter-item search">
        <input 
          type="text" 
          v-model="searchParams.keyword" 
          placeholder="搜索题目..."
          @keyup.enter="loadQuestions"
        />
        <button class="btn-search" @click="loadQuestions">搜索</button>
      </div>
    </div>
    
    <!-- 题目列表 -->
    <div class="question-list">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>题目</th>
            <th>学科</th>
            <th>题型</th>
            <th>分类</th>
            <th>难度</th>
            <th>正确率</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="question in questions" :key="question.id">
            <td>{{ question.id }}</td>
            <td class="question-title">{{ question.title }}</td>
            <td>{{ question.subjectName || '未设置' }}</td>
            <td>{{ getQuestionType(question.type) }}</td>
            <td>{{ getCategoryName(question.categoryId) }}</td>
            <td>{{ getDifficultyLevel(question.difficulty) }}</td>
            <td>{{ (question.correctRate * 100).toFixed(1) }}%</td>
            <td class="action-buttons">
              <button class="btn-edit" @click="editQuestion(question.id)">
                编辑
              </button>
              <button class="btn-delete" @click="deleteQuestion(question.id)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <button 
        class="btn-page" 
        :disabled="currentPage === 1" 
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        class="btn-page" 
        :disabled="currentPage === totalPages" 
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { questionApi } from '../api/exercise';

export default {
  name: 'QuestionManagementView',
  setup() {
    const router = useRouter();
    const questions = ref([]);
    const categories = ref([]);
    const subjects = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const loading = ref(false);
    
    const searchParams = ref({
      keyword: '',
      subjectId: '',
      categoryId: '',
      type: '',
      difficulty: ''
    });
    
    const totalPages = computed(() => {
      return Math.ceil(total.value / pageSize.value);
    });
    
    // 加载题目列表
    const loadQuestions = async () => {
      loading.value = true;
      try {
        const response = await questionApi.getQuestions({
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          keyword: searchParams.value.keyword,
          subjectId: searchParams.value.subjectId,
          categoryId: searchParams.value.categoryId,
          type: searchParams.value.type,
          difficulty: searchParams.value.difficulty
        });
        questions.value = response.data.records;
        total.value = response.data.total;
      } catch (error) {
        console.error('加载题目失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    // 加载分类列表
    const loadCategories = async () => {
      try {
        const response = await questionApi.getAllCategories();
        categories.value = response.data;
      } catch (error) {
        console.error('加载分类失败:', error);
      }
    };
    
    // 加载学科列表
    const loadSubjects = async () => {
      try {
        const response = await questionApi.getAllSubjects();
        subjects.value = response.data;
      } catch (error) {
        console.error('加载学科失败:', error);
      }
    };
    
    // 切换页面
    const changePage = (page) => {
      currentPage.value = page;
      loadQuestions();
    };
    
    // 添加题目
    const addQuestion = () => {
      router.push('/exercise/questions/new');
    };
    
    // 编辑题目
    const editQuestion = (id) => {
      router.push(`/exercise/questions/${id}`);
    };
    
    // 删除题目
    const deleteQuestion = async (id) => {
      if (confirm('确定要删除这个题目吗？')) {
        try {
          await questionApi.deleteQuestion(id);
          loadQuestions();
        } catch (error) {
          console.error('删除题目失败:', error);
        }
      }
    };
    
    // 获取题目类型
    const getQuestionType = (type) => {
      const types = {
        1: '单选题',
        2: '多选题',
        3: '判断题',
        4: '填空题',
        5: '简答题'
      };
      return types[type] || '未知';
    };
    
    // 获取难度等级
    const getDifficultyLevel = (difficulty) => {
      const levels = {
        1: '简单',
        2: '中等',
        3: '困难'
      };
      return levels[difficulty] || '未知';
    };
    
    // 获取分类名称
    const getCategoryName = (categoryId) => {
      if (!categoryId) return '未分类';
      const category = categories.value.find(c => c.id === categoryId);
      return category ? category.name : '未知';
    };
    
    onMounted(() => {
      loadCategories();
      loadSubjects();
      loadQuestions();
    });
    
    return {
      questions,
      categories,
      subjects,
      currentPage,
      totalPages,
      searchParams,
      loadQuestions,
      changePage,
      addQuestion,
      editQuestion,
      deleteQuestion,
      getQuestionType,
      getDifficultyLevel,
      getCategoryName
    };
  }
};
</script>

<style scoped>
.question-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 20px;
  color: #333;
}

.action-bar {
  margin-bottom: 20px;
}

.btn-primary {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:hover {
  background-color: #66b1ff;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #606266;
}

.filter-item select,
.filter-item input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.filter-item.search {
  flex: 1;
  min-width: 300px;
}

.filter-item.search input {
  flex: 1;
  margin-right: 8px;
}

.btn-search {
  background-color: #67c23a;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.question-list {
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

th {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #303133;
}

.question-title {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-edit {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-delete {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.btn-page {
  background-color: #ffffff;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-page:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #606266;
}
</style>