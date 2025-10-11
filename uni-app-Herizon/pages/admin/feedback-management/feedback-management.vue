<template>
  <view class="page">
    <view class="filter-bar">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="filter-chip"
        :class="{ active: selectedStatus === tab.value }"
        @click="changeStatus(tab.value)"
      >
        {{ tab.label }}
      </view>
      <button class="refresh-btn" size="mini" type="default" @click="fetchFeedback" :loading="loading">
        刷新
      </button>
    </view>

    <view class="list">
      <view class="feedback-item" v-for="item in feedbackList" :key="item.id">
        <view class="item-header">
          <text class="item-type">{{ typeName(item.type) }}</text>
          <text class="item-status" :class="item.status">{{ item.statusLabel }}</text>
        </view>

        <text class="item-content">{{ item.content }}</text>

        <view class="meta">
          <text class="meta-text">{{ userLabel(item) }}</text>
          <text class="meta-text">{{ formatDate(item.createdAt) }}</text>
        </view>

        <view class="contact-row" v-if="item.contact">
          <text class="contact-label">联系方式</text>
          <text class="contact-text">{{ item.contact }}</text>
        </view>

        <view class="reply-block" v-if="item.adminReply">
          <text class="reply-label">管理员回复</text>
          <text class="reply-text">{{ item.adminReply }}</text>
        </view>

        <view class="actions">
          <button
            type="primary"
            size="mini"
            @click="handleReply(item)"
            :loading="replyingId === item.id"
          >
            {{ item.status === 'pending' ? '回复' : '修改回复' }}
          </button>
        </view>
      </view>

      <view class="empty" v-if="!loading && feedbackList.length === 0">
        <text>暂无反馈记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { adminApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusTabs: [
        { label: '全部', value: '' },
        { label: '未处理', value: 'pending' },
        { label: '已解决', value: 'resolved' }
      ],
      selectedStatus: '',
      loading: false,
      replyingId: null,
      feedbackList: []
    }
  },
  onShow() {
    this.fetchFeedback()
  },
  methods: {
    async changeStatus(value) {
      if (this.selectedStatus === value) {
        return
      }
      this.selectedStatus = value
      await this.fetchFeedback()
    },
    async fetchFeedback() {
      try {
        this.loading = true
        const params = {}
        if (this.selectedStatus) {
          params.status = this.selectedStatus
        }
        const data = await adminApi.getFeedbackList(params)
        this.feedbackList = Array.isArray(data) ? data : []
      } catch (error) {
        uni.showToast({
          title: error.message || '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    typeName(type) {
      const map = {
        bug: '功能异常',
        feature: '功能建议',
        experience: '体验优化',
        complaint: '投诉反馈',
        other: '其他问题'
      }
      return map[type] || '其他问题'
    },
    userLabel(item) {
      const name = item.username || `用户${item.userId}`
      return `${name} · ${item.userRoleLabel || ''}`.trim()
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
    async handleReply(item) {
      this.replyingId = item.id
      try {
        const modalResult = await new Promise((resolve) => {
          uni.showModal({
            title: '回复用户',
            editable: true,
            placeholderText: '请输入回复内容',
            confirmText: '提交',
            success: (res) => resolve(res),
            fail: () => resolve({ cancel: true })
          })
        })

        if (!modalResult || modalResult.cancel) {
          return
        }
        const reply = (modalResult.content || '').trim()
        if (!reply) {
          uni.showToast({
            title: '回复内容不能为空',
            icon: 'none'
          })
          return
        }

        await adminApi.replyFeedback(item.id, { reply })
        uni.showToast({
          title: '回复成功',
          icon: 'success'
        })
        await this.fetchFeedback()
      } catch (error) {
        uni.showToast({
          title: error.message || '回复失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.replyingId = null
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx 24rpx 40rpx;
  background-color: #f3f4f6;
  box-sizing: border-box;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.filter-chip {
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  background-color: #ffffff;
  color: #4b5563;
  font-size: 26rpx;
  border: 2rpx solid transparent;
  box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.08);
}

.filter-chip.active {
  background-color: #2563eb;
  color: #ffffff;
  border-color: #1d4ed8;
}

.refresh-btn {
  margin-left: auto;
  border-radius: 999rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.feedback-item {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 12rpx 30rpx rgba(15, 23, 42, 0.08);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.item-type {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
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
  color: #1f2937;
  line-height: 1.6;
}

.meta {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.meta-text {
  max-width: 48%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contact-row {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  font-size: 24rpx;
  color: #374151;
}

.contact-label {
  font-weight: 600;
  color: #2563eb;
}

.reply-block {
  margin-top: 18rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  background-color: #eff6ff;
}

.reply-label {
  font-size: 24rpx;
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 8rpx;
  display: block;
}

.reply-text {
  font-size: 26rpx;
  color: #1f2937;
  line-height: 1.6;
}

.actions {
  margin-top: 20rpx;
  display: flex;
  justify-content: flex-end;
}

.empty {
  padding: 60rpx 0;
  text-align: center;
  color: #6b7280;
  font-size: 26rpx;
}
</style>
