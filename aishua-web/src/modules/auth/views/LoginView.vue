<template>
  <div class="login-container">
    <LoginForm @login-success="handleLoginSuccess" />
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import LoginForm from '../components/LoginForm.vue';

export default {
  name: 'LoginView',
  components: {
    LoginForm
  },
  setup() {
    const router = useRouter();

    const handleLoginSuccess = (userData) => {
      // 保存用户信息到localStorage
      console.log('登录成功，保存用户数据:', userData);
      localStorage.setItem('token', userData.token);
      localStorage.setItem('userId', userData.userId);
      localStorage.setItem('username', userData.nickname); // 使用nickname作为用户名
      localStorage.setItem('isAdmin', userData.isAdmin ? 'true' : 'false'); // 保存管理员标识
      
      // 验证保存是否成功
      console.log('保存后token:', localStorage.getItem('token'));
      console.log('保存后userId:', localStorage.getItem('userId'));
      
      // 跳转到首页
      router.push('/dashboard');
    };

    return {
      handleLoginSuccess
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>