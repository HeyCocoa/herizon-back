<!-- 搜索页面 - 仅搜索帖子 -->
<template>
	<view class="search-container">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

		<view class="nav-bar">
			<view class="nav-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="nav-center">
				<text class="nav-title">搜索</text>
			</view>
			<view class="nav-right"></view>
		</view>

		<view class="search-bar">
			<view class="search-input-wrapper">
				<text class="search-icon">🔍</text>
				<input
					class="search-input"
					v-model="searchKeyword"
					placeholder="搜索帖子..."
					:focus="true"
					@confirm="performSearch"
					confirm-type="search">
				</input>
				<text class="clear-btn" v-if="searchKeyword" @click="clearSearch">✕</text>
			</view>
			<text class="search-btn" @click="performSearch">搜索</text>
		</view>

		<view class="search-suggestions" v-if="!hasSearched">
			<view class="suggestion-section" v-if="searchHistory.length > 0">
				<view class="section-header">
					<text class="section-title">搜索历史</text>
					<text class="clear-history" @click="clearHistory">清空</text>
				</view>
				<view class="history-tags">
					<text class="history-tag"
						  v-for="(item, index) in searchHistory"
						  :key="index"
						  @click="searchHistoryItem(item)">
						{{ item }}
					</text>
				</view>
			</view>

			<view class="suggestion-section">
				<view class="section-header">
					<text class="section-title">热门搜索</text>
				</view>
				<view class="hot-tags">
					<text class="hot-tag"
						  v-for="(item, index) in hotSearches"
						  :key="index"
						  @click="searchHotItem(item)">
						{{ item }}
					</text>
				</view>
			</view>
		</view>

		<scroll-view class="results-scroll"
					 v-if="hasSearched"
					 scroll-y="true"
					 @scrolltolower="loadMoreResults"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshResults"
					 :style="{ height: scrollViewHeight + 'px' }">

			<block v-if="loading && searchResults.length === 0">
				<view class="skeleton-item" v-for="n in 5" :key="'skeleton-' + n">
					<view class="skeleton-avatar"></view>
					<view class="skeleton-content">
						<view class="skeleton-line"></view>
						<view class="skeleton-line short"></view>
					</view>
				</view>
			</block>

			<view v-else class="posts-results">
				<view class="post-result-item"
					  v-for="post in searchResults"
					  :key="post.id"
					  @click="goToPostDetail(post.id)">
					<view class="post-author">
						<image class="author-avatar" :src="post.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
						<text class="author-name">{{ post.nickname || post.username }}</text>
						<text class="post-time">{{ formatTime(post.createdAt) }}</text>
					</view>

					<view class="post-title" v-if="post.title">
						<text class="title-content">{{ post.title }}</text>
					</view>

					<view class="post-content">
						<text class="content-text">{{ truncateContent(post.content, 150) }}</text>
						<text class="read-more" v-if="post.content.length > 150">...阅读全文</text>
					</view>

					<view class="post-images" v-if="post.imageUrls && post.imageUrls.length">
						<image class="result-image"
							   :src="post.imageUrls[0]"
							   mode="aspectFill">
						</image>
						<text class="more-images" v-if="post.imageUrls.length > 1">+{{ post.imageUrls.length - 1 }}</text>
					</view>

					<view class="post-stats">
						<text class="stat-item">👁 {{ post.viewCount || 0 }}</text>
						<text class="stat-item">👍 {{ post.likeCount || 0 }}</text>
						<text class="stat-item">💬 {{ post.commentCount || 0 }}</text>
					</view>

					<view class="post-tags" v-if="post.tags && post.tags.length">
						<text class="post-tag" v-for="tag in post.tags.slice(0, 3)" :key="tag.id">
							#{{ tag.name }}
						</text>
					</view>
				</view>
			</view>

			<view class="empty-results" v-if="!hasResults && !loading && hasSearched">
				<text class="empty-icon">🔍</text>
				<text class="empty-text">未找到相关帖子</text>
				<text class="empty-tip">试试其他关键词</text>
			</view>

			<view class="load-more" v-if="hasMoreResults && !loading">
				<text class="load-more-text">加载更多...</text>
			</view>

			<view class="no-more" v-if="!hasMoreResults && hasResults && !loading">
				<text class="no-more-text">没有更多了</text>
			</view>
		</scroll-view>

		<view class="loading-overlay" v-if="loading && hasSearched">
			<text class="loading-text">搜索中...</text>
		</view>
	</view>
</template>

<script>
import { postApi } from '@/utils/api.js'

export default {
	data() {
		return {
			statusBarHeight: 0,
			scrollViewHeight: 0,
			searchKeyword: '',
			hasSearched: false,
			loading: false,
			isRefreshing: false,
			searchResults: [],
			totalResults: 0,
			currentPage: 1,
			pageSize: 20,
			hasMoreResults: false,
			searchHistory: [],
			hotSearches: [
				'职场发展', '创业经验', '女性权益', '技能提升',
				'工作平衡', '投资理财', '健康生活', '学习成长'
			]
		}
	},

	computed: {
		hasResults() {
			return this.searchResults.length > 0
		}
	},

	onLoad(options) {
		this.getSystemInfo()
		if (options.keyword) {
			this.searchKeyword = decodeURIComponent(options.keyword)
			this.performSearch()
		}
		this.loadSearchHistory()
	},

	methods: {
		getSystemInfo() {
			try {
				const systemInfo = uni.getSystemInfoSync()
				this.statusBarHeight = systemInfo.statusBarHeight || 20
				this.scrollViewHeight = systemInfo.windowHeight - 100
			} catch (error) {
				console.error('获取系统信息失败:', error)
				this.statusBarHeight = 20
				this.scrollViewHeight = 600
			}
		},

		async performSearch() {
			const keyword = this.searchKeyword.trim()
			if (!keyword) {
				uni.showToast({ title: '请输入搜索关键词', icon: 'error' })
				return
			}

			this.loading = true
			this.hasSearched = true
			this.saveSearchHistory(keyword)
			this.searchResults = []
			this.currentPage = 1
			this.hasMoreResults = false

			try {
				await this.searchPosts(keyword, false)
			} catch (error) {
				console.error('搜索失败:', error)
				uni.showToast({
					title: error.message || '搜索失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		async searchPosts(keyword, loadMore = false) {
			try {
				const params = {
					keyword: keyword,
					current: loadMore ? this.currentPage : 1,
					size: this.pageSize
				}

				const pageResult = await postApi.searchPosts(params)
				const newPosts = pageResult.records || []

				if (loadMore) {
					this.searchResults = [...this.searchResults, ...newPosts]
				} else {
					this.searchResults = newPosts
					this.totalResults = pageResult.total || 0
				}

				this.currentPage = pageResult.current || 1
				this.hasMoreResults = this.currentPage < (pageResult.pages || 1)
			} catch (error) {
				console.error('搜索帖子失败:', error)
				throw error
			}
		},

		async loadMoreResults() {
			if (!this.hasMoreResults || this.loading) return

			this.currentPage++
			this.loading = true

			try {
				await this.searchPosts(this.searchKeyword, true)
			} catch (error) {
				console.error('加载更多失败:', error)
				this.currentPage--
			} finally {
				this.loading = false
			}
		},

		async refreshResults() {
			this.isRefreshing = true
			try {
				await this.performSearch()
			} finally {
				this.isRefreshing = false
			}
		},

		clearSearch() {
			this.searchKeyword = ''
			this.hasSearched = false
			this.searchResults = []
			this.totalResults = 0
			this.currentPage = 1
			this.hasMoreResults = false
		},

		goBack() {
			uni.navigateBack()
		},

		searchHistoryItem(keyword) {
			this.searchKeyword = keyword
			this.$nextTick(() => {
				this.performSearch()
			})
		},

		searchHotItem(keyword) {
			this.searchKeyword = keyword
			this.$nextTick(() => {
				this.performSearch()
			})
		},

		goToPostDetail(postId) {
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?id=${postId}`
			})
		},

		loadSearchHistory() {
			try {
				const history = uni.getStorageSync('searchHistory') || []
				this.searchHistory = history.slice(0, 10)
			} catch (error) {
				console.error('加载搜索历史失败:', error)
				this.searchHistory = []
			}
		},

		saveSearchHistory(keyword) {
			try {
				let history = uni.getStorageSync('searchHistory') || []
				history = history.filter(item => item !== keyword)
				history.unshift(keyword)
				history = history.slice(0, 20)
				uni.setStorageSync('searchHistory', history)
				this.searchHistory = history.slice(0, 10)
			} catch (error) {
				console.error('保存搜索历史失败:', error)
			}
		},

		clearHistory() {
			uni.showModal({
				title: '确认清空',
				content: '确定要清空所有搜索历史吗?',
				success: (res) => {
					if (res.confirm) {
						uni.removeStorageSync('searchHistory')
						this.searchHistory = []
						uni.showToast({ title: '已清空', icon: 'success' })
					}
				}
			})
		},

		truncateContent(content, maxLength) {
			if (!content || content.length <= maxLength) {
				return content || ''
			}
			return content.substring(0, maxLength)
		},

		formatTime(timeString) {
			if (!timeString) return ''

			const now = new Date()
			const time = new Date(timeString)
			const diff = now - time

			const minute = 60 * 1000
			const hour = 60 * minute
			const day = 24 * hour

			if (diff < hour) {
				return `${Math.floor(diff / minute)}分钟前`
			} else if (diff < day) {
				return `${Math.floor(diff / hour)}小时前`
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
.search-container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

.status-bar {
	background-color: white;
	width: 100%;
}

.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
	padding: 0 30rpx;
}

.nav-left {
	width: 80rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 36rpx;
	color: #333;
	font-weight: bold;
}

.nav-center {
	flex: 1;
	text-align: center;
}

.nav-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.nav-right {
	width: 80rpx;
	height: 60rpx;
}

.search-bar {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
}

.search-input-wrapper {
	flex: 1;
	display: flex;
	align-items: center;
	background-color: #f5f5f5;
	border-radius: 25rpx;
	padding: 16rpx 24rpx;
	margin-right: 20rpx;
}

.search-icon {
	font-size: 28rpx;
	color: #999;
	margin-right: 12rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.clear-btn {
	font-size: 24rpx;
	color: #999;
	padding: 8rpx;
}

.search-btn {
	font-size: 28rpx;
	color: #1890ff;
	padding: 8rpx;
}

.search-suggestions {
	padding: 30rpx;
}

.suggestion-section {
	margin-bottom: 40rpx;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.clear-history {
	font-size: 26rpx;
	color: #666;
}

.history-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.history-tag {
	padding: 12rpx 20rpx;
	background-color: white;
	color: #666;
	font-size: 26rpx;
	border-radius: 20rpx;
	border: 1rpx solid #e0e0e0;
}

.hot-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.hot-tag {
	padding: 12rpx 20rpx;
	background-color: #fff3cd;
	color: #856404;
	font-size: 26rpx;
	border-radius: 20rpx;
	border: 1rpx solid #ffeaa7;
}

.results-scroll {
	background-color: #f5f5f5;
}

.skeleton-item {
	display: flex;
	padding: 30rpx;
	background-color: white;
	margin-bottom: 20rpx;
}

.skeleton-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background-color: #e0e0e0;
	margin-right: 16rpx;
}

.skeleton-content {
	flex: 1;
}

.skeleton-line {
	height: 28rpx;
	background-color: #e0e0e0;
	border-radius: 4rpx;
	margin-bottom: 12rpx;
}

.skeleton-line.short {
	width: 60%;
}

.posts-results {
	padding: 0 30rpx;
}

.post-result-item {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
}

.post-author {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.author-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}

.author-name {
	flex: 1;
	font-size: 26rpx;
	color: #333;
	margin-right: 16rpx;
}

.post-time {
	font-size: 22rpx;
	color: #999;
}

.post-title {
	margin-bottom: 16rpx;
}

.title-content {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
}

.post-content {
	margin-bottom: 20rpx;
}

.content-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
}

.read-more {
	color: #1890ff;
	font-size: 26rpx;
	margin-left: 8rpx;
}

.post-images {
	position: relative;
	margin-bottom: 20rpx;
}

.result-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 8rpx;
}

.more-images {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	background-color: rgba(0, 0, 0, 0.6);
	color: white;
	font-size: 20rpx;
	padding: 4rpx 8rpx;
	border-radius: 8rpx;
}

.post-stats {
	display: flex;
	gap: 30rpx;
	margin-bottom: 16rpx;
}

.stat-item {
	font-size: 24rpx;
	color: #666;
}

.post-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.post-tag {
	font-size: 22rpx;
	color: #1890ff;
	background-color: #f0f8ff;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

.empty-results {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 400rpx;
	background-color: white;
	margin: 20rpx 30rpx;
	border-radius: 12rpx;
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
}

.load-more {
	text-align: center;
	padding: 40rpx;
}

.load-more-text {
	font-size: 26rpx;
	color: #666;
}

.no-more {
	text-align: center;
	padding: 40rpx;
}

.no-more-text {
	font-size: 26rpx;
	color: #999;
}

.loading-overlay {
	position: fixed;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	background-color: rgba(0, 0, 0, 0.7);
	color: white;
	padding: 20rpx 40rpx;
	border-radius: 12rpx;
	z-index: 1000;
}

.loading-text {
	font-size: 28rpx;
}
</style>
