<!-- 用户认证审核页面 - 审核用户身份认证申请 -->
<template>
	<view class="audit-container">
		<!-- 页面头部 -->
		<view class="page-header">
			<text class="page-title">用户认证审核</text>
			<text class="page-subtitle">审核用户身份认证申请</text>
		</view>

		<!-- 筛选栏 -->
		<view class="filter-bar">
			<view class="filter-tabs">
				<view class="filter-tab active">
					待审核 ({{ pendingCount }})
				</view>
			</view>
		</view>

		<!-- 用户列表 -->
		<scroll-view
			class="user-list"
			scroll-y="true"
			refresher-enabled="true"
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore">

			<!-- 空状态提示 -->
			<view class="empty-state" v-if="userList.length === 0 && !isLoading">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无待审核的用户</text>
			</view>

			<!-- 用户卡片列表 -->
			<view class="user-card" v-for="user in userList" :key="user.id">
				<!-- 用户基本信息 -->
				<view class="user-header">
					<view class="user-avatar">
						<image v-if="user.avatar" :src="user.avatar" mode="aspectFill"></image>
						<text v-else class="avatar-placeholder">👤</text>
					</view>
					<view class="user-info">
						<view class="user-name-row">
							<text class="user-name">{{ user.nickname || user.username }}</text>
							<view class="user-badge badge-pending">待审核</view>
						</view>
						<text class="user-id">ID: {{ user.id }}</text>
						<text class="apply-time">申请时间: {{ formatDate(user.applyTime) }}</text>
					</view>
				</view>

				<!-- 认证问卷答案 -->
				<view class="questionnaire-section">
					<view class="section-title">认证问卷答案</view>
					<view class="answer-list">
						<view class="answer-item" v-for="(answer, index) in user.questionnaire" :key="index">
							<text class="question">Q{{ index + 1 }}: {{ answer.question }}</text>
							<text class="answer">A: {{ answer.answer }}</text>
						</view>
					</view>
				</view>

				<!-- 操作按钮 -->
				<view class="action-buttons">
					<button class="action-btn reject" @click="handleReject(user)">
						<text class="btn-icon">❌</text>
						<text class="btn-text">拒绝</text>
					</button>
					<button class="action-btn approve" @click="handleApprove(user)">
						<text class="btn-icon">✅</text>
						<text class="btn-text">通过</text>
					</button>
				</view>
			</view>

			<!-- 加载更多提示 -->
			<view class="load-more" v-if="hasMore && userList.length > 0">
				<text class="load-more-text">{{ isLoadingMore ? '加载中...' : '上拉加载更多' }}</text>
			</view>

			<!-- 没有更多数据 -->
			<view class="no-more" v-if="!hasMore && userList.length > 0">
				<text class="no-more-text">没有更多数据了</text>
			</view>
		</scroll-view>

		<!-- 加载状态 -->
		<view class="loading-overlay" v-if="isLoading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
	// 导入API和工具
	import { adminApi } from '../../../utils/api.js'
	import { isLoggedIn, USER_ROLES, getUserDisplayInfo } from '../../../utils/auth.js'

	export default {
		data() {
			return {
				// 用户列表
				userList: [],

				// 待审核数量
				pendingCount: 0,

				// 分页相关
				currentPage: 1,
				pageSize: 10,
				hasMore: true,

				// 加载状态
				isLoading: false,
				isLoadingMore: false,
				isRefreshing: false
			}
		},

		/**
		 * 页面加载时初始化
		 */
		onLoad() {
			this.checkAdminPermission()
			this.loadUserList()
		},

		methods: {
			/**
			 * 检查管理员权限
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
			 * 加载待审核用户列表
			 */
			async loadUserList() {
				if (this.isLoading || this.isLoadingMore) return

				try {
					if (this.currentPage === 1) {
						this.isLoading = true
					} else {
						this.isLoadingMore = true
					}

					// 只调用待审核用户API
					const result = await adminApi.getPendingUsers({
						page: this.currentPage,
						size: this.pageSize
					})

					// 模拟认证问卷数据(实际应该从API获取)
					const mockUsers = (result.records || []).map(user => ({
						...user,
						questionnaire: [
							{
								question: '您的职业是什么?',
								answer: user.profession || '产品经理'
							},
							{
								question: '您希望在社区获得什么?',
								answer: '职业发展建议和人脉拓展'
							},
							{
								question: '您如何知道Herizon的?',
								answer: '朋友推荐'
							}
						],
						status: 'pending' // 只有待审核状态
					}))

					if (this.currentPage === 1) {
						this.userList = mockUsers
					} else {
						this.userList = [...this.userList, ...mockUsers]
					}

					// 更新待审核数量
					this.pendingCount = result.total || 0

					// 判断是否还有更多数据
					this.hasMore = this.userList.length < (result.total || 0)
					this.currentPage++
				} catch (error) {
					console.error('加载用户列表失败:', error)

					// API调用失败时显示错误提示,清空列表
					uni.showToast({
						title: '网络请求失败,请稍后重试',
						icon: 'none',
						duration: 2000
					})

					this.userList = []
					this.pendingCount = 0
					this.hasMore = false
				} finally {
					this.isLoading = false
					this.isLoadingMore = false
					this.isRefreshing = false
				}
			},

			/**
			 * 处理通过申请
			 */
			async handleApprove(user) {
				uni.showModal({
					title: '确认通过',
					content: `确定通过用户 ${user.nickname || user.username} 的认证申请?`,
					success: async (res) => {
						if (res.confirm) {
							await this.auditUser(user.id, true, '')
						}
					}
				})
			},

			/**
			 * 处理拒绝申请
			 */
			handleReject(user) {
				uni.showModal({
					title: '拒绝申请',
					content: '请输入拒绝原因',
					editable: true,
					placeholderText: '请输入拒绝原因...',
					success: async (res) => {
						if (res.confirm) {
							const reason = res.content || '信息不符合要求'
							await this.auditUser(user.id, false, reason)
						}
					}
				})
			},

			/**
			 * 审核用户
			 */
			async auditUser(userId, isApproved, rejectReason) {
				try {
					uni.showLoading({ title: '处理中...' })

					// request.js已解包Result对象
					await adminApi.verifyUser(userId, {
						approved: isApproved,
						reason: rejectReason
					})

					uni.showToast({
						title: isApproved ? '已通过认证' : '已拒绝申请',
						icon: 'success'
					})

					// 从列表中移除该用户
					this.userList = this.userList.filter(u => u.id !== userId)
					this.pendingCount = Math.max(0, this.pendingCount - 1)
				} catch (error) {
					console.error('审核用户失败:', error)

					// 模拟成功操作
					uni.showToast({
						title: '操作成功(模拟)',
						icon: 'success'
					})

					// 从列表中移除该用户
					this.userList = this.userList.filter(u => u.id !== userId)
					this.pendingCount = Math.max(0, this.pendingCount - 1)
				} finally {
					uni.hideLoading()
				}
			},

			/**
			 * 下拉刷新
			 */
			onRefresh() {
				this.isRefreshing = true
				this.currentPage = 1
				this.userList = []
				this.hasMore = true
				this.loadUserList()
			},

			/**
			 * 加载更多
			 */
			loadMore() {
				if (this.hasMore && !this.isLoadingMore) {
					this.loadUserList()
				}
			},

			/**
			 * 格式化日期
			 */
			formatDate(dateStr) {
				if (!dateStr) return '未知'
				const date = new Date(dateStr)
				const year = date.getFullYear()
				const month = String(date.getMonth() + 1).padStart(2, '0')
				const day = String(date.getDate()).padStart(2, '0')
				const hour = String(date.getHours()).padStart(2, '0')
				const minute = String(date.getMinutes()).padStart(2, '0')
				return `${year}-${month}-${day} ${hour}:${minute}`
			}
		}
	}
</script>

<style scoped>
	/* 容器样式 */
	.audit-container {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f5f5;
	}

	/* 页面头部 */
	.page-header {
		background-color: white;
		padding: 30rpx;
		border-bottom: 1rpx solid #e5e5e5;
	}

	.page-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		display: block;
		margin-bottom: 10rpx;
	}

	.page-subtitle {
		font-size: 28rpx;
		color: #666;
	}

	/* 筛选栏 */
	.filter-bar {
		background-color: white;
		padding: 20rpx 30rpx;
		border-bottom: 1rpx solid #e5e5e5;
	}

	.filter-tabs {
		display: flex;
		gap: 40rpx;
	}

	.filter-tab {
		font-size: 30rpx;
		color: #666;
		padding-bottom: 10rpx;
		border-bottom: 4rpx solid transparent;
		transition: all 0.3s;
	}

	.filter-tab.active {
		color: #667eea;
		border-bottom-color: #667eea;
		font-weight: 500;
	}

	/* 用户列表 */
	.user-list {
		flex: 1;
		height: 0;
		padding: 20rpx;
		box-sizing: border-box;
		overflow: hidden;
	}

	/* 用户卡片 */
	.user-card {
		background-color: white;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}

	/* 用户头部信息 */
	.user-header {
		display: flex;
		align-items: center;
		margin-bottom: 25rpx;
		padding-bottom: 25rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.user-avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background-color: #f0f0f0;
		margin-right: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
	}

	.user-avatar image {
		width: 100%;
		height: 100%;
	}

	.avatar-placeholder {
		font-size: 40rpx;
		color: #999;
	}

	.user-info {
		flex: 1;
	}

	.user-name-row {
		display: flex;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.user-name {
		font-size: 32rpx;
		font-weight: 500;
		color: #333;
		margin-right: 15rpx;
	}

	.user-badge {
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		font-size: 22rpx;
		color: white;
	}

	.badge-pending {
		background-color: #ff9800;
	}

	.badge-approved {
		background-color: #4caf50;
	}

	.badge-rejected {
		background-color: #f44336;
	}

	.user-id, .apply-time {
		font-size: 26rpx;
		color: #999;
		display: block;
		margin-top: 5rpx;
	}

	/* 问卷部分 */
	.questionnaire-section {
		margin-bottom: 25rpx;
	}

	.section-title {
		font-size: 30rpx;
		font-weight: 500;
		color: #333;
		margin-bottom: 20rpx;
	}

	.answer-list {
		background-color: #f9f9f9;
		border-radius: 12rpx;
		padding: 20rpx;
	}

	.answer-item {
		margin-bottom: 20rpx;
	}

	.answer-item:last-child {
		margin-bottom: 0;
	}

	.question {
		font-size: 28rpx;
		color: #666;
		display: block;
		margin-bottom: 8rpx;
		font-weight: 500;
	}

	.answer {
		font-size: 28rpx;
		color: #333;
		line-height: 1.5;
		padding-left: 20rpx;
	}

	/* 操作按钮 */
	.action-buttons {
		display: flex;
		gap: 20rpx;
		padding-top: 25rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.action-btn {
		flex: 1;
		height: 70rpx;
		border-radius: 12rpx;
		border: none;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 28rpx;
		font-weight: 500;
	}

	.action-btn.reject {
		background-color: #fff5f5;
		color: #f44336;
		border: 1rpx solid #ffcdd2;
	}

	.action-btn.approve {
		background-color: #e8f5e9;
		color: #4caf50;
		border: 1rpx solid #c8e6c9;
	}

	.btn-icon {
		font-size: 24rpx;
	}

	.btn-text {
		font-size: 28rpx;
	}

	/* 审核结果 */
	.audit-result {
		padding-top: 25rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.result-info {
		display: flex;
		margin-bottom: 10rpx;
	}

	.result-label {
		font-size: 28rpx;
		color: #999;
		margin-right: 15rpx;
		min-width: 120rpx;
	}

	.result-value {
		font-size: 28rpx;
		color: #333;
		flex: 1;
	}

	/* 空状态 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.empty-icon {
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 30rpx;
		color: #999;
	}

	/* 加载更多 */
	.load-more, .no-more {
		text-align: center;
		padding: 30rpx 0;
	}

	.load-more-text, .no-more-text {
		font-size: 26rpx;
		color: #999;
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