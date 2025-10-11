<!-- 标签帖子页面 - 显示特定标签下的所有帖子 -->
<template>
	<view class="tag-posts-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="nav-title">#{{ tagName }}</view>
			<view class="nav-right"></view>
		</view>

		<!-- 帖子列表 -->
		<scroll-view class="content-scroll"
					 scroll-y="true"
					 enable-back-to-top="true"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshPosts"
					 @scrolltolower="loadMorePosts">

			<!-- 帖子卡片 -->
			<view class="post-item"
				  v-for="post in postList"
				  :key="post.id"
				  @click="goToDetail(post)">

				<!-- 用户信息 -->
				<view class="post-header">
					<view class="user-info">
						<image class="user-avatar" :src="post.userAvatar || '/static/img/default-avatar.png'"></image>
						<view class="user-details">
							<text class="username">{{ post.username }}</text>
							<text class="post-time">{{ formatTime(post.createdAt) }}</text>
						</view>
					</view>
				</view>

				<!-- 帖子内容 -->
				<view class="post-content">
					<text class="post-title">{{ post.title }}</text>
					<text class="post-summary" v-if="post.summary">{{ post.summary }}</text>
				</view>

				<!-- 帖子图片 - Performance: Lazy loading enabled -->
				<view class="post-images" v-if="post.imageUrls && post.imageUrls.length > 0">
					<image class="post-image"
						   v-for="(img, index) in post.imageUrls.slice(0, 3)"
						   :key="index"
						   :src="img"
						   mode="aspectFill"
						   lazy-load
						   :class="{ 'image-loaded': imageLoaded[`${post.id}_${index}`] }"
						   @load="onImageLoad(post.id, index)"
						   @error="onImageError(post.id, index)"></image>
				</view>

				<!-- 帖子统计 -->
				<view class="post-stats">
					<view class="stat-item">
						<text class="stat-icon">👍</text>
						<text class="stat-count">{{ post.likeCount || 0 }}</text>
					</view>
					<view class="stat-item">
						<text class="stat-icon">💬</text>
						<text class="stat-count">{{ post.commentCount || 0 }}</text>
					</view>
					<view class="stat-item">
						<text class="stat-icon">👁</text>
						<text class="stat-count">{{ post.viewCount || 0 }}</text>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="!isLoading && postList.length === 0">
				<text class="empty-icon">📝</text>
				<text class="empty-text">该话题下暂无内容</text>
				<text class="empty-hint">快来发布第一篇帖子吧</text>
			</view>

			<!-- 加载更多 -->
			<view class="load-more" v-if="hasMore && postList.length > 0">
				<text v-if="!isLoadingMore">上拉加载更多</text>
				<text v-else>加载中...</text>
			</view>

			<!-- 没有更多数据 -->
			<view class="no-more" v-if="!hasMore && postList.length > 0">
				<text>已加载所有内容</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
/**
 * 标签帖子页面
 *
 * 功能特性:
 * - 显示特定标签下的所有帖子
 * - 支持下拉刷新和上拉加载更多
 * - 点击帖子进入详情页
 * - 显示帖子统计信息(点赞、评论、浏览数)
 *
 * Performance Optimizations (2025-10-04):
 * - Image lazy loading for better performance
 * - Memory cleanup on page unload
 */

import { postApi } from '../../utils/api.js'

export default {
	data() {
		return {
			// 状态栏高度
			statusBarHeight: 0,

			// 页面参数
			tagId: '',
			tagName: '',

			// 帖子列表数据
			postList: [],

			// 分页参数
			pageParams: {
				current: 1,
				size: 10
			},

			// 加载状态
			isLoading: false,
			isLoadingMore: false,
			isRefreshing: false,
			hasMore: true,

			// Performance: Image lazy loading state
			imageLoaded: {},
			imageErrors: {}
		}
	},

	onLoad(options) {
		// 获取系统状态栏高度
		const systemInfo = uni.getSystemInfoSync()
		this.statusBarHeight = systemInfo.statusBarHeight || 0

		// 获取页面参数,防止参数缺失导致API调用失败
		if (!options.tagId) {
			uni.showToast({ title: '标签不存在', icon: 'error' })
			uni.navigateBack()
			return
		}

		this.tagId = options.tagId
		this.tagName = decodeURIComponent(options.tagName || '')

		// 加载帖子列表
		this.loadPostList(true)
	},

	onUnload() {
		// Performance: Memory cleanup
		this.postList = []
		this.imageLoaded = {}
		this.imageErrors = {}
	},

	methods: {
		/**
		 * 加载帖子列表
		 * @param {boolean} reset - 是否重置列表
		 */
		async loadPostList(reset = false) {
			if (this.isLoading || (!reset && this.isLoadingMore)) {
				return
			}

			if (reset) {
				this.pageParams.current = 1
				this.isLoading = true
				this.hasMore = true
			} else {
				this.isLoadingMore = true
			}

			try {
				// Defensive validation: ensure tagId exists
				if (!this.tagId) {
					throw new Error('tagId is required')
				}

				const params = {
					current: this.pageParams.current,
					size: this.pageParams.size
				}

				// 修复BUG-004: getPostsByTag需要tagId作为第一个参数
				// API定义: getPostsByTag(tagId, params)
				const result = await postApi.getPostsByTag(this.tagId, params)
				const currentPage = this.pageParams.current
				const pageSize = this.pageParams.size || 1
				const records = result.records || []

				if (reset) {
					this.postList = records
				} else {
					this.postList.push(...records)
				}

				const pagesRaw = typeof result.pages !== 'undefined' ? result.pages : undefined
				let totalPages = Number(pagesRaw)
				if (!Number.isFinite(totalPages) || totalPages <= 0) {
					const totalRaw = typeof result.total !== 'undefined' ? result.total : undefined
					const total = Number(totalRaw)
					if (Number.isFinite(total) && total > 0) {
						totalPages = Math.max(1, Math.ceil(total / pageSize))
					} else if (records.length === pageSize) {
						totalPages = currentPage + 1
					} else {
						totalPages = currentPage
					}
				}

				this.hasMore = currentPage < totalPages
				this.pageParams.current = this.hasMore ? currentPage + 1 : Math.max(totalPages, 1)

			} catch (error) {
				console.error('加载帖子列表失败:', error)

				// API调用失败时显示错误提示,清空列表
				uni.showToast({
					title: '网络请求失败,请稍后重试',
					icon: 'none',
					duration: 2000
				})

				this.postList = []
				this.hasMore = false
			} finally {
				this.isLoading = false
				this.isLoadingMore = false
				this.isRefreshing = false
			}
		},

		/**
		 * 下拉刷新
		 */
		refreshPosts() {
			this.isRefreshing = true
			this.loadPostList(true)
		},

		/**
		 * 上拉加载更多
		 */
		loadMorePosts() {
			if (this.hasMore && !this.isLoadingMore) {
				this.loadPostList(false)
			}
		},

		/**
		 * 返回上一页
		 */
		goBack() {
			uni.navigateBack()
		},

		/**
		 * 跳转到帖子详情
		 */
		goToDetail(post) {
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?postId=${post.id}`
			})
		},

		/**
		 * Performance: Image lazy loading event handlers
		 */
		onImageLoad(postId, index) {
			const key = `${postId}_${index}`
			this.$set(this.imageLoaded, key, true)
		},

		onImageError(postId, index) {
			const key = `${postId}_${index}`
			this.$set(this.imageErrors, key, true)
			console.warn(`图片加载失败: Post ${postId}, Image ${index}`)
		},

		/**
		 * 格式化时间
		 */
		formatTime(time) {
			if (!time) return ''

			const now = new Date()
			const target = new Date(time)
			const diff = now - target

			if (diff < 60 * 1000) {
				return '刚刚'
			}

			if (diff < 60 * 60 * 1000) {
				const minutes = Math.floor(diff / (60 * 1000))
				return `${minutes}分钟前`
			}

			if (diff < 24 * 60 * 60 * 1000) {
				const hours = Math.floor(diff / (60 * 60 * 1000))
				return `${hours}小时前`
			}

			if (diff < 7 * 24 * 60 * 60 * 1000) {
				const days = Math.floor(diff / (24 * 60 * 60 * 1000))
				return `${days}天前`
			}

			const year = target.getFullYear()
			const month = target.getMonth() + 1
			const date = target.getDate()

			if (year === now.getFullYear()) {
				return `${month}月${date}日`
			}

			return `${year}年${month}月${date}日`
		}
	}
}
</script>

<style lang="scss" scoped>
.tag-posts-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f7f7f7;
}

.top-nav {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 10px 15px;
	background-color: #ffffff;
	border-bottom: 1px solid #e5e5e5;
	z-index: 100;
}

.nav-left {
	width: 40px;
}

.back-icon {
	font-size: 20px;
	color: #333333;
}

.nav-title {
	font-size: 18px;
	font-weight: 600;
	color: #f33e54;
}

.nav-right {
	width: 40px;
}

.content-scroll {
	flex: 1;
	height: 0; /* 确保flex子元素正确计算高度 */
	padding: 20upx 0;
	box-sizing: border-box;
	overflow: hidden; /* 确保scroll-view正确工作 */
}

.post-item {
	background-color: #ffffff;
	border-radius: 12px;
	padding: 30upx;
	margin: 20upx;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.post-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 12px;
}

.user-info {
	display: flex;
	align-items: center;
}

.user-avatar {
	width: 40px;
	height: 40px;
	border-radius: 20px;
	margin-right: 10px;
}

.username {
	font-size: 14px;
	font-weight: 500;
	color: #333333;
	display: block;
	margin-bottom: 2px;
}

.post-time {
	font-size: 12px;
	color: #999999;
}

.post-content {
	margin-bottom: 12px;
}

.post-title {
	font-size: 16px;
	font-weight: 500;
	color: #333333;
	line-height: 1.4;
	display: block;
	margin-bottom: 8px;
}

.post-summary {
	font-size: 14px;
	color: #666666;
	line-height: 1.4;
}

.post-images {
	display: flex;
	gap: 8px;
	margin-bottom: 12px;
}

.post-image {
	width: 80px;
	height: 80px;
	border-radius: 8px;
	background-color: #f5f5f5;
	opacity: 0.6;
	transition: opacity 0.3s ease-in-out;
}

.post-image.image-loaded {
	opacity: 1;
}

.post-stats {
	display: flex;
	align-items: center;
	gap: 20px;
}

.stat-item {
	display: flex;
	align-items: center;
	gap: 4px;
}

.stat-icon {
	font-size: 14px;
}

.stat-count {
	font-size: 12px;
	color: #999999;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60px 20px;
}

.empty-icon {
	font-size: 48px;
	margin-bottom: 16px;
}

.empty-text {
	font-size: 16px;
	color: #666666;
	margin-bottom: 8px;
}

.empty-hint {
	font-size: 14px;
	color: #999999;
}

.load-more, .no-more {
	display: flex;
	justify-content: center;
	padding: 20px;
	color: #999999;
	font-size: 14px;
}
</style>
