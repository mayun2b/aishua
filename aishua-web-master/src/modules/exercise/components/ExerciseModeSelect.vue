<template>
  <div class="exercise-mode-select">
    <h2>选择练习模式</h2>
    
    <div v-if="!isLoggedIn" class="guest-notice">
      <p>您当前是游客模式，可以免费练习，但练习记录不会保存。</p>
      <van-button type="primary" size="small" @click="goToLogin">立即登录</van-button>
    </div>
    
    <div class="mode-options">
      <van-card 
        v-for="mode in exerciseModes" 
        :key="mode.value"
        :class="{ active: selectedMode === mode.value, 'require-login': mode.requireLogin }"
        @click="selectMode(mode.value)"
      >
        <template #title>
          <h3>{{ mode.label }}</h3>
        </template>
        <template #desc>
          <p>{{ mode.description }}</p>
          <span v-if="mode.requireLogin" class="login-required">需登录</span>
        </template>
      </van-card>
    </div>
    
    <div class="config-section" v-if="selectedMode">
      <van-form>
        <van-field label="题目数量">
          <template #input>
            <van-slider 
              v-model="configForm.count" 
              :min="5" 
              :max="50" 
              :step="5"
            />
          </template>
        </van-field>
        
        <van-field label="难度等级">
          <template #input>
            <van-dropdown-menu>
              <van-dropdown-item 
                v-model="configForm.difficulty" 
                :options="difficultyOptions"
                placeholder="选择难度"
              />
            </van-dropdown-menu>
          </template>
        </van-field>
        
        <van-field label="题目分类">
          <template #input>
            <van-dropdown-menu>
              <van-dropdown-item 
                v-model="configForm.categoryId" 
                :options="categoryOptions"
                placeholder="选择分类"
              />
            </van-dropdown-menu>
          </template>
        </van-field>
      </van-form>
      
      <van-button type="primary" block @click="startExercise" :loading="loading">
        开始练习
      </van-button>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { 
  Toast,
  Card as vanCard, 
  Button as vanButton, 
  Form as vanForm, 
  Field as vanField, 
  Slider as vanSlider,
  DropdownMenu as vanDropdownMenu,
  DropdownItem as vanDropdownItem
} from 'vant';
import { exerciseApi, questionApi } from '@/modules/exercise/api/exercise';

export default {
  name: 'ExerciseModeSelect',
  components: {
    [vanCard.name]: vanCard,
    [vanButton.name]: vanButton,
    [vanForm.name]: vanForm,
    [vanField.name]: vanField,
    [vanSlider.name]: vanSlider,
    [vanDropdownMenu.name]: vanDropdownMenu,
    [vanDropdownItem.name]: vanDropdownItem
  },
  setup(props, { emit }) {
    const router = useRouter();
    const selectedMode = ref(null);
    const loading = ref(false);
    const isLoggedIn = ref(!!localStorage.getItem('token'));
    const configForm = reactive({
      count: 10,
      difficulty: null,
      categoryId: null
    });
    
    const exerciseModes = [
      {
        value: 1,
        label: '顺序练习',
        description: '按顺序答题，可查看答案解析'
      },
      {
        value: 2,
        label: '随机练习',
        description: '随机抽取题目，提高应变能力'
      },
      {
        value: 3,
        label: '错题重练',
        description: '专门练习之前的错题',
        requireLogin: true
      },
      {
        value: 4,
        label: '限时练习',
        description: '在规定时间内完成题目',
        requireLogin: true
      }
    ];
    
    const difficultyOptions = [
      { text: '简单', value: 1 },
      { text: '中等', value: 2 },
      { text: '困难', value: 3 }
    ];
    
    const categoryOptions = ref([]);
    
    const loadCategories = async () => {
      try {
        const response = await questionApi.getAllCategories();
        if (response && response.code === 200) {
          categoryOptions.value = (response.data || []).map(category => ({
            text: category.name,
            value: category.id
          }));
        }
      } catch (error) {
        console.error('加载题目分类失败:', error);
        Toast.fail('加载题目分类失败');
      }
    };
    
    onMounted(() => {
      loadCategories();
    });
    
    const selectMode = (mode) => {
      const modeConfig = exerciseModes.find(m => m.value === mode);
      if (modeConfig && modeConfig.requireLogin && !isLoggedIn.value) {
        Toast.fail('此功能需要登录才能使用');
        return;
      }
      selectedMode.value = mode;
    };
    
    const startExercise = async () => {
      if (!selectedMode.value) {
        Toast.fail('请选择练习模式');
        return;
      }

      loading.value = true;
      try {
        const params = {
          count: configForm.count,
          exerciseMode: selectedMode.value
        };

        if (configForm.difficulty) {
          params.difficulty = configForm.difficulty;
        }
        if (configForm.categoryId) {
          params.categoryId = configForm.categoryId;
        }

        const response = await exerciseApi.startExercise(params);
        if (response && response.code === 200) {
          // 将题目数据传递给练习页面
          emit('exercise-started', {
            questions: response.data,
            mode: selectedMode.value,
            config: { ...configForm }
          });
        } else {
          Toast.fail('获取题目失败');
        }
      } catch (error) {
        console.error('开始练习失败:', error);
        Toast.fail('开始练习失败，请重试');
      } finally {
        loading.value = false;
      }
    };
    
    const goToLogin = () => {
      router.push('/login');
    };
    
    return {
      selectedMode,
      loading,
      isLoggedIn,
      configForm,
      exerciseModes,
      difficultyOptions,
      categoryOptions,
      selectMode,
      startExercise,
      goToLogin
    };
  }
};
</script>

<style scoped>
.exercise-mode-select {
  padding: 20px;
}

.guest-notice {
  background-color: #f4f4f5;
  color: #909399;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mode-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.van-card {
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.van-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.van-card.active {
  border: 2px solid #1989fa;
}

.van-card.require-login .van-card__content {
  opacity: 0.7;
}

.login-required {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ee0a24;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
}

.config-section {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #ebedf0;
  border-radius: 4px;
}

.config-section .van-button {
  margin-top: 20px;
}
</style>