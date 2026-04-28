<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>用户注册</h1>
      <p class="subtitle">先完成注册和登录闭环，后续再扩展更复杂的注册校验。</p>

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
          <span>昵称</span>
          <input
            v-model.trim="form.nickname"
            type="text"
            maxlength="32"
            placeholder="可选，不填将自动生成"
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

        <label>
          <span>确认密码</span>
          <input
            v-model="form.confirmPassword"
            type="password"
            maxlength="32"
            placeholder="请再次输入密码"
          />
        </label>

        <button type="submit" :disabled="submitting">
          {{ submitting ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="actions">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { showToast } from 'vant'

const router = useRouter()
const store = useStore()
const submitting = ref(false)

const form = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: ''
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

  if (form.password !== form.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }

  submitting.value = true
  try {
    await store.dispatch('auth/register', {
      phone: form.phone,
      nickname: form.nickname,
      password: form.password
    })
    showToast('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    showToast(error.message || '注册失败')
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
  background: linear-gradient(135deg, #f6f0df 0%, #d7e6f2 100%);
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
  background: #0f5c4d;
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
