<!-- 评论详情页 - 嵌套评论展示和多级回复 -->
<template>
	<!-- 主容器：评论详情展示 -->
	<view class="comment-detail-container">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 评论内容 -->
		<view v-else class="comment-content">
			<!-- 主评论 -->
			<view class="main-comment" v-if="mainComment">
				<!-- 评论者信息 -->
				<view class="comment-user" @click="goToUserProfile(mainComment.userId)">
					<image class="user-avatar" :src="mainComment.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
					<view class="user-info">
						<text class="username">{{ mainComment.username }}</text>
						<text class="comment-time">{{ formatTime(mainComment.createdAt) }}</text>
					</view>
					<view class="comment-actions">
						<text class="like-btn" :class="{ 'liked': mainComment.isLiked }" @click="toggleLike(mainComment.id)">
							👍 {{ mainComment.likeCount || 0 }}
						</text>
					</view>
				</view>

				<!-- 评论内容 -->
				<view class="comment-text">
					<text class="content">{{ mainComment.content }}</text>
				</view>

				<!-- 操作按钮 -->
				<view class="comment-operations">
					<text class="op-btn" @click="replyToComment(mainComment)">回复</text>
					<text class="op-btn" v-if="canDeleteComment(mainComment)" @click="deleteComment(mainComment.id)">删除</text>
					<text class="op-btn" @click="reportComment(mainComment.id)">举报</text>
				</view>
			</view>

			<!-- 回复列表 -->
			<view class="replies-section">
				<view class="replies-header">
					<text class="replies-title">全部回复 ({{ replyList.length }})</text>
					<view class="sort-options">
						<text class="sort-btn" :class="{ 'active': sortType === 'time' }" @click="changeSortType('time')">最新</text>
						<text class="sort-btn" :class="{ 'active': sortType === 'hot' }" @click="changeSortType('hot')">热门</text>
					</view>
				</view>

				<!-- 回复列表 -->
				<scroll-view class="replies-scroll"
							 scroll-y="true"
							 @scrolltolower="loadMoreReplies"
							 refresher-enabled="true"
							 :refresher-triggered="isRefreshing"
							 @refresherrefresh="refreshReplies">

					<!-- 回复项 -->
					<view class="reply-item" v-for="reply in replyList" :key="reply.id">
						<!-- 回复者信息 -->
						<view class="reply-user" @click="goToUserProfile(reply.userId)">
							<image class="reply-avatar" :src="reply.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
							<view class="reply-info">
								<text class="reply-username">{{ reply.username }}</text>
								<text class="reply-time">{{ formatTime(reply.createdAt) }}</text>
							</view>
							<view class="reply-actions">
								<text class="reply-like" :class="{ 'liked': reply.isLiked }" @click="toggleLike(reply.id)">
									👍 {{ reply.likeCount || 0 }}
								</text>
							</view>
						</view>

						<!-- 回复内容 -->
						<view class="reply-content">
							<!-- 回复目标 -->
							<text class="reply-target" v-if="reply.replyToUsername">
								回复 @{{ reply.replyToUsername }}:
							</text>
							<text class="reply-text">{{ reply.content }}</text>
						</view>

						<!-- 回复操作 -->
						<view class="reply-operations">
							<text class="reply-op" @click="replyToComment(reply)">回复</text>
							<text class="reply-op" v-if="canDeleteComment(reply)" @click="deleteComment(reply.id)">删除</text>
							<text class="reply-op" @click="reportComment(reply.id)">举报</text>
						</view>

						<!-- 子回复展示 -->
						<view class="sub-replies" v-if="reply.subReplies && reply.subReplies.length">
							<view class="sub-reply" v-for="subReply in reply.subReplies" :key="subReply.id">
								<text class="sub-reply-user">{{ subReply.username }}</text>
								<text class="sub-reply-target" v-if="subReply.replyToUsername"> 回复 @{{ subReply.replyToUsername }}</text>
								<text class="sub-reply-content">: {{ subReply.content }}</text>
							</view>
						</view>
					</view>

					<!-- 空状态 -->
					<view class="empty-replies" v-if="replyList.length === 0 && !loading">
						<text class="empty-text">暂无回复</text>
						<text class="empty-tip">快来抢沙发吧~</text>
					</view>

					<!-- 加载更多 -->
					<view class="load-more" v-if="hasMoreReplies">
						<text class="load-more-text">{{ loading ? '加载中...' : '加载更多' }}</text>
					</view>
				</scroll-view>
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
					<button class="send-btn" :disabled="!commentContent.trim()" @click="submitReply">发送</button>
				</view>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<button class="reply-btn" @click="showReplyInput">💬 写回复</button>
		</view>
	</view>
</template>

<script>
// 引入API和工具函数
import { commentApi, actionApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 页面状态
			loading: true,
			isRefreshing: false,

			// 评论信息
			mainComment: null,
			commentId: null,

			// 回复列表
			replyList: [],
			sortType: 'time', // time | hot
			currentPage: 1,
			pageSize: 20,
			hasMoreReplies: true,

			// 回复输入
			showCommentBox: false,
			commentContent: '',
			replyTarget: null,

			// 用户信息
			currentUser: null
		}
	},

	onLoad(options) {
		// 获取评论ID参数
		this.commentId = options.commentId || options.id
		if (!this.commentId) {
			uni.showToast({ title: '评论不存在', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 获取当前用户信息
		this.currentUser = getAuthInfo()

		// 加载评论详情
		this.loadCommentDetail()
		this.loadReplies()
	},

	methods: {
		/**
		 * 加载评论详情
		 */
		async loadCommentDetail() {
			try {
				const response = await commentApi.getCommentDetail(this.commentId)

				if (response.code === 200) {
					this.mainComment = response.data
					uni.setNavigationBarTitle({
						title: '评论详情'
					})
				} else {
					throw new Error(response.message || '获取评论详情失败')
				}
			} catch (error) {
				console.error('加载评论详情失败:', error)
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
		 * 加载回复列表
		 * @param {boolean} refresh - 是否刷新数据
		 */
		async loadReplies(refresh = false) {
			try {
				if (refresh) {
					this.currentPage = 1
					this.replyList = []
					this.hasMoreReplies = true
				}

				const response = await commentApi.getReplies(this.commentId)

				if (response.code === 200) {
					const newReplies = response.data || []

					if (refresh) {
						this.replyList = newReplies
					} else {
						this.replyList = [...this.replyList, ...newReplies]
					}

					// 这里简化处理，实际应该根据分页信息判断
					this.hasMoreReplies = newReplies.length >= this.pageSize
				} else {
					throw new Error(response.message || '获取回复失败')
				}
			} catch (error) {
				console.error('加载回复失败:', error)
				uni.showToast({
					title: error.message || '加载回复失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 加载更多回复
		 */
		loadMoreReplies() {
			if (this.hasMoreReplies && !this.loading) {
				this.currentPage++
				this.loadReplies()
			}
		},

		/**
		 * 刷新回复
		 */
		refreshReplies() {
			this.isRefreshing = true
			this.loadReplies(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 切换排序方式
		 * @param {string} type - 排序类型
		 */
		changeSortType(type) {
			if (this.sortType !== type) {
				this.sortType = type
				this.loadReplies(true)
			}
		},

		/**
		 * 切换点赞状态
		 * @param {number} commentId - 评论ID
		 */
		async toggleLike(commentId) {
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
					// 更新对应评论的点赞状态
					if (this.mainComment && this.mainComment.id === commentId) {
						this.mainComment.isLiked = !this.mainComment.isLiked
						this.mainComment.likeCount += this.mainComment.isLiked ? 1 : -1
					} else {
						const reply = this.replyList.find(r => r.id === commentId)
						if (reply) {
							reply.isLiked = !reply.isLiked
							reply.likeCount += reply.isLiked ? 1 : -1
						}
					}
				} else {
					throw new Error(response.message || '操作失败')
				}
			} catch (error) {
				console.error('点赞失败:', error)
				uni.showToast({
					title: error.message || '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 显示回复输入
		 */
		showReplyInput() {
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
		 * @param {Object} comment - 要回复的评论
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
		 * 提交回复
		 */
		async submitReply() {
			if (!this.commentContent.trim()) {
				uni.showToast({ title: '请输入回复内容', icon: 'error' })
				return
			}

			try {
				const replyData = {
					parentId: this.commentId,
					content: this.commentContent.trim()
				}

				// 如果是回复特定用户，添加回复目标
				if (this.replyTarget && this.replyTarget.id !== this.commentId) {
					replyData.replyToUserId = this.replyTarget.userId
				}

				const response = await commentApi.createComment(replyData)

				if (response.code === 200) {
					uni.showToast({ title: '回复成功', icon: 'success' })

					// 清空输入框并隐藏
					this.commentContent = ''
					this.showCommentBox = false
					this.replyTarget = null

					// 刷新回复列表
					this.loadReplies(true)
				} else {
					throw new Error(response.message || '回复失败')
				}
			} catch (error) {
				console.error('发表回复失败:', error)
				uni.showToast({
					title: error.message || '回复失败',
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

					if (commentId === this.commentId) {
						// 删除的是主评论，返回上一页
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					} else {
						// 删除的是回复，从列表中移除
						this.replyList = this.replyList.filter(r => r.id !== commentId)
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
		 * 跳转到用户资料
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
		 * 检查是否可以删除评论
		 * @param {Object} comment - 评论对象
		 * @returns {boolean} 是否可以删除
		 */
		canDeleteComment(comment) {
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
.comment-detail-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 140rpx;
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

/* 评论内容 */
.comment-content {
	padding-bottom: 40rpx;
}

/* 主评论 */
.main-comment {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
}

/* 评论用户信息 */
.comment-user {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.user-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.user-info {
	flex: 1;
}

.username {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
}

.comment-time {
	font-size: 24rpx;
	color: #999;
}

.comment-actions {
	display: flex;
	align-items: center;
}

.like-btn {
	font-size: 26rpx;
	color: #666;
	padding: 8rpx;
}

.like-btn.liked {
	color: #ff4757;
}

/* 评论文本 */
.comment-text {
	margin-bottom: 20rpx;
}

.content {
	font-size: 30rpx;
	color: #333;
	line-height: 1.6;
	word-break: break-word;
}

/* 评论操作 */
.comment-operations {
	display: flex;
	gap: 30rpx;
}

.op-btn {
	font-size: 26rpx;
	color: #666;
	padding: 8rpx;
}

/* 回复区域 */
.replies-section {
	background-color: white;
}

/* 回复标题栏 */
.replies-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.replies-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.sort-options {
	display: flex;
	gap: 20rpx;
}

.sort-btn {
	font-size: 26rpx;
	color: #666;
	padding: 10rpx;
}

.sort-btn.active {
	color: #1890ff;
	font-weight: bold;
}

/* 回复滚动视图 */
.replies-scroll {
	height: calc(100vh - 300rpx);
	padding: 0 30rpx;
}

/* 回复项 */
.reply-item {
	padding: 30rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.reply-item:last-child {
	border-bottom: none;
}

/* 回复用户 */
.reply-user {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.reply-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}

.reply-info {
	flex: 1;
}

.reply-username {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 6rpx;
}

.reply-time {
	font-size: 22rpx;
	color: #999;
}

.reply-actions {
	display: flex;
	align-items: center;
}

.reply-like {
	font-size: 24rpx;
	color: #666;
	padding: 8rpx;
}

.reply-like.liked {
	color: #ff4757;
}

/* 回复内容 */
.reply-content {
	margin-bottom: 16rpx;
}

.reply-target {
	font-size: 26rpx;
	color: #1890ff;
	margin-right: 8rpx;
}

.reply-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
}

/* 回复操作 */
.reply-operations {
	display: flex;
	gap: 30rpx;
	margin-bottom: 20rpx;
}

.reply-op {
	font-size: 24rpx;
	color: #666;
	padding: 8rpx;
}

/* 子回复 */
.sub-replies {
	background-color: #f8f9fa;
	border-radius: 12rpx;
	padding: 20rpx;
	margin-left: 76rpx;
}

.sub-reply {
	margin-bottom: 12rpx;
	font-size: 26rpx;
}

.sub-reply:last-child {
	margin-bottom: 0;
}

.sub-reply-user {
	color: #1890ff;
	margin-right: 8rpx;
}

.sub-reply-target {
	color: #999;
	margin-right: 8rpx;
}

.sub-reply-content {
	color: #333;
}

/* 空状态 */
.empty-replies {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 300rpx;
	padding: 40rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 12rpx;
}

.empty-tip {
	font-size: 24rpx;
	color: #ccc;
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
	bottom: 80rpx;
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

/* 底部操作栏 */
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: white;
	border-top: 1rpx solid #e0e0e0;
	padding: 20rpx 30rpx;
	z-index: 99;
}

.reply-btn {
	width: 100%;
	height: 80rpx;
	background-color: #1890ff;
	color: white;
	font-size: 28rpx;
	border: none;
	border-radius: 40rpx;
}
</style>