<!-- 用户资料页 - 显示用户信息和内容 -->
<template>
	<!-- 主容器:用户资料展示 -->
	<view class="user-profile-container">
		<!-- 骨架屏 - 加载中显示 -->
		<view v-if="loading && !userProfile" class="skeleton-container">
			<user-skeleton></user-skeleton>
			<post-skeleton v-for="n in 3" :key="'skeleton-' + n"></post-skeleton>
		</view>

		<!-- 用户资料内容 -->
		<view v-else-if="userProfile" class="profile-content">
			<!-- 用户基本信息卡片 -->
			<view class="user-card">
				<!-- 用户头像和基本信息 -->
				<view class="user-header">
					<image class="user-avatar" :src="userProfile.avatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
					<view class="user-info">
						<text class="user-nickname">{{ userProfile.nickname }}</text>
						<text class="user-username">@{{ userProfile.username }}</text>
						<view class="user-stats">
							<text class="stat-item" @click="showFollowingList">
								<text class="stat-number">{{ userProfile.followingCount || 0 }}</text>
								<text class="stat-label">关注</text>
							</text>
							<text class="stat-item" @click="showFollowersList">
								<text class="stat-number">{{ userProfile.followersCount || 0 }}</text>
								<text class="stat-label">粉丝</text>
							</text>
							<text class="stat-item">
								<text class="stat-number">{{ userProfile.postCount || 0 }}</text>
								<text class="stat-label">帖子</text>
							</text>
                            <text class="stat-item">
                                <text class="stat-number">{{ userProfile.totalLikes || 0 }}</text>
                                <text class="stat-label">获赞</text>
                            </text>
                            <text class="stat-item" @click="showCollections">
                                <text class="stat-number">{{ userProfile.collectCount || 0 }}</text>
                                <text class="stat-label">收藏</text>
                            </text>
						</view>
					</view>
				</view>

				<!-- 用户简介 -->
				<view class="user-bio" v-if="userProfile.bio">
					<text class="bio-text">{{ userProfile.bio }}</text>
				</view>

				<!-- 用户验证状态 -->
				<view class="user-verification" v-if="userProfile.isVerified">
					<text class="verification-badge">✓ 已认证</text>
					<text class="verification-text">已通过身份认证的女性用户</text>
				</view>

				<!-- 操作按钮 -->
				<view class="action-buttons" v-if="!isCurrentUser">
					<button class="follow-btn" :class="{ 'following': userProfile.isFollowing }" @click="toggleFollow">
						{{ userProfile.isFollowing ? '已关注' : '关注' }}
					</button>
				</view>

				<!-- 编辑资料按钮(当前用户) -->
				<view class="edit-profile" v-if="isCurrentUser">
					<button class="edit-btn" @click="editProfile">
						编辑资料
					</button>
				</view>
			</view>

			<!-- 内容列表 -->
			<view class="content-list">
				<!-- 帖子列表 -->
				<scroll-view class="posts-scroll"
							 scroll-y="true"
							 @scrolltolower="loadMorePosts"
							 refresher-enabled="true"
							 :refresher-triggered="isRefreshing"
							 @refresherrefresh="refreshPosts">

					<!-- 帖子卡片 -->
					<view class="post-card" v-for="post in userPosts" :key="post.id" @click="goToPostDetail(post.id)">
						<!-- 帖子头部信息 -->
						<view class="post-header">
							<text class="post-time">{{ formatTime(post.createdAt) }}</text>
							<text class="post-type">{{ getPostTypeText(post.type) }}</text>
						</view>

						<!-- 帖子标题 -->
						<view class="post-title" v-if="post.title">
							<text class="title-text">{{ post.title }}</text>
						</view>

						<!-- 帖子内容 -->
						<view class="post-content">
							<text class="content-text">{{ post.content.substring(0, 100) }}{{ post.content.length > 100 ? '...' : '' }}</text>
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

						<!-- 帖子统计信息 -->
						<view class="post-stats">
							<text class="stat">👁 {{ post.viewCount || 0 }}</text>
							<text class="stat">👍 {{ post.likeCount || 0 }}</text>
							<text class="stat">💬 {{ post.commentCount || 0 }}</text>
							<text class="stat">⭐ {{ post.collectCount || 0 }}</text>
						</view>

						<!-- 帖子标签 -->
						<view class="post-tags" v-if="post.tags && post.tags.length">
							<text class="tag" v-for="tag in post.tags.slice(0, 3)" :key="tag.id">
								#{{ tag.name }}
							</text>
						</view>
					</view>

					<!-- 空状态 -->
					<view class="empty-state" v-if="userPosts.length === 0 && !loading">
						<text class="empty-text">{{ isCurrentUser ? '还没有发布帖子' : 'Ta还没有发布帖子' }}</text>
					</view>

					<!-- 加载更多 -->
					<view class="load-more" v-if="hasMorePosts">
						<text class="load-more-text">加载更多...</text>
					</view>
				</scroll-view>

					<!-- 点赞与图片内容已下线，保留帖子列表 -->
			</view>
		</view>

	</view>
</template>

<script>
// 引入API和工具函数
import { userApi, postApi, actionApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'
import PostSkeleton from '@/components/skeleton/post-skeleton.vue'
import UserSkeleton from '@/components/skeleton/user-skeleton.vue'
import EmptyState from '@/components/EmptyState/EmptyState.vue'

export default {
	components: {
		PostSkeleton,
		UserSkeleton,
		EmptyState
	},
	data() {
		return {
			// 页面状态
			loading: true,
			isRefreshing: false,

			// 用户信息
			userProfile: null,
			userId: null,
			currentUser: null,
			isCurrentUser: false,

			// 帖子数据
			userPosts: [],

			// 分页状态
			postsPage: 1,
			pageSize: 20,
			hasMorePosts: true
		}
	},

	onLoad(options) {
		// 获取用户ID参数
		this.userId = options.userId || options.id
		if (!this.userId) {
			uni.showToast({ title: '用户不存在', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 获取当前用户信息
		this.currentUser = getAuthInfo()
		this.isCurrentUser = this.currentUser?.userId == this.userId

		// 加载用户资料
		this.loadUserProfile()
		this.loadUserPosts()
	},

	onShow() {
		// 页面显示时刷新数据
		if (this.userProfile) {
			this.loadUserProfile()
		}
	},

	methods: {
		/**
		 * 加载用户基本信息
		 * 获取用户的资料及统计数据
		 */
		async loadUserProfile() {
			try {
				this.loading = true

				const result = this.isCurrentUser
					? await userApi.getMyProfile()
					: await userApi.getUserProfile(this.userId)

				this.userProfile = result

				uni.setNavigationBarTitle({
					title: this.userProfile.nickname || this.userProfile.username || '用户资料'
				})
			} catch (error) {
				console.error('加载用户资料失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})

				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} finally {
				this.loading = false
			}
		},

		/**
		 * 加载用户发布的帖子
		 * @param {boolean} refresh - 是否为刷新操作
		 */
		async loadUserPosts(refresh = false) {
			try {
				if (refresh) {
					this.postsPage = 1
					this.userPosts = []
					this.hasMorePosts = true
				}

				const params = {
					current: this.postsPage,
					size: this.pageSize
				}

				const result = await postApi.getUserPosts(this.userId, params)
				const newPosts = result.records || []

				if (refresh) {
					this.userPosts = newPosts
				} else {
					this.userPosts = [...this.userPosts, ...newPosts]
				}

				const total = Number(result?.total)
				if (Number.isFinite(total) && total >= 0) {
					this.hasMorePosts = this.userPosts.length < total
				} else {
					this.hasMorePosts = newPosts.length === this.pageSize
				}
			} catch (error) {
				console.error('加载用户帖子失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 切换关注状态
		 */
		async toggleFollow() {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			if (this.isCurrentUser) {
				uni.showToast({ title: '不能关注自己', icon: 'error' })
				return
			}

			try {
				const isFollowing = this.userProfile.isFollowing

				await actionApi.toggleFollow({ targetUserId: this.userId })

				this.userProfile.isFollowing = !isFollowing
				this.userProfile.followersCount += isFollowing ? -1 : 1

				uni.showToast({
					title: isFollowing ? '取消关注成功' : '关注成功',
					icon: 'success'
				})
			} catch (error) {
				console.error('关注操作失败:', error)
				uni.showToast({
					title: error.message || '请求失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 加载更多帖子
		 */
		loadMorePosts() {
			if (!this.hasMorePosts || this.loading) {
				return
			}
			this.postsPage++
			this.loadUserPosts()
		},

		/**
		 * 下拉刷新帖子
		 */
		refreshPosts() {
			this.isRefreshing = true
			this.loadUserPosts(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 跳转到编辑资料
		 */
		editProfile() {
			uni.navigateTo({
				url: '/pages/edit-profile/edit-profile'
			})
		},

		/**
		 * 打开帖子详情
		 * @param {number} postId - 帖子ID
		 */
		goToPostDetail(postId) {
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?id=${postId}`
			})
		},

		/**
		 * 查看关注列表
		 */
		showFollowingList() {
			const title = encodeURIComponent('关注列表')
			uni.navigateTo({
				url: `/pages/follow-list/follow-list?userId=${this.userId}&type=following&title=${title}`
			})
		},

		/**
		 * 查看粉丝列表
		 */
		showFollowersList() {
			const title = encodeURIComponent('粉丝列表')
			uni.navigateTo({
				url: `/pages/follow-list/follow-list?userId=${this.userId}&type=followers&title=${title}`
			})
		},

		/**
		 * 查看收藏列表
		 */
		showCollections() {
			uni.navigateTo({
				url: '/pages/collect-list/collect-list'
			})
		},

		/**
		 * 获取帖子类型文案
		 * @param {number} type - 帖子类型
		 * @returns {string} 类型名称
		 */
		getPostTypeText(type) {
			const typeMap = {
				0: '普通',
				1: '投票',
				2: '提问'
			}
			return typeMap[type] || '普通'
		},

		/**
		 * 格式化时间显示
		 * @param {string} timeString - 时间字符串
		 * @returns {string} 格式化结果
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

			if (diff < minute) {
				return '刚刚'
			} else if (diff < hour) {
				return `${Math.floor(diff / minute)}分钟前`
			} else if (diff < day) {
				return `${Math.floor(diff / hour)}小时前`
			} else if (diff < week) {
				return `${Math.floor(diff / day)}天前`
			}

			return time.toLocaleDateString('zh-CN', {
				month: 'short',
				day: 'numeric'
			})
		}
	}
}</script>

<style scoped>
/* 主容器样式 */
.user-profile-container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

/* 加载状态 */
.loading-container {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 400rpx;
}

.loading-text {
	color: #999;
	font-size: 28rpx;
}

/* 用户资料内容 */
.profile-content {
	padding-bottom: 40rpx;
}

/* 用户卡片 */
.user-card {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 40rpx 30rpx;
}

/* 用户头部信息 */
.user-header {
	display: flex;
	align-items: flex-start;
	margin-bottom: 30rpx;
}

.user-avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	margin-right: 30rpx;
}

.user-info {
	flex: 1;
}

.user-nickname {
	display: block;
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.user-username {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 20rpx;
}

.user-stats {
	display: flex;
	gap: 40rpx;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 6rpx;
}

.stat-number {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.stat-label {
	font-size: 24rpx;
	color: #666;
}

/* 用户简介 */
.user-bio {
	margin-bottom: 30rpx;
}

.bio-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
}

/* 用户验证状态 */
.user-verification {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 30rpx;
	padding: 16rpx;
	background-color: #f0f8ff;
	border-radius: 12rpx;
	border: 1rpx solid #d6e4ff;
}

.verification-badge {
	color: #1890ff;
	font-size: 26rpx;
	font-weight: bold;
}

.verification-text {
	color: #666;
	font-size: 24rpx;
}

/* 操作按钮 */
.action-buttons {
	display: flex;
	gap: 20rpx;
}

.follow-btn {
	flex: 1;
	height: 80rpx;
	background-color: #1890ff;
	color: white;
	font-size: 28rpx;
	border: none;
	border-radius: 40rpx;
}

.follow-btn.following {
	background-color: #f5f5f5;
	color: #666;
	border: 1rpx solid #d9d9d9;
}

/* 编辑资料 */
.edit-profile {
	width: 100%;
}

.edit-btn {
	width: 100%;
	height: 80rpx;
	background-color: #f5f5f5;
	color: #333;
	font-size: 28rpx;
	border: none;
	border-radius: 40rpx;
}

/* 内容列表 */
.content-list {
	min-height: 600rpx;
}

/* 帖子滚动视图 */
.posts-scroll {
	height: calc(100vh - 400rpx);
}

/* 帖子卡片 */
.post-card {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
}

/* 帖子头部 */
.post-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.post-time {
	font-size: 24rpx;
	color: #999;
}

.post-type {
	font-size: 22rpx;
	color: #1890ff;
	background-color: #f0f8ff;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

/* 帖子标题 */
.post-title {
	margin-bottom: 16rpx;
}

.title-text {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
}

/* 帖子内容 */
.post-content {
	margin-bottom: 20rpx;
}

.content-text {
	font-size: 28rpx;
	color: #333;
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
	font-size: 32rpx;
	font-weight: bold;
}

/* 帖子统计 */
.post-stats {
	display: flex;
	gap: 30rpx;
	margin-bottom: 16rpx;
}

.stat {
	font-size: 24rpx;
	color: #666;
}

/* 帖子标签 */
.post-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.tag {
	font-size: 22rpx;
	color: #1890ff;
	background-color: #f0f8ff;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

/* 空状态 */
.empty-state {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 300rpx;
	background-color: white;
	margin: 20rpx 30rpx;
	border-radius: 12rpx;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}

/* 加载更多 */
.load-more {
	text-align: center;
	padding: 40rpx;
	background-color: white;
	margin: 0 30rpx 20rpx;
	border-radius: 12rpx;
}

.load-more-text {
	font-size: 26rpx;
	color: #666;
}

</style>


