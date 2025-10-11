<!-- 我的帖子页 - 简化版本 -->
<template>
	<view class="my-posts-page">
		<view class="top-actions" v-if="postList.length > 0">
			<text class="total-count">共{{ totalCount }}篇帖子</text>
		</view>

		<scroll-view class="posts-scroll"
					 scroll-y="true"
					 show-scrollbar="false"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshPosts"
					 @scrolltolower="loadMorePosts">

			<view class="post-item" v-for="post in postList" :key="post.id">
				<view class="post-card" @click="goToPostDetail(post.id)">
					<view class="post-main">
						<view class="post-header">
							<text class="post-title">{{ post.displayTitle }}</text>
							<text class="post-time">{{ formatTime(post.createdAt) }}</text>
						</view>
						<view class="post-summary" v-if="post.summary">
							<text class="post-text">{{ post.summary }}</text>
						</view>
						<view class="post-meta">
							<text class="post-stats">
								{{ post.viewCount }} 阅读 · {{ post.likeCount }} 赞 · {{ post.commentCount }} 评论
							</text>
						</view>
						<view class="post-tags" v-if="post.tags && post.tags.length">
							<text class="post-tag" v-for="tag in post.tags.slice(0, 3)" :key="tag.id || tag.name">
								#{{ tag.name }}
							</text>
						</view>
					</view>
					<view class="post-thumb" v-if="post.image">
						<image class="thumb-image" :src="post.image" mode="aspectFill"></image>
					</view>
					<view class="post-divider"></view>
					<view class="post-actions">
						<text class="action-btn delete" @click.stop="confirmDelete(post.id)">
							🗑 删除
						</text>
					</view>
				</view>
			</view>

			<view class="empty-state" v-if="postList.length === 0 && !loading">
				<text class="empty-icon">📭</text>
				<text class="empty-text">还没有发布过帖子</text>
				<text class="empty-tip">去首页分享你的故事吧</text>
				<button class="navigate-btn" @click="goToExplore">去首页</button>
			</view>

			<view class="load-more" v-if="hasMoreData && postList.length > 0">
				<text class="load-more-text">{{ loading ? '加载中...' : '上拉加载更多' }}</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { postApi } from '@/utils/api.js'
import { getAuthInfo, getUserId } from '@/utils/auth.js'

export default {
	data() {
		return {
			loading: false,
			isRefreshing: false,
			postList: [],
			totalCount: 0,
			currentPage: 1,
			pageSize: 20,
			hasMoreData: true,
			currentUser: null
		}
	},

	onLoad() {
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

		this.loadMyPosts(true)
	},

	onShow() {
		if (this.postList.length > 0 && !this.loading) {
			this.refreshPosts()
		}
	},

	methods: {
		async loadMyPosts(refresh = false) {
			if (refresh) {
				this.currentPage = 1
				this.hasMoreData = true
				this.totalCount = 0
			} else if (!this.hasMoreData || this.loading) {
				return
			}

			const page = this.currentPage

			try {
				this.loading = true

				const userId = this.currentUser?.userId || getUserId()
				if (!userId) {
					throw new Error('用户未登录')
				}

				const params = {
					current: page,
					size: this.pageSize
				}

				const result = await postApi.getUserPosts(userId, params)
				const records = Array.isArray(result?.records) ? result.records : []
				const normalized = records.map(item => this.transformPostItem(item))

				if (refresh) {
					this.postList = normalized
				} else {
					this.postList = [...this.postList, ...normalized]
				}

				const total = Number(result?.total ?? this.postList.length)
				this.totalCount = Number.isFinite(total) ? total : this.postList.length
				this.hasMoreData = this.postList.length < this.totalCount
				this.currentPage = this.hasMoreData ? page + 1 : page
			} catch (error) {
				console.error('加载帖子失败:', error)
				uni.showToast({
					title: error?.message || '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
				if (refresh) {
					this.isRefreshing = false
				}
			}
		},

		async refreshPosts() {
			if (this.loading) return
			this.isRefreshing = true
			await this.loadMyPosts(true)
		},

		async loadMorePosts() {
			await this.loadMyPosts(false)
		},

		transformPostItem(raw) {
			const contentText = (raw?.content || '').trim()
			const summary = contentText.length > 120 ? `${contentText.substring(0, 120)}...` : contentText
			const title = raw?.title?.trim()
			const displayTitle = title ||
				(contentText.length > 0 ? `${contentText.substring(0, 30)}...` : '未命名帖子')

			const images = Array.isArray(raw?.imageUrls) ? raw.imageUrls : []
			const firstImage = images.length > 0 ? images[0] : ''

			return {
				id: raw?.id,
				displayTitle,
				summary,
				createdAt: raw?.createdAt || raw?.updatedAt || '',
				likeCount: raw?.likeCount ?? 0,
				commentCount: raw?.commentCount ?? 0,
				viewCount: raw?.viewCount ?? 0,
				tags: Array.isArray(raw?.tags) ? raw.tags : [],
				image: firstImage
			}
		},

		goToPostDetail(postId) {
			if (!postId) return
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?id=${postId}`
			})
		},

		goToExplore() {
			uni.switchTab({
				url: '/pages/tabbar/tabbar-1/tabbar-1'
			})
		},

		async confirmDelete(postId) {
			if (!postId) return

			try {
				await uni.showModal({
					title: '确认删除',
					content: '删除后无法恢复，确定要删除这篇帖子吗？',
					confirmText: '删除',
					confirmColor: '#ff4757'
				})

				await postApi.deleteMyPost(postId)
				this.postList = this.postList.filter(post => post.id !== postId)
				this.totalCount = Math.max(0, this.totalCount - 1)

				uni.showToast({ title: '删除成功', icon: 'success' })

				if (this.postList.length === 0 && this.hasMoreData) {
					await this.loadMyPosts(true)
				}
			} catch (error) {
				if (error?.message === 'cancel') {
					return
				}
				console.error('删除帖子失败:', error)
				uni.showToast({
					title: '删除失败',
					icon: 'error'
				})
			}
		},

		formatTime(timeString) {
			if (!timeString) return ''

			const time = new Date(timeString)
			if (Number.isNaN(time.getTime())) {
				return ''
			}

			const now = new Date()
			const diff = now - time
			const minute = 60 * 1000
			const hour = 60 * minute
			const day = 24 * hour
			const week = 7 * day

			if (diff < hour) {
				const minutes = Math.max(1, Math.floor(diff / minute))
				return `${minutes}分钟前`
			}
			if (diff < day) {
				return `${Math.floor(diff / hour)}小时前`
			}
			if (diff < week) {
				return `${Math.floor(diff / day)}天前`
			}
			return time.toLocaleDateString('zh-CN', {
				month: 'short',
				day: 'numeric'
			})
		}
	}
}
</script>

<style scoped>
.my-posts-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f7f8fa;
}

.top-actions {
	display: flex;
	align-items: center;
	padding: 24rpx 32rpx;
	background-color: #ffffff;
	border-bottom: 1rpx solid #f0f0f0;
}

.total-count {
	font-size: 28rpx;
	color: #333333;
	font-weight: 500;
}

.posts-scroll {
	flex: 1;
	padding: 0 24rpx 24rpx;
	box-sizing: border-box;
}

.post-item {
	margin-top: 24rpx;
}

.post-card {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding: 24rpx;
	background-color: #ffffff;
	border-radius: 18rpx;
	box-shadow: 0 12rpx 30rpx rgba(15, 25, 40, 0.06);
}

.post-main {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.post-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 16rpx;
}

.post-title {
	flex: 1;
	font-size: 32rpx;
	font-weight: 600;
	color: #1f2f3d;
	line-height: 1.4;
}

.post-time {
	font-size: 24rpx;
	color: #9aa0a6;
}

.post-summary {
	color: #4a5568;
	font-size: 26rpx;
	line-height: 1.6;
}

.post-text {
	display: inline-block;
}

.post-meta {
	color: #6b7280;
	font-size: 24rpx;
}

.post-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.post-tag {
	font-size: 22rpx;
	color: #1890ff;
	background-color: rgba(24, 144, 255, 0.12);
	padding: 6rpx 12rpx;
	border-radius: 16rpx;
}

.post-thumb {
	width: 164rpx;
	height: 164rpx;
	border-radius: 16rpx;
	overflow: hidden;
}

.thumb-image {
	width: 100%;
	height: 100%;
}

.post-divider {
	height: 1rpx;
	background: linear-gradient(90deg, rgba(255, 255, 255, 0), #eef1f3 40%, rgba(255, 255, 255, 0));
}

.post-actions {
	display: flex;
	justify-content: flex-end;
	align-items: center;
	padding-top: 12rpx;
}

.action-btn {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	font-size: 26rpx;
	color: #ff4d4f;
	background-color: rgba(255, 77, 79, 0.12);
	padding: 12rpx 24rpx;
	border-radius: 999rpx;
}

.action-btn.delete:active {
	background-color: rgba(255, 77, 79, 0.2);
}

.empty-state {
	margin-top: 120rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
	color: #9ca3af;
}

.empty-icon {
	font-size: 96rpx;
}

.empty-text {
	font-size: 30rpx;
	color: #1f2933;
	font-weight: 500;
}

.empty-tip {
	font-size: 26rpx;
	color: #6b7280;
}

.navigate-btn {
	margin-top: 24rpx;
	padding: 16rpx 48rpx;
	font-size: 28rpx;
	color: #ffffff;
	background-color: #1890ff;
	border: none;
	border-radius: 999rpx;
}

.load-more {
	padding: 32rpx 0;
	display: flex;
	justify-content: center;
	color: #9aa0a6;
}

.load-more-text {
	font-size: 26rpx;
}
</style>
