<!-- 用户管理页面 - 管理员可提升或删除用户 -->
<template>
	<view class="management-container">
		<view class="page-header">
			<text class="page-title">用户管理</text>
			<text class="page-subtitle">查看并管理平台所有用户</text>
		</view>

		<scroll-view
			class="user-list"
			scroll-y="true"
			refresher-enabled="true"
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore">

			<view class="empty-state" v-if="!isLoading && userList.length === 0">
				<text class="empty-icon">🧑‍🤝‍🧑</text>
				<text class="empty-text">暂无用户数据</text>
			</view>

			<view class="user-card" v-for="user in userList" :key="user.id">
				<view class="card-header">
					<view class="avatar">
						<image v-if="user.avatar" :src="user.avatar" mode="aspectFill"></image>
						<text v-else class="avatar-placeholder">👤</text>
					</view>
					<view class="info">
						<view class="name-row">
							<text class="name">{{ user.nickname || user.username || '未设置昵称' }}</text>
							<view :class="getRoleTagClass(user.role)">
								{{ getRoleLabel(user.role) }}
							</view>
						</view>
						<text class="meta-text">ID：{{ user.id }}</text>
						<text class="meta-text" v-if="user.email">邮箱：{{ user.email }}</text>
						<text class="meta-text">注册时间：{{ formatDate(user.createdAt) }}</text>
					</view>
				</view>

				<view class="card-footer">
					<button
						class="action-btn promote"
						v-if="user.role !== USER_ROLES.ADMIN"
						@click="handlePromote(user)">
						<text class="btn-icon">⭐</text>
						<text class="btn-text">设为管理员</text>
					</button>
					<button
						class="action-btn delete"
						:class="{ disabled: user.id === currentAdminId }"
						:disabled="user.id === currentAdminId"
						@click="handleDelete(user)">
						<text class="btn-icon">🗑️</text>
						<text class="btn-text">{{ user.id === currentAdminId ? '无法删除' : '删除用户' }}</text>
					</button>
				</view>

				<view class="self-hint" v-if="user.id === currentAdminId">
					<text>不能删除当前登录的管理员账号</text>
				</view>
			</view>

			<view class="load-more" v-if="hasMore && userList.length > 0">
				<text class="load-more-text">{{ isLoadingMore ? '加载中...' : '上拉加载更多' }}</text>
			</view>

			<view class="no-more" v-if="!hasMore && userList.length > 0">
				<text class="no-more-text">没有更多用户了</text>
			</view>
		</scroll-view>

		<view class="loading-overlay" v-if="isLoading && userList.length === 0">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
	import { adminApi } from '../../../utils/api.js'
	import { isLoggedIn, USER_ROLES, getUserDisplayInfo } from '../../../utils/auth.js'

	const ROLE_LABELS = {
		[USER_ROLES.TRIAL]: '体验用户',
		[USER_ROLES.VERIFIED]: '正式用户',
		[USER_ROLES.ADMIN]: '管理员'
	}

	export default {
		data() {
			return {
				USER_ROLES,
				userList: [],
				currentPage: 1,
				pageSize: 10,
				totalPages: 1,
				hasMore: true,
				isLoading: false,
				isLoadingMore: false,
				isRefreshing: false,
				currentAdminId: null
			}
		},

		onLoad() {
			this.initializePage()
		},

		onShow() {
			this.initializeAdminId()
		},

		methods: {
			initializePage() {
				if (!this.checkAdminPermission()) {
					return
				}
				this.initializeAdminId()
				this.fetchUsers(true)
			},

			initializeAdminId() {
				const info = getUserDisplayInfo()
				this.currentAdminId = info ? info.id : null
			},

			checkAdminPermission() {
				if (!isLoggedIn()) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					uni.redirectTo({
						url: '/pages/login/login'
					})
					return false
				}

				const userInfo = getUserDisplayInfo()
				if (!userInfo || userInfo.role !== USER_ROLES.ADMIN) {
					uni.showToast({
						title: '权限不足',
						icon: 'none'
					})
					uni.navigateBack()
					return false
				}
				return true
			},

			async fetchUsers(reset = false) {
				if (this.isLoading || this.isLoadingMore) {
					return
				}

				if (reset) {
					this.currentPage = 1
					this.totalPages = 1
					this.hasMore = true
					this.userList = []
				}

				const isLoadMore = !reset && this.currentPage > 1
				if (isLoadMore) {
					this.isLoadingMore = true
				} else if (!this.isRefreshing) {
					this.isLoading = true
				}

				try {
					const result = await adminApi.getAllUsers({
						current: this.currentPage,
						size: this.pageSize
					})

					const records = result.records || []
					if (this.currentPage === 1) {
						this.userList = records
					} else {
						this.userList = [...this.userList, ...records]
					}

					const total = result.total || 0
					this.totalPages = Math.max(1, Math.ceil(total / this.pageSize))
					this.hasMore = this.currentPage < this.totalPages
				} catch (error) {
					console.error('加载用户列表失败:', error)
					uni.showToast({
						title: '加载失败,请稍后重试',
						icon: 'none'
					})
					if (this.currentPage > 1) {
						this.currentPage -= 1
					}
				} finally {
					this.isLoading = false
					this.isLoadingMore = false
					if (this.isRefreshing) {
						this.isRefreshing = false
					}
				}
			},

			async loadMore() {
				if (!this.hasMore || this.isLoadingMore) {
					return
				}
				this.currentPage += 1
				await this.fetchUsers()
			},

			async onRefresh() {
				if (this.isRefreshing) {
					return
				}
				this.isRefreshing = true
				await this.fetchUsers(true)
			},

			getRoleLabel(role) {
				return ROLE_LABELS[role] || '未知'
			},

			getRoleTagClass(role) {
				switch (role) {
					case USER_ROLES.ADMIN:
						return 'role-tag admin'
					case USER_ROLES.VERIFIED:
						return 'role-tag verified'
					default:
						return 'role-tag trial'
				}
			},

			formatDate(value) {
				if (!value) {
					return '—'
				}
				const date = typeof value === 'string'
					? new Date(value.replace(' ', 'T'))
					: new Date(value)
				if (Number.isNaN(date.getTime())) {
					return value
				}
				const y = date.getFullYear()
				const m = String(date.getMonth() + 1).padStart(2, '0')
				const d = String(date.getDate()).padStart(2, '0')
				return `${y}-${m}-${d}`
			},

			async handlePromote(user) {
				if (user.role === USER_ROLES.ADMIN) {
					return
				}

				const confirmed = await this.showConfirm('确定将该用户设为管理员吗？')
				if (!confirmed) {
					return
				}

				try {
					await adminApi.promoteUser(user.id)
					user.role = USER_ROLES.ADMIN
					user.roleDescription = this.getRoleLabel(USER_ROLES.ADMIN)
					uni.showToast({
						title: '已设为管理员',
						icon: 'success'
					})
				} catch (error) {
					console.error('提升管理员失败:', error)
					uni.showToast({
						title: error.message || '操作失败',
						icon: 'none'
					})
				}
			},

			async handleDelete(user) {
				if (user.id === this.currentAdminId) {
					uni.showToast({
						title: '不能删除自己',
						icon: 'none'
					})
					return
				}

				const confirmed = await this.showConfirm('删除后该用户将无法登录，确定删除吗？')
				if (!confirmed) {
					return
				}

				try {
					await adminApi.deleteUser(user.id)
					this.userList = this.userList.filter(item => item.id !== user.id)
					uni.showToast({
						title: '用户已删除',
						icon: 'success'
					})

					if (this.userList.length === 0 && this.hasMore) {
						await this.fetchUsers()
					}
				} catch (error) {
					console.error('删除用户失败:', error)
					uni.showToast({
						title: error.message || '删除失败',
						icon: 'none'
					})
				}
			},

			showConfirm(message) {
				return new Promise((resolve) => {
					uni.showModal({
						title: '确认操作',
						content: message,
						confirmColor: '#d63031',
						success: (res) => resolve(res.confirm),
						fail: () => resolve(false)
					})
				})
			}
		}
	}
</script>

<style scoped>
	.management-container {
		min-height: 100vh;
		background: #f5f6fa;
		padding: 30rpx 20rpx 40rpx;
		box-sizing: border-box;
	}

	.page-header {
		margin-bottom: 30rpx;
	}

	.page-title {
		font-size: 40rpx;
		font-weight: 700;
		color: #1f2f4a;
		display: block;
		margin-bottom: 8rpx;
	}

	.page-subtitle {
		font-size: 28rpx;
		color: #778bad;
	}

	.user-list {
		max-height: calc(100vh - 200rpx);
	}

	.user-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 24rpx 28rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 6rpx 18rpx rgba(31, 47, 74, 0.06);
	}

	.card-header {
		display: flex;
		gap: 20rpx;
		margin-bottom: 20rpx;
	}

	.avatar {
		width: 90rpx;
		height: 90rpx;
		border-radius: 50%;
		overflow: hidden;
		background: #f0f4ff;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 40rpx;
	}

	.avatar image {
		width: 100%;
		height: 100%;
	}

	.avatar-placeholder {
		color: #5c6c8c;
	}

	.info {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}

	.name-row {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex-wrap: wrap;
	}

	.name {
		font-size: 34rpx;
		font-weight: 600;
		color: #1f2f4a;
		max-width: 400rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.role-tag {
		padding: 6rpx 16rpx;
		border-radius: 12rpx;
		font-size: 24rpx;
		font-weight: 500;
	}

	.role-tag.trial {
		background: rgba(102, 126, 234, 0.12);
		color: #667eea;
	}

	.role-tag.verified {
		background: rgba(46, 213, 115, 0.12);
		color: #2ed573;
	}

	.role-tag.admin {
		background: rgba(255, 99, 72, 0.12);
		color: #ff6348;
	}

	.meta-text {
		font-size: 26rpx;
		color: #697a98;
	}

	.card-footer {
		display: flex;
		gap: 20rpx;
	}

	.action-btn {
		flex: 1;
		height: 72rpx;
		border-radius: 14rpx;
		border: none;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		font-size: 28rpx;
		font-weight: 500;
	}

	.action-btn.promote {
		background: #f0f4ff;
		color: #3353d7;
	}

	.action-btn.delete {
		background: #fff5f5;
		color: #d63031;
		border: 1rpx solid rgba(214, 48, 49, 0.3);
	}

	.action-btn.delete.disabled {
		opacity: 0.5;
	}

	.btn-icon {
		font-size: 30rpx;
	}

	.btn-text {
		font-size: 28rpx;
	}

	.self-hint {
		margin-top: 16rpx;
		font-size: 24rpx;
		color: #a0aec0;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 0;
		color: #8f9bb3;
	}

	.empty-icon {
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 28rpx;
	}

	.load-more,
	.no-more {
		text-align: center;
		padding: 30rpx 0;
		color: #8f9bb3;
		font-size: 26rpx;
	}

	.loading-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.15);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.loading-text {
		background: #fff;
		padding: 32rpx 60rpx;
		border-radius: 18rpx;
		font-size: 28rpx;
		color: #1f2f4a;
	}
</style>
