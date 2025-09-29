<!-- 我的帖子页 - 发布的帖子管理 -->
<template>
	<!-- 主容器：我的帖子列表 -->
	<view class="my-posts-container">
		<!-- 顶部统计和操作栏 -->
		<view class="top-stats" v-if="postStats">
			<view class="stats-item">
				<text class="stats-number">{{ postStats.totalPosts }}</text>
				<text class="stats-label">发布</text>
			</view>
			<view class="stats-item">
				<text class="stats-number">{{ postStats.totalViews }}</text>
				<text class="stats-label">浏览</text>
			</view>
			<view class="stats-item">
				<text class="stats-number">{{ postStats.totalLikes }}</text>
				<text class="stats-label">点赞</text>
			</view>
			<view class="stats-item">
				<text class="stats-number">{{ postStats.totalComments }}</text>
				<text class="stats-label">评论</text>
			</view>
		</view>

		<!-- 筛选和排序 -->
		<view class="filter-bar">
			<scroll-view class="filter-tags" scroll-x="true" show-scrollbar="false">
				<text class="filter-tag" :class="{ 'active': currentStatus === 'all' }" @click="filterByStatus('all')">
					全部
				</text>
				<text class="filter-tag" :class="{ 'active': currentStatus === 'published' }" @click="filterByStatus('published')">
					已发布
				</text>
				<text class="filter-tag" :class="{ 'active': currentStatus === 'draft' }" @click="filterByStatus('draft')">
					草稿
				</text>
				<text class="filter-tag" :class="{ 'active': currentStatus === 'reviewing' }" @click="filterByStatus('reviewing')">
					审核中
				</text>
			</scroll-view>
			<view class="sort-selector">
				<text class="sort-btn" :class="{ 'active': sortType === 'time' }" @click="changeSortType('time')">最新</text>
				<text class="sort-btn" :class="{ 'active': sortType === 'hot' }" @click="changeSortType('hot')">最热</text>
			</view>
		</view>

		<!-- 帖子列表 -->
		<scroll-view class="posts-scroll"
					 scroll-y="true"
					 @scrolltolower="loadMorePosts"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshPosts">

			<!-- 帖子项 -->
			<view class="post-item" v-for="post in myPostsList" :key="post.id" @click="goToPostDetail(post.id)">
				<!-- 帖子状态标识 -->
				<view class="post-status" :class="getStatusClass(post.status)">
					<text class="status-text">{{ getStatusText(post.status) }}</text>
				</view>

				<!-- 帖子内容 -->
				<view class="post-content">
					<!-- 帖子标题 -->
					<view class="post-title" v-if="post.title">
						<text class="title-text">{{ post.title }}</text>
					</view>

					<!-- 帖子内容摘要 -->
					<view class="post-summary">
						<text class="summary-text">{{ post.content.substring(0, 150) }}{{ post.content.length > 150 ? '...' : '' }}</text>
					</view>

					<!-- 帖子图片预览 -->
					<view class="post-images" v-if="post.imageUrls && post.imageUrls.length">
						<image class="preview-image"
							   :src="post.imageUrls[0]"
							   mode="aspectFill"
							   v-if="post.imageUrls.length === 1">
						</image>
						<view class="image-grid" v-else>
							<image class="grid-image"
								   v-for="(img, index) in post.imageUrls.slice(0, 4)"
								   :key="index"
								   :src="img"
								   mode="aspectFill">
							</image>
							<view class="more-images" v-if="post.imageUrls.length > 4">
								<text class="more-count">+{{ post.imageUrls.length - 4 }}</text>
							</view>
						</view>
					</view>

					<!-- 帖子元信息 -->
					<view class="post-meta">
						<text class="post-time">{{ formatTime(post.createdAt) }}</text>
						<view class="post-stats">
							<text class="stat-item">👁 {{ post.viewCount || 0 }}</text>
							<text class="stat-item">👍 {{ post.likeCount || 0 }}</text>
							<text class="stat-item">💬 {{ post.commentCount || 0 }}</text>
						</view>
					</view>

					<!-- 帖子标签 -->
					<view class="post-tags" v-if="post.tags && post.tags.length">
						<text class="tag" v-for="tag in post.tags.slice(0, 3)" :key="tag.id">
							#{{ tag.name }}
						</text>
					</view>
				</view>

				<!-- 帖子操作 -->
				<view class="post-actions" @click.stop="">
					<text class="action-btn" @click="editPost(post)" v-if="post.status === 'draft'">编辑</text>
					<text class="action-btn" @click="sharePost(post)" v-if="post.status === 'published'">分享</text>
					<text class="action-btn" @click="viewData(post)" v-if="post.status === 'published'">数据</text>
					<text class="action-btn danger" @click="deletePost(post.id)">删除</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="myPostsList.length === 0 && !loading">
				<text class="empty-icon">📝</text>
				<text class="empty-text">还没有发布任何帖子</text>
				<text class="empty-tip">分享你的想法和经验吧</text>
				<button class="create-btn" @click="goToCreate">立即发布</button>
			</view>

			<!-- 加载更多 -->
			<view class="load-more" v-if="hasMoreData && myPostsList.length > 0">
				<text class="load-more-text">{{ loading ? '加载中...' : '加载更多' }}</text>
			</view>
		</scroll-view>

		<!-- 浮动发布按钮 -->
		<view class="floating-create-btn" @click="goToCreate">
			<text class="create-icon">✏️</text>
		</view>

		<!-- 帖子数据弹窗 -->
		<uni-popup ref="dataPopup" type="bottom">
			<view class="data-popup-content" v-if="selectedPost">
				<view class="popup-header">
					<text class="popup-title">帖子数据</text>
					<text class="close-btn" @click="$refs.dataPopup.close()">✕</text>
				</view>
				<view class="data-overview">
					<view class="data-item">
						<text class="data-number">{{ selectedPost.viewCount || 0 }}</text>
						<text class="data-label">浏览量</text>
					</view>
					<view class="data-item">
						<text class="data-number">{{ selectedPost.likeCount || 0 }}</text>
						<text class="data-label">点赞数</text>
					</view>
					<view class="data-item">
						<text class="data-number">{{ selectedPost.commentCount || 0 }}</text>
						<text class="data-label">评论数</text>
					</view>
					<view class="data-item">
						<text class="data-number">{{ selectedPost.collectCount || 0 }}</text>
						<text class="data-label">收藏数</text>
					</view>
				</view>
				<view class="data-details">
					<text class="details-title">详细数据</text>
					<view class="detail-row">
						<text class="detail-label">发布时间</text>
						<text class="detail-value">{{ formatDetailTime(selectedPost.createdAt) }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">最后互动</text>
						<text class="detail-value">{{ formatDetailTime(selectedPost.lastInteraction) }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">互动率</text>
						<text class="detail-value">{{ calculateEngagementRate(selectedPost) }}%</text>
					</view>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
// 引入API和工具函数
import { postApi, userApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 页面状态
			loading: false,
			isRefreshing: false,

			// 帖子列表
			myPostsList: [],
			postStats: null,
			selectedPost: null,

			// 筛选和排序
			currentStatus: 'all', // all | published | draft | reviewing
			sortType: 'time', // time | hot

			// 分页状态
			currentPage: 1,
			pageSize: 20,
			hasMoreData: true,

			// 用户信息
			currentUser: null
		}
	},

	onLoad() {
		// 获取当前用户信息
		this.currentUser = getAuthInfo()
		if (!this.currentUser?.userId) {
			uni.showToast({ title: '请先登录', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 加载数据
		this.loadPostStats()
		this.loadMyPosts()
	},

	onShow() {
		// 页面显示时刷新数据（从发布页面返回后）
		if (this.myPostsList.length > 0) {
			this.refreshPosts()
		}
	},

	methods: {
		/**
		 * 加载帖子统计数据
		 */
		async loadPostStats() {
			try {
				// 模拟统计数据
				this.postStats = {
					totalPosts: 23,
					totalViews: 5420,
					totalLikes: 892,
					totalComments: 156
				}
			} catch (error) {
				console.error('加载统计数据失败:', error)
			}
		},

		/**
		 * 加载我的帖子列表
		 * @param {boolean} refresh - 是否刷新数据
		 */
		async loadMyPosts(refresh = false) {
			try {
				if (refresh) {
					this.currentPage = 1
					this.myPostsList = []
					this.hasMoreData = true
				}

				this.loading = true

				// 模拟我的帖子数据
				const mockPosts = await this.getMockMyPosts()

				if (refresh) {
					this.myPostsList = mockPosts.list
				} else {
					this.myPostsList = [...this.myPostsList, ...mockPosts.list]
				}

				this.hasMoreData = this.myPostsList.length < mockPosts.total
			} catch (error) {
				console.error('加载我的帖子失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 获取模拟我的帖子数据
		 */
		async getMockMyPosts() {
			await new Promise(resolve => setTimeout(resolve, 500))

			const mockList = [
				{
					id: 1,
					title: '职场女性如何平衡工作与生活',
					content: '在现代社会中，职场女性面临着前所未有的挑战。如何在追求事业成功的同时，保持工作与生活的平衡，是每个职场女性都需要思考的问题...',
					imageUrls: ['/static/img/post1.jpg'],
					tags: [{ id: 1, name: '职场女性' }, { id: 2, name: '工作平衡' }],
					status: 'published',
					viewCount: 1250,
					likeCount: 89,
					commentCount: 23,
					collectCount: 45,
					createdAt: '2025-01-15T10:30:00Z',
					lastInteraction: '2025-01-16T14:20:00Z'
				},
				{
					id: 2,
					title: '创业路上的那些坑',
					content: '作为一名女性创业者，我想分享一些创业路上遇到的挑战和经验。希望能够帮助到更多有创业想法的姐妹们...',
					imageUrls: [],
					tags: [{ id: 3, name: '创业经验' }],
					status: 'published',
					viewCount: 2100,
					likeCount: 156,
					commentCount: 45,
					collectCount: 78,
					createdAt: '2025-01-14T15:20:00Z',
					lastInteraction: '2025-01-15T09:45:00Z'
				},
				{
					id: 3,
					title: '女性投资理财入门指南',
					content: '想要分享一些投资理财的心得，希望能帮助更多姐妹实现财务自由...',
					imageUrls: ['/static/img/post2.jpg', '/static/img/post3.jpg'],
					tags: [{ id: 4, name: '投资理财' }],
					status: 'draft',
					viewCount: 0,
					likeCount: 0,
					commentCount: 0,
					collectCount: 0,
					createdAt: '2025-01-13T20:00:00Z',
					lastInteraction: null
				}
			]

			return {
				list: this.currentPage === 1 ? mockList : [],
				total: mockList.length,
				current: this.currentPage,
				size: this.pageSize
			}
		},

		/**
		 * 按状态筛选
		 * @param {string} status - 状态类型
		 */
		filterByStatus(status) {
			if (this.currentStatus !== status) {
				this.currentStatus = status
				this.loadMyPosts(true)
			}
		},

		/**
		 * 切换排序方式
		 * @param {string} type - 排序类型
		 */
		changeSortType(type) {
			if (this.sortType !== type) {
				this.sortType = type
				this.loadMyPosts(true)
			}
		},

		/**
		 * 加载更多帖子
		 */
		loadMorePosts() {
			if (this.hasMoreData && !this.loading) {
				this.currentPage++
				this.loadMyPosts()
			}
		},

		/**
		 * 刷新帖子列表
		 */
		refreshPosts() {
			this.isRefreshing = true
			this.loadMyPosts(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 跳转到帖子详情
		 * @param {number} postId - 帖子ID
		 */
		goToPostDetail(postId) {
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?id=${postId}`
			})
		},

		/**
		 * 跳转到创建帖子
		 */
		goToCreate() {
			uni.navigateTo({
				url: '/pages/tabbar/tabbar-3/tabbar-3'
			})
		},

		/**
		 * 编辑帖子
		 * @param {Object} post - 帖子对象
		 */
		editPost(post) {
			// 跳转到编辑页面，可以复用发布页面
			uni.navigateTo({
				url: `/pages/edit-post/edit-post?postId=${post.id}`
			})
		},

		/**
		 * 分享帖子
		 * @param {Object} post - 帖子对象
		 */
		sharePost(post) {
			const shareData = {
				title: post.title || '精彩内容分享',
				summary: post.content.substring(0, 100),
				href: `https://herizon.com/post/${post.id}`,
				imageUrl: post.imageUrls?.[0] || '/static/img/logo.png'
			}

			uni.share({
				...shareData,
				success: () => {
					uni.showToast({ title: '分享成功', icon: 'success' })
				},
				fail: () => {
					uni.setClipboardData({
						data: shareData.href,
						success: () => {
							uni.showToast({ title: '链接已复制', icon: 'success' })
						}
					})
				}
			})
		},

		/**
		 * 查看帖子数据
		 * @param {Object} post - 帖子对象
		 */
		viewData(post) {
			this.selectedPost = post
			this.$refs.dataPopup.open()
		},

		/**
		 * 删除帖子
		 * @param {number} postId - 帖子ID
		 */
		async deletePost(postId) {
			try {
				await uni.showModal({
					title: '确认删除',
					content: '确定要删除这篇帖子吗？删除后无法恢复。',
					confirmText: '删除',
					confirmColor: '#ff4757'
				})

				// 模拟删除API
				await this.mockDeletePost(postId)

				// 从列表中移除
				this.myPostsList = this.myPostsList.filter(post => post.id !== postId)

				uni.showToast({ title: '删除成功', icon: 'success' })

				// 更新统计数据
				this.loadPostStats()
			} catch (error) {
				if (error.message !== 'cancel') {
					console.error('删除帖子失败:', error)
					uni.showToast({
						title: '删除失败',
						icon: 'error'
					})
				}
			}
		},

		/**
		 * 模拟删除帖子API
		 * @param {number} postId - 帖子ID
		 */
		async mockDeletePost(postId) {
			await new Promise(resolve => setTimeout(resolve, 300))
			return { code: 200, message: '删除成功' }
		},

		/**
		 * 获取状态样式类
		 * @param {string} status - 帖子状态
		 * @returns {string} 样式类名
		 */
		getStatusClass(status) {
			const classMap = {
				published: 'published',
				draft: 'draft',
				reviewing: 'reviewing',
				rejected: 'rejected'
			}
			return classMap[status] || 'unknown'
		},

		/**
		 * 获取状态文本
		 * @param {string} status - 帖子状态
		 * @returns {string} 状态文本
		 */
		getStatusText(status) {
			const textMap = {
				published: '已发布',
				draft: '草稿',
				reviewing: '审核中',
				rejected: '审核未通过'
			}
			return textMap[status] || '未知'
		},

		/**
		 * 计算互动率
		 * @param {Object} post - 帖子对象
		 * @returns {number} 互动率百分比
		 */
		calculateEngagementRate(post) {
			if (!post.viewCount || post.viewCount === 0) return 0
			const interactions = (post.likeCount || 0) + (post.commentCount || 0) + (post.collectCount || 0)
			return ((interactions / post.viewCount) * 100).toFixed(1)
		},

		/**
		 * 格式化时间显示
		 * @param {string} timeString - 时间字符串
		 * @returns {string} 格式化后的时间
		 */
		formatTime(timeString) {
			if (!timeString) return ''

			const now = new Date()
			const time = new Date(timeString)
			const diff = now - time

			const minute = 60 * 1000
			const hour = 60 * minute
			const day = 24 * hour
			const week = 7 * day

			if (diff < hour) {
				return `${Math.floor(diff / minute)}分钟前`
			} else if (diff < day) {
				return `${Math.floor(diff / hour)}小时前`
			} else if (diff < week) {
				return `${Math.floor(diff / day)}天前`
			} else {
				return time.toLocaleDateString('zh-CN', {
					month: 'short',
					day: 'numeric'
				})
			}
		},

		/**
		 * 格式化详细时间
		 * @param {string} timeString - 时间字符串
		 * @returns {string} 详细时间
		 */
		formatDetailTime(timeString) {
			if (!timeString) return '暂无'

			const time = new Date(timeString)
			return time.toLocaleString('zh-CN', {
				year: 'numeric',
				month: 'short',
				day: 'numeric',
				hour: '2-digit',
				minute: '2-digit'
			})
		}
	}
}
</script>

<style scoped>
/* 主容器样式 */
.my-posts-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 120rpx;
}

/* 顶部统计 */
.top-stats {
	display: flex;
	background-color: white;
	padding: 40rpx 30rpx;
	margin-bottom: 20rpx;
}

.stats-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.stats-number {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.stats-label {
	font-size: 24rpx;
	color: #666;
}

/* 筛选栏 */
.filter-bar {
	display: flex;
	align-items: center;
	background-color: white;
	padding: 20rpx 30rpx;
	margin-bottom: 20rpx;
}

.filter-tags {
	flex: 1;
	display: flex;
	gap: 20rpx;
	margin-right: 30rpx;
}

.filter-tag {
	flex-shrink: 0;
	padding: 12rpx 24rpx;
	font-size: 26rpx;
	color: #666;
	background-color: #f5f5f5;
	border-radius: 20rpx;
}

.filter-tag.active {
	color: #1890ff;
	background-color: #f0f8ff;
	border: 1rpx solid #d6e4ff;
}

.sort-selector {
	display: flex;
	gap: 20rpx;
}

.sort-btn {
	font-size: 26rpx;
	color: #666;
	padding: 8rpx 16rpx;
}

.sort-btn.active {
	color: #1890ff;
	font-weight: bold;
}

/* 帖子滚动视图 */
.posts-scroll {
	height: calc(100vh - 240rpx);
	padding: 0 30rpx;
}

/* 帖子项 */
.post-item {
	background-color: white;
	margin-bottom: 20rpx;
	border-radius: 12rpx;
	overflow: hidden;
	position: relative;
}

/* 帖子状态 */
.post-status {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	padding: 6rpx 12rpx;
	border-radius: 12rpx;
	z-index: 1;
}

.post-status.published {
	background-color: #f6ffed;
	border: 1rpx solid #b7eb8f;
}

.post-status.draft {
	background-color: #fff7e6;
	border: 1rpx solid #ffd591;
}

.post-status.reviewing {
	background-color: #e6f7ff;
	border: 1rpx solid #91d5ff;
}

.post-status.rejected {
	background-color: #fff2f0;
	border: 1rpx solid #ffccc7;
}

.status-text {
	font-size: 22rpx;
	color: #333;
}

/* 帖子内容 */
.post-content {
	padding: 30rpx;
	padding-bottom: 0;
}

/* 帖子标题 */
.post-title {
	margin-bottom: 16rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
}

/* 帖子摘要 */
.post-summary {
	margin-bottom: 20rpx;
}

.summary-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
}

/* 帖子图片 */
.post-images {
	margin-bottom: 20rpx;
}

.preview-image {
	width: 100%;
	height: 300rpx;
	border-radius: 12rpx;
}

.image-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 10rpx;
	position: relative;
}

.grid-image {
	width: 100%;
	height: 150rpx;
	border-radius: 8rpx;
}

.more-images {
	position: absolute;
	top: 0;
	right: 0;
	width: 100%;
	height: 150rpx;
	background-color: rgba(0, 0, 0, 0.5);
	border-radius: 8rpx;
	display: flex;
	justify-content: center;
	align-items: center;
}

.more-count {
	color: white;
	font-size: 28rpx;
	font-weight: bold;
}

/* 帖子元信息 */
.post-meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.post-time {
	font-size: 24rpx;
	color: #999;
}

.post-stats {
	display: flex;
	gap: 20rpx;
}

.stat-item {
	font-size: 22rpx;
	color: #666;
}

/* 帖子标签 */
.post-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-bottom: 20rpx;
}

.tag {
	font-size: 22rpx;
	color: #1890ff;
	background-color: #f0f8ff;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

/* 帖子操作 */
.post-actions {
	display: flex;
	gap: 1rpx;
	border-top: 1rpx solid #f0f0f0;
}

.action-btn {
	flex: 1;
	text-align: center;
	padding: 24rpx;
	font-size: 26rpx;
	color: #666;
	background-color: #fafafa;
}

.action-btn.danger {
	color: #ff4757;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 600rpx;
	background-color: white;
	border-radius: 12rpx;
	padding: 40rpx;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 30rpx;
	color: #666;
	margin-bottom: 12rpx;
}

.empty-tip {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 40rpx;
}

.create-btn {
	background-color: #1890ff;
	color: white;
	font-size: 28rpx;
	border: none;
	border-radius: 25rpx;
	padding: 16rpx 40rpx;
}

/* 加载更多 */
.load-more {
	text-align: center;
	padding: 40rpx;
	background-color: white;
	border-radius: 12rpx;
}

.load-more-text {
	font-size: 26rpx;
	color: #666;
}

/* 浮动创建按钮 */
.floating-create-btn {
	position: fixed;
	bottom: 40rpx;
	right: 40rpx;
	width: 100rpx;
	height: 100rpx;
	background-color: #1890ff;
	border-radius: 50%;
	display: flex;
	justify-content: center;
	align-items: center;
	box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.3);
	z-index: 100;
}

.create-icon {
	font-size: 36rpx;
	color: white;
}

/* 数据弹窗 */
.data-popup-content {
	background-color: white;
	border-radius: 24rpx 24rpx 0 0;
	padding: 40rpx 30rpx;
	min-height: 600rpx;
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 40rpx;
}

.popup-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.close-btn {
	font-size: 28rpx;
	color: #999;
	padding: 8rpx;
}

/* 数据概览 */
.data-overview {
	display: flex;
	margin-bottom: 40rpx;
}

.data-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	padding: 30rpx 20rpx;
	background-color: #f8f9fa;
	border-radius: 12rpx;
	margin-right: 16rpx;
}

.data-item:last-child {
	margin-right: 0;
}

.data-number {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.data-label {
	font-size: 24rpx;
	color: #666;
}

/* 详细数据 */
.data-details {
	border-top: 1rpx solid #f0f0f0;
	padding-top: 30rpx;
}

.details-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.detail-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.detail-row:last-child {
	border-bottom: none;
}

.detail-label {
	font-size: 26rpx;
	color: #666;
}

.detail-value {
	font-size: 26rpx;
	color: #333;
}
</style>