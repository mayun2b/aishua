<template>
  <div class="register-container">
    <RegisterForm @register-success="handleRegisterSuccess" />
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import RegisterForm from '../components/RegisterForm.vue';

export default {
  name: 'RegisterView',
  components: {
    RegisterForm
  },
  setup() {
    const router = useRouter();

    const handleRegisterSuccess = () => {
      // 注册成功后跳转到登录页面
      router.push('/login');
    };

    return {
      handleRegisterSuccess
    };
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>