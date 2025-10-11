<template>
  <view class="page">
    <view class="top-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <text class="nav-title">工具</text>
      <view class="nav-actions">
        <text class="more-icon" @click="showMoreTools">⋯</text>
      </view>
    </view>

    <scroll-view
      class="scroll"
      scroll-y
      enable-back-to-top="true"
      refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="refreshTools"
    >
      <view class="section">
        <text class="section-subtitle">了解自我</text>
        <text class="section-title">职业测评</text>
        <view class="card" @click="handleToolClick('mbti')">
          <view class="card-left">
            <text class="card-icon">🧠</text>
            <view class="card-info">
              <text class="card-title">MBTI 性格测试</text>
              <text class="card-desc">93 题快速回答,匹配 16 型人格</text>
              <text class="card-tag">本地计算结果,不上传答案</text>
            </view>
          </view>
          <text class="card-status use-now">立即使用</text>
        </view>

        <view class="card" @click="handleToolClick('career-assessment')">
          <view class="card-left">
            <text class="card-icon">🎯</text>
            <view class="card-info">
              <text class="card-title">霍兰德职业兴趣</text>
              <text class="card-desc">RIASEC 六维测评,找到优势领域</text>
              <text class="card-tag">60 题标准问卷</text>
            </view>
          </view>
          <text class="card-status use-now">立即使用</text>
        </view>
      </view>

      <view class="section">
        <text class="section-subtitle">精算收入</text>
        <text class="section-title">薪酬与税务</text>
        <view class="card" @click="handleToolClick('tax-calculator')">
          <view class="card-left">
            <text class="card-icon">🧮</text>
            <view class="card-info">
              <text class="card-title">个人所得税计算</text>
              <text class="card-desc">2024 税率表,实时估算税后收入</text>
              <text class="card-tag">支持专项附加扣除</text>
            </view>
          </view>
          <text class="card-status use-now">立即使用</text>
        </view>

      </view>

      <view class="section">
        <text class="section-subtitle">备战面试</text>
        <text class="section-title">求职工具</text>
        <view class="card" @click="handleToolClick('interview-questions')">
          <view class="card-left">
            <text class="card-icon">💬</text>
            <view class="card-info">
              <text class="card-title">面试题库助手</text>
              <text class="card-desc">行为、情景与 HR 高频题一目了然</text>
              <text class="card-tag">含答题思路与复盘提示</text>
            </view>
          </view>
          <text class="card-status use-now">立即使用</text>
        </view>
      </view>

      <view class="section">
        <text class="section-subtitle">掌控节奏</text>
        <text class="section-title">效率与身心</text>
        <view class="card" @click="handleToolClick('time-matrix')">
          <view class="card-left">
            <text class="card-icon">⏱️</text>
            <view class="card-info">
              <text class="card-title">艾森豪威尔时间矩阵</text>
              <text class="card-desc">划分四象限,排定每日优先级</text>
              <text class="card-tag">支持长按删除与分类</text>
            </view>
          </view>
          <text class="card-status use-now">立即使用</text>
        </view>

        <view class="card" @click="handleToolClick('stress-assessment')">
          <view class="card-left">
            <text class="card-icon">🌿</text>
            <view class="card-info">
              <text class="card-title">职场压力自测</text>
              <text class="card-desc">20 题了解压力水平,获取减压建议</text>
              <text class="card-tag">涵盖情绪、睡眠与支持度</text>
            </view>
          </view>
          <text class="card-status use-now">立即使用</text>
        </view>
      </view>

      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      isRefreshing: false
    };
  },
  onLoad() {
    const info = uni.getSystemInfoSync();
    this.statusBarHeight = info.statusBarHeight || 0;
  },
  methods: {
    handleToolClick(toolType) {
      const toolRoutes = {
        mbti: '/pages/tools/mbti-test/mbti-test',
        'career-assessment': '/pages/tools/career-assessment/career-assessment',
        'tax-calculator': '/pages/tools/tax-calculator/tax-calculator',
        'interview-questions': '/pages/tools/interview-questions/interview-questions',
        'time-matrix': '/pages/tools/time-matrix/time-matrix',
        'stress-assessment': '/pages/tools/stress-assessment/stress-assessment'
      };

      if (toolRoutes[toolType]) {
        uni.navigateTo({ url: toolRoutes[toolType] });
        return;
      }

      const toolNames = {};

      const name = toolNames[toolType] || '该功能';
      this.showComingSoon(name);
    },
    showComingSoon(name) {
      uni.showModal({
        title: '即将上线',
        content: `${name}正在筹备中,我们正在接入最新数据源,敬请期待。`,
        showCancel: false,
        confirmText: '知道了'
      });
    },
    showMoreTools() {
      uni.showActionSheet({
        itemList: ['我要提需求', '意见反馈', '关于工具'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.goToFeedback('工具需求');
          } else if (res.tapIndex === 1) {
            this.goToFeedback('意见反馈');
          } else if (res.tapIndex === 2) {
            uni.showModal({
              title: '关于工具',
              content: 'Herizon 正逐步上线更多职场工具,欢迎随时告诉我们最想要的能力。',
              showCancel: false,
              confirmText: '了解'
            });
          }
        }
      });
    },
    goToFeedback(type) {
      uni.navigateTo({
        url: `/pages/feedback/feedback?type=${encodeURIComponent(type)}`
      });
    },
    refreshTools() {
      this.isRefreshing = true;
      setTimeout(() => {
        this.isRefreshing = false;
      }, 500);
    }
  }
};
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
}

.nav-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.nav-actions {
  display: flex;
  align-items: center;
}

.more-icon {
  font-size: 22px;
  color: #6b7280;
  padding: 6px 10px;
}

.scroll {
  flex: 1;
  padding: 20px 24px 32px;
  box-sizing: border-box;
}

.section {
  margin-bottom: 32px;
}

.section-subtitle {
  font-size: 14px;
  color: #6b7280;
  letter-spacing: 1px;
}

.section-title {
  display: block;
  margin: 6px 0 16px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 20px;
  margin-bottom: 16px;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.card-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
}

.card-icon {
  font-size: 28px;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.card-desc {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
}

.card-tag {
  font-size: 12px;
  color: #6d28d9;
}

.card-status {
  min-width: 96px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 12px;
  border-radius: 999px;
}

.card-status.use-now {
  color: #047857;
  background: #d1fae5;
}

.card-status.coming-soon {
  color: #92400e;
  background: #fef3c7;
}

.bottom-space {
  height: 48px;
}
</style>
