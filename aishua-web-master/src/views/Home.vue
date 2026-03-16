<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <header class="home-header" :class="{ 'scrolled': isScrolled }">
      <div class="container">
        <div class="header-content">
          <div class="logo" @click="goToHome">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>爱刷题系统</span>
          </div>
          <nav class="nav-menu">
            <a href="#features" class="nav-link">功能特色</a>
            <a href="#how-it-works" class="nav-link">使用流程</a>
            <a href="#testimonials" class="nav-link">用户评价</a>
          </nav>
          <div class="header-actions">
            <button class="btn-outline" @click="goToLogin">登录</button>
            <button class="btn-primary" @click="goToRegister">注册</button>
          </div>
        </div>
      </div>
    </header>

    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <h1>智能刷题系统</h1>
            <h2>让学习更高效</h2>
            <p>通过AI智能算法，为您定制个性化学习方案，提供精准的题目推荐和详细的学习分析，帮助您快速掌握知识点，提高学习效率。</p>
            <div class="hero-buttons">
              <button class="btn-primary" @click="goToExercise">开始刷题</button>
              <button class="btn-outline" @click="goToDashboard">了解更多</button>
            </div>
          </div>
          <div class="hero-image">
            <div class="hero-image-container">
              <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20education%20platform%20dashboard%20with%20statistics%20and%20charts%2C%20clean%20design%2C%20blue%20theme%2C%203d%20elements&image_size=landscape_16_9" alt="智能刷题系统" />
              <div class="hero-image-overlay"></div>
            </div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">10,000+</span>
            <span class="stat-label">题库数量</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">98%</span>
            <span class="stat-label">用户满意度</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">50,000+</span>
            <span class="stat-label">活跃用户</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色 -->
    <section id="features" class="features-section">
      <div class="container">
        <div class="section-header">
          <h2>功能特色</h2>
          <p>我们提供丰富的功能，帮助您高效学习</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" data-aos="fade-up" data-aos-delay="100">
            <div class="feature-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>多样化题型</h3>
            <p>支持单选题、多选题、判断题、填空题和简答题等多种题型，满足不同学科的学习需求。</p>
          </div>
          <div class="feature-card" data-aos="fade-up" data-aos-delay="200">
            <div class="feature-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 20V10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 20V4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6 20V14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>学习统计</h3>
            <p>详细的学习数据统计，包括正确率、答题时间、学习进度等，帮助您了解自己的学习情况。</p>
          </div>
          <div class="feature-card" data-aos="fade-up" data-aos-delay="300">
            <div class="feature-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>错题本</h3>
            <p>自动收集错题，提供详细的解析和相关知识点，帮助您针对性地进行复习。</p>
          </div>
          <div class="feature-card" data-aos="fade-up" data-aos-delay="400">
            <div class="feature-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>考试模式</h3>
            <p>模拟真实考试环境，限时答题，帮助您适应考试节奏，提高应试能力。</p>
          </div>
          <div class="feature-card" data-aos="fade-up" data-aos-delay="500">
            <div class="feature-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22 10v6M2 10l10-5 10 5-10 5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6 12v5c3 3 9 3 12 0v-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>学科分类</h3>
            <p>按学科和知识点进行分类，方便您针对性地进行练习和复习。</p>
          </div>
          <div class="feature-card" data-aos="fade-up" data-aos-delay="600">
            <div class="feature-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>用户管理</h3>
            <p>管理员可以方便地管理用户、题目和学科，系统操作简单直观。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 使用流程 -->
    <section id="how-it-works" class="how-it-works-section">
      <div class="container">
        <div class="section-header">
          <h2>使用流程</h2>
          <p>简单几步，开始高效学习</p>
        </div>
        <div class="steps-container">
          <div class="step-item" data-aos="fade-up" data-aos-delay="100">
            <div class="step-number">1</div>
            <h3>注册登录</h3>
            <p>创建账号并登录系统，开始您的学习之旅。</p>
          </div>
          <div class="step-item" data-aos="fade-up" data-aos-delay="200">
            <div class="step-number">2</div>
            <h3>选择学科</h3>
            <p>根据您的学习需求，选择相应的学科和知识点。</p>
          </div>
          <div class="step-item" data-aos="fade-up" data-aos-delay="300">
            <div class="step-number">3</div>
            <h3>开始练习</h3>
            <p>选择练习模式，开始刷题，系统会自动记录您的答题情况。</p>
          </div>
          <div class="step-item" data-aos="fade-up" data-aos-delay="400">
            <div class="step-number">4</div>
            <h3>查看统计</h3>
            <p>查看学习统计和错题本，针对性地进行复习和提升。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 用户评价 -->
    <section id="testimonials" class="testimonials-section">
      <div class="container">
        <div class="section-header">
          <h2>用户评价</h2>
          <p>听听我们的用户怎么说</p>
        </div>
        <div class="testimonials-grid">
          <div class="testimonial-card" data-aos="fade-up" data-aos-delay="100">
            <div class="rating">⭐⭐⭐⭐⭐</div>
            <p>这个系统真的很棒！界面美观，功能齐全，特别是错题本功能，帮助我快速掌握薄弱知识点。</p>
            <div class="user-info">
              <div class="user-avatar">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="user-details">
                <h4>小明</h4>
                <p>大学生</p>
              </div>
            </div>
          </div>
          <div class="testimonial-card" data-aos="fade-up" data-aos-delay="200">
            <div class="rating">⭐⭐⭐⭐⭐</div>
            <p>作为一名教师，我推荐我的学生使用这个系统。它不仅提供了丰富的题目，还能帮助学生分析学习情况，非常实用。</p>
            <div class="user-info">
              <div class="user-avatar">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="user-details">
                <h4>李老师</h4>
                <p>高中教师</p>
              </div>
            </div>
          </div>
          <div class="testimonial-card" data-aos="fade-up" data-aos-delay="300">
            <div class="rating">⭐⭐⭐⭐⭐</div>
            <p>考试前使用这个系统进行复习，效果非常好！系统推荐的题目很有针对性，帮助我在考试中取得了好成绩。</p>
            <div class="user-info">
              <div class="user-avatar">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="user-details">
                <h4>小红</h4>
                <p>高中生</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>



    <!-- 行动召唤 -->
    <section class="cta-section">
      <div class="container">
        <h2>准备好开始您的学习之旅了吗？</h2>
        <p>加入我们，体验智能刷题的乐趣，提高学习效率，实现学习目标。</p>
        <div class="cta-buttons">
          <button class="btn-primary" @click="goToRegister">立即注册</button>
          <button class="btn-outline" @click="goToLogin">已有账号？登录</button>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <div class="footer-logo" @click="goToHome">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>爱刷题系统</span>
            </div>
            <p>智能刷题系统，让学习更高效。通过AI智能算法，为您定制个性化学习方案。</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <ul>
              <li><a href="#features">功能特色</a></li>
              <li><a href="#how-it-works">使用流程</a></li>
              <li><a href="#testimonials">用户评价</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>联系我们</h4>
            <ul>
              <li>邮箱：contact@aishuati.com</li>
              <li>电话：400-123-4567</li>
              <li>地址：北京市海淀区中关村</li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>关注我们</h4>
            <div class="social-links">
              <a href="#" class="social-link">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18 2H15C13.6739 2 12.4021 2.52678 11.4645 3.46447C10.5268 4.40215 10 5.67392 10 7V10H7V14H10V22H14V14H17L18 10H14V7C14 6.73478 14.1054 6.48043 14.2929 6.29289C14.4804 6.10536 14.7348 6 15 6H18V2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </a>
              <a href="#" class="social-link">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </a>
              <a href="#" class="social-link">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="2" y="2" width="20" height="20" rx="5" ry="5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <line x1="17.5" y1="6.5" x2="17.51" y2="6.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </a>
              <a href="#" class="social-link">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <rect x="2" y="9" width="4" height="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="4" cy="4" r="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 爱刷题系统. 保留所有权利.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'

const router = useRouter()
const isScrolled = ref(false)

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const goToExercise = () => {
  router.push('/exercise')
}

const goToDashboard = () => {
  router.push('/dashboard')
}

const goToHome = () => {
  router.push('/')
}

onMounted(() => {
  // 添加滚动事件监听
  window.addEventListener('scroll', handleScroll)
  
  // 初始化AOS动画库
  if (typeof window !== 'undefined') {
    // 动态导入AOS
    import('aos').then(AOS => {
      AOS.default.init({
        duration: 800,
        easing: 'ease-in-out',
        once: true
      })
    })
  }
})

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}
</script>

<style scoped>
:root {
  --primary-color: #3b82f6;
  --primary-dark: #2563eb;
  --primary-light: #60a5fa;
  --secondary-color: #64748b;
  --background-color: #ffffff;
  --card-background: #f9fafb;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --border-color: #e5e7eb;
  --success-color: #10b981;
  --warning-color: #f59e0b;
  --danger-color: #ef4444;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --border-radius: 8px;
  --transition: all 0.3s ease;
  --gradient-primary: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  --gradient-secondary: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: var(--text-primary);
  background-color: var(--background-color);
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 导航栏 */
.home-header {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-sm);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 1rem 0;
  transition: var(--transition);
}

.home-header.scrolled {
  background-color: rgba(255, 255, 255, 0.98);
  box-shadow: var(--shadow-md);
  padding: 0.75rem 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 1.25rem;
  font-weight: bold;
  color: var(--primary-color);
  gap: 0.5rem;
  cursor: pointer;
  transition: var(--transition);
}

.logo:hover {
  transform: scale(1.05);
}

.nav-menu {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: var(--text-primary);
  text-decoration: none;
  font-weight: 500;
  transition: var(--transition);
  position: relative;
  padding: 0.5rem 0;
}

.nav-link:hover {
  color: var(--primary-color);
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: var(--primary-color);
  transition: var(--transition);
}

.nav-link:hover::after {
  width: 100%;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

/* 按钮样式 */
.btn-primary {
  background: var(--gradient-primary);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: var(--transition);
}

.btn-primary:hover::before {
  left: 100%;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.btn-outline {
  background-color: white;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
}

.btn-outline::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--gradient-secondary);
  transition: var(--transition);
  z-index: -1;
}

.btn-outline:hover {
  color: white;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.btn-outline:hover::before {
  left: 0;
}

/* 英雄区域 */
.hero-section {
  padding: 8rem 0 4rem;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 50%;
  height: 200%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0px);
  }
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  align-items: center;
  margin-bottom: 3rem;
  position: relative;
  z-index: 1;
}

.hero-text h1 {
  font-size: 3rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.2;
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--primary-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-text h2 {
  font-size: 1.5rem;
  color: var(--primary-color);
  margin-bottom: 1.5rem;
  font-weight: 600;
}

.hero-text p {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
  max-width: 500px;
}

.hero-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.hero-image-container {
  position: relative;
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  transform: perspective(1000px) rotateY(-5deg);
  transition: var(--transition);
}

.hero-image-container:hover {
  transform: perspective(1000px) rotateY(0deg) scale(1.02);
}

.hero-image img {
  max-width: 100%;
  height: auto;
  display: block;
}

.hero-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.05) 100%);
  pointer-events: none;
}

.hero-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 2rem;
  border-top: 1px solid rgba(100, 116, 139, 0.1);
  position: relative;
  z-index: 1;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
  backdrop-filter: blur(5px);
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
  background-color: white;
}

.stat-number {
  display: block;
  font-size: 1.75rem;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 0.25rem;
  transition: var(--transition);
}

.stat-item:hover .stat-number {
  transform: scale(1.1);
}

.stat-label {
  display: block;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* 通用部分样式 */
.section-header {
  text-align: center;
  margin-bottom: 3rem;
  padding-top: 2rem;
}

.section-header h2 {
  font-size: 2.25rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  position: relative;
  display: inline-block;
}

.section-header h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: var(--gradient-primary);
  border-radius: 2px;
}

.section-header p {
  font-size: 1.125rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 1rem auto 0;
}

/* 功能特色 */
.features-section {
  padding: 6rem 0;
  background-color: white;
  position: relative;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background-color: var(--card-background);
  padding: 2.5rem;
  border-radius: var(--border-radius);
  transition: var(--transition);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--gradient-primary);
  transform: scaleX(0);
  transition: var(--transition);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.feature-icon {
  color: var(--primary-color);
  margin-bottom: 1.5rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.05) 100%);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.feature-card:hover .feature-icon {
  background: var(--gradient-primary);
  color: white;
  transform: scale(1.1);
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 使用流程 */
.how-it-works-section {
  padding: 6rem 0;
  background-color: var(--card-background);
  position: relative;
}

.how-it-works-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 10% 20%, rgba(59, 130, 246, 0.05) 0%, transparent 20%),
              radial-gradient(circle at 90% 80%, rgba(37, 99, 235, 0.05) 0%, transparent 20%);
  pointer-events: none;
}

.steps-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  position: relative;
  z-index: 1;
}

.step-item {
  background-color: white;
  padding: 2.5rem 2rem;
  border-radius: var(--border-radius);
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.step-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, transparent 100%);
  transform: translateY(100%);
  transition: var(--transition);
  z-index: 0;
}

.step-item:hover::before {
  transform: translateY(0);
}

.step-item:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.step-number {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 40px;
  background: var(--gradient-primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  box-shadow: var(--shadow-md);
  transition: var(--transition);
  z-index: 1;
}

.step-item:hover .step-number {
  transform: translateX(-50%) scale(1.1);
  box-shadow: var(--shadow-lg);
}

.step-item h3 {
  margin-top: 1.5rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
  position: relative;
  z-index: 1;
}

.step-item p {
  color: var(--text-secondary);
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

/* 用户评价 */
.testimonials-section {
  padding: 6rem 0;
  background-color: white;
  position: relative;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.testimonial-card {
  background-color: var(--card-background);
  padding: 2.5rem;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.testimonial-card::before {
  content: '"';
  position: absolute;
  top: 10px;
  right: 20px;
  font-size: 6rem;
  color: rgba(59, 130, 246, 0.1);
  font-family: serif;
  line-height: 1;
  z-index: 0;
}

.testimonial-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.rating {
  color: var(--warning-color);
  margin-bottom: 1rem;
  font-size: 1.25rem;
  position: relative;
  z-index: 1;
}

.testimonial-card p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1.5rem;
  font-style: italic;
  position: relative;
  z-index: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  z-index: 1;
}

.user-avatar {
  color: var(--primary-color);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.05) 100%);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.testimonial-card:hover .user-avatar {
  background: var(--gradient-primary);
  color: white;
  transform: scale(1.1);
}

.user-details h4 {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.user-details p {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
  font-style: normal;
}



/* 行动召唤 */
.cta-section {
  padding: 6rem 0;
  background: var(--gradient-primary);
  color: white;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.cta-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 30%),
              radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 30%);
  pointer-events: none;
}

.cta-section h2 {
  font-size: 2.25rem;
  font-weight: bold;
  margin-bottom: 1rem;
  position: relative;
  z-index: 1;
}

.cta-section p {
  font-size: 1.125rem;
  margin-bottom: 2.5rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.cta-buttons .btn-outline {
  background-color: transparent;
  color: white;
  border-color: white;
}

.cta-buttons .btn-outline:hover {
  background-color: white;
  color: var(--primary-color);
}

/* 页脚 */
.footer {
  padding: 4rem 0 2rem;
  background-color: var(--text-primary);
  color: white;
  position: relative;
  overflow: hidden;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--gradient-primary);
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
  position: relative;
  z-index: 1;
}

.footer-logo {
  display: flex;
  align-items: center;
  font-size: 1.25rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: white;
  gap: 0.5rem;
  cursor: pointer;
  transition: var(--transition);
}

.footer-logo:hover {
  transform: scale(1.05);
}

.footer-section p {
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 1rem;
}

.footer-section h4 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: white;
  position: relative;
  padding-bottom: 0.5rem;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background: var(--gradient-primary);
  border-radius: 1px;
}

.footer-section ul {
  list-style: none;
}

.footer-section ul li {
  margin-bottom: 0.75rem;
}

.footer-section ul li a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: var(--transition);
  display: inline-block;
  position: relative;
  padding-left: 10px;
}

.footer-section ul li a::before {
  content: '→';
  position: absolute;
  left: 0;
  opacity: 0;
  transition: var(--transition);
}

.footer-section ul li a:hover {
  color: white;
  transform: translateX(5px);
}

.footer-section ul li a:hover::before {
  opacity: 1;
}

.social-links {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.social-link {
  color: rgba(255, 255, 255, 0.7);
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.social-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--gradient-primary);
  transition: var(--transition);
  z-index: -1;
}

.social-link:hover {
  color: white;
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.social-link:hover::before {
  left: 0;
}

.footer-bottom {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.875rem;
  position: relative;
  z-index: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .hero-text h1 {
    font-size: 2.25rem;
  }
  
  .hero-buttons {
    justify-content: center;
  }
  
  .nav-menu {
    display: none;
  }
  
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .cta-buttons button {
    width: 100%;
    max-width: 250px;
  }
  
  .features-grid,
  .steps-container,
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header h2 {
    font-size: 1.75rem;
  }
  
  .hero-section,
  .features-section,
  .how-it-works-section,
  .testimonials-section,
  .cta-section {
    padding: 4rem 0;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 15px;
  }
  
  .hero-text h1 {
    font-size: 2rem;
  }
  
  .feature-card,
  .step-item,
  .testimonial-card,
  .pricing-card {
    padding: 1.5rem;
  }
  
  .price span {
    position: static;
    display: block;
    margin-top: 0.5rem;
  }
}
</style>