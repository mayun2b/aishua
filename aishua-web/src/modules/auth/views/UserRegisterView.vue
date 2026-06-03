<template>
  <div class="auth-page">
    <div class="auth-card">
      <p class="eyebrow">Create Account</p>
      <h1>用户注册</h1>
      <p class="subtitle">完成注册后立即登录，系统会根据你的角色进入对应工作台。</p>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label>
          <span>手机号</span>
          <input
            v-model.trim="form.phone"
            type="text"
            maxlength="11"
            autocomplete="tel"
            placeholder="请输入 11 位手机号"
          />
        </label>

        <label>
          <span>昵称</span>
          <input
            v-model.trim="form.nickname"
            type="text"
            maxlength="32"
            autocomplete="nickname"
            placeholder="可选，不填将自动生成"
          />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            maxlength="32"
            autocomplete="new-password"
            placeholder="请输入密码"
          />
        </label>

        <label>
          <span>确认密码</span>
          <input
            v-model="form.confirmPassword"
            type="password"
            maxlength="32"
            autocomplete="new-password"
            placeholder="请再次输入密码"
          />
        </label>

        <button type="submit" :disabled="submitting">
          {{ submitting ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="actions">
        <router-link :to="loginLink">已有账号？去登录</router-link>
        <router-link to="/">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const store = useStore()
const submitting = ref(false)

const form = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const isValidPhone = (value) => /^1\d{10}$/.test(String(value || ''))

const resolveRedirectPath = (value) => {
  if (typeof value !== 'string' || !value.startsWith('/')) {
    return ''
  }
  if (value.startsWith('/login') || value.startsWith('/register')) {
    return ''
  }
  return value
}

const loginLink = computed(() => {
  const redirect = resolveRedirectPath(route.query.redirect)
  if (!redirect) {
    return { path: '/login' }
  }
  return {
    path: '/login',
    query: {
      redirect
    }
  }
})

const handleSubmit = async () => {
  if (!isValidPhone(form.phone)) {
    showToast('请输入正确的 11 位手机号')
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
    router.replace(loginLink.value)
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
  background:
    radial-gradient(circle at top right, rgba(23, 50, 77, 0.16), transparent 34%),
    radial-gradient(circle at top left, rgba(15, 122, 67, 0.16), transparent 30%),
    linear-gradient(135deg, #f6f0df 0%, #d7e6f2 100%);
}

.auth-card {
  width: min(430px, 100%);
  padding: 36px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.15);
}

.eyebrow {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.15em;
}

.auth-card h1 {
  margin: 10px 0 0;
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
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.auth-form input:focus {
  border-color: #1d4ed8;
  box-shadow: 0 0 0 3px rgba(29, 78, 216, 0.12);
}

.auth-form button {
  border: 0;
  border-radius: 14px;
  min-height: 46px;
  padding: 12px 18px;
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
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.actions a {
  color: #17324d;
  text-decoration: none;
  font-weight: 600;
}

@media (max-width: 560px) {
  .auth-page {
    padding: 16px;
  }

  .auth-card {
    padding: 28px 22px;
    border-radius: 20px;
  }
}
</style>
