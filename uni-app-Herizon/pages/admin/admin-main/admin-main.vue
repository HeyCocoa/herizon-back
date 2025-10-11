<!-- 管理员主页面 - 管理功能导航入口 -->
<template>
	<!-- 管理员主页面容器 -->
	<view class="admin-container">
		<!-- 页面标题头部 -->
		<view class="admin-header">
			<view class="header-content">
				<view class="title-section">
					<text class="main-title">管理后台</text>
					<text class="sub-title">Herizon 社区管理中心</text>
				</view>
				<view class="admin-avatar">
					<text class="admin-icon">👑</text>
				</view>
			</view>
		</view>

		<!-- 统计概览卡片 -->
		<view class="stats-overview">
			<view class="overview-title">待处理概览</view>
			<view class="stats-grid">
				<view class="stat-card pending">
					<view class="stat-icon">👥</view>
					<view class="stat-info">
						<text class="stat-number">{{ statsData.pendingUsers }}</text>
						<text class="stat-label">待审核用户</text>
					</view>
				</view>
				<view class="stat-card tags">
					<view class="stat-icon">🏷️</view>
					<view class="stat-info">
						<text class="stat-number">{{ statsData.totalTags }}</text>
						<text class="stat-label">标签总数</text>
					</view>
				</view>
				<view class="stat-card posts">
					<view class="stat-icon">📝</view>
					<view class="stat-info">
						<text class="stat-number">{{ statsData.totalPosts }}</text>
						<text class="stat-label">帖子总数</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 管理功能菜单 -->
		<view class="admin-menu">
			<view class="menu-title">管理功能</view>
			<view class="menu-list">
				<!-- 用户认证审核 -->
				<view class="menu-item priority-high" @click="goToUserAudit">
					<view class="menu-icon-wrapper">
						<text class="menu-icon">👤</text>
						<view class="badge" v-if="statsData.pendingUsers > 0">{{ statsData.pendingUsers }}</view>
					</view>
					<view class="menu-content">
						<text class="menu-text">用户认证审核</text>
						<text class="menu-desc">审核用户身份认证申请</text>
					</view>
					<view class="menu-arrow">→</view>
				</view>

				<!-- 用户管理 -->
				<view class="menu-item priority-normal" @click="goToUserManagement">
					<view class="menu-icon-wrapper">
						<text class="menu-icon">🧑‍💼</text>
					</view>
					<view class="menu-content">
						<text class="menu-text">用户管理</text>
						<text class="menu-desc">提升或删除用户账号</text>
					</view>
					<view class="menu-arrow">→</view>
				</view>

				<!-- 帖子管理 -->
				<view class="menu-item priority-normal" @click="goToPostManagement">
					<view class="menu-icon-wrapper">
						<text class="menu-icon">📝</text>
					</view>
					<view class="menu-content">
						<text class="menu-text">帖子管理</text>
						<text class="menu-desc">查看并删除所有帖子</text>
					</view>
					<view class="menu-arrow">→</view>
				</view>

				<!-- 标签管理 -->
				<view class="menu-item priority-normal" @click="goToTagManagement">
					<view class="menu-icon-wrapper">
						<text class="menu-icon">🏷️</text>
					</view>
					<view class="menu-content">
						<text class="menu-text">标签管理</text>
						<text class="menu-desc">创建、编辑和删除标签</text>
					</view>
					<view class="menu-arrow">→</view>
				</view>

				<!-- 用户反馈 -->
				<view class="menu-item priority-normal" @click="goToFeedbackManagement">
					<view class="menu-icon-wrapper">
						<text class="menu-icon">💬</text>
					</view>
					<view class="menu-content">
						<text class="menu-text">用户反馈</text>
						<text class="menu-desc">查看并回复用户反馈</text>
					</view>
					<view class="menu-arrow">→</view>
				</view>
			</view>
		</view>

		<!-- 快捷操作 -->
		<view class="quick-actions">
			<view class="action-title">快捷操作</view>
			<view class="action-buttons">
				<button class="action-btn primary" @click="refreshStats">
					<text class="btn-icon">🔄</text>
					<text class="btn-text">刷新数据</text>
				</button>
			</view>
		</view>

		<!-- 底部间距 -->
		<view class="bottom-space"></view>

		<!-- 加载状态 -->
		<view class="loading-overlay" v-if="isLoading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
	// 导入API工具
	import { adminApi } from '../../../utils/api.js'
	import { isLoggedIn, USER_ROLES, getUserDisplayInfo } from '../../../utils/auth.js'

	export default {
		data() {
			return {
				// 统计数据(简化版 - 符合MVP原则)
				statsData: {
					pendingUsers: 0,    // 待审核用户数
					totalTags: 0,       // 标签总数
					totalPosts: 0       // 帖子总数
				},

				// 加载状态
				isLoading: false
			}
		},

		/**
		 * 页面加载时初始化
		 */
		onLoad() {
			this.checkAdminPermission()
			this.loadStatistics()
		},

		/**
		 * 页面显示时刷新数据
		 */
		onShow() {
			this.checkAdminPermission()
			this.refreshStats()
		},

		methods: {
			/**
			 * 检查管理员权限,非管理员用户重定向
			 */
			checkAdminPermission() {
				if (!isLoggedIn()) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					uni.redirectTo({
						url: '/pages/login/login'
					})
					return
				}

				const userInfo = getUserDisplayInfo()
				if (!userInfo || userInfo.role !== USER_ROLES.ADMIN) {
					uni.showToast({
						title: '权限不足',
						icon: 'none'
					})
					uni.navigateBack()
					return
				}
			},

			/**
			 * 加载统计数据(简化版 - 符合MVP原则)
			 * 使用真实API,失败时显示友好错误提示,不回退到Mock数据
			 *
			 * 简化说明(2025-10-02):
			 * 后端返回字段已简化为 { pendingUsers, totalTags, totalPosts }
			 */
			async loadStatistics() {
				try {
					this.isLoading = true

					// 调用真实API获取管理员统计数据
					// request.js已解包Result对象
					const result = await adminApi.getStatistics()

					// ✅ API调用成功:更新统计数据
					this.statsData = {
						pendingUsers: result.pendingUsers || 0,
						totalTags: result.totalTags || 0,
						totalPosts: result.totalPosts || 0
					}
				} catch (error) {
					// ❌ 网络错误:显示网络异常提示
					console.error('加载统计数据异常:', error)
					uni.showToast({
						title: '网络请求失败,请稍后重试',
						icon: 'none',
						duration: 2000
					})
					// 设置空数据状态,不使用Mock数据
					this.statsData = {
						pendingUsers: 0,
						totalTags: 0,
						totalPosts: 0
					}
				} finally {
					this.isLoading = false
				}
			},

			/**
			 * 刷新统计数据
			 */
			async refreshStats() {
				await this.loadStatistics()
				uni.showToast({
					title: '数据已刷新',
					icon: 'success'
				})
			},

			/**
			 * 跳转到用户认证审核页面
			 */
			goToUserAudit() {
				uni.navigateTo({
					url: '/pages/admin/user-audit/user-audit'
				})
			},

			/**
			 * 跳转到用户管理页面
			 */
			goToUserManagement() {
				uni.navigateTo({
					url: '/pages/admin/user-management/user-management'
				})
			},

			/**
			 * 跳转到帖子管理页面
			 */
			goToPostManagement() {
				uni.navigateTo({
					url: '/pages/admin/post-management/post-management'
				})
			},

			/**
			 * 跳转到用户反馈管理
			 */
			goToFeedbackManagement() {
				uni.navigateTo({
					url: '/pages/admin/feedback-management/feedback-management'
				})
			},

			/**
			 * 跳转到标签管理页面
			 */
			goToTagManagement() {
				uni.navigateTo({
					url: '/pages/admin/tag-management/tag-management'
				})
			}
		}
	}
</script>

<style scoped>
	/* 主容器样式 */
	.admin-container {
		min-height: 100vh;
		background-color: #f5f5f5;
	}

	/* 页面头部 */
	.admin-header {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		padding: 40rpx 30rpx 30rpx;
		color: white;
	}

	.header-content {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.title-section {
		flex: 1;
	}

	.main-title {
		font-size: 44rpx;
		font-weight: bold;
		display: block;
		margin-bottom: 8rpx;
	}

	.sub-title {
		font-size: 28rpx;
		opacity: 0.9;
	}

	.admin-avatar {
		width: 80rpx;
		height: 80rpx;
		background: rgba(255, 255, 255, 0.2);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.admin-icon {
		font-size: 40rpx;
	}

	/* 统计概览 */
	.stats-overview {
		margin: 30rpx 20rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx;
	}

	.overview-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 25rpx;
	}

	.stats-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 20rpx;
	}

	.stat-card {
		display: flex;
		align-items: center;
		padding: 25rpx 20rpx;
		border-radius: 16rpx;
		background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	}

	.stat-card.pending {
		background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
	}

	.stat-card.tags {
		background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
	}

	.stat-card.posts {
		background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
	}

	.stat-icon {
		font-size: 40rpx;
		margin-right: 15rpx;
	}

	.stat-info {
		display: flex;
		flex-direction: column;
	}

	.stat-number {
		font-size: 32rpx;
		font-weight: bold;
		color: white;
		line-height: 1.2;
	}

	.stat-label {
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.9);
	}

	/* 管理菜单 */
	.admin-menu {
		margin: 0 20rpx 30rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx 0;
	}

	.menu-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		padding: 0 30rpx 20rpx;
	}

	.menu-list {
		padding: 0;
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 25rpx 30rpx;
		border-bottom: 1rpx solid #f8f8f8;
		position: relative;
	}

	.menu-item:last-child {
		border-bottom: none;
	}

	.menu-item.priority-high {
		background: linear-gradient(90deg, rgba(255, 107, 107, 0.05), rgba(255, 107, 107, 0));
	}

	.menu-icon-wrapper {
		position: relative;
		margin-right: 20rpx;
	}

	.menu-icon {
		font-size: 44rpx;
		width: 60rpx;
		text-align: center;
	}

	.badge {
		position: absolute;
		top: -8rpx;
		right: -8rpx;
		background-color: #ff4757;
		color: white;
		font-size: 20rpx;
		padding: 4rpx 8rpx;
		border-radius: 12rpx;
		min-width: 24rpx;
		text-align: center;
		line-height: 1;
	}

	.menu-content {
		flex: 1;
	}

	.menu-text {
		font-size: 32rpx;
		color: #333;
		font-weight: 500;
		display: block;
		margin-bottom: 5rpx;
	}

	.menu-desc {
		font-size: 26rpx;
		color: #999;
	}

	.menu-arrow {
		font-size: 32rpx;
		color: #ccc;
		font-weight: bold;
	}

	/* 快捷操作 */
	.quick-actions {
		margin: 0 20rpx 30rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx;
	}

	.action-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 25rpx;
	}

	.action-buttons {
		display: flex;
		gap: 20rpx;
	}

	.action-btn {
		flex: 1;
		height: 80rpx;
		border-radius: 16rpx;
		border: none;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 28rpx;
	}

	.action-btn.primary {
		background-color: #667eea;
		color: white;
	}

	.action-btn.secondary {
		background-color: #f8f9fa;
		color: #666;
		border: 1rpx solid #e9ecef;
	}

	.btn-icon {
		font-size: 28rpx;
	}

	.btn-text {
		font-size: 28rpx;
	}

	/* 底部间距 */
	.bottom-space {
		height: 100rpx;
	}

	/* 加载状态 */
	.loading-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.3);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.loading-text {
		background: white;
		padding: 30rpx 60rpx;
		border-radius: 16rpx;
		font-size: 28rpx;
		color: #333;
	}
</style>
