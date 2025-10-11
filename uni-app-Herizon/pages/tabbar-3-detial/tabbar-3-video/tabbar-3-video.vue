<template>
  <view class="page-container">
    <view class="notice-card">
      <text class="notice-title">视频发布暂未开放</text>
      <text class="notice-desc">感谢关注,视频发布功能正在迭代排期中。如需发布内容,请使用图文或问答入口。</text>
    </view>
  </view>
</template>

<script>
export default {
  navigationBarTitleText: '发布视频',
  onLoad() {
    console.log('暂存的视频发布占位页加载')
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  padding: 40rpx;
  box-sizing: border-box;
}

.notice-card {
  background: #ffffff;
  padding: 48rpx 40rpx;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(15, 35, 95, 0.08);
  text-align: center;
}

.notice-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2933;
  margin-bottom: 16rpx;
}

.notice-desc {
  font-size: 28rpx;
  color: #52606d;
  line-height: 1.6;
}
</style>
