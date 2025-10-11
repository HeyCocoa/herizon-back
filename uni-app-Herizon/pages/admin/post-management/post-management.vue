<!-- 帖子管理页面 - 管理员可查看并删除任意帖子 -->
<template>
	<view class="management-container">
		<view class="page-header">
			<text class="page-title">帖子管理</text>
			<text class="page-subtitle">全站帖子一览及删除操作</text>
		</view>

		<scroll-view
			class="post-list"
			scroll-y="true"
			refresher-enabled="true"
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore">

			<view class="empty-state" v-if="!isLoading && postList.length === 0">
				<text class="empty-icon">🗂️</text>
				<text class="empty-text">暂无帖子数据</text>
			</view>

			<view class="post-card" v-for="post in postList" :key="post.id">
				<view class="card-header">
					<text class="title">{{ post.title || '未填写标题' }}</text>
					<view :class="getRoleTagClass(post.userRole)">
						{{ getRoleLabel(post.userRole) }}
					</view>
				</view>

				<view class="meta-row">
					<text class="meta-text">作者：{{ post.nickname || post.username || '未知用户' }}</text>
					<text class="meta-text">帖子ID：{{ post.id }}</text>
				</view>

				<view class="meta-row">
					<text class="meta-text">创建时间：{{ formatDate(post.createdAt) }}</text>
					<text class="meta-text">浏览：{{ post.viewCount || 0 }} · 评论：{{ post.commentCount || 0 }} · 点赞：{{ post.likeCount || 0 }}</text>
				</view>

				<view class="content-preview">
					<text>{{ getContentPreview(post.content) }}</text>
				</view>

				<view class="card-footer">
					<button class="action-btn view" @click="openPostDetail(post.id)">
						<text class="btn-icon">🔍</text>
						<text class="btn-text">查看详情</text>
					</button>
					<button class="action-btn delete" @click="handleDelete(post)">
						<text class="btn-icon">🗑️</text>
						<text class="btn-text">删除帖子</text>
					</button>
				</view>
			</view>

			<view class="load-more" v-if="hasMore && postList.length > 0">
				<text class="load-more-text">{{ isLoadingMore ? '加载中...' : '上拉加载更多' }}</text>
			</view>

			<view class="no-more" v-if="!hasMore && postList.length > 0">
				<text class="no-more-text">没有更多帖子了</text>
			</view>
		</scroll-view>

		<view class="loading-overlay" v-if="isLoading && postList.length === 0">
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
				postList: [],
				currentPage: 1,
				pageSize: 10,
				totalPages: 1,
				hasMore: true,
				isLoading: false,
				isLoadingMore: false,
				isRefreshing: false
			}
		},

		onLoad() {
			this.initializePage()
		},

		methods: {
			initializePage() {
				if (!this.checkAdminPermission()) {
					return
				}
				this.fetchPosts(true)
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

			async fetchPosts(reset = false) {
				if (this.isLoading || this.isLoadingMore) {
					return
				}

				if (reset) {
					this.currentPage = 1
					this.totalPages = 1
					this.hasMore = true
					this.postList = []
				}

				const isLoadMore = !reset && this.currentPage > 1
				if (isLoadMore) {
					this.isLoadingMore = true
				} else if (!this.isRefreshing) {
					this.isLoading = true
				}

				try {
					const result = await adminApi.getAllPosts({
						current: this.currentPage,
						size: this.pageSize
					})

					const records = result.records || []
					if (this.currentPage === 1) {
						this.postList = records
					} else {
						this.postList = [...this.postList, ...records]
					}

					const total = result.total || 0
					this.totalPages = Math.max(1, Math.ceil(total / this.pageSize))
					this.hasMore = this.currentPage < this.totalPages
				} catch (error) {
					console.error('加载帖子失败:', error)
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
				await this.fetchPosts()
			},

			async onRefresh() {
				if (this.isRefreshing) {
					return
				}
				this.isRefreshing = true
				await this.fetchPosts(true)
			},

			getRoleLabel(role) {
				return ROLE_LABELS[role] || '体验用户'
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
				const hh = String(date.getHours()).padStart(2, '0')
				const mm = String(date.getMinutes()).padStart(2, '0')
				return `${y}-${m}-${d} ${hh}:${mm}`
			},

			getContentPreview(content) {
				if (!content) {
					return '暂无正文内容'
				}
				const text = String(content).replace(/<[^>]+>/g, '').trim()
				if (text.length === 0) {
					return '暂无正文内容'
				}
				return text.length > 80 ? `${text.slice(0, 80)}…` : text
			},

			openPostDetail(postId) {
				if (!postId) {
					uni.showToast({
						title: '无法打开帖子详情',
						icon: 'none'
					})
					return
				}
				uni.navigateTo({
					url: `/pages/post-detail/post-detail?id=${postId}`
				})
			},

			async handleDelete(post) {
				const confirmed = await this.showConfirm('删除后帖子将不可恢复，确定删除吗？')
				if (!confirmed) {
					return
				}

				try {
					await adminApi.deletePost(post.id)
					this.postList = this.postList.filter(item => item.id !== post.id)
					uni.showToast({
						title: '帖子已删除',
						icon: 'success'
					})

					if (this.postList.length === 0 && this.hasMore) {
						await this.fetchPosts()
					}
				} catch (error) {
					console.error('删除帖子失败:', error)
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
		background: #f4f6fb;
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
		color: #7a8bb0;
	}

	.post-list {
		max-height: calc(100vh - 200rpx);
	}

	.post-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 26rpx 28rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 6rpx 18rpx rgba(31, 47, 74, 0.05);
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 12rpx;
	}

	.title {
		font-size: 34rpx;
		font-weight: 600;
		color: #1f2f4a;
		flex: 1;
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

	.meta-row {
		display: flex;
		justify-content: space-between;
		gap: 12rpx;
		flex-wrap: wrap;
	}

	.meta-text {
		font-size: 26rpx;
		color: #6c7b9c;
	}

	.content-preview {
		background: #f7f9fc;
		border-radius: 14rpx;
		padding: 20rpx;
		font-size: 26rpx;
		color: #4a5a78;
		line-height: 1.6;
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

	.action-btn.view {
		background: #eef2ff;
		color: #3949ab;
	}

	.action-btn.delete {
		background: #fff5f5;
		color: #d63031;
		border: 1rpx solid rgba(214, 48, 49, 0.3);
	}

	.btn-icon {
		font-size: 30rpx;
	}

	.btn-text {
		font-size: 28rpx;
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
