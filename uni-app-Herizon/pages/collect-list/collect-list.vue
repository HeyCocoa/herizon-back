<!-- 收藏列表页 - 收藏的帖子管理 -->
<template>
	<!-- 主容器:收藏列表 -->
	<view class="collect-page">
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
					 @refresherrefresh="refreshCollects"
					 show-scrollbar="false">

			<!-- 收藏项 -->
			<view class="collect-item" v-for="(item, index) in collectList" :key="item.id">
				<view class="collect-card" :class="{ 'with-select': isEditMode }" @click="goToPostDetail(item.postId)">
					<view class="collect-card-main">
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

						<view class="post-thumb" v-if="item.postImage">
							<image class="thumb-image" :src="item.postImage" mode="aspectFill"></image>
						</view>
					</view>
				</view>

				<view class="quick-actions" v-if="!isEditMode">
					<text class="action-btn" @click="removeCollect(item.id)">取消收藏</text>
				</view>

				<view class="select-overlay" v-if="isEditMode" @click.stop="toggleSelect(index)">
					<view class="select-circle" :class="{ selected: item.selected }">
						<text class="select-icon">{{ item.selected ? '✔' : '' }}</text>
					</view>
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
				<button class="batch-btn delete-btn" :disabled="selectedCount === 0" @click="batchRemove">
					删除({{ selectedCount }})
				</button>
			</view>
		</view>
	</view>
</template>

<script>
// 引入API和工具函数
import { actionApi } from '@/utils/api.js'
import { getAuthInfo, getUserId } from '@/utils/auth.js'

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
		this.currentUser = getAuthInfo() || {}
		const userId = getUserId()
		if (!userId) {
			uni.showToast({ title: '请先登录', icon: 'error' })
			uni.navigateBack()
			return
		}

		if (!this.currentUser.userId) {
			this.currentUser.userId = this.currentUser.id || userId
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

				const userId = this.currentUser.userId || getUserId()
				if (!userId) {
					throw new Error('用户未登录')
				}

				const params = {
					current: this.currentPage,
					size: this.pageSize
				}

				const result = await actionApi.getCollections(userId, params)
				const records = Array.isArray(result?.records) ? result.records : []
				const mappedCollects = records.map(item => this.transformCollectItem(item))

				if (refresh) {
					this.collectList = mappedCollects
				} else {
					this.collectList = [...this.collectList, ...mappedCollects]
				}

				const total = typeof result?.total === 'number' ? result.total : this.collectList.length
				this.totalCount = total
				this.hasMoreData = this.collectList.length < total
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
		 * 标准化收藏数据
		 */
		transformCollectItem(raw) {
			const firstImage = Array.isArray(raw?.imageUrls) ? (raw.imageUrls[0] || '') : (raw?.postImage || raw?.coverImage || '')
			const contentText = (raw?.postContent || raw?.content || '') || ''

			const normalized = {
				...raw,
				id: raw?.id ?? raw?.postId,
				postId: raw?.postId ?? raw?.id,
				postTitle: raw?.postTitle || raw?.title || '',
				postContent: contentText,
				postAuthor: raw?.postAuthor || raw?.authorNickname || raw?.authorUsername || raw?.nickname || raw?.username || '匿名用户',
				postLikeCount: raw?.postLikeCount ?? raw?.likeCount ?? 0,
				postCommentCount: raw?.postCommentCount ?? raw?.commentCount ?? 0,
				postImage: firstImage,
				createdAt: raw?.createdAt || raw?.collectedAt || raw?.updatedAt || ''
			}

			return {
				...normalized,
				selected: false
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
					content: '确定要取消收藏这个内容吗?',
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
					content: `确定要删除选中的${this.selectedCount}个收藏吗?`,
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
.collect-page {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 120rpx;
}

/* 顶部操作栏 */
.top-actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin: 0 30rpx 20rpx;
	padding: 26rpx 30rpx;
	background-color: #ffffff;
	border-radius: 12rpx;
	box-shadow: 0 8rpx 24rpx rgba(15, 35, 95, 0.06);
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
	height: calc(100vh - 200rpx);
	padding: 0 30rpx 30rpx;
	box-sizing: border-box;
}

/* 收藏项 */
.collect-item {
	margin-bottom: 20rpx;
	background-color: #ffffff;
	border-radius: 12rpx;
	box-shadow: 0 12rpx 32rpx rgba(15, 35, 95, 0.06);
	overflow: hidden;
	position: relative;
}

.collect-card {
	padding: 28rpx;
	padding-bottom: 0;
	box-sizing: border-box;
}

.collect-card.with-select {
	padding-left: 100rpx;
}

.collect-card-main {
	display: flex;
	gap: 24rpx;
	align-items: flex-start;
	min-width: 0;
}

.post-info {
	flex: 1;
	min-width: 0;
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
	font-weight: 600;
	color: #1f2933;
	line-height: 1.4;
	margin-right: 16rpx;
	word-break: break-word;
	overflow-wrap: break-word;
}

.collect-time {
	flex-shrink: 0;
	font-size: 22rpx;
	color: #9aa5b1;
}

.post-summary {
	margin-bottom: 16rpx;
}

.post-text {
	font-size: 26rpx;
	color: #52606d;
	line-height: 1.6;
	word-break: break-word;
	overflow-wrap: break-word;
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
	color: #9aa5b1;
}

.post-thumb {
	flex-shrink: 0;
	width: 150rpx;
	height: 150rpx;
	border-radius: 12rpx;
	overflow: hidden;
	background-color: #f5f7fa;
}

.thumb-image {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

/* 快捷操作 */
.quick-actions {
	display: flex;
	width: 100%;
	border-top: 1rpx solid #f0f0f0;
	gap: 1rpx;
	padding: 0;
	background-color: #fff;
}

.action-btn {
	flex: 1;
	font-size: 26rpx;
	color: #666;
	text-align: center;
	padding: 24rpx;
	background-color: #fafafa;
}

.action-btn:active {
	background-color: #f0f5ff;
}

/* 编辑模式选择 */
.select-overlay {
	position: absolute;
	top: 28rpx;
	left: 30rpx;
	z-index: 2;
}

.collect-card.with-select .collect-card-main {
	position: relative;
}

.select-circle {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	border: 2rpx solid #d9d9d9;
	background-color: #fff;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #ccc;
	font-size: 32rpx;
}

.select-circle.selected {
	background-color: #1890ff;
	border-color: #1890ff;
	color: #fff;
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


.delete-btn {
	background-color: #ff4757;
	color: white;
}

.batch-btn[disabled] {
	background-color: #ccc;
	color: #999;
}
</style>
