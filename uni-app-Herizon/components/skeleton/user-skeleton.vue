<template>
	<!--
		用户卡片骨架屏组件
		用于:用户列表、关注/粉丝列表等加载占位
	-->
	<view class="user-skeleton">
		<!-- 用户头像 -->
		<view class="skeleton-avatar shimmer"></view>

		<!-- 用户信息 -->
		<view class="skeleton-info">
			<view class="skeleton-line name shimmer"></view>
			<view class="skeleton-line bio shimmer"></view>
		</view>

		<!-- 操作按钮占位 -->
		<view class="skeleton-button shimmer" v-if="showButton"></view>
	</view>
</template>

<script>
/**
 * UserSkeleton 用户卡片骨架屏组件
 *
 * @description 用于用户列表加载时的占位显示
 * @property {Boolean} showButton - 是否显示按钮占位,默认true
 */
export default {
	name: 'UserSkeleton',
	props: {
		// 是否显示按钮占位
		showButton: {
			type: Boolean,
			default: true
		}
	}
}
</script>

<style lang="scss" scoped>
/* 用户骨架屏容器 */
.user-skeleton {
	display: flex;
	align-items: center;
	padding: 30rpx;
	background-color: #fff;
	margin-bottom: 20rpx;
}

/* 头像占位 */
.skeleton-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	background: #f0f0f0;
	margin-right: 24rpx;
	flex-shrink: 0;
}

/* 信息区域 */
.skeleton-info {
	flex: 1;
}

/* 线条占位 */
.skeleton-line {
	height: 28rpx;
	background: #f0f0f0;
	border-radius: 4rpx;
}

.skeleton-line.name {
	width: 50%;
	margin-bottom: 16rpx;
}

.skeleton-line.bio {
	width: 70%;
}

/* 按钮占位 */
.skeleton-button {
	width: 120rpx;
	height: 60rpx;
	background: #f0f0f0;
	border-radius: 30rpx;
}

/* Shimmer动画效果 */
.shimmer {
	background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: shimmer 1.5s infinite ease-in-out;
}

@keyframes shimmer {
	0% {
		background-position: 200% 0;
	}
	100% {
		background-position: -200% 0;
	}
}
</style>
