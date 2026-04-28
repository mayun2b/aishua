<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>账号登录</h1>
      <p class="subtitle">登录后会自动识别角色并进入用户工作台或管理控制台。</p>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label>
          <span>手机号</span>
          <input
            v-model.trim="form.phone"
            type="text"
            maxlength="11"
            placeholder="请输入手机号"
          />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            maxlength="32"
            placeholder="请输入密码"
          />
        </label>

        <button type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="actions">
        <router-link to="/register">没有账号？去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const store = useStore()
const submitting = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const handleSubmit = async () => {
  if (!form.phone) {
    showToast('请输入手机号')
    return
  }

  if (!form.password) {
    showToast('请输入密码')
    return
  }

  submitting.value = true
  try {
    const auth = await store.dispatch('auth/login', { ...form })
    showToast('登录成功')

    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect.startsWith('/')) {
      router.push(redirect)
      return
    }

    router.push(auth.user.isAdmin === 1 ? '/admin' : '/dashboard')
  } catch (error) {
    showToast(error.message || '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #f4efe6 0%, #dfe9f3 100%);
}

.auth-card {
  width: min(420px, 100%);
  padding: 36px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.15);
}

.auth-card h1 {
  margin: 0;
  font-size: 30px;
  color: #17324d;
}

.subtitle {
  margin: 12px 0 0;
  color: #5e6d7c;
  line-height: 1.6;
}

.auth-form {
  display: grid;
  gap: 18px;
  margin-top: 28px;
}

.auth-form label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.auth-form input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #c7d4e0;
  border-radius: 14px;
  font-size: 15px;
  box-sizing: border-box;
}

.auth-form button {
  border: 0;
  border-radius: 14px;
  padding: 14px 18px;
  background: #17324d;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}

.auth-form button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.actions {
  margin-top: 20px;
}

.actions a {
  color: #17324d;
  text-decoration: none;
  font-weight: 600;
}
</style>
