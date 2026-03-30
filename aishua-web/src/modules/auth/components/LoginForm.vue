<template>
  <div class="login-form">
    <h2>用户登录</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="phone">手机号:</label>
        <input
          type="text"
          id="phone"
          v-model="loginForm.phone"
          placeholder="请输入手机号"
          required
        />
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input
          type="password"
          id="password"
          v-model="loginForm.password"
          placeholder="请输入密码"
          required
        />
      </div>
      <button type="submit" :disabled="loading" class="login-button">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </form>
    <div class="additional-actions">
      <router-link to="/register" class="register-link">
        注册账号
      </router-link>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
// 移除未使用的useRouter导入
import { login } from '../api/auth';

export default {
  name: 'LoginForm',
  emits: ['login-success'],
  setup(props, { emit }) {
    // 移除未使用的router变量
    const loginForm = ref({
      phone: '',
      password: ''
    });
    const loading = ref(false);
    const error = ref('');

    const validateForm = () => {
      // 只验证非空，不验证手机号格式
      if (!loginForm.value.phone) {
        error.value = '手机号不能为空';
        return false;
      }
      
      if (!loginForm.value.password) {
        error.value = '密码不能为空';
        return false;
      }
      
      return true;
    };

    const handleLogin = async () => {
      // 先进行前端验证
      if (!validateForm()) {
        return;
      }
      
      loading.value = true;
      error.value = '';
      
      // 调试：打印发送的数据
      console.log('发送登录数据:', loginForm.value);
      
      try {
        const response = await login(loginForm.value);
        console.log('登录响应:', response);
        if (response.code === 200) {
          // 触发登录成功事件
          emit('login-success', response.data);
        } else {
          error.value = response.message || '登录失败';
        }
      } catch (err) {
        console.error('登录错误:', err);
        // 调试：打印详细的错误信息
        if (err.response) {
          console.error('错误响应:', err.response.data);
        }
        error.value = err.response?.data?.message || '网络错误，请稍后重试';
      } finally {
        loading.value = false;
      }
    };

    return {
      loginForm,
      loading,
      error,
      handleLogin
    };
  }
};
</script>

<style scoped>
.login-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.login-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover:not(:disabled) {
  background-color: #337ecc;
}

.login-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.error-message {
  color: #f56565;
  margin-top: 1rem;
  text-align: center;
  font-size: 0.875rem;
}

.additional-actions {
  display: flex;
  justify-content: center;
  margin-top: 1.5rem;
}

.register-link {
  padding: 0.75rem;
  background-color: #28a745;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-size: 1rem;
  text-align: center;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-link:hover {
  background-color: #218838;
}
</style>