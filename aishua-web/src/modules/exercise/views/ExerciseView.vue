<template>
  <div class="exercise-container">
    <!-- 流程进度指示器 -->
    <div class="progress-indicator" v-if="exerciseState !== 'practicing' && exerciseState !== 'result'">
      <div class="progress-step" :class="{ active: exerciseState === 'subject-selection', completed: exerciseState !== 'subject-selection' }">
        <div class="step-number">{{ exerciseState !== 'subject-selection' ? '✓' : '1' }}</div>
        <div class="step-label">选择学科</div>
      </div>
      <div class="progress-line"></div>
      <div class="progress-step" :class="{ active: exerciseState === 'mode-selection', completed: exerciseState === 'config' || exerciseState === 'practicing' || exerciseState === 'result' }">
        <div class="step-number">{{ exerciseState === 'mode-selection' ? '2' : exerciseState === 'subject-selection' ? '2' : '✓' }}</div>
        <div class="step-label">选择模式</div>
      </div>
      <div class="progress-line"></div>
      <div class="progress-step" :class="{ active: exerciseState === 'config', completed: exerciseState === 'practicing' || exerciseState === 'result' }">
        <div class="step-number">{{ exerciseState === 'config' ? '3' : exerciseState === 'subject-selection' || exerciseState === 'mode-selection' ? '3' : '✓' }}</div>
        <div class="step-label">配置练习</div>
      </div>
    </div>

    <!-- 第一步：选择学科 -->
    <div v-if="exerciseState === 'subject-selection'" class="page-container">
      <div class="page-header">
        <button class="back-button" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>选择练习学科</h1>
        <div class="header-right"></div>
      </div>
      
      <div class="page-content">
        <div class="section-header">
          <h2>请选择学科</h2>
          <p>选择学科后才能进行练习</p>
        </div>
        
        <div class="subject-options">
          <div 
            v-for="subject in subjectOptions" 
            :key="subject.value"
            :class="{ selected: configForm.subjectId === subject.value }"
            @click="selectSubject(subject.value)"
            class="subject-card"
          >
            <div class="subject-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="subject-info">
              <h3>{{ subject.text }}</h3>
              <p>点击选择此学科</p>
            </div>
            <div class="subject-action">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 第二步：选择练习模式 -->
    <div v-else-if="exerciseState === 'mode-selection'" class="page-container">
      <div class="page-header">
        <button class="back-button" @click="goBackToSubject">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>选择练习模式</h1>
        <div class="header-right">
          <span class="selected-subject-info">当前学科：{{ subjectOptions.find(s => s.value === configForm.subjectId)?.text }}</span>
        </div>
      </div>
      
      <div class="page-content">
        <div v-if="!isLoggedIn" class="guest-notice">
          <p>您当前是游客模式，可以免费练习，但练习记录不会保存。</p>
          <button class="login-button" @click="goToLogin">立即登录</button>
        </div>
        
        <div class="mode-options">
          <div 
            v-for="mode in exerciseModes" 
            :key="mode.value"
            :class="{ active: selectedMode === mode.value, 'require-login': mode.requireLogin }"
            @click="selectMode(mode.value)"
            class="mode-card"
          >
            <div class="mode-icon">
              <svg v-if="mode.value === 1" width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="9 22 9 12 15 12 15 22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else-if="mode.value === 2" width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else-if="mode.value === 3" width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="m15 3 4 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <h3>{{ mode.label }}</h3>
            <p>{{ mode.description }}</p>
            <span v-if="mode.requireLogin" class="login-required">需登录</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 第三步：配置练习 -->
    <div v-else-if="exerciseState === 'config'" class="page-container">
      <div class="page-header">
        <button class="back-button" @click="goBackToMode">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>配置练习</h1>
        <div class="header-right">
          <span class="selected-subject-info">当前学科：{{ subjectOptions.find(s => s.value === configForm.subjectId)?.text }}</span>
        </div>
      </div>
      
      <div class="page-content">
        <div class="mode-info">
          <h3>{{ exerciseModes.find(m => m.value === selectedMode)?.label }}</h3>
          <p>{{ exerciseModes.find(m => m.value === selectedMode)?.description }}</p>
        </div>
        
        <!-- 按知识点练习的特殊配置 -->
        <div v-if="selectedMode === 1" class="knowledge-point-config">
          <h4>选择知识点</h4>
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>加载知识点中...</p>
          </div>
          <div v-else-if="knowledgePoints.length === 0" class="empty-state">
            <p>暂无知识点数据</p>
          </div>
          <div v-else class="knowledge-points-list">
            <div 
              v-for="point in knowledgePoints" 
              :key="point.id"
              :class="{ selected: configForm.categoryId === point.id }"
              @click="selectKnowledgePoint(point)"
              class="knowledge-point-item"
            >
              <div class="knowledge-point-info">
                <h3>{{ point.name }}</h3>
                <div class="knowledge-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: point.progress + '%' }"></div>
                  </div>
                  <span class="progress-text">{{ point.progress }}%</span>
                </div>
                <div class="knowledge-stats">
                  <span>{{ point.completedQuestions }}/{{ point.totalQuestions }} 题</span>
                </div>
              </div>
              <div class="knowledge-point-action">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 通用配置项 -->
        <div class="config-grid">
          <div class="config-item">
            <label>题目数量</label>
            <div class="slider-container">
              <input 
                type="range" 
                v-model="configForm.count" 
                min="5" 
                max="50" 
                step="5"
                class="config-slider"
              />
              <span class="slider-value">{{ configForm.count }}题</span>
            </div>
          </div>
          
          <div class="config-item">
            <label>难度等级</label>
            <select v-model="configForm.difficulty" class="config-select">
              <option value="">选择难度</option>
              <option v-for="option in difficultyOptions" :key="option.value" :value="option.value">
                {{ option.text }}
              </option>
            </select>
          </div>
          
          <div v-if="selectedMode !== 1" class="config-item">
            <label>知识点分类</label>
            <select v-model="configForm.categoryId" class="config-select">
              <option value="">选择知识点</option>
              <option v-for="option in categoryOptions" :key="option.value" :value="option.value">
                {{ option.text }}
              </option>
            </select>
          </div>
        </div>
        
        <button class="start-button" @click="startExercise" :disabled="loading || (selectedMode === 1 && !configForm.categoryId)">
          {{ loading ? '加载中...' : '开始练习' }}
        </button>
      </div>
    </div>
    
    <!-- 练习页面 -->
    <div v-else-if="exerciseState === 'practicing'" class="exercise-page">
      <div class="page-header">
        <button class="back-button" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>刷题练习</h1>
        <div class="header-right">
          <span class="question-progress">第 {{ currentQuestionIndex + 1 }} 题 / 共 {{ totalQuestions }} 题</span>
        </div>
      </div>
      
      <div class="progress-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
      </div>
      
      <div class="question-container" v-if="currentQuestion">
        <div class="question-header">
          <h3 class="question-title">{{ currentQuestion?.title || '' }}</h3>
          <div class="question-type" v-if="currentQuestion && currentQuestion.type">
            {{ getQuestionTypeText(currentQuestion.type) }}
          </div>
        </div>
        
        <!-- 错题模式的实时判断结果 -->
        <div v-if="selectedMode === 3 && userAnswers.length > 0 && currentQuestion && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id" class="real-time-result">
          <div :class="['result-card', userAnswers[userAnswers.length - 1].isCorrect ? 'correct' : 'wrong']">
            <div class="result-icon">
              <svg v-if="userAnswers[userAnswers.length - 1].isCorrect" width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="result-content">
              <h4 class="result-title">{{ userAnswers[userAnswers.length - 1].isCorrect ? '回答正确！' : '回答错误' }}</h4>
              <div class="answer-info">
                <div class="answer-item">
                  <span class="answer-label">你的答案：</span>
                  <span :class="['answer-value', userAnswers[userAnswers.length - 1].isCorrect ? 'correct' : 'wrong']">
                    {{ userAnswers[userAnswers.length - 1].userAnswer || '未作答' }}
                  </span>
                </div>
                <div class="answer-item" v-if="!userAnswers[userAnswers.length - 1].isCorrect">
                  <span class="answer-label">正确答案：</span>
                  <span class="answer-value correct">{{ userAnswers[userAnswers.length - 1].correctAnswer || '未知' }}</span>
                </div>
              </div>
              <!-- 答案解析 -->
              <div v-if="userAnswers[userAnswers.length - 1].analysis" class="analysis-section">
                <h5>解析：</h5>
                <div class="analysis-content">{{ userAnswers[userAnswers.length - 1].analysis }}</div>
              </div>
              <!-- 继续按钮 -->
              <div class="result-actions">
                <button 
                  class="action-button primary continue-button"
                  @click="continueToNextQuestion"
                >
                  {{ isLastQuestion ? '完成练习' : '下一题' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="options-container" v-if="currentQuestion && !(selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id)">
          <!-- 单选题 -->
          <div v-if="currentQuestion && currentQuestion.type === 1" class="option-group">
            <div
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :class="{ selected: selectedAnswer === option.label, correct: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && option.label === userAnswers[userAnswers.length - 1].correctAnswer, wrong: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && option.label === userAnswers[userAnswers.length - 1].userAnswer && !userAnswers[userAnswers.length - 1].isCorrect }"
              @click="selectOption(option.label)"
              class="option-item"
            >
              <div class="option-radio">
                <div class="radio-circle" :class="{ checked: selectedAnswer === option.label }"></div>
              </div>
              <div class="option-content">
                <span class="option-label">{{ option.label }}. </span>
                <span class="option-value">{{ option.value }}</span>
              </div>
            </div>
          </div>

          <!-- 多选题 -->
          <div v-else-if="currentQuestion && currentQuestion.type === 2" class="option-group">
            <div
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :class="{ selected: selectedAnswers.includes(option.label), correct: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && option.label === userAnswers[userAnswers.length - 1].correctAnswer, wrong: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && selectedAnswers.includes(option.label) && option.label !== userAnswers[userAnswers.length - 1].correctAnswer }"
              @click="toggleMultiOption(option.label)"
              class="option-item"
            >
              <div class="option-checkbox">
                <div class="checkbox-square" :class="{ checked: selectedAnswers.includes(option.label) }">
                  <svg v-if="selectedAnswers.includes(option.label)" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 6L9 17L4 12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
              <div class="option-content">
                <span class="option-label">{{ option.label }}. </span>
                <span class="option-value">{{ option.value }}</span>
              </div>
            </div>
          </div>

          <!-- 判断题：约定 A=正确，B=错误 -->
          <div v-else-if="currentQuestion && currentQuestion.type === 3" class="option-group">
            <div
              :class="{ selected: selectedAnswer === 'A', correct: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && 'A' === userAnswers[userAnswers.length - 1].correctAnswer, wrong: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && selectedAnswer === 'A' && 'A' !== userAnswers[userAnswers.length - 1].correctAnswer }"
              @click="selectOption('A')"
              class="option-item"
            >
              <div class="option-radio">
                <div class="radio-circle" :class="{ checked: selectedAnswer === 'A' }"></div>
              </div>
              <div class="option-content">
                <span class="option-label">A. </span>
                <span class="option-value">正确</span>
              </div>
            </div>
            <div
              :class="{ selected: selectedAnswer === 'B', correct: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && 'B' === userAnswers[userAnswers.length - 1].correctAnswer, wrong: selectedMode === 3 && userAnswers.length > 0 && userAnswers[userAnswers.length - 1].questionId === currentQuestion.id && selectedAnswer === 'B' && 'B' !== userAnswers[userAnswers.length - 1].correctAnswer }"
              @click="selectOption('B')"
              class="option-item"
            >
              <div class="option-radio">
                <div class="radio-circle" :class="{ checked: selectedAnswer === 'B' }"></div>
              </div>
              <div class="option-content">
                <span class="option-label">B. </span>
                <span class="option-value">错误</span>
              </div>
            </div>
          </div>

          <!-- 填空题 -->
          <div v-else-if="currentQuestion && currentQuestion.type === 4" class="text-answer">
            <input
              v-model="textAnswer"
              type="text"
              placeholder="请输入答案"
              class="text-input"
            />
          </div>

          <!-- 简答题 -->
          <div v-else-if="currentQuestion && currentQuestion.type === 5" class="text-answer">
            <textarea
              v-model="textAnswer"
              rows="5"
              placeholder="请输入答案"
              class="text-area"
            ></textarea>
          </div>

          <div v-else class="no-question">
            <p>未知题型，无法作答</p>
          </div>
        </div>
      </div>
      
      <div class="loading-container" v-else>
        <div class="loading-spinner"></div>
        <p>加载题目中...</p>
      </div>
      
      <div class="action-buttons">
        <!-- 错题模式显示移除错题按钮 -->
        <button 
          v-if="selectedMode === 3 && isLoggedIn"
          class="action-button danger"
          @click="removeFromWrongList"
          :disabled="!currentQuestion"
        >
          移除错题
        </button>
        <button 
          class="action-button secondary"
          @click="skipQuestion"
          :disabled="isLastQuestion || !currentQuestion"
        >
          跳过
        </button>
        <button 
          class="action-button primary"
          @click="submitAnswer"
          :disabled="!canSubmitCurrent || isSubmitting || !currentQuestion"
        >
          <span v-if="isSubmitting" class="loading-dots">提交中...</span>
          <span v-else>{{ isLastQuestion ? '提交练习' : '下一题' }}</span>
        </button>
      </div>
      
      <!-- 练习完成结果弹窗 -->
      <div v-if="showResult" class="result-modal">
        <div class="modal-overlay" @click="showResult = false"></div>
        <div class="modal-content">
          <div class="modal-header">
            <h3>练习完成！</h3>
            <button class="close-button" @click="showResult = false">×</button>
          </div>
          <div class="modal-body">
            <p>您已完成本次练习</p>
            <div class="result-stats">
              <div class="stat-item">
                <div class="stat-number">{{ correctCount }}</div>
                <div class="stat-label">答对</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ totalQuestions - correctCount }}</div>
                <div class="stat-label">答错</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ Math.round((correctCount / totalQuestions) * 100) }}%</div>
                <div class="stat-label">正确率</div>
              </div>
            </div>
            
            <!-- 查看作答详情按钮 -->
            <button class="modal-button secondary" @click="toggleAnswerDetails">
              {{ showAnswerDetails ? '收起作答详情' : '查看作答详情' }}
            </button>
            
            <!-- 题目作答详情（默认隐藏） -->
            <div v-if="showAnswerDetails" class="question-details">
              <h4>作答详情</h4>
              <div v-if="!userAnswers || !Array.isArray(userAnswers) || userAnswers.length === 0" class="empty-state">
                <p>暂无作答记录</p>
              </div>
              <div v-else class="question-list">
                <div 
                  v-for="(item, index) in userAnswers" 
                  :key="item?.questionId || index"
                  class="question-item"
                >
                  <div class="question-header" @click="toggleQuestion(index)">
                    <div class="question-info">
                      <span class="question-number">{{ index + 1 }}.</span>
                      <span class="question-title">{{ item.question?.title || '未知题目' }}</span>
                      <span 
                        :class="['result-badge', item.isCorrect ? 'correct' : 'wrong']"
                      >
                        {{ item.isCorrect ? '正确' : '错误' }}
                      </span>
                    </div>
                    <div class="question-toggle">
                      <svg 
                        width="16" 
                        height="16" 
                        viewBox="0 0 24 24" 
                        fill="none" 
                        xmlns="http://www.w3.org/2000/svg"
                        :class="{ rotated: expandedQuestions[index] }"
                      >
                        <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                    </div>
                  </div>
                  
                  <div v-if="expandedQuestions[index]" class="question-content">
                    <div class="answer-section">
                      <div class="answer-item">
                        <span class="answer-label">你的答案：</span>
                        <span :class="['answer-value', item.isCorrect ? 'correct' : 'wrong']">
                          {{ item.userAnswer || '未作答' }}
                        </span>
                      </div>
                      <div class="answer-item">
                        <span class="answer-label">正确答案：</span>
                        <span class="answer-value correct">{{ item.correctAnswer || '未知' }}</span>
                      </div>
                    </div>
                    
                    <!-- 题目选项（如果有） -->
                    <div v-if="item.question && item.question.options && Array.isArray(item.question.options) && item.question.options.length > 0" class="options-section">
                      <h5>选项：</h5>
                      <div class="options-list">
                        <div 
                          v-for="option in item.question.options" 
                          :key="option.label"
                          :class="[
                            'option-detail',
                            option.label === item.correctAnswer ? 'correct-option' : '',
                            option.label === item.userAnswer && !item.isCorrect ? 'wrong-option' : ''
                          ]"
                        >
                          <span class="option-label">{{ option.label }}.</span>
                          <span class="option-value">{{ option.value }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 答案解析 -->
                    <div v-if="item.analysis" class="analysis-section">
                      <h5>解析：</h5>
                      <div class="analysis-content">{{ item.analysis }}</div>
                    </div>
                    <div v-else class="analysis-section">
                      <h5>解析：</h5>
                      <div class="analysis-content">暂无解析</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <button class="modal-button primary" @click="viewStats">查看统计</button>
          </div>
          <div class="modal-footer">
            <button class="modal-button secondary" @click="startNewExercise">继续练习</button>
            <button class="modal-button primary" @click="goToStats">查看统计</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getRandomQuestions, getAllCategories, getCategoriesBySubjectId, getKnowledgePointProgress, getAllSubjects, batchSubmitAnswers as batchSubmitAnswersApi, getUserStats, getWrongQuestions, removeFromWrong } from '../api/exercise';
import { parseQuestionOptions, getQuestionTypeText, calculateTimeCost, getCurrentSubmitAnswer } from '../utils/questionUtils';

// 简单的提示函数
const showToast = (message) => {
  alert(message);
};

export default {
  name: 'ExerciseView',
  setup() {
    const router = useRouter();
    
    // 练习流程状态管理
    const exerciseState = ref('subject-selection'); // subject-selection, mode-selection, config, practicing, result
    const selectedMode = ref(null);
    const loading = ref(false);
    const isLoggedIn = ref(!!localStorage.getItem('token'));
    const knowledgePoints = ref([]);
    
    // 检查登录状态
    const checkLoginStatus = () => {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      isLoggedIn.value = !!token;
      console.log('登录状态:', isLoggedIn.value);
      console.log('Token:', token);
      console.log('UserId:', userId);
    };
    const configForm = reactive({
      count: 10,
      difficulty: null,
      subjectId: null,
      categoryId: null
    });
    
    // 练习模式选项
    const exerciseModes = [
      {
        value: 1,
        label: '按知识点练习',
        description: '只刷对应知识点的题，针对性练习'
      },
      {
        value: 2,
        label: '随机练习',
        description: '将所有知识点题目随机抽取，组合练习'
      },
      {
        value: 3,
        label: '错题重练',
        description: '只练本学科的错题，巩固薄弱知识点',
        requireLogin: true
      }
    ];
    
    // 难度等级选项
    const difficultyOptions = [
      { text: '简单', value: 1 },
      { text: '中等', value: 2 },
      { text: '困难', value: 3 }
    ];
    
    // 题目分类选项
    const categoryOptions = ref([]);
    
    // 学科选项
    const subjectOptions = ref([]);
    
    // 从后端获取的真实数据
    const questions = ref([]);
    const currentQuestionIndex = ref(0);
    const selectedAnswer = ref('');
    const selectedAnswers = ref([]);
    const textAnswer = ref('');
    const userAnswers = ref([]);
    const isSubmitting = ref(false);
    const showResult = ref(false);
    const correctCount = ref(0);
    const questionStartTime = ref(null); // 记录当前题目的开始时间
    const expandedQuestions = ref({}); // 控制题目展开/收起状态
    const showAnswerDetails = ref(false); // 控制是否显示作答详情
    
    const currentQuestion = computed(() => {
      return questions.value[currentQuestionIndex.value] || null;
    });
    
    const totalQuestions = computed(() => questions.value.length);
    const progress = computed(() => {
      return Math.round(((currentQuestionIndex.value + 1) / totalQuestions.value) * 100);
    });
    const isLastQuestion = computed(() => {
      return currentQuestionIndex.value >= totalQuestions.value - 1;
    });
    
    // 检查是否有选中学科
    const hasSelectedSubject = computed(() => {
      return !!configForm.subjectId;
    });
    


    const canSubmitCurrent = computed(() => {
      if (!currentQuestion.value) return false;
      const type = currentQuestion.value.type;
      if (type === 2) return Array.isArray(selectedAnswers.value) && selectedAnswers.value.length > 0;
      if (type === 4 || type === 5) return typeof textAnswer.value === 'string' && textAnswer.value.trim().length > 0;
      return !!selectedAnswer.value;
    });
    
    // 加载题目分类
    const loadCategories = async (subjectId = null) => {
      try {
        let response;
        if (subjectId) {
          // 如果指定了学科ID，只加载该学科的分类
          response = await getCategoriesBySubjectId(subjectId);
        } else {
          // 否则加载所有分类
          response = await getAllCategories();
        }
        
        if (response && response.code === 200) {
          categoryOptions.value = (response.data || []).filter(category => category != null).map(category => ({
            text: category.name,
            value: category.id
          }));
        }
      } catch (error) {
        console.error('加载题目分类失败:', error);
        showToast('加载题目分类失败');
      }
    };
    
    // 加载学科
    const loadSubjects = async () => {
      try {
        const response = await getAllSubjects();
        if (response && response.code === 200) {
          subjectOptions.value = (response.data || []).filter(subject => subject != null).map(subject => ({
            text: subject.name,
            value: subject.id
          }));
        }
      } catch (error) {
        console.error('加载学科失败:', error);
        showToast('加载学科失败');
      }
    };
    
    // 从后端获取题目
    const loadQuestions = async (params) => {
      try {
        loading.value = true;
        console.log('获取题目参数:', params);
        const response = await getRandomQuestions(params || { count: 10 });
        console.log('获取题目响应:', response);
        
        if (response.code === 200) {
          if (Array.isArray(response.data)) {
            // 转换后端返回的题目数据为前端所需格式
            questions.value = response.data.map(q => {
              // 处理选项数据，确保格式正确
              let options = [];

              // 尝试从不同的可能字段获取选项
              if (q.options) {
                options = parseQuestionOptions(q.options);
              } else if (q.optionA) {
                // 如果后端使用单独的选项字段
                const labels = ['A', 'B', 'C', 'D', 'E', 'F'];
                const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE', 'optionF'];
                
                options = optionKeys
                  .filter(key => q[key] !== undefined && q[key] !== null)
                  .map((key, idx) => ({
                    label: labels[idx],
                    value: q[key]
                  }));
              }
              
              return {
                ...q,
                options: options
              };
            });
            console.log('处理后的题目数据:', questions.value);
          } else {
            console.error('获取题目失败: 响应数据不是数组', response.data);
            questions.value = [];
          }
        } else {
          console.error('获取题目失败:', response.message);
          // 如果获取失败，使用默认数据
          questions.value = [];
        }
      } catch (error) {
        console.error('获取题目时发生错误:', error);
        // 发生错误时使用默认数据
        questions.value = [];
      } finally {
        loading.value = false;
      }
    };
    
    const loadKnowledgePoints = async () => {
      try {
        loading.value = true;
        if (configForm.subjectId) {
          // 获取当前用户ID
          const userId = localStorage.getItem('userId');
          
          // 调用后端API获取知识点进度
          const response = await getKnowledgePointProgress({
            subjectId: configForm.subjectId,
            userId: userId ? parseInt(userId) : null
          });
          
          if (response && response.code === 200) {
            knowledgePoints.value = response.data || [];
          } else {
            // 如果API调用失败，使用默认数据
            await loadCategories(configForm.subjectId);
            knowledgePoints.value = categoryOptions.value.map(category => ({
              id: category.value,
              name: category.text,
              progress: 0,
              totalQuestions: 0,
              completedQuestions: 0
            }));
          }
        } else {
          // 如果没有选中学科，先加载所有学科
          await loadSubjects();
          // 默认选择第一个学科
          if (subjectOptions.value.length > 0) {
            configForm.subjectId = subjectOptions.value[0].value;
            await loadKnowledgePoints();
          }
        }
      } catch (error) {
        console.error('加载知识点失败:', error);
        showToast('加载知识点失败');
        // 失败时使用默认数据
        if (configForm.subjectId) {
          await loadCategories(configForm.subjectId);
          knowledgePoints.value = categoryOptions.value.map(category => ({
            id: category.value,
            name: category.text,
            progress: 0,
            totalQuestions: 0,
            completedQuestions: 0
          }));
        }
      } finally {
        loading.value = false;
      }
    };

    // 选择学科
    const selectSubject = async (subjectId) => {
      configForm.subjectId = subjectId;
      // 保存到本地存储
      localStorage.setItem('selectedSubjectId', subjectId);
      // 加载该学科的分类
      await loadCategories(subjectId);
      // 切换到模式选择页面
      exerciseState.value = 'mode-selection';
    };

    const selectMode = async (mode) => {
      const modeConfig = exerciseModes.find(m => m.value === mode);
      if (modeConfig && modeConfig.requireLogin && !isLoggedIn.value) {
        showToast('此功能需要登录才能使用');
        return;
      }
      selectedMode.value = mode;
      
      // 如果选择按知识点练习，加载知识点并进入配置页面
      if (mode === 1) {
        await loadKnowledgePoints();
        exerciseState.value = 'config';
      } else {
        // 随机练习和错题练习直接开始，不需要配置
        await startExercise();
      }
    };

    const selectKnowledgePoint = async (point) => {
      // 设置选择的知识点
      configForm.categoryId = point.id;
      console.log('选择知识点:', point.name, 'ID:', point.id);
      console.log('configForm.categoryId:', configForm.categoryId);
      // 选择知识点后自动开始练习
      await startExercise();
    };
    
    // 学科变更时加载对应分类
    const handleSubjectChange = async (subjectId) => {
      if (subjectId) {
        // 如果当前是按知识点练习模式，调用API获取知识点进度
        if (selectedMode.value === 1) {
          await loadKnowledgePoints();
        } else {
          await loadCategories(subjectId);
        }
      } else {
        await loadCategories();
      }
    };
    
    const startExercise = async () => {
      console.log('开始练习 - selectedMode:', selectedMode.value);
      console.log('开始练习 - configForm.categoryId:', configForm.categoryId);
      
      // 按知识点练习时必须选择知识点
      if (selectedMode.value === 1 && !configForm.categoryId) {
        showToast('按知识点练习请选择知识点');
        return;
      }

      loading.value = true;
      try {
        if (selectedMode.value === 2) {
          // 随机练习模式：直接获取50道题，不分知识点，必须是用户选择的学科
          const params = {
            count: 50,
            exerciseMode: selectedMode.value,
            subjectId: configForm.subjectId
          };
          
          // 调用后端API获取题目
          await loadQuestions(params);
        } else if (selectedMode.value === 3) {
          // 错题练习模式：获取该学科的所有错题
          if (!isLoggedIn.value) {
            showToast('错题重练需要登录');
            loading.value = false;
            return;
          }
          
          const params = {
            subjectId: configForm.subjectId
          };
          
          // 调用错题重练API，获取所有错题
          const response = await getWrongQuestions(params);
          console.log('错题练习API响应:', response);
          console.log('response.data:', response.data);
          console.log('response.data.list:', response.data?.list);
          console.log('response.data.records:', response.data?.records);
          console.log('response.data.content:', response.data?.content);
          
          // 检查响应数据结构（支持分页格式）
          let dataArray = [];
          if (response.data) {
            if (Array.isArray(response.data)) {
              dataArray = response.data;
            } else if (Array.isArray(response.data.list)) {
              dataArray = response.data.list;
            } else if (Array.isArray(response.data.records)) {
              dataArray = response.data.records;
            } else if (Array.isArray(response.data.content)) {
              dataArray = response.data.content;
            }
          }
          console.log('实际使用的数据:', dataArray);
          
          if (response.code === 200 && Array.isArray(dataArray) && dataArray.length > 0) {
            questions.value = dataArray.map(q => {
              let options = [];
              if (q.options) {
                options = parseQuestionOptions(q.options);
              } else if (q.optionA) {
                const labels = ['A', 'B', 'C', 'D', 'E', 'F'];
                const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE', 'optionF'];
                
                options = optionKeys
                  .filter(key => q[key] !== undefined && q[key] !== null)
                  .map((key, idx) => ({
                    label: labels[idx],
                    value: q[key]
                  }));
              }
              
              return {
                ...q,
                options: options
              };
            });
          } else {
            questions.value = [];
          }
        } else {
          // 按知识点练习模式
          const params = {
            count: configForm.count,
            exerciseMode: selectedMode.value,
            subjectId: configForm.subjectId,
            categoryId: configForm.categoryId
          };

          if (configForm.difficulty) {
            params.difficulty = configForm.difficulty;
          }

          // 调用后端API获取题目
          await loadQuestions(params);
        }
        
        if (questions.value.length > 0) {
          console.log('成功获取题目，数量:', questions.value.length);
          // 开始练习
          exerciseState.value = 'practicing';
          // 重置练习状态
          currentQuestionIndex.value = 0;
          selectedAnswer.value = '';
          selectedAnswers.value = [];
          textAnswer.value = '';
          userAnswers.value = [];
          correctCount.value = 0;
          expandedQuestions.value = {}; // 重置展开状态
          showAnswerDetails.value = false; // 重置作答详情显示状态
          // 初始化第一个题目的开始时间
          questionStartTime.value = Date.now();
        } else {
          console.log('获取题目失败，questions.value.length:', questions.value.length);
          showToast(selectedMode.value === 3 ? '暂无错题记录' : '获取题目失败，请重试');
        }
      } catch (error) {
        console.error('开始练习失败:', error);
        showToast('开始练习失败，请重试');
      } finally {
        loading.value = false;
      }
    };
    
    const selectOption = (value) => {
      selectedAnswer.value = value;
    };
    
    const onAnswerSelect = (value) => {
      selectedAnswer.value = value;
    };

    const onAnswersSelect = (values) => {
      selectedAnswers.value = values;
    };

    const toggleMultiOption = (label) => {
      const list = Array.isArray(selectedAnswers.value) ? [...selectedAnswers.value] : [];
      const idx = list.indexOf(label);
      if (idx >= 0) list.splice(idx, 1);
      else list.push(label);
      selectedAnswers.value = list;
    };

    const toggleQuestion = (index) => {
      expandedQuestions.value = {
        ...expandedQuestions.value,
        [index]: !expandedQuestions.value[index]
      };
    };

    const toggleAnswerDetails = () => {
      showAnswerDetails.value = !showAnswerDetails.value;
    };


    
    // 继续到下一题（错题模式专用）
    const continueToNextQuestion = () => {
      if (isLastQuestion.value) {
        // 练习完成
        showToast('已完成所有错题练习');
        exerciseState.value = 'mode-selection';
      } else {
        // 下一题
        currentQuestionIndex.value++;
        selectedAnswer.value = '';
        selectedAnswers.value = [];
        textAnswer.value = '';
      }
    };

    const submitAnswer = async () => {
      if (!currentQuestion.value) return;
      if (!canSubmitCurrent.value) return;
      
      isSubmitting.value = true;
      
      try {
        const submitAns = getCurrentSubmitAnswer(currentQuestion.value, selectedAnswer.value, selectedAnswers.value, textAnswer.value);

        // 计算答题时间（秒）
        const timeCost = calculateTimeCost(questionStartTime.value);
        
        // 保存用户答案和完整题目信息
        const answerData = {
          questionId: currentQuestion.value.id,
          question: currentQuestion.value, // 保存完整题目信息
          userAnswer: submitAns,
          correctAnswer: currentQuestion.value.answer,
          isCorrect: submitAns === currentQuestion.value.answer,
          timeCost: timeCost,
          analysis: currentQuestion.value.analysis // 保存解析信息
        };
        userAnswers.value.push(answerData);
        
        if (submitAns === currentQuestion.value.answer) {
          correctCount.value++;
        }
        
        // 检查登录状态
        checkLoginStatus();
        
        // 错题模式：实时判断，显示结果后等待用户点击继续
        if (selectedMode.value === 3) {
          // 立即提交当前答案
          try {
            const submitData = {
              questionId: currentQuestion.value.id,
              userAnswer: submitAns,
              timeCost: timeCost
            };
            await batchSubmitAnswersApi([submitData]);
            
            // 如果答对了，可以选择从错题本中移除
            if (submitAns === currentQuestion.value.answer) {
              if (confirm('回答正确！是否将这道题从错题本中移除？')) {
                await removeFromWrong({
                  questionId: currentQuestion.value.id
                });
                showToast('已从错题本中移除');
                // 从当前题目列表中移除这道题
                questions.value.splice(currentQuestionIndex.value, 1);
                // 如果还有题目，更新当前索引
                if (questions.value.length > 0) {
                  currentQuestionIndex.value--; // 调整索引以保持正确位置
                }
              }
            }
          } catch (error) {
            console.error('提交答案失败:', error);
          }
          
          // 不自动进入下一题，等待用户点击继续按钮
        } else if (isLastQuestion.value) {
          // 其他模式：练习完成，批量提交所有答案
          if (userAnswers.value.length > 0) {
            const batchData = userAnswers.value.map(item => ({
              questionId: item.questionId,
              userAnswer: item.userAnswer,
              timeCost: item.timeCost
            }));
            
            console.log('批量提交答案请求数据:', batchData);
            console.log('登录状态:', isLoggedIn.value);
            console.log('Token:', localStorage.getItem('token'));
            
            try {
              const batchResponse = await batchSubmitAnswersApi(batchData);
              console.log('批量提交答案响应:', batchResponse);
            } catch (error) {
              console.error('批量提交答案失败:', error);
              showToast('提交答案失败，请重试');
            }
          }
          
          // 显示结果
          showResult.value = true;
        } else {
          // 其他模式：下一题（中间题目不提交，只在最后一题批量提交）
          currentQuestionIndex.value++;
          selectedAnswer.value = '';
          selectedAnswers.value = [];
          textAnswer.value = '';
        }
      } catch (error) {
        console.error('提交答案时发生错误:', error);
        showToast('提交答案失败，请重试');
      } finally {
        isSubmitting.value = false;
      }
    };
    
    const skipQuestion = () => {
      if (isLastQuestion.value) return;
      currentQuestionIndex.value++;
      selectedAnswer.value = '';
      selectedAnswers.value = [];
      textAnswer.value = '';
    };

    // 移除错题
    const removeFromWrongList = async () => {
      if (!currentQuestion.value) return;
      
      if (confirm('确定要将这道题从错题本中移除吗？')) {
        try {
          const response = await removeFromWrong({
            questionId: currentQuestion.value.id
          });
          
          if (response && response.code === 200) {
            showToast('已从错题本中移除');
            
            // 从当前题目列表中移除这道题
            questions.value.splice(currentQuestionIndex.value, 1);
            
            // 如果还有题目，继续练习
            if (questions.value.length > 0) {
              selectedAnswer.value = '';
              selectedAnswers.value = [];
              textAnswer.value = '';
            } else {
              // 如果没有题目了，结束练习
              showToast('已完成所有错题练习');
              exerciseState.value = 'mode-selection';
            }
          } else {
            showToast('移除失败，请重试');
          }
        } catch (error) {
          console.error('移除错题失败:', error);
          showToast('移除失败，请重试');
        }
      }
    };
    
    // 返回上一步
    const goBackToSubject = () => {
      exerciseState.value = 'subject-selection';
    };

    const goBackToMode = () => {
      exerciseState.value = 'mode-selection';
    };

    const goBack = () => {
      if (exerciseState.value === 'practicing') {
        // 如果已经开始练习，询问是否确认返回学习中心
        if (confirm('确定要退出练习吗？当前进度将会丢失。')) {
          router.push('/dashboard');
        }
      } else if (exerciseState.value === 'config') {
        exerciseState.value = 'mode-selection';
      } else if (exerciseState.value === 'mode-selection') {
        exerciseState.value = 'subject-selection';
      } else {
        // 如果在学科选择页面，返回到上一页（dashboard）
        router.go(-1);
      }
    };
    
    const goToLogin = () => {
      router.push('/login');
    };
    
    const viewStats = () => {
      showResult.value = false;
      router.push('/exercise/stats');
    };
    
    const startNewExercise = () => {
      showResult.value = false;
      exerciseState.value = 'config';
      selectedMode.value = null;
      configForm.count = 10;
      configForm.difficulty = null;
      configForm.categoryId = null;
      // 保留学科选择，让用户继续练习同一学科
    };
    
    const goToHome = () => {
      showResult.value = false;
      router.push('/exercise');
    };
    
    const goToStats = () => {
      showResult.value = false;
      router.push('/exercise/stats');
    };

    watch(currentQuestionIndex, () => {
      selectedAnswer.value = '';
      selectedAnswers.value = [];
      textAnswer.value = '';
      // 记录新题目的开始时间
      questionStartTime.value = Date.now();
    });
    
    onMounted(async () => {
      // 检查登录状态
      checkLoginStatus();
      
      // 加载学科
      await loadSubjects();
      
      // 检查本地存储中是否有选中学科
      const selectedSubjectId = localStorage.getItem('selectedSubjectId');
      if (selectedSubjectId) {
        // 设置学科ID
        configForm.subjectId = parseInt(selectedSubjectId);
        
        // 加载该学科的分类
        await loadCategories(configForm.subjectId);
        
        // 如果有选中学科，直接进入模式选择页面
        exerciseState.value = 'mode-selection';
      } else {
        // 如果没有选中学科，加载所有分类
        await loadCategories();
        
        // 默认显示学科选择页面
        exerciseState.value = 'subject-selection';
      }
      
      // 测试获取用户统计，验证登录状态
      if (isLoggedIn.value) {
        try {
          const statsResponse = await getUserStats();
          console.log('获取用户统计响应:', statsResponse);
        } catch (error) {
          console.error('获取用户统计失败:', error);
        }
      }
    });
    
    return {
      // 练习流程状态管理
      exerciseState,
      selectedMode,
      loading,
      isLoggedIn,
      configForm,
      exerciseModes,
      difficultyOptions,
      subjectOptions,
      categoryOptions,
      hasSelectedSubject,
      selectSubject,
      selectMode,
      handleSubjectChange,
      startExercise,
      goToLogin,
      checkLoginStatus,
      // 知识点练习相关
      knowledgePoints,
      selectKnowledgePoint,
      
      // 练习相关
      currentQuestion,
      currentQuestionIndex,
      selectedAnswer,
      selectedAnswers,
      textAnswer,
      userAnswers,
      totalQuestions,
      progress,
      isLastQuestion,
      isSubmitting,
      showResult,
      correctCount,
      getQuestionTypeText,
      canSubmitCurrent,
      selectOption,
      onAnswerSelect,
      onAnswersSelect,
      toggleMultiOption,
      toggleQuestion,
      toggleAnswerDetails,
      expandedQuestions,
      showAnswerDetails,
      submitAnswer,
      skipQuestion,
      removeFromWrongList,
      continueToNextQuestion,
      goBack,
      goBackToSubject,
      goBackToMode,
      viewStats,
      startNewExercise,
      goToHome,
      goToStats
    };
  }
};
</script>

<style scoped>
.exercise-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 流程进度指示器 */
.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  gap: 2rem;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s;
}

.step-label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.progress-step.active .step-number {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.progress-step.active .step-label {
  color: #409eff;
  font-weight: 600;
}

.progress-step.completed .step-number {
  background: #10b981;
  color: white;
}

.progress-step.completed .step-label {
  color: #10b981;
}

.progress-line {
  flex: 1;
  height: 2px;
  background: #e5e7eb;
  max-width: 100px;
}

/* 页面容器 */
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.back-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.header-right {
  display: flex;
  align-items: center;
}

.question-progress {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 页面内容区域 */
.page-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  width: 100%;
}

/* 模式信息 */
.mode-info {
  background: #f0f9ff;
  border: 1px solid #c6e2ff;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.mode-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.mode-info p {
  margin: 0;
  color: #6b7280;
  line-height: 1.5;
}

/* 知识点配置区域 */
.knowledge-point-config {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.knowledge-point-config h4 {
  margin: 0 0 1.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

/* 学科卡片样式 */
.subject-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.subject-card:hover {
  border-color: #409eff;
  background: #f9fafb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.subject-card.selected {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.subject-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  border-radius: 12px;
  color: white;
}

.subject-info {
  flex: 1;
}

.subject-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.subject-info p {
  margin: 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.subject-action {
  color: #6b7280;
  transition: color 0.3s;
}

.subject-card:hover .subject-action {
  color: #409eff;
}

/* 模式卡片样式 */
.mode-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
}

.mode-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.mode-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.mode-card.require-login {
  opacity: 0.7;
}

.mode-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  border-radius: 12px;
  color: white;
}

.mode-card h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.mode-card p {
  margin: 0 0 1rem 0;
  color: #6b7280;
  line-height: 1.5;
}

.login-required {
  background: #fefce8;
  color: #d97706;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

/* 章节标题样式 */
.section-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
  position: relative;
}

.section-header h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
}

.section-header p {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
}

.section-header .back-button-small {
  position: absolute;
  left: 0;
  top: 0;
}

/* 学科选择部分 */
.subject-selection-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.subject-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.subject-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.subject-card:hover {
  border-color: #409eff;
  background: #f9fafb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.subject-card.selected {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.subject-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.subject-info p {
  margin: 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.subject-action {
  color: #6b7280;
  transition: color 0.3s;
}

.subject-card:hover .subject-action {
  color: #409eff;
}

/* 模式选择部分 */
.mode-selection-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.guest-notice {
  background: #fef0f0;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.guest-notice p {
  margin: 0;
  color: #dc2626;
  font-weight: 500;
}

.login-button {
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.login-button:hover {
  background: #b91c1c;
  transform: translateY(-2px);
}

/* 模式选择卡片 */
.mode-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

/* 知识点列表页面 */
.knowledge-points-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.knowledge-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.back-button-small {
  background: #f3f4f6;
  color: #374151;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s;
}

.back-button-small:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.knowledge-points-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.knowledge-point-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.knowledge-point-item:hover {
  border-color: #409eff;
  background: #f9fafb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.knowledge-point-info {
  flex: 1;
}

.knowledge-point-info h3 {
  margin: 0 0 0.75rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.knowledge-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.knowledge-progress .progress-bar {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.knowledge-progress .progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff 0%, #2a7fff 100%);
  transition: width 0.3s ease;
}

.progress-text {
  font-weight: 600;
  color: #409eff;
  min-width: 50px;
  text-align: right;
}

.knowledge-stats {
  font-size: 0.875rem;
  color: #6b7280;
}

.knowledge-point-action {
  color: #6b7280;
  transition: color 0.3s;
}

.knowledge-point-item:hover .knowledge-point-action {
  color: #409eff;
}

.mode-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.mode-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.mode-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.mode-card.require-login {
  opacity: 0.7;
}

.mode-card h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.mode-card p {
  margin: 0 0 1rem 0;
  color: #6b7280;
  line-height: 1.5;
}

.login-required {
  background: #fefce8;
  color: #d97706;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

/* 配置区域 */
.config-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.config-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.config-item label {
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

/* 滑块样式 */
.slider-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.config-slider {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: #e5e7eb;
  outline: none;
  -webkit-appearance: none;
}

.config-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #409eff;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

.slider-value {
  background: #409eff;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-weight: 500;
  min-width: 60px;
  text-align: center;
}

/* 选择框样式 */
.config-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.config-select:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

/* 已选择学科样式 */
.selected-subject-info {
  padding: 0.75rem 1rem;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 8px;
  font-weight: 500;
  border: 1px solid #c6e2ff;
}

/* 开始按钮 */
.start-button {
  width: 100%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.start-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.start-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 进度条 */
.progress-container {
  padding: 1rem 2rem;
  background: white;
}

.progress-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  transition: width 0.3s ease;
}

/* 题目容器 */
.question-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 2rem;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.question-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.6;
}

.question-type {
  background: #ecf5ff;
  color: #409eff;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

/* 选项组 */
.options-container {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
  background: #f9fafb;
}

.option-item.selected {
  border-color: #409eff;
  background: #f0f9ff;
}

/* 单选按钮 */
.option-radio {
  display: flex;
  align-items: center;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.radio-circle.checked {
  border-color: #409eff;
  background: #409eff;
}

.radio-circle.checked::after {
  content: '';
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

/* 复选框 */
.option-checkbox {
  display: flex;
  align-items: center;
}

.checkbox-square {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.checkbox-square.checked {
  border-color: #409eff;
  background: #409eff;
}

.option-content {
  flex: 1;
}

.option-label {
  font-weight: 600;
  color: #374151;
  margin-right: 0.5rem;
}

.option-value {
  color: #4b5563;
  line-height: 1.5;
}

/* 文本输入 */
.text-answer {
  margin-top: 2rem;
}

.text-input,
.text-area {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.3s;
}

.text-input:focus,
.text-area:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.text-area {
  resize: vertical;
  min-height: 120px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  gap: 1rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  padding: 2rem;
  background: white;
  position: sticky;
  bottom: 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
}

.action-button {
  flex: 1;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.action-button.primary {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
}

.action-button.secondary {
  background: #6c757d;
  color: white;
}

.action-button.danger {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
}

.action-button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.action-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-dots {
  display: inline-block;
}

/* 结果弹窗 */
.result-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  z-index: 1001;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0.25rem;
  border-radius: 4px;
}

.close-button:hover {
  background: #f3f4f6;
}

.modal-body {
  padding: 2rem;
  text-align: center;
}

.modal-body p {
  margin: 0 0 2rem 0;
  color: #374151;
  font-size: 1.1rem;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin-bottom: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #409eff;
}

.stat-label {
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.modal-button {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-button.primary {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
}

.modal-button.secondary {
  background: #f3f4f6;
  color: #374151;
}

.modal-button:hover {
  transform: translateY(-2px);
}

/* 题目详情样式 */
.question-details {
  margin-top: 2rem;
  border-top: 1px solid #e5e7eb;
  padding-top: 1.5rem;
}

.question-details h4 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.question-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: #f9fafb;
  cursor: pointer;
  transition: background-color 0.3s;
}

.question-header:hover {
  background: #f3f4f6;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.question-number {
  font-weight: 600;
  color: #409eff;
  min-width: 24px;
}

.question-title {
  flex: 1;
  color: #374151;
  font-weight: 500;
  line-height: 1.4;
}

.result-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.result-badge.correct {
  background: #d1fae5;
  color: #065f46;
}

.result-badge.wrong {
  background: #fee2e2;
  color: #991b1b;
}

.question-toggle {
  color: #6b7280;
  transition: color 0.3s, transform 0.3s;
}

.question-toggle svg.rotated {
  transform: rotate(180deg);
}

.question-content {
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
}

.answer-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.answer-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.answer-label {
  font-weight: 600;
  color: #374151;
  min-width: 70px;
}

.answer-value {
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.answer-value.correct {
  background: #d1fae5;
  color: #065f46;
}

.answer-value.wrong {
  background: #fee2e2;
  color: #991b1b;
}

.options-section {
  margin-bottom: 1rem;
}

.options-section h5 {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.option-detail {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.option-detail.correct-option {
  background: #d1fae5;
  border-color: #22c55e;
}

.option-detail.wrong-option {
  background: #fee2e2;
  border-color: #ef4444;
}

.analysis-section {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.analysis-section h5 {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.analysis-content {
  color: #4b5563;
  line-height: 1.5;
  font-size: 0.9rem;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px dashed #e5e7eb;
}


.result-dialog-content {
  padding: 16px;
  text-align: center;
}

.result-dialog-content p {
  color: #666;
  font-size: 14px;
  margin: 0 0 24px 0;
}

.result-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-actions .van-button {
  height: 48px;
  font-weight: 600;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}
</style>