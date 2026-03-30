<template>
  <div class="subject-management">
    <h1>学科管理</h1>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <button class="btn-primary" @click="addSubject">
        <i class="icon-plus"></i> 添加学科
      </button>
    </div>
    
    <!-- 学科列表 -->
    <div class="subject-list">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>学科名称</th>
            <th>学科代码</th>
            <th>排序</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="subject in subjects" :key="subject.id">
            <td>{{ subject.id }}</td>
            <td>{{ subject.name }}</td>
            <td>{{ subject.code }}</td>
            <td>{{ subject.sort }}</td>
            <td class="action-buttons">
              <button class="btn-edit" @click="editSubject(subject.id)">
                编辑
              </button>
              <button class="btn-delete" @click="deleteSubject(subject.id)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { questionApi } from '../api/exercise';

export default {
  name: 'SubjectManagementView',
  setup() {
    const router = useRouter();
    const subjects = ref([]);
    const loading = ref(false);
    
    // 加载学科列表
    const loadSubjects = async () => {
      loading.value = true;
      try {
        const response = await questionApi.getAllSubjects();
        subjects.value = response.data;
      } catch (error) {
        console.error('加载学科失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    // 添加学科
    const addSubject = () => {
      router.push('/exercise/subjects/new');
    };
    
    // 编辑学科
    const editSubject = (id) => {
      router.push(`/exercise/subjects/${id}`);
    };
    
    // 删除学科
    const deleteSubject = async (id) => {
      if (confirm('确定要删除这个学科吗？')) {
        try {
          await questionApi.deleteSubject(id);
          loadSubjects();
        } catch (error) {
          console.error('删除学科失败:', error);
        }
      }
    };
    
    onMounted(() => {
      loadSubjects();
    });
    
    return {
      subjects,
      loadSubjects,
      addSubject,
      editSubject,
      deleteSubject
    };
  }
};
</script>

<style scoped>
.subject-management {
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

.subject-list {
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
</style>