<!-- 帖子详情页 - 展示完整帖子内容和评论系统 -->
<template>
	<!-- 主容器：帖子详情和评论 -->
	<view class="post-detail-container">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 帖子内容 -->
		<view v-else-if="postDetail" class="post-content">
			<!-- 用户信息栏 -->
			<view class="user-info" @click="goToUserProfile(postDetail.userId)">
				<image class="avatar" :src="postDetail.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
				<view class="user-meta">
					<text class="username">{{ postDetail.username }}</text>
					<text class="post-time">{{ formatTime(postDetail.createdAt) }}</text>
				</view>
				<view class="more-btn" @click.stop="showMoreActions">
					<text class="more-icon">⋯</text>
				</view>
			</view>

			<!-- 帖子标题 -->
			<view class="post-title" v-if="postDetail.title">
				<text class="title-text">{{ postDetail.title }}</text>
			</view>

			<!-- 帖子内容 -->
			<view class="post-text">
				<text class="content-text">{{ postDetail.content }}</text>
			</view>

			<!-- 图片展示 -->
			<view class="image-gallery" v-if="postDetail.imageUrls && postDetail.imageUrls.length">
				<view class="image-grid" :class="`grid-${Math.min(postDetail.imageUrls.length, 3)}`">
					<image
						v-for="(imageUrl, index) in postDetail.imageUrls.slice(0, 9)"
						:key="index"
						class="post-image"
						:src="imageUrl"
						mode="aspectFill"
						@click="previewImages(index)">
					</image>
				</view>
			</view>

			<!-- 视频播放 -->
			<view class="video-container" v-if="postDetail.videoUrl">
				<video class="post-video" :src="postDetail.videoUrl" controls></video>
			</view>

			<!-- 标签列表 -->
			<view class="tags-list" v-if="postDetail.tags && postDetail.tags.length">
				<text class="tag-item" v-for="tag in postDetail.tags" :key="tag.id" @click="searchByTag(tag.id)">
					#{{ tag.name }}
				</text>
			</view>

			<!-- 帖子统计信息 -->
			<view class="post-stats">
				<text class="stat-item">浏览 {{ postDetail.viewCount || 0 }}</text>
				<text class="stat-item">点赞 {{ postDetail.likeCount || 0 }}</text>
				<text class="stat-item">评论 {{ postDetail.commentCount || 0 }}</text>
				<text class="stat-item">收藏 {{ postDetail.collectCount || 0 }}</text>
			</view>

			<!-- 互动按钮栏 -->
			<view class="action-bar">
				<view class="action-btn" @click="toggleLike">
					<text class="action-icon" :class="{ 'liked': postDetail.isLiked }">👍</text>
					<text class="action-text">{{ postDetail.isLiked ? '已点赞' : '点赞' }}</text>
				</view>
				<view class="action-btn" @click="toggleCollect">
					<text class="action-icon" :class="{ 'collected': postDetail.isCollected }">⭐</text>
					<text class="action-text">{{ postDetail.isCollected ? '已收藏' : '收藏' }}</text>
				</view>
				<view class="action-btn" @click="showShareOptions">
					<text class="action-icon">📤</text>
					<text class="action-text">分享</text>
				</view>
				<view class="action-btn" @click="showCommentInput">
					<text class="action-icon">💬</text>
					<text class="action-text">评论</text>
				</view>
			</view>
		</view>

		<!-- 评论区域 -->
		<view class="comments-section">
			<!-- 评论标题栏 -->
			<view class="comments-header">
				<text class="comments-title">评论 ({{ commentList.length }})</text>
				<view class="sort-selector">
					<text class="sort-option" :class="{ 'active': commentSort === 'hot' }" @click="changeCommentSort('hot')">热门</text>
					<text class="sort-divider">|</text>
					<text class="sort-option" :class="{ 'active': commentSort === 'time' }" @click="changeCommentSort('time')">最新</text>
				</view>
			</view>

			<!-- 评论列表 -->
			<view class="comments-list">
				<view class="comment-item" v-for="comment in commentList" :key="comment.id">
					<!-- 评论者信息 -->
					<view class="comment-user" @click="goToUserProfile(comment.userId)">
						<image class="comment-avatar" :src="comment.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
						<view class="comment-info">
							<text class="comment-username">{{ comment.username }}</text>
							<text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
						</view>
						<view class="comment-actions">
							<text class="comment-like" :class="{ 'liked': comment.isLiked }" @click="toggleCommentLike(comment.id)">
								👍 {{ comment.likeCount || 0 }}
							</text>
						</view>
					</view>

					<!-- 评论内容 -->
					<view class="comment-content">
						<!-- 回复标识 -->
						<text class="reply-to" v-if="comment.replyToUsername">
							回复 @{{ comment.replyToUsername }}:
						</text>
						<text class="comment-text">{{ comment.content }}</text>
					</view>

					<!-- 评论操作 -->
					<view class="comment-operations">
						<text class="comment-op" @click="replyToComment(comment)">回复</text>
						<text class="comment-op" v-if="canDeleteComment(comment)" @click="deleteComment(comment.id)">删除</text>
						<text class="comment-op" @click="reportComment(comment.id)">举报</text>
					</view>

					<!-- 子评论 -->
					<view class="sub-comments" v-if="comment.replies && comment.replies.length">
						<view class="sub-comment" v-for="reply in comment.replies.slice(0, 3)" :key="reply.id">
							<text class="sub-comment-user">{{ reply.username }}</text>
							<text class="sub-comment-content">: {{ reply.content }}</text>
						</view>
						<text class="more-replies" v-if="comment.replyCount > 3" @click="showMoreReplies(comment.id)">
							查看全部{{ comment.replyCount }}条回复 >
						</text>
					</view>
				</view>

				<!-- 加载更多评论 -->
				<view class="load-more" v-if="hasMoreComments" @click="loadMoreComments">
					<text class="load-more-text">加载更多评论</text>
				</view>
			</view>
		</view>

		<!-- 评论输入框 -->
		<view class="comment-input-bar" v-if="showCommentBox">
			<view class="input-container">
				<textarea
					class="comment-textarea"
					v-model="commentContent"
					:placeholder="replyTarget ? `回复 @${replyTarget.username}` : '说点什么...'"
					:maxlength="500"
					auto-height>
				</textarea>
				<view class="input-actions">
					<text class="char-count">{{ commentContent.length }}/500</text>
					<button class="send-btn" :disabled="!commentContent.trim()" @click="submitComment">发送</button>
				</view>
			</view>
		</view>

		<!-- 更多操作弹窗 -->
		<uni-popup ref="moreActions" type="bottom">
			<view class="popup-content">
				<view class="popup-item" @click="sharePost">分享帖子</view>
				<view class="popup-item" @click="copyLink">复制链接</view>
				<view class="popup-item" @click="reportPost">举报帖子</view>
				<view class="popup-item cancel" @click="$refs.moreActions.close()">取消</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
// 引入API和工具函数
import { postApi, commentApi, actionApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 页面状态
			loading: true,

			// 帖子信息
			postDetail: null,
			postId: null,

			// 评论相关
			commentList: [],
			commentSort: 'hot', // hot | time
			currentPage: 1,
			pageSize: 20,
			hasMoreComments: true,

			// 评论输入
			showCommentBox: false,
			commentContent: '',
			replyTarget: null, // 回复目标评论

			// 用户信息
			currentUser: null
		}
	},

	onLoad(options) {
		// 获取帖子ID参数
		this.postId = options.id || options.postId
		if (!this.postId) {
			uni.showToast({ title: '帖子不存在', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 获取当前用户信息
		this.currentUser = getAuthInfo()

		// 加载帖子详情
		this.loadPostDetail()
		this.loadComments()

		// 增加浏览量
		this.incrementViewCount()
	},

	onShow() {
		// 页面显示时刷新数据（如从其他页面返回）
		if (this.postDetail) {
			this.loadPostDetail()
		}
	},

	methods: {
		/**
		 * 加载帖子详情
		 * 获取帖子的完整信息，包括用户交互状态
		 */
		async loadPostDetail() {
			try {
				this.loading = true
				const response = await postApi.getPostDetail(this.postId)

				if (response.code === 200) {
					this.postDetail = response.data
					// 设置页面标题为帖子标题
					uni.setNavigationBarTitle({
						title: this.postDetail.title || '帖子详情'
					})
				} else {
					throw new Error(response.message || '获取帖子详情失败')
				}
			} catch (error) {
				console.error('加载帖子详情失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})
				// 如果帖子不存在，返回上一页
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} finally {
				this.loading = false
			}
		},

		/**
		 * 增加帖子浏览量
		 * 用户进入页面时自动增加浏览计数
		 */
		async incrementViewCount() {
			try {
				await postApi.addPostView(this.postId)
				// 浏览量在后台自动增加，无需更新UI
			} catch (error) {
				console.error('增加浏览量失败:', error)
				// 浏览量增加失败不影响用户体验，静默处理
			}
		},

		/**
		 * 加载评论列表
		 * 支持分页和排序
		 */
		async loadComments(refresh = false) {
			try {
				if (refresh) {
					this.currentPage = 1
					this.commentList = []
					this.hasMoreComments = true
				}

				const params = {
					current: this.currentPage,
					size: this.pageSize,
					sort: this.commentSort
				}

				const response = await commentApi.getPostComments(this.postId, params)

				if (response.code === 200) {
					const newComments = response.data.records || []

					if (refresh) {
						this.commentList = newComments
					} else {
						this.commentList = [...this.commentList, ...newComments]
					}

					// 检查是否还有更多评论
					this.hasMoreComments = this.commentList.length < response.data.total

					// 为每个评论加载子评论
					await this.loadRepliesForComments(newComments)
				} else {
					throw new Error(response.message || '获取评论失败')
				}
			} catch (error) {
				console.error('加载评论失败:', error)
				uni.showToast({
					title: error.message || '加载评论失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 为评论加载子评论
		 * @param {Array} comments - 评论列表
		 */
		async loadRepliesForComments(comments) {
			for (const comment of comments) {
				if (comment.replyCount > 0) {
					try {
						const response = await commentApi.getReplies(comment.id)
						if (response.code === 200) {
							comment.replies = response.data || []
						}
					} catch (error) {
						console.error(`加载评论${comment.id}的回复失败:`, error)
						comment.replies = []
					}
				}
			}
		},

		/**
		 * 加载更多评论
		 */
		loadMoreComments() {
			if (this.hasMoreComments) {
				this.currentPage++
				this.loadComments()
			}
		},

		/**
		 * 切换评论排序方式
		 * @param {string} sortType - 排序类型：hot | time
		 */
		changeCommentSort(sortType) {
			if (this.commentSort !== sortType) {
				this.commentSort = sortType
				this.loadComments(true) // 刷新评论列表
			}
		},

		/**
		 * 切换帖子点赞状态
		 */
		async toggleLike() {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			try {
				const response = await actionApi.toggleLike({
					targetId: this.postId,
					targetType: 'post'
				})

				if (response.code === 200) {
					// 更新UI状态
					this.postDetail.isLiked = !this.postDetail.isLiked
					this.postDetail.likeCount += this.postDetail.isLiked ? 1 : -1

					uni.showToast({
						title: this.postDetail.isLiked ? '点赞成功' : '取消点赞',
						icon: 'success'
					})
				} else {
					throw new Error(response.message || '操作失败')
				}
			} catch (error) {
				console.error('点赞操作失败:', error)
				uni.showToast({
					title: error.message || '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 切换帖子收藏状态
		 */
		async toggleCollect() {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			try {
				const response = await actionApi.toggleCollect({
					targetId: this.postId,
					targetType: 'post'
				})

				if (response.code === 200) {
					// 更新UI状态
					this.postDetail.isCollected = !this.postDetail.isCollected
					this.postDetail.collectCount += this.postDetail.isCollected ? 1 : -1

					uni.showToast({
						title: this.postDetail.isCollected ? '收藏成功' : '取消收藏',
						icon: 'success'
					})
				} else {
					throw new Error(response.message || '操作失败')
				}
			} catch (error) {
				console.error('收藏操作失败:', error)
				uni.showToast({
					title: error.message || '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 切换评论点赞状态
		 * @param {number} commentId - 评论ID
		 */
		async toggleCommentLike(commentId) {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			try {
				const response = await actionApi.toggleLike({
					targetId: commentId,
					targetType: 'comment'
				})

				if (response.code === 200) {
					// 找到对应评论并更新状态
					const comment = this.commentList.find(c => c.id === commentId)
					if (comment) {
						comment.isLiked = !comment.isLiked
						comment.likeCount += comment.isLiked ? 1 : -1
					}
				} else {
					throw new Error(response.message || '操作失败')
				}
			} catch (error) {
				console.error('评论点赞失败:', error)
				uni.showToast({
					title: error.message || '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 显示评论输入框
		 */
		showCommentInput() {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			this.showCommentBox = true
			this.replyTarget = null
			this.commentContent = ''
		},

		/**
		 * 回复评论
		 * @param {Object} comment - 要回复的评论对象
		 */
		replyToComment(comment) {
			if (!this.currentUser?.userId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			this.showCommentBox = true
			this.replyTarget = comment
			this.commentContent = ''
		},

		/**
		 * 提交评论
		 */
		async submitComment() {
			if (!this.commentContent.trim()) {
				uni.showToast({ title: '请输入评论内容', icon: 'error' })
				return
			}

			try {
				const commentData = {
					postId: this.postId,
					content: this.commentContent.trim()
				}

				// 如果是回复评论，添加父评论ID
				if (this.replyTarget) {
					commentData.parentId = this.replyTarget.id
				}

				const response = await commentApi.createComment(commentData)

				if (response.code === 200) {
					uni.showToast({ title: '评论成功', icon: 'success' })

					// 清空输入框并隐藏
					this.commentContent = ''
					this.showCommentBox = false
					this.replyTarget = null

					// 刷新评论列表
					this.loadComments(true)

					// 更新帖子评论数
					if (this.postDetail) {
						this.postDetail.commentCount = (this.postDetail.commentCount || 0) + 1
					}
				} else {
					throw new Error(response.message || '评论失败')
				}
			} catch (error) {
				console.error('发表评论失败:', error)
				uni.showToast({
					title: error.message || '评论失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 删除评论
		 * @param {number} commentId - 评论ID
		 */
		async deleteComment(commentId) {
			try {
				await uni.showModal({
					title: '确认删除',
					content: '确定要删除这条评论吗？',
					confirmText: '删除',
					confirmColor: '#ff4757'
				})

				const response = await commentApi.deleteComment(commentId)

				if (response.code === 200) {
					uni.showToast({ title: '删除成功', icon: 'success' })

					// 从列表中移除评论
					this.commentList = this.commentList.filter(c => c.id !== commentId)

					// 更新帖子评论数
					if (this.postDetail) {
						this.postDetail.commentCount = Math.max(0, (this.postDetail.commentCount || 0) - 1)
					}
				} else {
					throw new Error(response.message || '删除失败')
				}
			} catch (error) {
				console.error('删除评论失败:', error)
				if (error.message !== 'cancel') {
					uni.showToast({
						title: error.message || '删除失败',
						icon: 'error'
					})
				}
			}
		},

		/**
		 * 举报评论
		 * @param {number} commentId - 评论ID
		 */
		reportComment(commentId) {
			uni.navigateTo({
				url: `/pages/report/report?targetType=comment&targetId=${commentId}`
			})
		},

		/**
		 * 举报帖子
		 */
		reportPost() {
			this.$refs.moreActions.close()
			uni.navigateTo({
				url: `/pages/report/report?targetType=post&targetId=${this.postId}`
			})
		},

		/**
		 * 跳转到用户资料页
		 * @param {number} userId - 用户ID
		 */
		goToUserProfile(userId) {
			if (userId) {
				uni.navigateTo({
					url: `/pages/user-profile/user-profile?userId=${userId}`
				})
			}
		},

		/**
		 * 按标签搜索
		 * @param {number} tagId - 标签ID
		 */
		searchByTag(tagId) {
			uni.navigateTo({
				url: `/pages/search/search?tagId=${tagId}`
			})
		},

		/**
		 * 图片预览
		 * @param {number} index - 当前图片索引
		 */
		previewImages(index) {
			uni.previewImage({
				urls: this.postDetail.imageUrls,
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
		 * 显示分享选项
		 */
		showShareOptions() {
			this.$refs.moreActions.open()
		},

		/**
		 * 分享帖子
		 */
		async sharePost() {
			this.$refs.moreActions.close()

			try {
				// 记录分享行为
				await actionApi.recordShare({
					targetId: this.postId,
					targetType: 'post',
					platform: 'system'
				})

				// 执行系统分享
				uni.share({
					provider: "weixin",
					scene: "WXSceneSession",
					type: 0,
					href: `https://herizon.com/post/${this.postId}`,
					title: this.postDetail.title || '查看这个精彩内容',
					summary: this.postDetail.content.substring(0, 100),
					imageUrl: this.postDetail.imageUrls?.[0] || '/static/img/logo.png',
					success: () => {
						uni.showToast({ title: '分享成功', icon: 'success' })
					},
					fail: (error) => {
						console.error('分享失败:', error)
						this.copyLink() // 分享失败时复制链接
					}
				})
			} catch (error) {
				console.error('分享操作失败:', error)
				this.copyLink() // 出错时复制链接作为备选方案
			}
		},

		/**
		 * 复制帖子链接
		 */
		copyLink() {
			this.$refs.moreActions.close()

			const link = `https://herizon.com/post/${this.postId}`
			uni.setClipboardData({
				data: link,
				success: () => {
					uni.showToast({ title: '链接已复制', icon: 'success' })
				},
				fail: () => {
					uni.showToast({ title: '复制失败', icon: 'error' })
				}
			})
		},

		/**
		 * 显示更多回复
		 * @param {number} commentId - 评论ID
		 */
		showMoreReplies(commentId) {
			uni.navigateTo({
				url: `/pages/comment-detail/comment-detail?commentId=${commentId}`
			})
		},

		/**
		 * 检查是否可以删除评论
		 * @param {Object} comment - 评论对象
		 * @returns {boolean} 是否可以删除
		 */
		canDeleteComment(comment) {
			// 只有评论作者或管理员可以删除评论
			return this.currentUser?.userId === comment.userId || this.currentUser?.role >= 2
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
					year: 'numeric',
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
.post-detail-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 120rpx; /* 为评论输入框留出空间 */
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

/* 帖子内容区 */
.post-content {
	background-color: white;
	margin-bottom: 20rpx;
}

/* 用户信息栏 */
.user-info {
	display: flex;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.user-meta {
	flex: 1;
}

.username {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
}

.post-time {
	font-size: 24rpx;
	color: #999;
}

.more-btn {
	padding: 10rpx;
}

.more-icon {
	font-size: 36rpx;
	color: #666;
}

/* 帖子标题 */
.post-title {
	padding: 30rpx 30rpx 0;
}

.title-text {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
}

/* 帖子内容 */
.post-text {
	padding: 30rpx;
}

.content-text {
	font-size: 30rpx;
	color: #333;
	line-height: 1.6;
	word-break: break-word;
}

/* 图片展示 */
.image-gallery {
	padding: 0 30rpx 30rpx;
}

.image-grid {
	display: grid;
	gap: 10rpx;
}

.grid-1 {
	grid-template-columns: 1fr;
}

.grid-2 {
	grid-template-columns: 1fr 1fr;
}

.grid-3 {
	grid-template-columns: 1fr 1fr 1fr;
}

.post-image {
	width: 100%;
	height: 200rpx;
	border-radius: 12rpx;
}

/* 视频容器 */
.video-container {
	padding: 0 30rpx 30rpx;
}

.post-video {
	width: 100%;
	border-radius: 12rpx;
}

/* 标签列表 */
.tags-list {
	padding: 0 30rpx 30rpx;
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.tag-item {
	padding: 8rpx 16rpx;
	background-color: #f0f8ff;
	color: #1890ff;
	font-size: 24rpx;
	border-radius: 20rpx;
	border: 1rpx solid #d6e4ff;
}

/* 帖子统计 */
.post-stats {
	padding: 20rpx 30rpx;
	display: flex;
	gap: 40rpx;
	border-top: 1rpx solid #f0f0f0;
	border-bottom: 1rpx solid #f0f0f0;
}

.stat-item {
	font-size: 26rpx;
	color: #666;
}

/* 互动按钮栏 */
.action-bar {
	display: flex;
	padding: 20rpx 30rpx;
}

.action-btn {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 10rpx;
}

.action-icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}

.action-icon.liked {
	color: #ff4757;
}

.action-icon.collected {
	color: #ffa502;
}

.action-text {
	font-size: 24rpx;
	color: #666;
}

/* 评论区域 */
.comments-section {
	background-color: white;
}

/* 评论标题栏 */
.comments-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.comments-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.sort-selector {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.sort-option {
	font-size: 26rpx;
	color: #666;
	padding: 10rpx;
}

.sort-option.active {
	color: #1890ff;
	font-weight: bold;
}

.sort-divider {
	color: #ccc;
}

/* 评论列表 */
.comments-list {
	padding: 0 30rpx;
}

.comment-item {
	padding: 30rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.comment-item:last-child {
	border-bottom: none;
}

/* 评论者信息 */
.comment-user {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.comment-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}

.comment-info {
	flex: 1;
}

.comment-username {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 6rpx;
}

.comment-time {
	font-size: 22rpx;
	color: #999;
}

.comment-actions {
	display: flex;
	align-items: center;
}

.comment-like {
	font-size: 24rpx;
	color: #666;
	padding: 8rpx;
}

.comment-like.liked {
	color: #ff4757;
}

/* 评论内容 */
.comment-content {
	margin-bottom: 16rpx;
}

.reply-to {
	font-size: 26rpx;
	color: #1890ff;
	margin-right: 8rpx;
}

.comment-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
}

/* 评论操作 */
.comment-operations {
	display: flex;
	gap: 30rpx;
	margin-bottom: 20rpx;
}

.comment-op {
	font-size: 24rpx;
	color: #666;
	padding: 8rpx;
}

/* 子评论 */
.sub-comments {
	background-color: #f8f9fa;
	border-radius: 12rpx;
	padding: 20rpx;
	margin-left: 76rpx;
}

.sub-comment {
	margin-bottom: 12rpx;
	font-size: 26rpx;
}

.sub-comment:last-child {
	margin-bottom: 0;
}

.sub-comment-user {
	color: #1890ff;
	margin-right: 8rpx;
}

.sub-comment-content {
	color: #333;
}

.more-replies {
	font-size: 24rpx;
	color: #1890ff;
	margin-top: 16rpx;
}

/* 加载更多 */
.load-more {
	text-align: center;
	padding: 40rpx;
}

.load-more-text {
	font-size: 26rpx;
	color: #666;
}

/* 评论输入框 */
.comment-input-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: white;
	border-top: 1rpx solid #e0e0e0;
	padding: 20rpx;
	z-index: 100;
}

.input-container {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.comment-textarea {
	min-height: 80rpx;
	max-height: 200rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	padding: 16rpx;
	font-size: 28rpx;
	color: #333;
}

.input-actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.char-count {
	font-size: 24rpx;
	color: #999;
}

.send-btn {
	background-color: #1890ff;
	color: white;
	font-size: 26rpx;
	border: none;
	border-radius: 20rpx;
	padding: 12rpx 24rpx;
}

.send-btn[disabled] {
	background-color: #ccc;
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