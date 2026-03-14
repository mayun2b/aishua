<template>
  <div class="subject-detail">
    <h1>{{ isEditMode ? '编辑学科' : '添加学科' }}</h1>
    
    <form @submit.prevent="saveSubject">
      <div class="form-section">
        <h2>基本信息</h2>
        
        <div class="form-item">
          <label for="name">学科名称：</label>
          <input 
            type="text" 
            id="name" 
            v-model="formData.name" 
            required 
            placeholder="请输入学科名称"
          />
        </div>
        
        <div class="form-item">
          <label for="code">学科代码：</label>
          <input 
            type="text" 
            id="code" 
            v-model="formData.code" 
            required 
            placeholder="请输入学科代码"
          />
        </div>
        
        <div class="form-item">
          <label for="description">学科描述：</label>
          <textarea 
            id="description" 
            v-model="formData.description" 
            placeholder="请输入学科描述"
            rows="4"
          ></textarea>
        </div>
        
        <div class="form-item">
          <label for="sort">排序值：</label>
          <input 
            type="number" 
            id="sort" 
            v-model.number="formData.sort" 
            required 
            placeholder="请输入排序值"
            min="0"
          />
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="goBack">取消</button>
        <button type="submit" class="btn-submit">保存</button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { questionApi } from '../api/exercise';

export default {
  name: 'SubjectDetailView',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const subjectId = route.params.id;
    const isEditMode = subjectId !== 'new';
    
    const formData = ref({
      name: '',
      code: '',
      description: '',
      sort: 0
    });
    
    // 加载学科详情
    const loadSubject = async () => {
      if (!isEditMode) return;
      
      try {
        const response = await questionApi.getSubjectById(subjectId);
        const subject = response.data;
        formData.value = {
          name: subject.name,
          code: subject.code,
          description: subject.description,
          sort: subject.sort
        };
      } catch (error) {
        console.error('加载学科失败:', error);
      }
    };
    
    // 保存学科
    const saveSubject = async () => {
      try {
        if (isEditMode) {
          await questionApi.updateSubject(subjectId, formData.value);
        } else {
          await questionApi.createSubject(formData.value);
        }
        router.push('/exercise/subjects');
      } catch (error) {
        console.error('保存学科失败:', error);
      }
    };
    
    // 返回列表
    const goBack = () => {
      router.push('/exercise/subjects');
    };
    
    onMounted(() => {
      loadSubject();
    });
    
    return {
      isEditMode,
      formData,
      saveSubject,
      goBack
    };
  }
};
</script>

<style scoped>
.subject-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 20px;
  color: #333;
}

h2 {
  margin: 20px 0 15px;
  color: #606266;
  font-size: 16px;
  font-weight: 600;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.form-item {
  margin-bottom: 15px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-item textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn-cancel {
  background-color: #ffffff;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover {
  background-color: #66b1ff;
}
</style>