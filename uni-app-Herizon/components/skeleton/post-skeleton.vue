<template>
	<!--
		帖子骨架屏组件
		用于:帖子列表加载中的占位显示
		特点:带有shimmer动画效果,提升加载体验
	-->
	<view class="post-skeleton">
		<!-- 用户信息栏 -->
		<view class="skeleton-header">
			<view class="skeleton-avatar shimmer"></view>
			<view class="skeleton-user-info">
				<view class="skeleton-line short shimmer"></view>
				<view class="skeleton-line shorter shimmer"></view>
			</view>
		</view>

		<!-- 帖子内容 -->
		<view class="skeleton-content">
			<view class="skeleton-line shimmer"></view>
			<view class="skeleton-line shimmer"></view>
			<view class="skeleton-line medium shimmer"></view>
		</view>

		<!-- 图片占位 -->
		<view class="skeleton-image shimmer" v-if="showImage"></view>

		<!-- 互动栏 -->
		<view class="skeleton-footer">
			<view class="skeleton-stat shimmer"></view>
			<view class="skeleton-stat shimmer"></view>
			<view class="skeleton-stat shimmer"></view>
		</view>
	</view>
</template>

<script>
/**
 * PostSkeleton 帖子骨架屏组件
 *
 * @description 用于帖子加载时的占位显示,带有shimmer动画效果
 * @property {Boolean} showImage - 是否显示图片占位,默认true
 */
export default {
	name: 'PostSkeleton',
	props: {
		// 是否显示图片占位
		showImage: {
			type: Boolean,
			default: true
		}
	}
}
</script>

<style lang="scss" scoped>
/* 帖子骨架屏容器 */
.post-skeleton {
	background-color: #fff;
	padding: 30rpx;
	margin: 0 20rpx 20rpx 20rpx;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

/* 头部区域 */
.skeleton-header {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

/* 头像占位 */
.skeleton-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background: #f0f0f0;
	margin-right: 20rpx;
	flex-shrink: 0;
}

/* 用户信息占位 */
.skeleton-user-info {
	flex: 1;
}

/* 通用线条占位 */
.skeleton-line {
	height: 28rpx;
	background: #f0f0f0;
	border-radius: 4rpx;
	margin-bottom: 12rpx;
}

.skeleton-line.short {
	width: 40%;
}

.skeleton-line.medium {
	width: 60%;
}

.skeleton-line.shorter {
	width: 25%;
}

/* 内容区域 */
.skeleton-content {
	margin-bottom: 20rpx;
}

/* 图片占位 */
.skeleton-image {
	width: 100%;
	height: 300rpx;
	background: #f0f0f0;
	border-radius: 8rpx;
	margin-bottom: 20rpx;
}

/* 底部互动栏 */
.skeleton-footer {
	display: flex;
	align-items: center;
	gap: 40rpx;
}

/* 互动数据占位 */
.skeleton-stat {
	width: 80rpx;
	height: 28rpx;
	background: #f0f0f0;
	border-radius: 4rpx;
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
