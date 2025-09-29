<!-- 用户资料页 - 显示用户信息和内容 -->
<template>
	<!-- 主容器：用户资料展示 -->
	<view class="user-profile-container">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<text class="loading-text">加载中...</text>
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
								<text class="stat-number">{{ userProfile.followerCount || 0 }}</text>
								<text class="stat-label">粉丝</text>
							</text>
							<text class="stat-item">
								<text class="stat-number">{{ userProfile.postCount || 0 }}</text>
								<text class="stat-label">帖子</text>
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
					<button class="message-btn" @click="sendMessage">
						私信
					</button>
					<button class="more-btn" @click="showMoreActions">
						⋯
					</button>
				</view>

				<!-- 编辑资料按钮（当前用户） -->
				<view class="edit-profile" v-if="isCurrentUser">
					<button class="edit-btn" @click="editProfile">
						编辑资料
					</button>
				</view>
			</view>

			<!-- 内容导航栏 -->
			<view class="content-tabs">
				<view class="tab-item" :class="{ 'active': currentTab === 'posts' }" @click="switchTab('posts')">
					<text class="tab-text">帖子</text>
					<text class="tab-count">({{ userProfile.postCount || 0 }})</text>
				</view>
				<view class="tab-item" :class="{ 'active': currentTab === 'liked' }" @click="switchTab('liked')" v-if="isCurrentUser">
					<text class="tab-text">点赞</text>
					<text class="tab-count">({{ userProfile.likedCount || 0 }})</text>
				</view>
				<view class="tab-item" :class="{ 'active': currentTab === 'media' }" @click="switchTab('media')">
					<text class="tab-text">图片</text>
				</view>
			</view>

			<!-- 内容列表 -->
			<view class="content-list">
				<!-- 帖子列表 -->
				<scroll-view class="posts-scroll"
							 scroll-y="true"
							 v-if="currentTab === 'posts'"
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

				<!-- 点赞列表（仅当前用户可见） -->
				<scroll-view class="liked-scroll"
							 scroll-y="true"
							 v-if="currentTab === 'liked' && isCurrentUser"
							 @scrolltolower="loadMoreLiked"
							 refresher-enabled="true"
							 :refresher-triggered="isRefreshing"
							 @refresherrefresh="refreshLiked">

					<!-- 点赞的帖子卡片 -->
					<view class="liked-card" v-for="post in likedPosts" :key="post.id" @click="goToPostDetail(post.id)">
						<view class="liked-content">
							<text class="liked-title">{{ post.title || post.content.substring(0, 50) }}</text>
							<text class="liked-author">@{{ post.username }}</text>
							<text class="liked-time">{{ formatTime(post.likedAt) }}</text>
						</view>
						<image class="liked-image"
							   :src="post.imageUrls?.[0] || '/static/img/placeholder.png'"
							   mode="aspectFill"
							   v-if="post.imageUrls?.[0]">
						</image>
					</view>

					<!-- 空状态 -->
					<view class="empty-state" v-if="likedPosts.length === 0 && !loading">
						<text class="empty-text">还没有点赞任何帖子</text>
					</view>
				</scroll-view>

				<!-- 图片瀑布流 -->
				<view class="media-grid" v-if="currentTab === 'media'">
					<image class="media-item"
						   v-for="(image, index) in mediaImages"
						   :key="index"
						   :src="image.url"
						   mode="aspectFill"
						   @click="previewImage(index)">
					</image>

					<!-- 空状态 -->
					<view class="empty-state" v-if="mediaImages.length === 0 && !loading">
						<text class="empty-text">{{ isCurrentUser ? '还没有发布图片' : 'Ta还没有发布图片' }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 更多操作弹窗 -->
		<uni-popup ref="moreActions" type="bottom">
			<view class="popup-content">
				<view class="popup-item" @click="shareProfile">分享用户</view>
				<view class="popup-item" @click="blockUser">拉黑用户</view>
				<view class="popup-item" @click="reportUser">举报用户</view>
				<view class="popup-item cancel" @click="$refs.moreActions.close()">取消</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
// 引入API和工具函数
import { userApi, postApi, actionApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
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

			// 内容标签
			currentTab: 'posts', // posts | liked | media

			// 帖子数据
			userPosts: [],
			likedPosts: [],
			mediaImages: [],

			// 分页状态
			postsPage: 1,
			likedPage: 1,
			pageSize: 20,
			hasMorePosts: true,
			hasMoreLiked: true
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
		this.loadUserContent()
	},

	onShow() {
		// 页面显示时刷新数据
		if (this.userProfile) {
			this.loadUserProfile()
		}
	},

	methods: {
		/**
		 * 加载用户资料信息
		 * 获取用户的基本信息和统计数据
		 */
		async loadUserProfile() {
			try {
				this.loading = true

				// 根据是否为当前用户选择不同的API
				const response = this.isCurrentUser
					? await userApi.getMyProfile()
					: await userApi.getUserProfile(this.userId)

				if (response.code === 200) {
					this.userProfile = response.data

					// 设置页面标题
					uni.setNavigationBarTitle({
						title: this.userProfile.nickname || this.userProfile.username || '用户资料'
					})
				} else {
					throw new Error(response.message || '获取用户信息失败')
				}
			} catch (error) {
				console.error('加载用户资料失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})

				// 如果用户不存在，返回上一页
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} finally {
				this.loading = false
			}
		},

		/**
		 * 加载用户内容（根据当前标签）
		 */
		loadUserContent() {
			switch (this.currentTab) {
				case 'posts':
					this.loadUserPosts()
					break
				case 'liked':
					if (this.isCurrentUser) {
						this.loadLikedPosts()
					}
					break
				case 'media':
					this.loadMediaImages()
					break
			}
		},

		/**
		 * 加载用户发布的帖子
		 * @param {boolean} refresh - 是否刷新数据
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
					size: this.pageSize,
					userId: this.userId,
					sort: 'time'
				}

				const response = await postApi.getPostList(params)

				if (response.code === 200) {
					const newPosts = response.data.records || []

					if (refresh) {
						this.userPosts = newPosts
					} else {
						this.userPosts = [...this.userPosts, ...newPosts]
					}

					this.hasMorePosts = this.userPosts.length < response.data.total
				} else {
					throw new Error(response.message || '获取帖子失败')
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
		 * 加载用户点赞的帖子（仅当前用户）
		 * @param {boolean} refresh - 是否刷新数据
		 */
		async loadLikedPosts(refresh = false) {
			if (!this.isCurrentUser) return

			try {
				if (refresh) {
					this.likedPage = 1
					this.likedPosts = []
					this.hasMoreLiked = true
				}

				// 这里需要后端提供获取用户点赞帖子的API
				// 暂时使用空数组模拟
				const mockLikedPosts = []

				if (refresh) {
					this.likedPosts = mockLikedPosts
				} else {
					this.likedPosts = [...this.likedPosts, ...mockLikedPosts]
				}

				this.hasMoreLiked = false
			} catch (error) {
				console.error('加载点赞帖子失败:', error)
			}
		},

		/**
		 * 加载用户发布的图片
		 */
		async loadMediaImages() {
			try {
				// 从用户帖子中提取图片
				const imageUrls = []
				this.userPosts.forEach(post => {
					if (post.imageUrls && post.imageUrls.length > 0) {
						post.imageUrls.forEach(url => {
							imageUrls.push({
								url: url,
								postId: post.id,
								postTitle: post.title || post.content.substring(0, 30)
							})
						})
					}
				})

				this.mediaImages = imageUrls
			} catch (error) {
				console.error('加载图片失败:', error)
			}
		},

		/**
		 * 切换内容标签
		 * @param {string} tab - 标签名称
		 */
		switchTab(tab) {
			if (this.currentTab !== tab) {
				this.currentTab = tab
				this.loadUserContent()
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
				// 这里需要后端提供关注/取消关注的API
				// 暂时模拟操作
				const isFollowing = this.userProfile.isFollowing

				// 模拟API调用
				const response = {
					code: 200,
					message: isFollowing ? '取消关注成功' : '关注成功'
				}

				if (response.code === 200) {
					this.userProfile.isFollowing = !isFollowing
					this.userProfile.followerCount += isFollowing ? -1 : 1

					uni.showToast({
						title: response.message,
						icon: 'success'
					})
				} else {
					throw new Error(response.message || '操作失败')
				}
			} catch (error) {
				console.error('关注操作失败:', error)
				uni.showToast({
					title: error.message || '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 加载更多帖子
		 */
		loadMorePosts() {
			if (this.hasMorePosts && this.currentTab === 'posts') {
				this.postsPage++
				this.loadUserPosts()
			}
		},

		/**
		 * 加载更多点赞
		 */
		loadMoreLiked() {
			if (this.hasMoreLiked && this.currentTab === 'liked') {
				this.likedPage++
				this.loadLikedPosts()
			}
		},

		/**
		 * 刷新帖子
		 */
		refreshPosts() {
			this.isRefreshing = true
			this.loadUserPosts(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 刷新点赞
		 */
		refreshLiked() {
			this.isRefreshing = true
			this.loadLikedPosts(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 发送私信
		 */
		sendMessage() {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			uni.navigateTo({
				url: `/pages/chat/chat?userId=${this.userId}&username=${this.userProfile.username}`
			})
		},

		/**
		 * 编辑资料
		 */
		editProfile() {
			uni.navigateTo({
				url: '/pages/edit-profile/edit-profile'
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
		 * 显示关注列表
		 */
		showFollowingList() {
			uni.navigateTo({
				url: `/pages/follow-list/follow-list?userId=${this.userId}&type=following&title=关注列表`
			})
		},

		/**
		 * 显示粉丝列表
		 */
		showFollowersList() {
			uni.navigateTo({
				url: `/pages/follow-list/follow-list?userId=${this.userId}&type=followers&title=粉丝列表`
			})
		},

		/**
		 * 预览图片
		 * @param {number} index - 图片索引
		 */
		previewImage(index) {
			const urls = this.mediaImages.map(img => img.url)
			uni.previewImage({
				urls: urls,
				current: index
			})
		},

		/**
		 * 显示更多操作
		 */
		showMoreActions() {
			this.$refs.moreActions.open()
		},

		/**
		 * 分享用户资料
		 */
		shareProfile() {
			this.$refs.moreActions.close()

			const shareData = {
				title: `${this.userProfile.nickname}的个人资料`,
				summary: this.userProfile.bio || `来看看${this.userProfile.nickname}在Herizon的动态吧`,
				href: `https://herizon.com/user/${this.userId}`,
				imageUrl: this.userProfile.avatar || '/static/img/logo.png'
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
		 * 拉黑用户
		 */
		async blockUser() {
			this.$refs.moreActions.close()

			try {
				await uni.showModal({
					title: '确认拉黑',
					content: `确定要拉黑用户 ${this.userProfile.nickname} 吗？拉黑后将不再看到Ta的内容。`,
					confirmText: '拉黑',
					confirmColor: '#ff4757'
				})

				// 这里需要后端提供拉黑用户的API
				uni.showToast({
					title: '已拉黑该用户',
					icon: 'success'
				})

				// 返回上一页
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} catch (error) {
				// 用户取消操作
			}
		},

		/**
		 * 举报用户
		 */
		reportUser() {
			this.$refs.moreActions.close()
			uni.navigateTo({
				url: `/pages/report/report?targetType=user&targetId=${this.userId}`
			})
		},

		/**
		 * 获取帖子类型文本
		 * @param {number} type - 帖子类型
		 * @returns {string} 类型文本
		 */
		getPostTypeText(type) {
			const typeMap = {
				0: '普通',
				1: '投票',
				2: '公告'
			}
			return typeMap[type] || '普通'
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

			if (diff < minute) {
				return '刚刚'
			} else if (diff < hour) {
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
		}
	}
}
</script>

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

.message-btn {
	flex: 1;
	height: 80rpx;
	background-color: white;
	color: #1890ff;
	font-size: 28rpx;
	border: 1rpx solid #1890ff;
	border-radius: 40rpx;
}

.more-btn {
	width: 80rpx;
	height: 80rpx;
	background-color: #f5f5f5;
	color: #666;
	font-size: 32rpx;
	border: none;
	border-radius: 40rpx;
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

/* 内容导航栏 */
.content-tabs {
	display: flex;
	background-color: white;
	margin-bottom: 20rpx;
}

.tab-item {
	flex: 1;
	display: flex;
	justify-content: center;
	align-items: center;
	gap: 8rpx;
	padding: 30rpx 20rpx;
	border-bottom: 4rpx solid transparent;
}

.tab-item.active {
	border-bottom-color: #1890ff;
}

.tab-text {
	font-size: 28rpx;
	color: #333;
}

.tab-item.active .tab-text {
	color: #1890ff;
	font-weight: bold;
}

.tab-count {
	font-size: 24rpx;
	color: #999;
}

.tab-item.active .tab-count {
	color: #1890ff;
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

/* 点赞列表 */
.liked-scroll {
	height: calc(100vh - 400rpx);
	padding: 0 30rpx;
}

.liked-card {
	display: flex;
	align-items: center;
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
}

.liked-content {
	flex: 1;
	margin-right: 20rpx;
}

.liked-title {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 12rpx;
	line-height: 1.4;
}

.liked-author {
	display: block;
	font-size: 24rpx;
	color: #1890ff;
	margin-bottom: 8rpx;
}

.liked-time {
	font-size: 22rpx;
	color: #999;
}

.liked-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 8rpx;
}

/* 图片网格 */
.media-grid {
	display: grid;
	grid-template-columns: 1fr 1fr 1fr;
	gap: 4rpx;
	padding: 0 30rpx;
}

.media-item {
	width: 100%;
	height: 200rpx;
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

/* 弹窗样式 */
.popup-content {
	background-color: white;
	border-radius: 24rpx 24rpx 0 0;
	padding: 40rpx 0;
}

.popup-item {
	text-align: center;
	padding: 30rpx;
	font-size: 30rpx;
	color: #333;
	border-bottom: 1rpx solid #f0f0f0;
}

.popup-item:last-child {
	border-bottom: none;
}

.popup-item.cancel {
	color: #999;
	margin-top: 20rpx;
}
</style>