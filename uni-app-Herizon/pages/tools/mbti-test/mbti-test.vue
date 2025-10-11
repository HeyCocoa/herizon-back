<template>
  <view class="page-container">
    <view class="top-nav" :style="{ paddingTop: topNavPadding }">
      <view class="nav-left" @click="goBack">
        <text class="icon">←</text>
      </view>
      <view class="nav-title">MBTI 性格测试</view>
      <view class="nav-right"></view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <view v-if="stage === 'intro'" class="intro-section">
        <view class="intro-card">
          <text class="intro-icon">🧠</text>
          <text class="intro-title">了解你的 16 型人格倾向</text>
          <text class="intro-text">参考开源题库,共 93 道二选一问题。按照第一直觉作答,大约 10-12 分钟。</text>
        </view>

        <view class="tips-card">
          <text class="tips-title">答题小贴士</text>
          <view class="tips-list">
            <text class="tips-item">- 没有对错之分,只选出更像自己的情况。</text>
            <text class="tips-item">- 保持节奏,不必纠结每一题。</text>
            <text class="tips-item">- 完成后立即给出人格类型简介。</text>
          </view>
        </view>

        <button class="primary-btn" @click="startTest">开始测试</button>
      </view>

      <view v-else-if="stage === 'quiz'" class="quiz-section">
        <view class="progress-card">
          <view class="progress-top">
            <text class="progress-text">题目 {{ currentIndex + 1 }} / {{ questions.length }}</text>
            <text class="progress-percent">{{ progressPercent }}%</text>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
          </view>
        </view>

        <view class="question-card">
          <text class="question-title">第 {{ currentIndex + 1 }} 题</text>
          <text class="question-text">{{ currentQuestion.question }}</text>
        </view>

        <view class="options">
          <view
            v-for="option in currentOptions"
            :key="option.value"
            class="option-item"
            :class="{ selected: selectedAnswer === option.value }"
            @click="selectAnswer(option.value)"
          >
            <text class="option-label">{{ option.text }}</text>
          </view>
        </view>

        <view class="nav-buttons">
          <button class="nav-btn" :disabled="currentIndex === 0" @click="prevQuestion">上一题</button>
          <button class="nav-btn primary" :disabled="!selectedAnswer" @click="nextQuestion">
            {{ currentIndex === questions.length - 1 ? '查看结果' : '下一题' }}
          </button>
        </view>
      </view>

      <view v-else class="result-section">
        <view class="result-card">
          <text class="result-label">你的类型</text>
          <text class="result-type">{{ resultLabel }}</text>
        </view>

        <view class="lines-card">
          <text
            v-for="(line, index) in resultLines"
            :key="index"
            class="result-line"
          >
            {{ line }}
          </text>
        </view>

        <view class="result-actions">
          <button class="primary-btn" @click="restartTest">重新测试</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { mbtiQuestions } from '../../../utils/mbti-questions.js'
import { calculateMbtiResult, mbtiProfiles } from '../../../utils/mbti-profiles.js'

export default {
  onLoad() {
    const info = uni.getSystemInfoSync()
    let safeTop = info.statusBarHeight || 0

    if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
      const rect = uni.getMenuButtonBoundingClientRect()
      if (rect && rect.top) {
        safeTop = Math.max(safeTop, rect.top)
      }
    }

    if (!safeTop) {
      safeTop = 20
    }

    this.statusBarHeight = safeTop
    this.topNavPadding = safeTop + 'px'
  },
  data() {
    return {
      statusBarHeight: 0,
      topNavPadding: '64px',
      questions: mbtiQuestions,
      stage: 'intro',
      currentIndex: 0,
      answers: Array(mbtiQuestions.length).fill(null),
      selectedAnswer: null,
      result: null
    }
  },
  computed: {
    currentQuestion() {
      return this.questions[this.currentIndex] || {}
    },
    currentOptions() {
      const question = this.currentQuestion
      if (!question) return []
      return [question.choice_a, question.choice_b]
    },
    progressPercent() {
      return Math.round(((this.currentIndex + 1) / this.questions.length) * 100)
    },
    resultLabel() {
      if (!this.result) return '—'
      const profile = mbtiProfiles[this.result.type]
      return profile ? profile.label : this.result.type
    },
    resultLines() {
      if (!this.result) return []
      const profile = mbtiProfiles[this.result.type]
      if (profile && Array.isArray(profile.lines)) {
        return profile.lines
      }
      return ['我们已记录你的结果,稍后将补充该类型的简介。']
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    startTest() {
      this.stage = 'quiz'
      this.currentIndex = 0
      this.selectedAnswer = this.answers[0]
    },
    selectAnswer(value) {
      this.selectedAnswer = value
    },
    prevQuestion() {
      if (this.currentIndex === 0) return
      this.answers[this.currentIndex] = this.selectedAnswer
      this.currentIndex -= 1
      this.selectedAnswer = this.answers[this.currentIndex]
    },
    nextQuestion() {
      if (!this.selectedAnswer) return
      this.answers[this.currentIndex] = this.selectedAnswer

      if (this.currentIndex === this.questions.length - 1) {
        this.finishTest()
        return
      }

      this.currentIndex += 1
      this.selectedAnswer = this.answers[this.currentIndex]
    },
    finishTest() {
      const collected = this.answers.filter(Boolean)
      this.result = calculateMbtiResult(collected)
      this.stage = 'result'
    },
    restartTest() {
      this.stage = 'intro'
      this.currentIndex = 0
      this.answers = Array(this.questions.length).fill(null)
      this.selectedAnswer = null
      this.result = null
    }
  }
}
</script>

<style scoped lang="scss">
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background-color: #ffffff;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
}

.nav-left,
.nav-right {
  width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 20px;
  color: #1f2937;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.content-scroll {
  flex: 1;
  padding: 24px 24px 40px;
  box-sizing: border-box;
}

.intro-section,
.quiz-section,
.result-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.intro-card,
.tips-card,
.progress-card,
.question-card,
.lines-card,
.result-card {
  background-color: #ffffff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
}

.intro-icon {
  font-size: 42px;
}

.intro-title {
  display: block;
  margin-top: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.intro-text {
  display: block;
  margin-top: 8px;
  font-size: 16px;
  color: #4b5563;
  line-height: 22px;
}

.tips-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #4b5563;
}

.primary-btn {
  width: 100%;
  border: none;
  border-radius: 999px;
  padding: 14px 0;
  background: linear-gradient(135deg, #ff7849 0%, #ff5a6b 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
}

.progress-top {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 12px;
}

.progress-bar {
  height: 8px;
  background-color: #eceff4;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 999px;
}

.question-card {
  background-color: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.06);
}

.question-title {
  font-size: 16px;
  font-weight: 600;
  color: #ff7849;
  margin-bottom: 12px;
}

.question-text {
  font-size: 18px;
  color: #111827;
  line-height: 26px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  border-radius: 16px;
  padding: 18px;
  background-color: #ffffff;
  border: 2px solid transparent;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.05);
  transition: all 0.2s ease;
}

.option-item.selected {
  border-color: #ff7849;
  background-color: #fff3ed;
}

.option-label {
  font-size: 16px;
  color: #111827;
}

.nav-buttons {
  display: flex;
  gap: 14px;
}

.nav-btn {
  flex: 1;
  padding: 12px;
  border-radius: 999px;
  font-size: 15px;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
  color: #374151;
}

.nav-btn.primary {
  border: none;
  background: linear-gradient(135deg, #ff7849, #ff5a6b);
  color: #ffffff;
}

.nav-btn:disabled {
  opacity: 0.4;
}

.result-card {
  text-align: center;
}

.result-label {
  font-size: 14px;
  color: #6b7280;
}

.result-type {
  display: block;
  margin-top: 10px;
  font-size: 30px;
  font-weight: 700;
  color: #ff7849;
}

.lines-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 15px;
  color: #374151;
  line-height: 24px;
}

.result-actions {
  margin-top: 16px;
}
</style>
