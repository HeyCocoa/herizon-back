<template>
  <view class="feedback-page">
    <view class="card">
      <view class="card-header">
        <text class="card-title">提交反馈</text>
        <text class="card-subtitle">我们会尽快处理并告知处理结果</text>
      </view>

      <view class="field">
        <text class="field-label">反馈类型</text>
        <view class="type-list">
          <view
            v-for="type in feedbackTypes"
            :key="type.id"
            class="type-chip"
            :class="{ active: selectedType === type.id, disabled: !canInteract }"
            @click="selectType(type.id)"
          >
            <text class="type-name">{{ type.name }}</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="field-label required">反馈内容</text>
        <textarea
          class="content-input"
          v-model="content"
          :maxlength="500"
          :disabled="!canInteract"
          placeholder="请描述遇到的问题、期望或建议，越详细越容易被解决"
        ></textarea>
        <text class="char-counter">{{ content.length }}/500</text>
      </view>

      <view class="field">
        <text class="field-label">联系方式（选填）</text>
        <input
          class="contact-input"
          v-model="contact"
          :disabled="!canInteract"
          placeholder="邮箱/微信号/手机号，方便我们联系您"
        />
      </view>

      <view class="hint info" v-if="!isLoggedIn">
        <text>请先登录后再提交反馈。</text>
      </view>
      <view class="hint warning" v-else-if="isTrialUser">
        <text>体验用户暂不支持在线提交反馈，请先完成身份认证。</text>
        <text class="link" @click="goToVerification">前往身份认证</text>
      </view>

      <button class="submit-btn" :disabled="!canSubmit" @click="submitFeedback">
        {{ submitButtonText }}
      </button>
    </view>

    <view class="card">
      <view class="card-header">
        <text class="card-title">我的反馈</text>
        <button class="refresh-btn" size="mini" type="default" @click="fetchMyFeedback" :loading="fetching">
          刷新
        </button>
      </view>

      <view class="empty-state" v-if="!isLoggedIn">
        <text>登录后可以查看反馈处理进度。</text>
      </view>

      <view class="empty-state" v-else-if="feedbackList.length === 0 && !fetching">
        <text>还没有反馈记录。</text>
      </view>

      <view v-else class="feedback-list">
        <view class="feedback-item" v-for="item in feedbackList" :key="item.id">
          <view class="item-header">
            <text class="item-type">{{ typeName(item.type) }}</text>
            <text class="item-status" :class="item.status">{{ item.statusLabel }}</text>
          </view>
          <text class="item-content">{{ item.content }}</text>
          <view class="admin-reply" v-if="item.adminReply">
            <text class="reply-title">管理员回复</text>
            <text class="reply-text">{{ item.adminReply }}</text>
          </view>
          <view class="item-footer">
            <text class="item-time">{{ formatDate(item.updatedAt || item.createdAt) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getAuthInfo, USER_ROLES } from '@/utils/auth.js'
import { feedbackApi } from '@/utils/api.js'

export default {
  data() {
    return {
      feedbackTypes: [
        { id: 'bug', name: '功能异常' },
        { id: 'feature', name: '功能建议' },
        { id: 'experience', name: '体验优化' },
        { id: 'complaint', name: '投诉反馈' },
        { id: 'other', name: '其他问题' }
      ],
      selectedType: 'bug',
      content: '',
      contact: '',
      loading: false,
      fetching: false,
      userInfo: null,
      feedbackList: []
    }
  },
  computed: {
    isLoggedIn() {
      return !!(this.userInfo && this.userInfo.id)
    },
    isTrialUser() {
      return this.userInfo && this.userInfo.role === USER_ROLES.TRIAL
    },
    canInteract() {
      return this.isLoggedIn && !this.isTrialUser
    },
    typeMap() {
      return this.feedbackTypes.reduce((acc, item) => {
        acc[item.id] = item.name
        return acc
      }, {})
    },
    canSubmit() {
      const text = this.content.trim()
      return this.canInteract && text.length >= 10 && !this.loading
    },
    submitButtonText() {
      if (!this.isLoggedIn) {
        return '请先登录'
      }
      if (this.isTrialUser) {
        return '体验用户无法提交'
      }
      return this.loading ? '提交中...' : '提交反馈'
    }
  },
  onShow() {
    this.initPage()
  },
  methods: {
    initPage() {
      this.userInfo = getAuthInfo() || null
      if (this.isLoggedIn) {
        this.fetchMyFeedback()
      } else {
        this.feedbackList = []
      }
    },
    selectType(typeId) {
      if (!this.canInteract) {
        return
      }
      this.selectedType = typeId
    },
    async submitFeedback() {
      if (!this.canSubmit) {
        return
      }
      try {
        this.loading = true
        const payload = {
          type: this.selectedType,
          content: this.content.trim(),
          contact: this.contact.trim()
        }
        await feedbackApi.submitFeedback(payload)
        uni.showToast({
          title: '反馈提交成功',
          icon: 'success'
        })
        this.content = ''
        this.contact = ''
        await this.fetchMyFeedback()
      } catch (error) {
        uni.showToast({
          title: error.message || '提交失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    async fetchMyFeedback() {
      if (!this.isLoggedIn) {
        return
      }
      try {
        this.fetching = true
        const data = await feedbackApi.getMyFeedback()
        this.feedbackList = Array.isArray(data) ? data : []
      } catch (error) {
        uni.showToast({
          title: error.message || '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.fetching = false
      }
    },
    typeName(type) {
      return this.typeMap[type] || '其他问题'
    },
    formatDate(value) {
      if (!value) {
        return ''
      }
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      const pad = (num) => String(num).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    goToVerification() {
      uni.navigateTo({
        url: '/pages/verification/verification'
      })
    }
  }
}
</script>

<style scoped>
.feedback-page {
  min-height: 100vh;
  padding: 32rpx 24rpx 48rpx;
  background-color: #f5f6f8;
  box-sizing: border-box;
}

.card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx 28rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 10rpx 30rpx rgba(17, 25, 40, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #111827;
}

.card-subtitle {
  font-size: 24rpx;
  color: #6b7280;
}

.field {
  margin-bottom: 28rpx;
}

.field-label {
  font-size: 28rpx;
  color: #111827;
  margin-bottom: 16rpx;
  display: block;
}

.field-label.required::after {
  content: '*';
  margin-left: 8rpx;
  color: #ef4444;
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.type-chip {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid #d1d5db;
  background-color: #ffffff;
  font-size: 26rpx;
  color: #4b5563;
}

.type-chip.active {
  border-color: #2563eb;
  color: #2563eb;
  background-color: rgba(37, 99, 235, 0.08);
}

.type-chip.disabled {
  opacity: 0.5;
}

.content-input {
  width: 100%;
  min-height: 220rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background-color: #f9fafb;
  font-size: 28rpx;
  line-height: 1.6;
  color: #111827;
}

.content-input:disabled {
  background-color: #f3f4f6;
}

.char-counter {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #9ca3af;
  margin-top: 8rpx;
}

.contact-input {
  width: 100%;
  height: 80rpx;
  border-radius: 16rpx;
  padding: 0 20rpx;
  background-color: #f9fafb;
  font-size: 28rpx;
  color: #111827;
}

.contact-input:disabled {
  background-color: #f3f4f6;
}

.hint {
  font-size: 26rpx;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.hint.info {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.hint.warning {
  background-color: #fef3c7;
  color: #92400e;
}

.hint .link {
  color: #2563eb;
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 84rpx;
  border-radius: 999rpx;
  background-color: #2563eb;
  color: #ffffff;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.submit-btn[disabled] {
  background-color: #d1d5db;
  color: #6b7280;
}

.refresh-btn {
  padding: 0 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  border: 2rpx solid #d1d5db;
  background-color: #ffffff;
  color: #4b5563;
}

.empty-state {
  padding: 40rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #6b7280;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.feedback-item {
  border-radius: 16rpx;
  padding: 24rpx;
  background-color: #f9fafb;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.item-type {
  font-size: 26rpx;
  color: #1f2937;
  font-weight: 500;
}

.item-status {
  font-size: 24rpx;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
}

.item-status.pending {
  background-color: #fee2e2;
  color: #b91c1c;
}

.item-status.resolved {
  background-color: #dcfce7;
  color: #15803d;
}

.item-content {
  font-size: 26rpx;
  color: #374151;
  line-height: 1.6;
}

.admin-reply {
  margin-top: 20rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  background-color: #eff6ff;
}

.reply-title {
  font-size: 24rpx;
  color: #2563eb;
  margin-bottom: 8rpx;
  display: block;
}

.reply-text {
  font-size: 26rpx;
  color: #1f2937;
  line-height: 1.6;
}

.item-footer {
  margin-top: 16rpx;
  display: flex;
  justify-content: flex-end;
}

.item-time {
  font-size: 22rpx;
  color: #9ca3af;
}
</style>
