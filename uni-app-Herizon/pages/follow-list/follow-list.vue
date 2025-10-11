<!-- 关注/粉丝列表页 - 通用用户列表展示 -->
<template>
	<!-- 主容器:用户列表 -->
	<view class="follow-list-container">
		<!-- 列表头部 -->
		<view class="list-header" v-if="userList.length > 0">
			<text class="total-count">共{{ totalCount }}位{{ pageTitle }}</text>
			<view class="search-box" @click="showSearchInput">
				<text class="search-icon">🔍</text>
				<text class="search-placeholder">搜索用户</text>
			</view>
		</view>

		<!-- 搜索输入框 -->
		<view class="search-input-bar" v-if="showSearch">
			<view class="input-wrapper">
				<input class="search-input"
					   v-model="searchKeyword"
					   placeholder="输入用户名或昵称"
					   @input="onSearchInput"
					   @confirm="performSearch">
				</input>
				<text class="clear-search" v-if="searchKeyword" @click="clearSearch">✕</text>
			</view>
			<text class="cancel-search" @click="hideSearchInput">取消</text>
		</view>

		<!-- 用户列表 -->
		<scroll-view class="users-scroll"
					 scroll-y="true"
					 @scrolltolower="loadMoreUsers"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshUsers">

			<!-- 用户项 -->
			<view class="user-item" v-for="user in filteredUserList" :key="user.id" @click="goToUserProfile(user.id)">
				<!-- 用户头像和信息 -->
				<view class="user-info">
					<image class="user-avatar" :src="user.avatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
					<view class="user-details">
						<view class="user-name-row">
							<text class="user-nickname">{{ user.nickname }}</text>
							<text class="verified-badge" v-if="user.isVerified">✓</text>
						</view>
						<text class="user-username">@{{ user.username }}</text>
						<text class="user-bio" v-if="user.bio">{{ user.bio.substring(0, 50) }}{{ user.bio.length > 50 ? '...' : '' }}</text>
						<view class="user-stats">
							<text class="user-stat">{{ user.postsCount || 0 }}条帖子</text>
							<text class="user-stat">{{ user.followersCount || 0 }}位粉丝</text>
						</view>
					</view>
				</view>

				<!-- 关注状态和操作 -->
				<view class="user-actions" @click.stop="">
					<!-- 当前用户不显示操作按钮 -->
					<view v-if="user.id === currentUserId" class="self-indicator">
						<text class="self-text">我</text>
					</view>
					<!-- 其他用户显示关注按钮 -->
					<view v-else class="follow-actions">
						<button class="follow-btn" :disabled="user._updating"
								:class="{ 'following': user.isFollowing, 'mutual': user.isMutualFollow }"
								@click="toggleFollow(user)">
							{{ getFollowButtonText(user) }}
						</button>
					</view>
				</view>

				<!-- 关注时间(关注列表显示) -->
				<view class="follow-time" v-if="listType === 'following' && user.followTime">
					<text class="time-text">{{ formatTime(user.followTime) }}关注</text>
				</view>

				<!-- 互相关注标识 -->
				<view class="mutual-badge" v-if="user.isMutualFollow">
					<text class="mutual-text">互相关注</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="userList.length === 0 && !loading">
				<text class="empty-icon">{{ getEmptyIcon() }}</text>
				<text class="empty-text">{{ getEmptyText() }}</text>
				<text class="empty-tip">{{ getEmptyTip() }}</text>
				<button class="explore-btn" @click="goToExplore" v-if="listType === 'following'">
					去发现用户
				</button>
			</view>

			<!-- 搜索无结果 -->
			<view class="no-search-result" v-if="searchKeyword && filteredUserList.length === 0 && userList.length > 0">
				<text class="no-result-text">没有找到相关用户</text>
				<text class="no-result-tip">试试其他关键词</text>
			</view>

			<!-- 加载更多 -->
			<view class="load-more" v-if="hasMoreData && userList.length > 0">
				<text class="load-more-text">{{ loading ? '加载中...' : '加载更多' }}</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { actionApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			loading: false,
			isRefreshing: false,
			userId: null,
			listType: 'following',
			pageTitle: '关注',
			userList: [],
			totalCount: 0,
			showSearch: false,
			searchKeyword: '',
			searchTimer: null,
			currentPage: 1,
			pageSize: 20,
			hasMoreData: true,
			currentUserId: null
		}
	},

	computed: {
		filteredUserList() {
			if (!this.searchKeyword) {
				return this.userList
			}

			const keyword = this.searchKeyword.toLowerCase()
			return this.userList.filter(user => {
				const nickname = (user.nickname || '').toLowerCase()
				const username = (user.username || '').toLowerCase()
				const bio = (user.bio || '').toLowerCase()
				return nickname.includes(keyword) ||
					username.includes(keyword) ||
					bio.includes(keyword)
			})
		}
	},

	onLoad(options) {
		this.userId = options.userId || options.id
		this.listType = options.type || 'following'

		const rawTitle = options.title
		const defaultTitle = this.listType === 'following' ? '关注' : '粉丝'

		if (rawTitle) {
			try {
				this.pageTitle = decodeURIComponent(rawTitle)
			} catch (error) {
				console.warn('[follow-list] decode title failed:', rawTitle, error)
				this.pageTitle = rawTitle
			}
		} else {
			this.pageTitle = defaultTitle
		}

		uni.setNavigationBarTitle({
			title: this.pageTitle
		})

		const userInfo = getAuthInfo()
		this.currentUserId = userInfo?.userId

		if (!this.userId) {
			this.userId = this.currentUserId
		}

		if (!this.userId) {
			uni.showToast({ title: '缺少参数', icon: 'error' })
			uni.navigateBack()
			return
		}

		this.loadUserList(true)
	},

	onUnload() {
		if (this.searchTimer) {
			clearTimeout(this.searchTimer)
		}
	},

	methods: {
		async loadUserList(refresh = false) {
			if (this.loading) {
				return
			}

			try {
				if (refresh) {
					this.currentPage = 1
					this.userList = []
					this.hasMoreData = true
				}

				this.loading = true

				const page = this.currentPage
				const params = {
					current: page,
					size: this.pageSize
				}

				const pageData = await (this.listType === 'following'
					? actionApi.getFollowing(this.userId, params)
					: actionApi.getFollowers(this.userId, params)) || {}
				const records = (pageData.records || []).map(item => ({
					...item,
					nickname: item.nickname || item.username || '',
					followersCount: typeof item.followersCount === 'number' ? item.followersCount : 0,
					followingCount: typeof item.followingCount === 'number' ? item.followingCount : 0,
					postsCount: typeof item.postsCount === 'number' ? item.postsCount : 0,
					isFollowing: !!item.isFollowing,
					isMutualFollow: !!item.isMutualFollow,
					isSelf: !!item.isSelf
				}))

				if (refresh) {
					this.userList = records
				} else {
					this.userList = [...this.userList, ...records]
				}

				this.totalCount = pageData.total || 0
				this.currentPage = pageData.current || page
				this.pageSize = pageData.size || this.pageSize
				this.hasMoreData = this.userList.length < this.totalCount
			} catch (error) {
				console.error('加载用户列表失败:', error)
				uni.showToast({
					title: error?.message || '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		loadMoreUsers() {
			if (!this.hasMoreData || this.loading) {
				return
			}
			this.currentPage += 1
			this.loadUserList()
		},

		refreshUsers() {
			if (this.isRefreshing) {
				return
			}
			this.isRefreshing = true
			this.loadUserList(true).finally(() => {
				this.isRefreshing = false
			})
		},

		showSearchInput() {
			this.showSearch = true
		},

		hideSearchInput() {
			this.showSearch = false
			this.searchKeyword = ''
		},

		clearSearch() {
			this.searchKeyword = ''
		},

		onSearchInput() {
			if (this.searchTimer) {
				clearTimeout(this.searchTimer)
			}

			this.searchTimer = setTimeout(() => {
				console.log('搜索关键字:', this.searchKeyword)
			}, 300)
		},

		performSearch() {
			console.log('执行搜索:', this.searchKeyword)
		},

		async toggleFollow(user) {
			if (!this.currentUserId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}
			if (!user || user.isSelf || user._updating) {
				return
			}

			user._updating = true
			try {
				const result = await actionApi.toggleFollow({ targetUserId: user.id }) || {}
				const isFollowing = typeof result.isFollowing === 'boolean' ? result.isFollowing : !user.isFollowing
				user.isFollowing = isFollowing
				if (typeof result.followersCount === 'number') {
					user.followersCount = result.followersCount
				}

				if (this.listType === 'followers') {
					user.isMutualFollow = isFollowing
				} else if (!isFollowing) {
					user.isMutualFollow = false
				}

				uni.showToast({
					title: isFollowing ? '关注成功' : '取消关注',
					icon: 'success'
				})

				await this.loadUserList(true)
			} catch (error) {
				console.error('关注操作失败:', error)
				uni.showToast({
					title: '操作失败',
					icon: 'error'
				})
			} finally {
				user._updating = false
			}
		},

		goToUserProfile(userId) {
			uni.navigateTo({
				url: `/pages/user-profile/user-profile?userId=${userId}`
			})
	},

		goToExplore() {
			uni.switchTab({
				url: '/pages/tabbar/tabbar-1/tabbar-1'
			})
		},

		getFollowButtonText(user) {
			if (user.isMutualFollow) {
				return '互相关注'
			}
			return user.isFollowing ? '已关注' : '关注'
		},

		getEmptyIcon() {
			return this.listType === 'following' ? '👀' : '📭'
		},

		getEmptyText() {
			if (this.listType === 'following') {
				return this.userId === this.currentUserId ? '你还没有关注任何人' : 'Ta 暂时没有关注任何人'
			}
			return this.userId === this.currentUserId ? '还没有人关注你' : 'Ta 暂时没有粉丝'
		},

		getEmptyTip() {
			if (this.listType === 'following') {
				return '去发现更多有意思的创作者吧'
			}
			return '多多分享优质内容，吸引更多关注'
		},

		formatTime(timeString) {
			if (!timeString) {
				return ''
			}

			const now = new Date()
			const time = new Date(timeString)
			const diff = now - time

			const day = 24 * 60 * 60 * 1000
			const week = 7 * day
			const month = 30 * day

			if (diff < day) {
				return '今天'
			}
			if (diff < week) {
				return Math.floor(diff / day) + '天前'
			}
			if (diff < month) {
				return Math.floor(diff / week) + '周前'
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
/* 主容器样式 */
.follow-list-container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

/* 列表头部 */
.list-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
}

.total-count {
	font-size: 26rpx;
	color: #666;
}

.search-box {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 20rpx;
	background-color: #f5f5f5;
	border-radius: 20rpx;
}

.search-icon {
	font-size: 24rpx;
	color: #999;
}

.search-placeholder {
	font-size: 24rpx;
	color: #999;
}

/* 搜索输入框 */
.search-input-bar {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
}

.input-wrapper {
	flex: 1;
	display: flex;
	align-items: center;
	background-color: #f5f5f5;
	border-radius: 25rpx;
	padding: 16rpx 24rpx;
	margin-right: 20rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.clear-search {
	font-size: 24rpx;
	color: #999;
	padding: 8rpx;
}

.cancel-search {
	font-size: 28rpx;
	color: #1890ff;
	padding: 8rpx;
}

/* 用户滚动视图 */
.users-scroll {
	height: calc(100vh - 120rpx);
	padding: 20rpx 30rpx;
	box-sizing: border-box;
	width: 100%;
}

/* 用户项 */
.user-item {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
	position: relative;
	width: 100%;
	box-sizing: border-box;
	overflow: hidden;
}

/* 用户信息 */
.user-info {
	display: flex;
	align-items: flex-start;
	margin-bottom: 20rpx;
	padding-right: 180rpx;
	box-sizing: border-box;
}

.user-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	margin-right: 24rpx;
}

.user-details {
	flex: 1;
	min-width: 0;
}

.user-name-row {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
}

.user-nickname {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-right: 12rpx;
}

.verified-badge {
	background-color: #1890ff;
	color: white;
	font-size: 20rpx;
	padding: 4rpx 8rpx;
	border-radius: 10rpx;
}

.user-username {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 12rpx;
}

.user-bio {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
	margin-bottom: 16rpx;
}

.user-stats {
	display: flex;
	gap: 30rpx;
	flex-wrap: wrap;
}

.user-stat {
	font-size: 22rpx;
	color: #999;
}

/* 用户操作 */
.user-actions {
	position: absolute;
	top: 30rpx;
	right: 30rpx;
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.self-indicator {
	padding: 12rpx 20rpx;
	background-color: #f5f5f5;
	border-radius: 20rpx;
}

.self-text {
	font-size: 24rpx;
	color: #999;
}

.follow-actions {
	display: flex;
	gap: 16rpx;
	align-items: center;
}

.follow-btn {
	background-color: #1890ff;
	color: white;
	font-size: 24rpx;
	border: none;
	border-radius: 20rpx;
	padding: 12rpx 24rpx;
	min-width: 100rpx;
}

.follow-btn.following {
	background-color: #f5f5f5;
	color: #666;
	border: 1rpx solid #d9d9d9;
}

.follow-btn.mutual {
	background-color: #52c41a;
	color: white;
}

/* 关注时间 */
.follow-time {
	position: absolute;
	bottom: 30rpx;
	right: 30rpx;
}

.time-text {
	font-size: 22rpx;
	color: #999;
}

/* 互相关注标识 */
.mutual-badge {
	position: absolute;
	top: 30rpx;
	left: 30rpx;
	background-color: #fff2e8;
	border: 1rpx solid #ffb366;
	border-radius: 12rpx;
	padding: 6rpx 12rpx;
}

.mutual-text {
	font-size: 20rpx;
	color: #d46b08;
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

/* 搜索无结果 */
.no-search-result {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 300rpx;
	background-color: white;
	border-radius: 12rpx;
	padding: 40rpx;
}

.no-result-text {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 12rpx;
}

.no-result-tip {
	font-size: 24rpx;
	color: #999;
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
</style>




