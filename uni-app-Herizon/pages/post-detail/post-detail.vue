<!-- 帖子详情页 - 展示完整帖子内容和评论系统 -->
<template>
	<!-- 主容器:帖子详情和评论 -->
	<view class="post-detail-container">
		<!-- 骨架屏 - 加载中显示 -->
		<view v-if="loading && !postDetail" class="skeleton-container">
			<post-skeleton :show-image="true"></post-skeleton>
		</view>

		<!-- 帖子内容 -->
		<view v-else-if="postDetail" class="post-content">
			<!-- 用户信息栏 -->
			<view class="user-info" @click="goToUserProfile(postDetail.userId)">
				<image class="avatar" :src="postDetail.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
				<view class="user-meta">
					<text class="username">{{ postDetail.nickname || postDetail.username }}</text>
					<text class="post-time">{{ formatTime(postDetail.createdAt) }}</text>
				</view>
				<view class="user-actions">
					<view class="follow-author" v-if="canFollowAuthor">
						<button class="follow-author-btn" :class="{ following: postDetail.isAuthorFollowed }" :disabled="followLoading" @click.stop="toggleAuthorFollow" hover-class="follow-author-btn--active">
							<text class="btn-icon" v-if="!postDetail.isAuthorFollowed">+</text>
							<text class="btn-icon" v-else>✓</text>
							<text class="btn-text">{{ postDetail.isAuthorFollowed ? '已关注' : '关注' }}</text>
						</button>
					</view>
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
				<view class="image-grid" :class="[imageGridColumnsClass, imageLayoutClass]">
					<image
						v-for="(imageUrl, index) in postDetail.imageUrls.slice(0, 9)"
						:key="index"
						:class="['post-image', { 'post-image--single': imageLayoutClass === 'layout-single' }]"
						:src="imageUrl"
						:mode="imageLayoutClass === 'layout-single' ? 'widthFix' : 'aspectFill'"
						@click="previewImages(index)">
					</image>
				</view>
			</view>

			<!-- 投票选项展示(仅投票帖显示) -->
			<view class="poll-options" v-if="postDetail.postType === 1 && postDetail.pollOptions && postDetail.pollOptions.length">
				<view class="poll-header">
					<text class="poll-title">投票选项</text>
					<text class="poll-status" v-if="postDetail.myVote">已投票</text>
					<text class="poll-status pending" v-else>未投票</text>
				</view>

				<view class="poll-list">
					<view
						v-for="option in postDetail.pollOptions"
						:key="option.id"
						class="poll-option"
						:class="{
							'selected': postDetail.myVote === option.id,
							'voted': postDetail.myVote !== null && postDetail.myVote !== undefined
						}"
						@click="handleVote(option.id)">

						<!-- 投票选项内容 -->
						<view class="poll-option-content">
							<view class="option-text-wrapper">
								<text class="option-text">{{ option.optionText }}</text>
								<text class="check-mark" v-if="postDetail.myVote === option.id">✓</text>
							</view>

							<!-- 投票进度条(已投票时显示) -->
							<view class="vote-bar" v-if="postDetail.myVote !== null && postDetail.myVote !== undefined">
								<view class="vote-bar-fill" :style="{ width: getVotePercentage(option) + '%' }"></view>
							</view>

							<!-- 投票统计 -->
							<view class="vote-stats">
								<text class="vote-count">{{ option.voteCount || 0 }} 票</text>
								<text class="vote-percentage" v-if="postDetail.myVote !== null && postDetail.myVote !== undefined">
									{{ getVotePercentage(option) }}%
								</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 总投票数 -->
				<view class="poll-total">
					<text class="total-text">共 {{ getTotalVotes() }} 人参与投票</text>
				</view>
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
							<text class="comment-username">{{ comment.nickname || comment.username }}</text>
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
						<text class="reply-to" v-if="comment.parentNickname || comment.replyToUsername">
							回复 @{{ comment.parentNickname || comment.replyToUsername }}:
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
							<text class="sub-comment-user">{{ reply.nickname || reply.username }}</text>
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
					:placeholder="replyTarget ? `回复 @${replyTarget.nickname || replyTarget.username}` : '说点什么...'"
					:maxlength="500"
					auto-height>
				</textarea>
				<view class="input-actions">
					<text class="char-count">{{ commentContent.length }}/500</text>
					<button class="send-btn" :disabled="!commentContent.trim()" @click="submitComment">发送</button>
				</view>
			</view>
		</view>

	</view>
</template>


<script>
import { postApi, commentApi, actionApi } from '@/utils/api.js'
import { getAuthInfo, verifyAndExecute, USER_ROLES } from '@/utils/auth.js'
import PostSkeleton from '@/components/skeleton/post-skeleton.vue'
import ErrorState from '@/components/error-state/error-state.vue'

export default {
  components: {
    PostSkeleton,
    ErrorState
  },
  data() {
    return {
      // ????
      loading: true,

      // ????
      postDetail: null,
      postId: null,

      // ???
      commentList: [],
      commentSort: 'hot', // hot | time
      currentPage: 1,
      pageSize: 20,
      hasMoreComments: true,
      commentTotal: 0,
      commentsLoading: false,

      // ????
      showCommentBox: false,
      commentContent: '',
      replyTarget: null, // ??????
      commentSubmitting: false,

      // ????
      currentUser: null,
      followLoading: false
    }
  },

 computed: {
    canFollowAuthor() {
      if (!this.postDetail || !this.postDetail.userId) {
        return false
      }
      if (!this.currentUser || !this.currentUser.userId) {
        return true
      }
      return this.currentUser.userId !== this.postDetail.userId
    },

    imageGridColumnsClass() {
      if (!this.postDetail || !Array.isArray(this.postDetail.imageUrls)) {
        return 'grid-1'
      }

      const count = this.postDetail.imageUrls.length
      if (count === 4) {
        return 'grid-2'
      }
      if (count <= 3) {
        return `grid-${count}`
      }
      return 'grid-3'
    },

    imageLayoutClass() {
      if (!this.postDetail || !Array.isArray(this.postDetail.imageUrls)) {
        return ''
      }

      const count = this.postDetail.imageUrls.length
      if (count === 1) {
        return 'layout-single'
      }
      if (count === 2 || count === 4) {
        return 'layout-double'
      }
      return 'layout-multi'
    }
  },

  onLoad(options) {
    // ????ID??
    this.postId = options.id || options.postId
    if (!this.postId) {
      uni.showToast({ title: '??????', icon: 'error' })
      uni.navigateBack()
      return
    }

    // ????????
    this.currentUser = getAuthInfo()

    // ?????????
    this.loadPostDetail()
    this.loadComments(true)

    // ?????
    this.incrementViewCount()
  },

  onShow() {
    // ??????????????????
    if (this.postDetail) {
      this.loadPostDetail()
    }
  },

  methods: {
    async loadPostDetail() {
      if (!this.postId) {
        return
      }

      if (!this.postDetail) {
        this.loading = true
      }

      try {
        const detail = await postApi.getPostDetail(this.postId)
        this.postDetail = this.normalizePostDetail(detail)
      } catch (error) {
        console.error('????????:', error)
        uni.showToast({
          title: error.message || '????',
          icon: 'error'
        })
      } finally {
        this.loading = false
      }
    },

    normalizePostDetail(detail) {
      if (!detail) {
        return null
      }

      const ensureImageArray = (value) => {
        if (Array.isArray(value)) {
          return value
        }
        if (!value) {
          return []
        }
        if (typeof value === 'string') {
          try {
            const parsed = JSON.parse(value)
            if (Array.isArray(parsed)) {
              return parsed
            }
          } catch (error) {
            // ignore parse error
          }
          return value.split(',').map(item => item.trim()).filter(Boolean)
        }
        return []
      }

      const ensureObjectArray = (value) => {
        if (Array.isArray(value)) {
          return value
        }
        if (!value) {
          return []
        }
        if (typeof value === 'string') {
          try {
            const parsed = JSON.parse(value)
            if (Array.isArray(parsed)) {
              return parsed
            }
          } catch (error) {
            // ignore parse error
          }
        }
        return []
      }

      return {
        ...detail,
        imageUrls: ensureImageArray(detail.imageUrls),
        tags: ensureObjectArray(detail.tags),
        pollOptions: ensureObjectArray(detail.pollOptions),
        isLiked: Boolean(detail.isLiked),
        isCollected: Boolean(detail.isCollected),
        likeCount: detail.likeCount ?? 0,
        viewCount: detail.viewCount ?? 0,
        commentCount: detail.commentCount ?? 0,
        collectCount: detail.collectCount ?? 0,
        isAuthorFollowed: Boolean(detail.isAuthorFollowed)
      }
    },

    async incrementViewCount() {
      if (!this.postId) {
        return
      }

      try {
        await postApi.addPostView(this.postId)
      } catch (error) {
        console.warn('???????:', error)
      }
    },

    async loadComments(reset = true) {
      if (!this.postId || this.commentsLoading) {
        return
      }

      if (!reset && !this.hasMoreComments) {
        return
      }

      const nextPage = reset ? 1 : this.currentPage + 1
      this.commentsLoading = true

      try {
        const result = await commentApi.getPostComments(this.postId, {
          current: nextPage,
          size: this.pageSize,
          sort: this.commentSort
        })

        const records = Array.isArray(result?.records) ? result.records : []
        const normalized = records
          .map((item) => this.normalizeComment(item))
          .filter(Boolean)

        if (reset) {
          this.commentList = normalized
        } else {
          this.commentList = this.commentList.concat(normalized)
        }

        this.currentPage = Number(result?.current ?? nextPage)
        this.commentTotal = Number(result?.total ?? this.commentList.length)

        if (result?.pages != null) {
          this.hasMoreComments = this.currentPage < Number(result.pages)
        } else {
          this.hasMoreComments = normalized.length === this.pageSize
        }
      } catch (error) {
        console.error('??????:', error)
        uni.showToast({
          title: error.message || '??????',
          icon: 'error'
        })
      } finally {
        this.commentsLoading = false
      }
    },

    normalizeComment(comment) {
      if (!comment) {
        return null
      }

      const replies = Array.isArray(comment.replies)
        ? comment.replies.map((reply) => ({
            ...reply,
            likeCount: reply.likeCount ?? 0,
            isLiked: Boolean(reply.isLiked)
          }))
        : []

      return {
        ...comment,
        replies,
        likeCount: comment.likeCount ?? 0,
        isLiked: Boolean(comment.isLiked)
      }
    },

    loadMoreComments() {
      if (this.commentsLoading || !this.hasMoreComments) {
        return
      }
      this.loadComments(false)
    },

    closeMoreActions() {
      if (this.$refs.moreActions && typeof this.$refs.moreActions.close === 'function') {
        this.$refs.moreActions.close()
      }
    },

    goToUserProfile(userId) {
      if (userId) {
        uni.navigateTo({
          url: `/pages/user-profile/user-profile?userId=${userId}`
        })
      }
    },

    previewImages(startIndex) {
      if (!this.postDetail?.imageUrls?.length) {
        return
      }

      const urls = this.postDetail.imageUrls
      const current = urls[startIndex] || urls[0]

      uni.previewImage({
        current,
        urls
      })
    },

    searchByTag(tagId) {
      if (!tagId) {
        return
      }

      uni.navigateTo({
        url: `/pages/tag-posts/tag-posts?tagId=${tagId}`
      })
    },

    toggleLike() {
      if (!this.postDetail) {
        return
      }

      verifyAndExecute(USER_ROLES.TRIAL, async () => {
        try {
          await actionApi.toggleLike({
            targetId: this.postDetail.id || this.postId,
            targetType: 'post'
          })

          const nextState = !this.postDetail.isLiked
          this.postDetail.isLiked = nextState
          const current = this.postDetail.likeCount ?? 0
          this.postDetail.likeCount = nextState ? current + 1 : Math.max(current - 1, 0)

          uni.showToast({
            title: nextState ? '????' : '?????',
            icon: 'success'
          })
        } catch (error) {
          console.error('????????:', error)
          uni.showToast({
            title: error.message || '????',
            icon: 'error'
          })
        }
      }, {
        loginPrompt: '???????'
      })
    },

    toggleCollect() {
      if (!this.postDetail) {
        return
      }

      verifyAndExecute(USER_ROLES.TRIAL, async () => {
        try {
          await actionApi.toggleCollect({
            targetId: this.postDetail.id || this.postId,
            targetType: 'post'
          })

          const nextState = !this.postDetail.isCollected
          this.postDetail.isCollected = nextState
          const current = this.postDetail.collectCount ?? 0
          this.postDetail.collectCount = nextState ? current + 1 : Math.max(current - 1, 0)

          uni.showToast({
            title: nextState ? '???' : '?????',
            icon: 'success'
          })
        } catch (error) {
          console.error('????????:', error)
          uni.showToast({
            title: error.message || '????',
            icon: 'error'
          })
        }
      }, {
        loginPrompt: '???????'
      })
    },

    toggleAuthorFollow() {
      if (!this.postDetail || !this.postDetail.userId || this.followLoading) {
        return
      }

      verifyAndExecute(USER_ROLES.TRIAL, async () => {
        this.followLoading = true
        try {
          const response = await actionApi.toggleFollow({ targetUserId: this.postDetail.userId })
          const result = response?.data || {}
          const nextState = typeof result.isFollowing === 'boolean' ? result.isFollowing : !this.postDetail.isAuthorFollowed
          this.postDetail.isAuthorFollowed = nextState
          uni.showToast({
            title: nextState ? '关注成功' : '已取消关注',
            icon: 'success'
          })
        } catch (error) {
          console.error('切换关注状态失败:', error)
          uni.showToast({
            title: error?.message || '操作失败',
            icon: 'error'
          })
        } finally {
          this.followLoading = false
        }
      }, {
        loginPrompt: '登录后才能关注作者'
      })
    },

    showCommentInput() {
      verifyAndExecute(USER_ROLES.TRIAL, () => {
        this.showCommentBox = true
        this.replyTarget = null
        this.commentContent = ''
      }, {
        loginPrompt: '???????'
      })
    },

    changeCommentSort(sort) {
      if (this.commentSort === sort) {
        return
      }
      this.commentSort = sort
      this.loadComments(true)
    },

    toggleCommentLike(commentId) {
      const comment = this.commentList.find(item => item.id === commentId)
      if (!comment) {
        return
      }

      verifyAndExecute(USER_ROLES.VERIFIED, async () => {
        try {
          await actionApi.toggleLike({
            targetId: commentId,
            targetType: 'comment'
          })

          comment.isLiked = !comment.isLiked
          comment.likeCount = comment.isLiked
            ? (comment.likeCount ?? 0) + 1
            : Math.max((comment.likeCount ?? 0) - 1, 0)
        } catch (error) {
          console.error('??????:', error)
          uni.showToast({
            title: error.message || '????',
            icon: 'error'
          })
        }
      }, {
        loginPrompt: '?????????',
        permissionPrompt: '????????????'
      })
    },

    replyToComment(comment) {
      if (!comment) {
        return
      }

      if (!this.currentUser?.userId) {
        uni.showToast({ title: '????', icon: 'error' })
        return
      }

      this.showCommentBox = true
      this.replyTarget = comment
      this.commentContent = ''
    },

    deleteComment(commentId) {
      const target = this.commentList.find(item => item.id === commentId)
      if (!target) {
        return
      }

      verifyAndExecute(USER_ROLES.VERIFIED, async () => {
        try {
          const { confirm } = await uni.showModal({
            title: '????',
            content: '???????????',
            confirmText: '??',
            confirmColor: '#ff4757'
          })

          if (!confirm) {
            throw new Error('cancel')
          }

          await commentApi.deleteComment(commentId)

          uni.showToast({ title: '????', icon: 'success' })

          this.commentList = this.commentList.filter(item => item.id !== commentId)
          this.commentTotal = Math.max(this.commentTotal - 1, 0)
          if (this.postDetail) {
            this.postDetail.commentCount = Math.max((this.postDetail.commentCount ?? 0) - 1, 0)
          }
        } catch (error) {
          console.error('??????:', error)
          if (error.message !== 'cancel') {
            uni.showToast({
              title: error.message || '????',
              icon: 'error'
            })
          }
        }
      }, {
        loginPrompt: '???????',
        permissionPrompt: '????????????'
      })
    },

    submitComment() {
      const content = this.commentContent.trim()
      if (!content) {
        uni.showToast({ title: '???????', icon: 'error' })
        return
      }

      if (this.commentSubmitting) {
        return
      }

      verifyAndExecute(USER_ROLES.VERIFIED, async () => {
        this.commentSubmitting = true
        try {
          const payload = {
            postId: this.postId,
            content
          }

          if (this.replyTarget?.id) {
            payload.parentId = this.replyTarget.id
          }

          await commentApi.createComment(payload)

          uni.showToast({ title: '????', icon: 'success' })

          this.commentContent = ''
          this.showCommentBox = false
          this.replyTarget = null

          await this.loadComments(true)
          if (this.postDetail) {
            this.postDetail.commentCount = (this.postDetail.commentCount ?? 0) + 1
          }
        } catch (error) {
          console.error('??????:', error)
          uni.showToast({
            title: error.message || '????',
            icon: 'error'
          })
        } finally {
          this.commentSubmitting = false
        }
      }, {
        loginPrompt: '???????',
        permissionPrompt: '??????????'
      })
    },

    reportPost() {
      if (!this.postId) {
        return
      }

      this.closeMoreActions()
      uni.navigateTo({
        url: `/pages/report/report?targetType=post&targetId=${this.postId}`
      })
    },

    reportComment(commentId) {
      if (!commentId) {
        return
      }

      uni.navigateTo({
        url: `/pages/report/report?targetType=comment&targetId=${commentId}`
      })
    },

    showMoreReplies(commentId) {
      uni.navigateTo({
        url: `/pages/comment-detail/comment-detail?commentId=${commentId}`
      })
    },

    canDeleteComment(comment) {
      if (!comment) {
        return false
      }
      return this.currentUser?.userId === comment.userId || this.currentUser?.role >= 2
    },

    async handleVote(optionId) {
      if (!this.currentUser?.userId) {
        uni.showToast({ title: '????', icon: 'error' })
        return
      }

      if (this.postDetail?.myVote !== null && this.postDetail?.myVote !== undefined) {
        uni.showToast({ title: '???????', icon: 'none' })
        return
      }

      try {
        await postApi.vote(this.postId, optionId)
        uni.showToast({ title: '????', icon: 'success' })
        await this.loadPostDetail()
      } catch (error) {
        console.error('????:', error)
        uni.showToast({
          title: error.message || '????',
          icon: 'error'
        })
      }
    },

    getVotePercentage(option) {
      const totalVotes = this.getTotalVotes()
      if (totalVotes === 0) return 0

      const voteCount = option.voteCount || 0
      return Math.round((voteCount / totalVotes) * 100)
    },

    getTotalVotes() {
      if (!this.postDetail?.pollOptions) return 0

      return this.postDetail.pollOptions.reduce((total, option) => {
        return total + (option.voteCount || 0)
      }, 0)
    },

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
        return '??'
      } else if (diff < hour) {
        return `${Math.floor(diff / minute)}???`
      } else if (diff < day) {
        return `${Math.floor(diff / hour)}???`
      } else if (diff < week) {
        return `${Math.floor(diff / day)}??`
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

.user-actions {
	display: flex;
	align-items: center;
	gap: 16rpx;
	flex-shrink: 0;
}

.follow-author {
	display: flex;
	align-items: center;
	flex-shrink: 0;
}

.follow-author-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	height: 60rpx;
	min-width: 160rpx;
	padding: 0 32rpx;
	border-radius: 999rpx;
	border: none;
	background: linear-gradient(135deg, #ff7a45, #ff4d4f);
	box-shadow: 0 12rpx 28rpx rgba(255, 77, 79, 0.22);
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 500;
	line-height: 1;
	transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}

.follow-author-btn .btn-icon {
	font-size: 32rpx;
}

.follow-author-btn .btn-text {
	font-size: 26rpx;
}

.follow-author-btn.following {
	background: linear-gradient(135deg, #f7f8fa, #eef1f5);
	color: #5c6b80;
	border: 1rpx solid rgba(92, 107, 128, 0.18);
	box-shadow: none;
}

.follow-author-btn.following .btn-icon {
	font-size: 28rpx;
	color: #52c41a;
}

.follow-author-btn.following .btn-text {
	color: inherit;
}

.follow-author-btn--active {
	transform: scale(0.97);
	box-shadow: 0 8rpx 20rpx rgba(255, 77, 79, 0.24);
}

.follow-author-btn[disabled] {
	opacity: 0.6;
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
	gap: 16rpx;
}

.grid-1 {
	grid-template-columns: 1fr;
}

.grid-2 {
	grid-template-columns: repeat(2, minmax(0, 1fr));
}

.grid-3 {
	grid-template-columns: repeat(3, minmax(0, 1fr));
}

.image-grid.layout-single {
	justify-items: center;
	gap: 20rpx;
}

.post-image {
	width: 100%;
	height: 220rpx;
	border-radius: 18rpx;
	display: block;
	background-color: #f5f6f8;
	overflow: hidden;
	transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.image-grid.layout-double .post-image {
	height: 320rpx;
}

.image-grid.layout-multi .post-image {
	height: 220rpx;
}

.post-image--single {
	height: auto;
	max-height: 680rpx;
	border-radius: 24rpx;
	box-shadow: 0 16rpx 32rpx rgba(15, 35, 95, 0.12);
	background-color: #ffffff;
}

.image-grid.layout-single .post-image {
	width: 100%;
}

/* 投票选项样式 */
.poll-options {
	padding: 30rpx;
	background-color: #f8f9fa;
	margin: 0 30rpx 30rpx;
	border-radius: 16rpx;
}

.poll-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
}

.poll-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.poll-status {
	font-size: 24rpx;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	background-color: #52c41a;
	color: white;
}

.poll-status.pending {
	background-color: #1890ff;
}

.poll-list {
	margin-bottom: 20rpx;
}

.poll-option {
	background-color: white;
	border-radius: 12rpx;
	padding: 24rpx;
	margin-bottom: 16rpx;
	border: 2rpx solid #e8e8e8;
	transition: all 0.3s;
}

.poll-option:last-child {
	margin-bottom: 0;
}

.poll-option.selected {
	border-color: #1890ff;
	background-color: #e6f7ff;
}

.poll-option.voted {
	cursor: not-allowed;
}

.poll-option:active:not(.voted) {
	transform: scale(0.98);
}

.poll-option-content {
	width: 100%;
}

.option-text-wrapper {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.option-text {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	flex: 1;
}

.check-mark {
	font-size: 32rpx;
	color: #1890ff;
	font-weight: bold;
	margin-left: 16rpx;
}

.vote-bar {
	height: 8rpx;
	background-color: #e8e8e8;
	border-radius: 4rpx;
	overflow: hidden;
	margin-bottom: 12rpx;
}

.vote-bar-fill {
	height: 100%;
	background: linear-gradient(90deg, #1890ff 0%, #40a9ff 100%);
	border-radius: 4rpx;
	transition: width 0.5s ease;
}

.vote-stats {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.vote-count {
	font-size: 24rpx;
	color: #666;
}

.vote-percentage {
	font-size: 24rpx;
	color: #1890ff;
	font-weight: bold;
}

.poll-total {
	padding-top: 20rpx;
	border-top: 1rpx solid #e8e8e8;
}

.total-text {
	font-size: 24rpx;
	color: #999;
	display: block;
	text-align: center;
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







