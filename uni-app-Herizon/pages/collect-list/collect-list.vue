<!-- 收藏列表页 - 收藏的帖子管理 -->
<template>
	<!-- 主容器：收藏列表 -->
	<view class="collect-list-container">
		<!-- 顶部操作栏 -->
		<view class="top-actions" v-if="collectList.length > 0">
			<view class="action-left">
				<text class="total-count">共{{ totalCount }}条收藏</text>
			</view>
			<view class="action-right">
				<text class="edit-btn" :class="{ 'active': isEditMode }" @click="toggleEditMode">
					{{ isEditMode ? '完成' : '编辑' }}
				</text>
			</view>
		</view>

		<!-- 收藏列表 -->
		<scroll-view class="collect-scroll"
					 scroll-y="true"
					 @scrolltolower="loadMoreCollects"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshCollects">

			<!-- 收藏项 -->
			<view class="collect-item" v-for="(item, index) in collectList" :key="item.id">
				<!-- 选择框（编辑模式） -->
				<view class="select-box" v-if="isEditMode" @click="toggleSelect(index)">
					<text class="select-icon" :class="{ 'selected': item.selected }">
						{{ item.selected ? '✓' : '○' }}
					</text>
				</view>

				<!-- 帖子内容 -->
				<view class="collect-content" @click="goToPostDetail(item.postId)">
					<!-- 帖子信息 -->
					<view class="post-info">
						<view class="post-header">
							<text class="post-title">{{ item.postTitle || item.postContent.substring(0, 30) + '...' }}</text>
							<text class="collect-time">{{ formatTime(item.createdAt) }}</text>
						</view>
						<view class="post-summary">
							<text class="post-text">{{ item.postContent.substring(0, 100) }}{{ item.postContent.length > 100 ? '...' : '' }}</text>
						</view>
						<view class="post-meta">
							<text class="post-author">@{{ item.postAuthor }}</text>
							<text class="post-stats">{{ item.postLikeCount }}赞 · {{ item.postCommentCount }}评论</text>
						</view>
					</view>

					<!-- 帖子图片 -->
					<view class="post-thumb" v-if="item.postImage">
						<image class="thumb-image" :src="item.postImage" mode="aspectFill"></image>
					</view>
				</view>

				<!-- 快捷操作（非编辑模式） -->
				<view class="quick-actions" v-if="!isEditMode">
					<text class="action-btn" @click="sharePost(item)">分享</text>
					<text class="action-btn" @click="removeCollect(item.id)">取消收藏</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="collectList.length === 0 && !loading">
				<text class="empty-icon">⭐</text>
				<text class="empty-text">还没有收藏任何内容</text>
				<text class="empty-tip">快去发现有趣的帖子收藏起来吧</text>
				<button class="explore-btn" @click="goToExplore">去发现</button>
			</view>

			<!-- 加载更多 -->
			<view class="load-more" v-if="hasMoreData && collectList.length > 0">
				<text class="load-more-text">{{ loading ? '加载中...' : '加载更多' }}</text>
			</view>
		</scroll-view>

		<!-- 编辑模式底部操作栏 -->
		<view class="edit-bottom-bar" v-if="isEditMode">
			<view class="select-all">
				<text class="select-all-btn" @click="toggleSelectAll">
					{{ isAllSelected ? '取消全选' : '全选' }}
				</text>
			</view>
			<view class="batch-actions">
				<button class="batch-btn share-btn" :disabled="selectedCount === 0" @click="batchShare">
					分享({{ selectedCount }})
				</button>
				<button class="batch-btn delete-btn" :disabled="selectedCount === 0" @click="batchRemove">
					删除({{ selectedCount }})
				</button>
			</view>
		</view>
	</view>
</template>

<script>
// 引入API和工具函数
import { actionApi, postApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 页面状态
			loading: false,
			isRefreshing: false,
			isEditMode: false,

			// 收藏列表
			collectList: [],
			totalCount: 0,

			// 分页状态
			currentPage: 1,
			pageSize: 20,
			hasMoreData: true,

			// 用户信息
			currentUser: null
		}
	},

	computed: {
		/**
		 * 选中的项目数量
		 */
		selectedCount() {
			return this.collectList.filter(item => item.selected).length
		},

		/**
		 * 是否全选
		 */
		isAllSelected() {
			return this.collectList.length > 0 && this.selectedCount === this.collectList.length
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

		// 加载收藏列表
		this.loadCollectList()
	},

	methods: {
		/**
		 * 加载收藏列表
		 * @param {boolean} refresh - 是否刷新数据
		 */
		async loadCollectList(refresh = false) {
			try {
				if (refresh) {
					this.currentPage = 1
					this.collectList = []
					this.hasMoreData = true
				}

				this.loading = true

				// 模拟收藏数据（实际应该调用收藏API）
				const mockCollects = await this.getMockCollectData()

				if (refresh) {
					this.collectList = mockCollects.list
				} else {
					this.collectList = [...this.collectList, ...mockCollects.list]
				}

				this.totalCount = mockCollects.total
				this.hasMoreData = this.collectList.length < this.totalCount
			} catch (error) {
				console.error('加载收藏列表失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 获取模拟收藏数据
		 */
		async getMockCollectData() {
			// 模拟API延时
			await new Promise(resolve => setTimeout(resolve, 500))

			const mockList = [
				{
					id: 1,
					postId: 101,
					postTitle: '职场女性如何平衡工作与生活',
					postContent: '在现代社会中，职场女性面临着前所未有的挑战。如何在追求事业成功的同时，保持工作与生活的平衡...',
					postAuthor: '张小美',
					postImage: '/static/img/post1.jpg',
					postLikeCount: 89,
					postCommentCount: 23,
					createdAt: '2025-01-15T10:30:00Z',
					selected: false
				},
				{
					id: 2,
					postId: 102,
					postTitle: '创业路上的那些坑',
					postContent: '作为一名女性创业者，我想分享一些创业路上遇到的挑战和经验。希望能够帮助到更多有创业想法的姐妹们...',
					postAuthor: '李创业',
					postImage: '',
					postLikeCount: 156,
					postCommentCount: 45,
					createdAt: '2025-01-14T15:20:00Z',
					selected: false
				},
				{
					id: 3,
					postId: 103,
					postTitle: '投资理财小白的入门指南',
					postContent: '作为一个刚开始学习投资理财的小白，我总结了一些基础知识和实用技巧，希望对同样想要开始理财的姐妹有帮助...',
					postAuthor: '财女王',
					postImage: '/static/img/post2.jpg',
					postLikeCount: 234,
					postCommentCount: 67,
					createdAt: '2025-01-13T09:15:00Z',
					selected: false
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
		 * 加载更多收藏
		 */
		loadMoreCollects() {
			if (this.hasMoreData && !this.loading) {
				this.currentPage++
				this.loadCollectList()
			}
		},

		/**
		 * 刷新收藏列表
		 */
		refreshCollects() {
			this.isRefreshing = true
			this.loadCollectList(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 切换编辑模式
		 */
		toggleEditMode() {
			this.isEditMode = !this.isEditMode
			if (!this.isEditMode) {
				// 退出编辑模式时清除所有选择
				this.collectList.forEach(item => {
					item.selected = false
				})
			}
		},

		/**
		 * 切换选择状态
		 * @param {number} index - 项目索引
		 */
		toggleSelect(index) {
			this.collectList[index].selected = !this.collectList[index].selected
		},

		/**
		 * 切换全选状态
		 */
		toggleSelectAll() {
			const selectAll = !this.isAllSelected
			this.collectList.forEach(item => {
				item.selected = selectAll
			})
		},

		/**
		 * 取消单个收藏
		 * @param {number} collectId - 收藏ID
		 */
		async removeCollect(collectId) {
			try {
				await uni.showModal({
					title: '确认取消收藏',
					content: '确定要取消收藏这个内容吗？',
					confirmText: '取消收藏',
					confirmColor: '#ff4757'
				})

				// 模拟API调用取消收藏
				await this.mockRemoveCollect(collectId)

				// 从列表中移除
				this.collectList = this.collectList.filter(item => item.id !== collectId)
				this.totalCount = Math.max(0, this.totalCount - 1)

				uni.showToast({ title: '已取消收藏', icon: 'success' })
			} catch (error) {
				if (error.message !== 'cancel') {
					console.error('取消收藏失败:', error)
					uni.showToast({
						title: '操作失败',
						icon: 'error'
					})
				}
			}
		},

		/**
		 * 批量删除收藏
		 */
		async batchRemove() {
			if (this.selectedCount === 0) return

			try {
				await uni.showModal({
					title: '确认批量删除',
					content: `确定要删除选中的${this.selectedCount}个收藏吗？`,
					confirmText: '删除',
					confirmColor: '#ff4757'
				})

				const selectedIds = this.collectList
					.filter(item => item.selected)
					.map(item => item.id)

				// 模拟批量删除API
				await this.mockBatchRemove(selectedIds)

				// 从列表中移除选中项
				this.collectList = this.collectList.filter(item => !item.selected)
				this.totalCount = Math.max(0, this.totalCount - selectedIds.length)

				uni.showToast({ title: `已删除${selectedIds.length}个收藏`, icon: 'success' })

				// 退出编辑模式
				this.toggleEditMode()
			} catch (error) {
				if (error.message !== 'cancel') {
					console.error('批量删除失败:', error)
					uni.showToast({
						title: '删除失败',
						icon: 'error'
					})
				}
			}
		},

		/**
		 * 分享帖子
		 * @param {Object} item - 收藏项
		 */
		sharePost(item) {
			const shareData = {
				title: item.postTitle || '精彩内容分享',
				summary: item.postContent.substring(0, 100),
				href: `https://herizon.com/post/${item.postId}`,
				imageUrl: item.postImage || '/static/img/logo.png'
			}

			uni.share({
				...shareData,
				success: () => {
					uni.showToast({ title: '分享成功', icon: 'success' })
				},
				fail: () => {
					// 分享失败时复制链接
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
		 * 批量分享
		 */
		batchShare() {
			if (this.selectedCount === 0) return

			const selectedItems = this.collectList.filter(item => item.selected)
			let shareText = '我在Herizon发现了这些精彩内容：\n\n'

			selectedItems.forEach((item, index) => {
				shareText += `${index + 1}. ${item.postTitle || item.postContent.substring(0, 30)}\n`
				shareText += `https://herizon.com/post/${item.postId}\n\n`
			})

			uni.setClipboardData({
				data: shareText,
				success: () => {
					uni.showToast({ title: '分享内容已复制', icon: 'success' })
					this.toggleEditMode() // 退出编辑模式
				},
				fail: () => {
					uni.showToast({ title: '分享失败', icon: 'error' })
				}
			})
		},

		/**
		 * 跳转到帖子详情
		 * @param {number} postId - 帖子ID
		 */
		goToPostDetail(postId) {
			if (!this.isEditMode) {
				uni.navigateTo({
					url: `/pages/post-detail/post-detail?id=${postId}`
				})
			}
		},

		/**
		 * 跳转到发现页面
		 */
		goToExplore() {
			uni.switchTab({
				url: '/pages/tabbar/tabbar-1/tabbar-1'
			})
		},

		/**
		 * 模拟删除收藏API
		 * @param {number} collectId - 收藏ID
		 */
		async mockRemoveCollect(collectId) {
			await new Promise(resolve => setTimeout(resolve, 300))
			return { code: 200, message: '取消收藏成功' }
		},

		/**
		 * 模拟批量删除API
		 * @param {Array} collectIds - 收藏ID数组
		 */
		async mockBatchRemove(collectIds) {
			await new Promise(resolve => setTimeout(resolve, 500))
			return { code: 200, message: '批量删除成功' }
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

			const day = 24 * 60 * 60 * 1000
			const week = 7 * day
			const month = 30 * day

			if (diff < day) {
				return '今天'
			} else if (diff < week) {
				return `${Math.floor(diff / day)}天前`
			} else if (diff < month) {
				return `${Math.floor(diff / week)}周前`
			} else {
				return time.toLocaleDateString('zh-CN', {
					month: 'short',
					day: 'numeric'
				})
			}
		}
	}
}
</script>

<style scoped>
/* 主容器样式 */
.collect-list-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 120rpx;
}

/* 顶部操作栏 */
.top-actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
}

.action-left {
	flex: 1;
}

.total-count {
	font-size: 26rpx;
	color: #666;
}

.action-right {
	flex-shrink: 0;
}

.edit-btn {
	font-size: 28rpx;
	color: #1890ff;
	padding: 8rpx 16rpx;
}

.edit-btn.active {
	color: #ff4757;
}

/* 收藏滚动视图 */
.collect-scroll {
	height: calc(100vh - 120rpx);
	padding: 20rpx 30rpx;
}

/* 收藏项 */
.collect-item {
	display: flex;
	align-items: flex-start;
	background-color: white;
	margin-bottom: 20rpx;
	border-radius: 12rpx;
	overflow: hidden;
}

/* 选择框 */
.select-box {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 80rpx;
	height: 80rpx;
	padding: 30rpx 0;
}

.select-icon {
	font-size: 36rpx;
	color: #ccc;
}

.select-icon.selected {
	color: #1890ff;
}

/* 收藏内容 */
.collect-content {
	flex: 1;
	display: flex;
	padding: 30rpx;
	padding-left: 0;
}

/* 帖子信息 */
.post-info {
	flex: 1;
	margin-right: 20rpx;
}

.post-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 16rpx;
}

.post-title {
	flex: 1;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
	margin-right: 16rpx;
}

.collect-time {
	flex-shrink: 0;
	font-size: 22rpx;
	color: #999;
}

.post-summary {
	margin-bottom: 16rpx;
}

.post-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.5;
}

.post-meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.post-author {
	font-size: 24rpx;
	color: #1890ff;
}

.post-stats {
	font-size: 22rpx;
	color: #999;
}

/* 帖子缩略图 */
.post-thumb {
	flex-shrink: 0;
}

.thumb-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 8rpx;
}

/* 快捷操作 */
.quick-actions {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	padding: 30rpx 20rpx;
	border-left: 1rpx solid #f0f0f0;
}

.action-btn {
	font-size: 24rpx;
	color: #666;
	text-align: center;
	padding: 12rpx;
	background-color: #f5f5f5;
	border-radius: 8rpx;
	min-width: 100rpx;
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
	text-align: center;
}

.explore-btn {
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

/* 编辑模式底部操作栏 */
.edit-bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	background-color: white;
	border-top: 1rpx solid #e0e0e0;
	padding: 20rpx 30rpx;
	z-index: 100;
}

.select-all {
	flex-shrink: 0;
	margin-right: 40rpx;
}

.select-all-btn {
	font-size: 26rpx;
	color: #1890ff;
	padding: 8rpx;
}

.batch-actions {
	flex: 1;
	display: flex;
	gap: 20rpx;
}

.batch-btn {
	flex: 1;
	height: 70rpx;
	font-size: 26rpx;
	border: none;
	border-radius: 35rpx;
}

.share-btn {
	background-color: #1890ff;
	color: white;
}

.delete-btn {
	background-color: #ff4757;
	color: white;
}

.batch-btn[disabled] {
	background-color: #ccc;
	color: #999;
}
</style>