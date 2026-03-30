<template>
  <div class="register-form">
    <h2>用户注册</h2>
    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label for="phone">手机号:</label>
        <input
          type="text"
          id="phone"
          v-model="registerForm.phone"
          placeholder="请输入手机号"
          required
        />
        <div v-if="errors.phone" class="error-message">{{ errors.phone }}</div>
      </div>
      <div class="form-group">
        <label for="nickname">昵称:</label>
        <input
          type="text"
          id="nickname"
          v-model="registerForm.nickname"
          placeholder="请输入昵称"
          required
        />
        <div v-if="errors.nickname" class="error-message">{{ errors.nickname }}</div>
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input
          type="password"
          id="password"
          v-model="registerForm.password"
          placeholder="请输入密码（6-20位）"
          required
        />
        <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
      </div>
      <button type="submit" :disabled="loading" class="register-button">
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </form>
  </div>
</template>

<script>
import { ref } from 'vue';
import { register } from '../api/auth';
import { useRouter } from 'vue-router';

export default {
  name: 'RegisterForm',
  emits: ['register-success'],
  setup(props, { emit }) {
    const registerForm = ref({
      phone: '',
      nickname: '',
      password: ''
    });
    const loading = ref(false);
    const error = ref('');
    const errors = ref({});
    const router = useRouter();

    const validateForm = () => {
      const newErrors = {};
      
      // 验证手机号
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!registerForm.value.phone) {
        newErrors.phone = '手机号不能为空';
      } else if (!phoneRegex.test(registerForm.value.phone)) {
        newErrors.phone = '请输入正确的手机号';
      }
      
      // 验证昵称
      if (!registerForm.value.nickname) {
        newErrors.nickname = '昵称不能为空';
      } else if (registerForm.value.nickname.length > 50) {
        newErrors.nickname = '昵称长度不能超过50个字符';
      }
      
      // 验证密码
      if (!registerForm.value.password) {
        newErrors.password = '密码不能为空';
      } else if (registerForm.value.password.length < 6 || registerForm.value.password.length > 20) {
        newErrors.password = '密码长度必须在6-20个字符之间';
      }
      
      errors.value = newErrors;
      return Object.keys(newErrors).length === 0;
    };

    const handleRegister = async () => {
      if (!validateForm()) {
        return;
      }
      
      loading.value = true;
      error.value = '';
      
      try {
        const response = await register(registerForm.value);
        if (response.code === 200) {
          // 触发注册成功事件
          emit('register-success');
          // 跳转到登录页面
          router.push('/login');
        } else {
          error.value = response.message || '注册失败';
        }
      } catch (err) {
        console.error('注册错误:', err);
        error.value = err.response?.data?.message || '网络错误，请稍后重试';
      } finally {
        loading.value = false;
      }
    };

    return {
      registerForm,
      loading,
      error,
      errors,
      handleRegister
    };
  }
};
</script>

<style scoped>
.register-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.register-form h2 {
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

.register-button {
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

.register-button:hover:not(:disabled) {
  background-color: #337ecc;
}

.register-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.error-message {
  color: #f56565;
  margin-top: 0.25rem;
  font-size: 0.875rem;
}

.login-link {
  margin-top: 1rem;
  text-align: center;
  font-size: 0.875rem;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>