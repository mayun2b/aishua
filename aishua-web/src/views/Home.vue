<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="home-container">
    <div class="dynamic-background">
      <canvas id="background-canvas"></canvas>
      <div class="grid-overlay"></div>
      <div class="particle-system"></div>
    </div>
    <header class="home-header" :class="{ 'scrolled': isScrolled }">
      <div class="container">
        <div class="header-content">
          <div class="logo" @click="goToHome">
            <div class="logo-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="logo-text">
              <div class="logo-main">AI刷题</div>
              <div class="logo-sub">智能知识点强化系统</div>
            </div>
          </div>
          <nav class="nav-menu">
            <a href="#home" class="nav-link">首页</a>
            <a href="#platform" class="nav-link">平台定位</a>
            <a href="#capabilities" class="nav-link">能力矩阵</a>
            <a href="#advantages" class="nav-link">核心优势</a>
            <a href="#scenarios" class="nav-link">场景覆盖</a>
          </nav>
          <div class="header-actions">
            <button class="btn-secondary" @click="goToLogin">登录</button>
            <button class="btn-primary" @click="goToRegister">免费注册</button>
          </div>
        </div>
      </div>
    </header>
    <div id="home"></div>
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-particles"></div>
      </div>
      <div class="container">
        <div class="hero-content">
          <div class="hero-main">
            <div class="hero-intro">
              <div class="hero-badge animated-badge">
                <span class="badge-icon">⚡</span>
                <span class="badge-text">RAG+大模型智能生成</span>
                <div class="badge-glow"></div>
              </div>
              
              <div class="hero-decoration">
                <div class="advanced-decoration">
                  <div class="decoration-circle primary-circle"></div>
                  <div class="decoration-circle secondary-circle"></div>
                  <div class="decoration-circle tertiary-circle"></div>
                  <div class="decoration-ring"></div>
                  <div class="decoration-particles">
                    <div class="particle p1"></div>
                    <div class="particle p2"></div>
                    <div class="particle p3"></div>
                    <div class="particle p4"></div>
                    <div class="particle p5"></div>
                  </div>
                </div>
              </div>
              
              <h1 class="hero-title" data-text="知识强化智能生成">
                <span class="title-main animated-title">知识强化</span>
                <span class="title-accent animated-title">智能生成</span>
              </h1>
              <h2 class="hero-subtitle animated-subtitle">AI智能知识点强化系统</h2>
              <p class="hero-description animated-description">
                基于RAG+大模型技术架构，通过向量数据库精准定位知识盲区，
                结合大模型智能生成针对性练习题目，实现高效学习和知识点深度掌握。
              </p>
            </div>
            
            <div class="hero-features">
              <div class="feature-row">
                <div class="feature-item animated-feature" data-delay="0">
                  <div class="feature-icon">🎯</div>
                  <div class="feature-content">
                    <h4>精准识别</h4>
                    <p>向量数据库精准定位知识盲区</p>
                  </div>
                </div>
                <div class="feature-item animated-feature" data-delay="100">
                  <div class="feature-icon">🤖</div>
                  <div class="feature-content">
                    <h4>智能生成</h4>
                    <p>大模型生成针对性练习题目</p>
                  </div>
                </div>
              </div>
              <div class="feature-row">
                <div class="feature-item animated-feature" data-delay="200">
                  <div class="feature-icon">⚡</div>
                  <div class="feature-content">
                    <h4>高效强化</h4>
                    <p>RAG技术实现知识点深度掌握</p>
                  </div>
                </div>
                <div class="feature-item animated-feature" data-delay="300">
                  <div class="feature-icon">📊</div>
                  <div class="feature-content">
                    <h4>数据驱动</h4>
                    <p>科学分析优化学习效果</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="hero-actions">
              <button class="btn-primary animated-button" @click="goToExercise">
                <span>开始强化练习</span>
                <div class="button-glow"></div>
              </button>
              <button class="btn-secondary animated-button" @click="goToDashboard">
              <span>开始练习</span>
              <div class="button-glow-secondary"></div>
            </button>
            </div>
            
            <div class="hero-particles-left">
              <div class="particle particle-1"></div>
              <div class="particle particle-2"></div>
              <div class="particle particle-3"></div>
              <div class="particle particle-4"></div>
              <div class="particle particle-5"></div>
            </div>
          </div>
          
          <div class="hero-tech-showcase">
            <div class="tech-showcase-card">
              <div class="tech-showcase-header">
                <h3>RAG技术架构</h3>
                <div class="tech-status">
                  <div class="status-pulse"></div>
                  <span>高性能运行</span>
                </div>
              </div>
              
              <div class="tech-architecture">
                <div class="tech-flow">
                  <div class="tech-step vector-db">
                    <div class="step-icon">🗄️</div>
                    <div class="step-content">
                      <h4>向量数据库</h4>
                      <p>存储知识点向量，精准检索匹配</p>
                    </div>
                  </div>
                  
                  <div class="tech-connector"></div>
                  
                  <div class="tech-step embedding">
                    <div class="step-icon">🔍</div>
                    <div class="step-content">
                      <h4>Embedding</h4>
                      <p>文本向量化，相似度计算</p>
                    </div>
                  </div>
                  
                  <div class="tech-connector"></div>
                  
                  <div class="tech-step llm">
                    <div class="step-icon">🤖</div>
                    <div class="step-content">
                      <h4>大模型</h4>
                      <p>智能生成针对性练习题目</p>
                    </div>
                  </div>
                  
                  <div class="tech-connector"></div>
                  
                  <div class="tech-step rag">
                    <div class="step-icon">🔄</div>
                    <div class="step-content">
                      <h4>RAG融合</h4>
                      <p>检索增强生成，知识点强化</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="tech-features">
                <div class="feature-item">
                  <div class="feature-content">
                    <h4>精准匹配</h4>
                    <p>智能识别知识点需求</p>
                  </div>
                </div>
                <div class="feature-item">
                  <div class="feature-content">
                    <h4>高效学习</h4>
                    <p>针对性强化薄弱环节</p>
                  </div>
                </div>
                <div class="feature-item">
                  <div class="feature-content">
                    <h4>智能优化</h4>
                    <p>实时调整学习策略</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        

      </div>
    </section>
    <section id="platform" class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">平台定位</h2>
          <p class="section-description">基于RAG+大模型的智能知识点强化系统</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="(feature, index) in features" :key="index" :data-index="index">
            <div class="feature-icon">
              <component :is="feature.icon" />
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
            <div class="feature-glow"></div>
          </div>
        </div>
      </div>
    </section>
    <section id="capabilities" class="process-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">能力矩阵</h2>
          <p class="section-description">全方位的AI智能学习能力体系</p>
        </div>
        <div class="process-container">
          <div class="process-step" v-for="(step, index) in processSteps" :key="index" :data-step="index">
            <div class="step-number">
              <span>{{ index + 1 }}</span>
              <div class="number-glow"></div>
            </div>
            <h3 class="step-title">{{ step.title }}</h3>
            <p class="step-description">{{ step.description }}</p>
            <div class="step-visual">
              <component :is="step.icon" />
            </div>
          </div>
          <div class="process-connector"></div>
        </div>
      </div>
    </section>

    <section id="advantages" class="advantages-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">核心优势</h2>
          <p class="section-description">基于RAG+大模型的智能学习解决方案</p>
        </div>
        <div class="advantages-grid">
          <div class="advantage-card">
            <div class="advantage-icon">🎯</div>
            <h3>精准识别</h3>
            <p>通过向量数据库精准定位知识点盲区，智能识别学习弱点</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-icon">🤖</div>
            <h3>智能生成</h3>
            <p>大模型生成针对性练习题目，强化薄弱知识点</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-icon">⚡</div>
            <h3>高效强化</h3>
            <p>RAG技术实现知识点深度掌握，提升学习效率45%以上</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-icon">📊</div>
            <h3>数据驱动</h3>
            <p>科学分析优化学习效果，实时调整学习策略</p>
          </div>
        </div>
      </div>
    </section>

    <section id="scenarios" class="scenarios-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">场景覆盖</h2>
          <p class="section-description">适用于各年龄段和各学科的智能学习解决方案</p>
        </div>
        <div class="scenarios-grid">
          <div class="scenario-card">
            <div class="scenario-icon">👶</div>
            <h3>小学教育</h3>
            <p>数学、语文、英语等基础学科的知识点强化</p>
          </div>
          <div class="scenario-card">
            <div class="scenario-icon">🧑‍🎓</div>
            <h3>初中高中</h3>
            <p>中考、高考各学科的系统性复习和强化训练</p>
          </div>
          <div class="scenario-card">
            <div class="scenario-icon">🎓</div>
            <h3>大学学习</h3>
            <p>专业课程的知识点巩固和能力提升</p>
          </div>
          <div class="scenario-card">
            <div class="scenario-icon">👨‍💼</div>
            <h3>职业培训</h3>
            <p>职业技能认证和专业知识强化</p>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">开启AI智能学习之旅</h2>
          <p class="cta-description">精准命中知识点盲区，实现高效补漏学习</p>
          <div class="cta-buttons">
            <button class="btn-primary" @click="goToRegister">
              <span>免费注册</span>
              <div class="button-glow"></div>
            </button>
            <button class="btn-secondary" @click="goToLogin">
              <span>已有账号？登录</span>
              <div class="button-glow-secondary"></div>
            </button>
          </div>
        </div>
      </div>
    </section>
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <div class="footer-logo" @click="goToHome">
              <div class="footer-logo-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <span>AI刷题</span>
            </div>
            <p class="footer-description">AI智能出题系统，精准命中知识点盲区，实现高效补漏学习。</p>
          </div>
          <div class="footer-section">
            <h4>产品</h4>
            <ul class="footer-links">
              <li><a href="#features">AI功能</a></li>
              <li><a href="#how-it-works">智能学习</a></li>
              <li><a href="#testimonials">用户反馈</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>支持</h4>
            <ul class="footer-links">
              <li><a href="#">帮助中心</a></li>
              <li><a href="#">联系我们</a></li>
              <li><a href="#">使用文档</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>关于</h4>
            <ul class="footer-links">
              <li><a href="#">公司介绍</a></li>
              <li><a href="#">团队成员</a></li>
              <li><a href="#">加入我们</a></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2026 AI刷题系统. 引领未来学习科技.</p>
        </div>
      </div>
    </footer>
  </div>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref, onUnmounted } from 'vue'
const router = useRouter()
const isScrolled = ref(false)

const features = ref([
  {
    title: 'AI知识点检测',
    description: '通过RAG技术精准分析学习数据，智能识别知识点盲区，找出您的薄弱环节。',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '智能出题策略',
    description: '基于向量数据库和大模型，智能生成针对性题目，精准补漏，提高学习效率。',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '实时学习分析',
    description: '实时跟踪学习进度，生成详细的学习报告，动态调整出题难度和策略。',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M18 20V10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M12 20V4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M6 20V14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '智能错题本',
    description: '自动收集错题，AI分析错误原因，推荐相关知识点练习，强化薄弱环节。',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '个性化学习路径',
    description: '根据学习情况动态调整学习路径，智能推荐最优学习策略和题目。',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '多维度数据分析',
    description: '从正确率、答题时间、知识点掌握度等多个维度进行深度分析。',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M22 10v6M2 10l10-5 10 5-10 5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M6 12v5c3 3 9 3 12 0v-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  }
])
const processSteps = ref([
  {
    title: 'AI知识检测',
    description: 'AI系统分析您的学习数据，精准识别知识点盲区',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>'
    }
  },
  {
    title: '智能出题',
    description: '基于盲区分析，智能生成针对性题目，精准补漏',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 12h-4l-3 9L9 3l-3 9H3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '强化练习',
    description: '针对性练习薄弱知识点，实时反馈学习效果',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  },
  {
    title: '知识巩固',
    description: 'AI持续优化学习路径，确保知识点完全掌握',
    icon: {
      template: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    }
  }
])

const goToLogin = () => {
  router.push('/login')
}
const goToRegister = () => {
  router.push('/register')
}
const goToExercise = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/exercise')
  } else {
    router.push('/login')
  }
}
const goToDashboard = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/exercise')
  } else {
    router.push('/login')
  }
}
const goToHome = () => {
  router.push('/')
}
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}
const initBackgroundAnimation = () => {
  const canvas = document.getElementById('background-canvas')
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  const particles = []
  const connections = []
  class Particle {
    constructor() {
      this.x = Math.random() * canvas.width
      this.y = Math.random() * canvas.height
      this.size = Math.random() * 3 + 1
      this.speedX = Math.random() * 0.5 - 0.25
      this.speedY = Math.random() * 0.5 - 0.25
      this.color = `rgba(${Math.random() * 100 + 155}, ${Math.random() * 100 + 155}, ${Math.random() * 255}, ${Math.random() * 0.5 + 0.5})`
    }
    update() {
      this.x += this.speedX
      this.y += this.speedY
      if (this.x > canvas.width) this.x = 0
      if (this.x< 0) this.x = canvas.width
      if (this.y >canvas.height) this.y = 0
      if (this.y< 0) this.y = canvas.height
    }
    draw() {
      ctx.fillStyle = this.color
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
      ctx.fill()
    }
  }
  const createParticles = () =>{
    for (let i = 0; i< 100; i++) {
      particles.push(new Particle())
    }
  }
  const connectParticles = () =>{
    connections.length = 0
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const distance = Math.sqrt(dx * dx + dy * dy)
        if (distance < 100) {
          connections.push({
            p1: particles[i],
            p2: particles[j],
            opacity: 1 - distance / 100
          })
        }
      }
    }
  }
  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    particles.forEach(particle => {
      particle.update()
      particle.draw()
    })
    connectParticles()
    connections.forEach(connection => {
      ctx.strokeStyle = `rgba(255, 255, 255, ${connection.opacity * 0.2})`
      ctx.lineWidth = 0.5
      ctx.beginPath()
      ctx.moveTo(connection.p1.x, connection.p1.y)
      ctx.lineTo(connection.p2.x, connection.p2.y)
      ctx.stroke()
    })
    requestAnimationFrame(animate)
  }
  createParticles()
  animate()
  window.addEventListener('resize', () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  })
}

const initScrollAnimations = () => {
  const animateElements = document.querySelectorAll('.feature-card, .process-step')
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.style.opacity = '1'
        entry.target.style.transform = 'translateY(0)'
      }
    })
  }, { threshold: 0.1 })
  animateElements.forEach(el => {
    el.style.opacity = '0'
    el.style.transform = 'translateY(30px)'
    el.style.transition = 'opacity 0.6s ease, transform 0.6s ease'
    observer.observe(el)
  })
}
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  initBackgroundAnimation()
  initScrollAnimations()
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>
<style scoped>
:root {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --dark-gradient: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 50%, #2a2d4a 100%);
  --primary-color: #667eea;
  --secondary-color: #764ba2;
  --accent-color: #f5576c;
  --success-color: #4facfe;
  --background-color: #0a0e27;
  --card-background: rgba(10, 14, 39, 0.9);
  --card-background-hover: rgba(26, 31, 58, 0.95);
  --text-primary: #ffffff;
  --text-secondary: #e0e7ff;
  --text-tertiary: #a5b4fc;
  --border-color: rgba(102, 126, 234, 0.3);
  --border-color-hover: rgba(102, 126, 234, 0.6);
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.4);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.5), 0 2px 4px -1px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.6), 0 4px 6px -2px rgba(0, 0, 0, 0.5);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.7), 0 10px 10px -5px rgba(0, 0, 0, 0.6);
  --shadow-glow: 0 0 30px rgba(102, 126, 234, 0.4);
  --shadow-glow-hover: 0 0 50px rgba(102, 126, 234, 0.6);
  --border-radius: 20px;
  --border-radius-lg: 28px;
  --border-radius-xl: 32px;
  --transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-fast: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.home-container {
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: var(--text-primary);
  background-color: var(--background-color);
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}
#background-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridMove 30s linear infinite;
}
@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(60px, 60px); }
}
.particle-system {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.home-header {
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.95) 50%, rgba(42, 45, 74, 0.92) 100%);
  backdrop-filter: blur(30px);
  border-bottom: 1px solid rgba(102, 126, 234, 0.3);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 1rem 0;
  transition: var(--transition);
  box-shadow: 
    0 0 80px rgba(102, 126, 234, 0.25),
    0 4px 20px rgba(0, 0, 0, 0.3);
}
.home-header.scrolled {
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.99) 0%, rgba(26, 31, 58, 0.97) 50%, rgba(42, 45, 74, 0.95) 100%);
  box-shadow: 
    0 0 120px rgba(102, 126, 234, 0.4),
    0 8px 30px rgba(0, 0, 0, 0.4);
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
  gap: 1rem;
  cursor: pointer;
  transition: var(--transition);
}

.logo-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-main {
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--primary-color);
  text-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
}

.logo-sub {
  font-size: 0.75rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.5px;
}
.logo-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo:hover {
  transform: scale(1.05);
}
.nav-menu {
  display: flex;
  gap: 3rem;
  align-items: center;
}
.nav-link {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-weight: 700;
  transition: var(--transition);
  position: relative;
  padding: 0.75rem 0;
  font-size: 1rem;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
  letter-spacing: 0.5px;
}
.nav-link:hover {
  color: #ffffff;
}
.nav-link::before {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: var(--transition);
  border-radius: 2px;
}
.nav-link::after {
  content: '';
  position: absolute;
  top: 50%;
  left: -5px;
  right: -5px;
  bottom: -2px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), transparent);
  opacity: 0;
  transition: var(--transition);
}
.nav-link:hover::before {
  width: 100%;
}
.nav-link:hover::after {
  opacity: 1;
}
.header-actions {
  display: flex;
  gap: 1rem;
}
.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f5576c 100%);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 25px;
  font-weight: 800;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 
    0 0 40px rgba(102, 126, 234, 0.5),
    0 4px 15px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
  font-size: 0.95rem;
  letter-spacing: 0.5px;
  text-transform: uppercase;
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
  box-shadow: 
    0 0 60px rgba(102, 126, 234, 0.7),
    0 6px 20px rgba(0, 0, 0, 0.4);
}

.btn-secondary {
  background: transparent;
  color: #667eea;
  border: 2px solid #667eea;
  padding: 0.8rem 1.5rem;
  border-radius: 25px;
  font-weight: 800;
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  font-size: 0.95rem;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.btn-secondary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: var(--transition);
  z-index: -1;
}
.btn-secondary:hover {
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 0 30px rgba(102, 126, 234, 0.4);
}
.btn-secondary:hover::before {
  left: 0;
}
.btn-secondary .button-glow-secondary {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  filter: blur(30px);
  opacity: 0;
  transition: var(--transition);
}
.btn-secondary:hover .button-glow-secondary {
  opacity: 0.3;
}
#home {
  position: absolute;
  top: -80px;
  left: 0;
}

.hero-section {
  padding: 8rem 0 6rem;
  position: relative;
  overflow: hidden;
}
.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}
.hero-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.15) 0%, transparent 40%),
              radial-gradient(circle at 80% 80%, rgba(118, 75, 162, 0.15) 0%, transparent 40%),
              radial-gradient(circle at 50% 20%, rgba(245, 87, 108, 0.1) 0%, transparent 40%);
  animation: gradientPulse 8s ease-in-out infinite;
}
@keyframes gradientPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 0.8; }
}
.hero-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle, rgba(59, 130, 246, 0.1) 1px, transparent 1px),
    radial-gradient(circle, rgba(139, 92, 246, 0.05) 1px, transparent 1px);
  background-size: 50px 50px, 30px 30px;
  animation: particlesFloat 20s linear infinite;
}
@keyframes particlesFloat {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}
.hero-content {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 4rem;
  align-items: center;
  margin-bottom: 4rem;
  position: relative;
  z-index: 1;
}
.hero-main {
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}
.hero-intro {
  position: relative;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  margin-bottom: 2rem;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}
.badge-icon {
  font-size: 1.25rem;
}
.badge-text {
  font-size: 0.95rem;
  font-weight: 600;
  color: #667eea;
  z-index: 1;
}
.badge-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  filter: blur(20px);
  opacity: 0.3;
  animation: badgeSlide 3s ease-in-out infinite;
}

.hero-decoration {
  position: absolute;
  top: 50%;
  left: 55%;
  transform: translateY(-50%);
  width: 200px;
  height: 200px;
  pointer-events: none;
  z-index: 0;
}

.advanced-decoration {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  animation: circleFloat 8s ease-in-out infinite;
}

.primary-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 40px rgba(102, 126, 234, 0.8);
  animation-delay: 0s;
}

.secondary-circle {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 2px solid rgba(102, 126, 234, 0.3);
  animation-delay: 2s;
}

.tertiary-circle {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.05) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 1px solid rgba(118, 75, 162, 0.2);
  animation-delay: 4s;
}

.decoration-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent 0deg, rgba(102, 126, 234, 0.6) 90deg, transparent 90deg);
  animation: ringRotate 20s linear infinite;
}

.decoration-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #667eea 0%, #f5576c 100%);
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.8);
  animation: particleOrbit 10s ease-in-out infinite;
}

.p1 {
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.p2 {
  top: 50%;
  right: 10%;
  transform: translateY(-50%);
  animation-delay: 2s;
}

.p3 {
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 4s;
}

.p4 {
  top: 50%;
  left: 10%;
  transform: translateY(-50%);
  animation-delay: 6s;
}

.p5 {
  top: 25%;
  right: 25%;
  animation-delay: 8s;
}

@keyframes circleFloat {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1); 
    opacity: 0.8;
  }
  25% { 
    transform: translate(-55%, -45%) scale(1.1); 
    opacity: 1;
  }
  50% { 
    transform: translate(-50%, -50%) scale(1); 
    opacity: 0.6;
  }
  75% { 
    transform: translate(-45%, -55%) scale(1.1); 
    opacity: 1;
  }
}

@keyframes ringRotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes particleOrbit {
  0%, 100% { 
    transform: translate(-50%, -50%) rotate(0deg) translateX(80px) rotate(0deg);
    opacity: 0.3;
  }
  25% { 
    transform: translate(-50%, -50%) rotate(90deg) translateX(80px) rotate(-90deg);
    opacity: 1;
  }
  50% { 
    transform: translate(-50%, -50%) rotate(180deg) translateX(80px) rotate(-180deg);
    opacity: 0.6;
  }
  75% { 
    transform: translate(-50%, -50%) rotate(270deg) translateX(80px) rotate(-270deg);
    opacity: 1;
  }
}
@keyframes badgeSlide {
  0% { left: -100%; }
  50% { left: 100%; }
  100% { left: 100%; }
}
.hero-title {
  font-size: 4rem;
  font-weight: 900;
  margin-bottom: 1.5rem;
  line-height: 1.1;
  color: #ffffff;
  text-shadow: 0 0 30px rgba(102, 126, 234, 0.6), 0 2px 4px rgba(0, 0, 0, 0.3);
  position: relative;
}

.hero-title::before {
  display: none;
}
.title-main {
  display: block;
  animation: titleFadeIn 1s ease-out 0.2s both;
  color: #ffffff;
  text-shadow: 0 0 30px rgba(102, 126, 234, 0.6);
}
.title-accent {
  display: block;
  animation: titleFadeIn 1s ease-out 0.5s both;
  color: #ffffff;
  text-shadow: 0 0 30px rgba(118, 75, 162, 0.6);
}
@keyframes titleFadeIn {
  from { 
    opacity: 0; 
    transform: translateY(30px); 
    filter: blur(10px);
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
    filter: blur(0);
  }
}
.hero-subtitle {
  font-size: 1.8rem;
  color: var(--secondary-color);
  margin-bottom: 1.5rem;
  font-weight: 700;
  text-shadow: 0 0 30px rgba(139, 92, 246, 0.4);
  letter-spacing: 1px;
  animation: subtitleSlideIn 1s ease-out 0.8s both;
}
@keyframes subtitleSlideIn {
  from { 
    opacity: 0; 
    transform: translateY(20px);
  }
  to { 
    opacity: 1; 
    transform: translateY(0);
  }
}
.hero-description {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.8;
  max-width: 500px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.7);
  font-weight: 500;
  animation: descriptionFadeIn 1s ease-out 1.1s both;
}
@keyframes descriptionFadeIn {
  from { 
    opacity: 0; 
    transform: translateY(15px);
  }
  to { 
    opacity: 1; 
    transform: translateY(0);
  }
}
.hero-features {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.feature-row {
  display: flex;
  gap: 1.5rem;
}
.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: var(--transition);
  animation: featureSlideIn 0.8s ease-out both;
}
.feature-item:nth-child(1) { animation-delay: 1.3s; }
.feature-item:nth-child(2) { animation-delay: 1.5s; }
@keyframes featureSlideIn {
  from { 
    opacity: 0; 
    transform: translateX(-20px);
  }
  to { 
    opacity: 1; 
    transform: translateX(0);
  }
}
.feature-item:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.2);
}
.feature-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
  text-shadow: 0 0 20px rgba(59, 130, 246, 0.5);
}
.feature-content h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}
.feature-content p {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.6;
}
.hero-actions {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}
.animated-badge {
  animation: badgePulse 2s ease-in-out infinite;
}
@keyframes badgePulse {
  0%, 100% { 
    transform: scale(1); 
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.3);
  }
  50% { 
    transform: scale(1.05); 
    box-shadow: 0 0 30px rgba(59, 130, 246, 0.5);
  }
}
.animated-title {
  animation: titleFloat 3s ease-in-out infinite;
}
.title-main {
  animation-delay: 0s;
}
.title-accent {
  animation-delay: 1s;
}
@keyframes titleFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}
.animated-subtitle {
  animation: subtitleGlow 4s ease-in-out infinite;
}
@keyframes subtitleGlow {
  0%, 100% { 
    text-shadow: 0 0 30px rgba(139, 92, 246, 0.4);
  }
  50% { 
    text-shadow: 0 0 50px rgba(139, 92, 246, 0.8);
  }
}
.animated-description {
  animation: descriptionPulse 3s ease-in-out infinite;
}
@keyframes descriptionPulse {
  0%, 100% { opacity: 0.9; }
  50% { opacity: 1; }
}
.animated-feature {
  animation: featureFloat 4s ease-in-out infinite;
}
.animated-feature[data-delay="0"] { animation-delay: 0s; }
.animated-feature[data-delay="100"] { animation-delay: 0.5s; }
.animated-feature[data-delay="200"] { animation-delay: 1s; }
.animated-feature[data-delay="300"] { animation-delay: 1.5s; }
@keyframes featureFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
}
.animated-button {
  animation: buttonGlow 2s ease-in-out infinite;
}
.btn-primary.animated-button { animation-delay: 0s; }
.btn-secondary.animated-button { animation-delay: 1s; }
@keyframes buttonGlow {
  0%, 100% { 
    box-shadow: var(--shadow-glow);
  }
  50% { 
    box-shadow: var(--shadow-glow-hover);
  }
}
.hero-particles-left {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
}
.particle {
  position: absolute;
  border-radius: 50%;
  background: var(--primary-gradient);
  opacity: 0.6;
  animation: particleFloat 6s ease-in-out infinite;
}
.particle-1 {
  width: 8px;
  height: 8px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}
.particle-2 {
  width: 12px;
  height: 12px;
  top: 40%;
  left: 80%;
  animation-delay: 1s;
}
.particle-3 {
  width: 6px;
  height: 6px;
  top: 60%;
  left: 20%;
  animation-delay: 2s;
}
.particle-4 {
  width: 10px;
  height: 10px;
  top: 80%;
  left: 70%;
  animation-delay: 3s;
}
.particle-5 {
  width: 14px;
  height: 14px;
  top: 30%;
  left: 50%;
  animation-delay: 4s;
}
@keyframes particleFloat {
  0%, 100% { 
    transform: translate(0, 0) rotate(0deg);
    opacity: 0.6;
  }
  25% { 
    transform: translate(10px, -10px) rotate(90deg);
    opacity: 0.8;
  }
  50% { 
    transform: translate(0, -20px) rotate(180deg);
    opacity: 1;
  }
  75% { 
    transform: translate(-10px, -10px) rotate(270deg);
    opacity: 0.8;
  }
}

.hero-tech-showcase {
  display: flex;
  justify-content: center;
  align-items: center;
}

.tech-showcase-card {
  width: 100%;
  max-width: 450px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.9) 50%, rgba(51, 65, 85, 0.85) 100%);
  border: 1px solid rgba(59, 130, 246, 0.6);
  border-radius: var(--border-radius-xl);
  backdrop-filter: blur(50px);
  padding: 2.5rem;
  box-shadow: 
    0 30px 80px rgba(0, 0, 0, 0.7),
    0 0 200px rgba(59, 130, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
  overflow: hidden;
  animation: cardFloat 8s ease-in-out infinite;
}
@keyframes cardFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.tech-showcase-card::before {
  content: '';
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: linear-gradient(45deg, #3b82f6, #8b5cf6, #ec4899, #10b981, #3b82f6);
  border-radius: var(--border-radius-xl);
  z-index: -1;
  animation: gradientShift 8s linear infinite;
  background-size: 400% 400%;
}
@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.tech-showcase-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(59, 130, 246, 0.4);
}
.tech-showcase-header h3 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-primary);
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
  margin: 0;
}

.tech-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #10b981;
  background: rgba(16, 185, 129, 0.15);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  border: 1px solid rgba(16, 185, 129, 0.5);
}
.status-pulse {
  width: 10px;
  height: 10px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 15px #10b981;
  animation: statusPulse 2s ease-in-out infinite;
}
@keyframes statusPulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

.tech-architecture {
  margin-bottom: 2rem;
}
.tech-flow {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.tech-step {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: var(--transition);
  animation: techStepSlideIn 0.8s ease-out both;
}
.tech-step:nth-child(1) { animation-delay: 0s; }
.tech-step:nth-child(3) { animation-delay: 0.2s; }
.tech-step:nth-child(5) { animation-delay: 0.4s; }
.tech-step:nth-child(7) { animation-delay: 0.6s; }
@keyframes techStepSlideIn {
  from { 
    opacity: 0; 
    transform: translateX(20px);
  }
  to { 
    opacity: 1; 
    transform: translateX(0);
  }
}
.tech-step:hover {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.2);
}
.step-icon {
  font-size: 2rem;
  text-shadow: 0 0 20px rgba(59, 130, 246, 0.5);
}
.step-content h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}
.step-content p {
  font-size: 0.8rem;
  color: var(--text-secondary);
  line-height: 1.5;
}
.tech-connector {
  height: 1px;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.3), rgba(59, 130, 246, 0.8), rgba(59, 130, 246, 0.3));
  margin: 0 4rem;
  animation: connectorPulse 3s ease-in-out infinite;
}
@keyframes connectorPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.8; }
}
.tech-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(59, 130, 246, 0.4);
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1rem;
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 16px;
  transition: var(--transition);
}
.feature-item:hover {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.2);
}
.feature-content h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}
.feature-content p {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

.vector-db { border-left: 4px solid #3b82f6; }
.embedding { border-left: 4px solid #8b5cf6; }
.llm { border-left: 4px solid #ec4899; }
.rag { border-left: 4px solid #10b981; }



.features-section {
  padding: 6rem 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.95) 0%, rgba(26, 31, 58, 0.9) 100%);
}

.features-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 30%, rgba(102, 126, 234, 0.1) 0%, transparent 40%),
              radial-gradient(circle at 80% 70%, rgba(118, 75, 162, 0.1) 0%, transparent 40%);
  animation: gradientPulse 8s ease-in-out infinite;
  z-index: 0;
}

.features-section .container {
  position: relative;
  z-index: 1;
}
.section-header {
  text-align: center;
  margin-bottom: 4rem;
}
.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 1rem;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(59, 130, 246, 0.3);
}
.section-description {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 600px;
  margin: 0 auto;
}
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}
.feature-card {
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 20px;
  padding: 2rem;
  backdrop-filter: blur(10px);
  transition: var(--transition);
  position: relative;
  overflow: hidden;
}
.feature-card:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(59, 130, 246, 0.3);
}
.feature-icon {
  font-size: 3rem;
  color: var(--primary-color);
  text-shadow: 0 0 20px rgba(59, 130, 246, 0.5);
  margin-bottom: 1.5rem;
}
.feature-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}
.feature-description {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
}
.feature-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: var(--transition);
}
.feature-card:hover .feature-glow {
  opacity: 1;
}
.process-section {
  padding: 6rem 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(26, 31, 58, 0.95) 0%, rgba(10, 14, 39, 0.9) 100%);
}

.process-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 80% 30%, rgba(118, 75, 162, 0.1) 0%, transparent 40%),
              radial-gradient(circle at 20% 70%, rgba(102, 126, 234, 0.1) 0%, transparent 40%);
  animation: gradientPulse 8s ease-in-out infinite;
  z-index: 0;
}

.process-section .container {
  position: relative;
  z-index: 1;
}
.process-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  position: relative;
}
.process-step {
  text-align: center;
  padding: 2rem;
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  transition: var(--transition);
  position: relative;
}
.process-step:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.2);
}
.step-number {
  width: 60px;
  height: 60px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  font-size: 1.5rem;
  font-weight: 800;
  color: white;
  position: relative;
  box-shadow: 0 0 30px rgba(59, 130, 246, 0.5);
}
.number-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: var(--primary-gradient);
  border-radius: 50%;
  filter: blur(10px);
  opacity: 0;
  transition: var(--transition);
}
.process-step:hover .number-glow {
  opacity: 0.6;
}
.step-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}
.step-description {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
}
.step-visual {
  margin-top: 1.5rem;
}
.process-connector {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.3), rgba(59, 130, 246, 0.8), rgba(59, 130, 246, 0.3));
  transform: translateY(-50%);
  z-index: -1;
}
.testimonials-section {
  padding: 6rem 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.8) 0%, rgba(30, 41, 59, 0.6) 100%);
}
.testimonials-slider {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}
.testimonial-card {
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 20px;
  padding: 2rem;
  backdrop-filter: blur(10px);
  transition: var(--transition);
}
.testimonial-card:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.2);
}
.rating {
  font-size: 1.2rem;
  margin-bottom: 1.5rem;
}
.testimonial-text {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 2rem;
  font-style: italic;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.user-avatar {
  width: 48px;
  height: 48px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}
.user-details h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}
.user-details p {
  font-size: 0.85rem;
  color: var(--text-secondary);
}
.advantages-section {
  padding: 6rem 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.95) 0%, rgba(26, 31, 58, 0.9) 100%);
}

.advantages-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 40% 20%, rgba(245, 87, 108, 0.1) 0%, transparent 40%),
              radial-gradient(circle at 60% 80%, rgba(102, 126, 234, 0.1) 0%, transparent 40%);
  animation: gradientPulse 8s ease-in-out infinite;
  z-index: 0;
}

.advantages-section .container {
  position: relative;
  z-index: 1;
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-top: 3rem;
}

.advantage-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 20px;
  padding: 2rem;
  text-align: center;
  transition: var(--transition);
  backdrop-filter: blur(10px);
}

.advantage-card:hover {
  transform: translateY(-5px);
  border-color: rgba(102, 126, 234, 0.6);
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.2);
}

.advantage-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.advantage-card h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.advantage-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.scenarios-section {
  padding: 6rem 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(26, 31, 58, 0.95) 0%, rgba(10, 14, 39, 0.9) 100%);
}

.scenarios-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 70% 40%, rgba(118, 75, 162, 0.1) 0%, transparent 40%),
              radial-gradient(circle at 30% 60%, rgba(245, 87, 108, 0.1) 0%, transparent 40%);
  animation: gradientPulse 8s ease-in-out infinite;
  z-index: 0;
}

.scenarios-section .container {
  position: relative;
  z-index: 1;
}

.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-top: 3rem;
}

.scenario-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(118, 75, 162, 0.3);
  border-radius: 20px;
  padding: 2rem;
  text-align: center;
  transition: var(--transition);
  backdrop-filter: blur(10px);
}

.scenario-card:hover {
  transform: translateY(-5px);
  border-color: rgba(118, 75, 162, 0.6);
  box-shadow: 0 20px 40px rgba(118, 75, 162, 0.2);
}

.scenario-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.scenario-card h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.scenario-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.cta-section {
  padding: 6rem 0;
  background: var(--primary-gradient);
  text-align: center;
}
.cta-content {
  max-width: 800px;
  margin: 0 auto;
}
.cta-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
  margin-bottom: 1.5rem;
  text-shadow: 0 0 30px rgba(0, 0, 0, 0.3);
}
.cta-description {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  margin-bottom: 3rem;
}
.cta-buttons {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
  flex-wrap: wrap;
}
.footer {
  background: rgba(15, 23, 42, 0.95);
  border-top: 1px solid var(--border-color);
  padding: 4rem 0 2rem;
}
.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 3rem;
  margin-bottom: 2rem;
}
.footer-section h4 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}
.footer-links {
  list-style: none;
}
.footer-links li {
  margin-bottom: 0.5rem;
}
.footer-links a {
  color: var(--text-secondary);
  text-decoration: none;
  transition: var(--transition);
  font-size: 0.9rem;
}
.footer-links a:hover {
  color: var(--primary-color);
}
.footer-logo {
  display: flex;
  align-items: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--primary-color);
  gap: 0.75rem;
  cursor: pointer;
  margin-bottom: 1rem;
}
.footer-logo-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.footer-description {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.6;
}
.footer-bottom {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: 0.85rem;
}
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  .hero-title {
    font-size: 3rem;
  }
  .hero-subtitle {
    font-size: 1.5rem;
  }
  .feature-row {
    flex-direction: column;
    gap: 1rem;
  }
  .features-grid {
    grid-template-columns: 1fr;
  }
  .process-container {
    grid-template-columns: 1fr;
  }
  .testimonials-slider {
    grid-template-columns: 1fr;
  }
  .footer-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
}
@media (max-width: 480px) {
  .hero-title {
    font-size: 2.5rem;
  }
  .hero-subtitle {
    font-size: 1.3rem;
  }
  .section-title {
    font-size: 2rem;
  }
  .cta-title {
    font-size: 2rem;
  }
}
</style>