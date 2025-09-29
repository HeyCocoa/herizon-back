<!-- 关注/粉丝列表页 - 通用用户列表展示 -->
<template>
	<!-- 主容器：用户列表 -->
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
							<text class="user-stat">{{ user.postCount || 0 }}条帖子</text>
							<text class="user-stat">{{ user.followerCount || 0 }}位粉丝</text>
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
						<button class="follow-btn"
								:class="{ 'following': user.isFollowing, 'mutual': user.isMutualFollow }"
								@click="toggleFollow(user)">
							{{ getFollowButtonText(user) }}
						</button>
						<button class="message-btn" @click="sendMessage(user)">
							💬
						</button>
					</view>
				</view>

				<!-- 关注时间（关注列表显示） -->
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
// 引入API和工具函数
import { userApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 页面状态
			loading: false,
			isRefreshing: false,

			// 页面参数
			userId: null,
			listType: 'following', // following | followers
			pageTitle: '关注',

			// 用户列表
			userList: [],
			totalCount: 0,

			// 搜索功能
			showSearch: false,
			searchKeyword: '',
			searchTimer: null,

			// 分页状态
			currentPage: 1,
			pageSize: 20,
			hasMoreData: true,

			// 当前用户信息
			currentUserId: null
		}
	},

	computed: {
		/**
		 * 过滤后的用户列表
		 */
		filteredUserList() {
			if (!this.searchKeyword) {
				return this.userList
			}

			const keyword = this.searchKeyword.toLowerCase()
			return this.userList.filter(user =>
				user.nickname.toLowerCase().includes(keyword) ||
				user.username.toLowerCase().includes(keyword) ||
				(user.bio && user.bio.toLowerCase().includes(keyword))
			)
		}
	},

	onLoad(options) {
		// 获取参数
		this.userId = options.userId || options.id
		this.listType = options.type || 'following'
		this.pageTitle = options.title || (this.listType === 'following' ? '关注' : '粉丝')

		// 设置页面标题
		uni.setNavigationBarTitle({
			title: this.pageTitle
		})

		// 获取当前用户信息
		const userInfo = getAuthInfo()
		this.currentUserId = userInfo?.userId

		// 如果没有指定用户ID，使用当前用户ID
		if (!this.userId) {
			this.userId = this.currentUserId
		}

		if (!this.userId) {
			uni.showToast({ title: '参数错误', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 加载用户列表
		this.loadUserList()
	},

	onUnload() {
		// 清理搜索定时器
		if (this.searchTimer) {
			clearTimeout(this.searchTimer)
		}
	},

	methods: {
		/**
		 * 加载用户列表
		 * @param {boolean} refresh - 是否刷新数据
		 */
		async loadUserList(refresh = false) {
			try {
				if (refresh) {
					this.currentPage = 1
					this.userList = []
					this.hasMoreData = true
				}

				this.loading = true

				// 模拟用户列表数据
				const mockUsers = await this.getMockUserList()

				if (refresh) {
					this.userList = mockUsers.list
				} else {
					this.userList = [...this.userList, ...mockUsers.list]
				}

				this.totalCount = mockUsers.total
				this.hasMoreData = this.userList.length < this.totalCount
			} catch (error) {
				console.error('加载用户列表失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 获取模拟用户列表数据
		 */
		async getMockUserList() {
			await new Promise(resolve => setTimeout(resolve, 500))

			const mockList = [
				{
					id: 1,
					username: 'zhangxiaomei',
					nickname: '张小美',
					avatar: '/static/img/avatar1.jpg',
					bio: '职场女性，专注于工作生活平衡的探索',
					isVerified: true,
					postCount: 45,
					followerCount: 1250,
					isFollowing: true,
					isMutualFollow: true,
					followTime: '2025-01-10T14:30:00Z'
				},
				{
					id: 2,
					username: 'lichuangye',
					nickname: '李创业',
					avatar: '/static/img/avatar2.jpg',
					bio: '女性创业者，分享创业路上的心得体会',
					isVerified: true,
					postCount: 32,
					followerCount: 890,
					isFollowing: true,
					isMutualFollow: false,
					followTime: '2025-01-08T09:15:00Z'
				},
				{
					id: 3,
					username: 'caiwangnu',
					nickname: '财女王',
					avatar: '/static/img/avatar3.jpg',
					bio: '投资理财达人，帮助女性实现财务自由',
					isVerified: false,
					postCount: 28,
					followerCount: 567,
					isFollowing: false,
					isMutualFollow: false,
					followTime: '2025-01-05T16:45:00Z'
				},
				{
					id: 4,
					username: 'jiankangmama',
					nickname: '健康妈妈',
					avatar: '/static/img/avatar4.jpg',
					bio: '营养师，分享健康生活和育儿心得',
					isVerified: true,
					postCount: 67,
					followerCount: 2340,
					isFollowing: true,
					isMutualFollow: true,
					followTime: '2025-01-03T11:20:00Z'
				}
			]

			return {
				list: this.currentPage === 1 ? mockList : [],
				total: mockList.length,
				current: this.currentPage,
				size: this.pageSize
			}
		},

		/**
		 * 加载更多用户
		 */
		loadMoreUsers() {
			if (this.hasMoreData && !this.loading) {
				this.currentPage++
				this.loadUserList()
			}
		},

		/**
		 * 刷新用户列表
		 */
		refreshUsers() {
			this.isRefreshing = true
			this.loadUserList(true).finally(() => {
				this.isRefreshing = false
			})
		},

		/**
		 * 显示搜索输入框
		 */
		showSearchInput() {
			this.showSearch = true
		},

		/**
		 * 隐藏搜索输入框
		 */
		hideSearchInput() {
			this.showSearch = false
			this.searchKeyword = ''
		},

		/**
		 * 清空搜索
		 */
		clearSearch() {
			this.searchKeyword = ''
		},

		/**
		 * 搜索输入处理（防抖）
		 */
		onSearchInput() {
			// 清除之前的定时器
			if (this.searchTimer) {
				clearTimeout(this.searchTimer)
			}

			// 设置防抖延时
			this.searchTimer = setTimeout(() => {
				// 这里可以实现实时搜索建议
				console.log('搜索关键词:', this.searchKeyword)
			}, 300)
		},

		/**
		 * 执行搜索
		 */
		performSearch() {
			// 实际项目中可以调用搜索API
			// 这里使用本地过滤实现
			console.log('执行搜索:', this.searchKeyword)
		},

		/**
		 * 切换关注状态
		 * @param {Object} user - 用户对象
		 */
		async toggleFollow(user) {
			if (!this.currentUserId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			try {
				// 模拟关注/取消关注API
				await this.mockToggleFollow(user.id, !user.isFollowing)

				// 更新用户状态
				user.isFollowing = !user.isFollowing
				user.followerCount += user.isFollowing ? 1 : -1

				// 如果是取消关注且在关注列表中，从列表中移除
				if (!user.isFollowing && this.listType === 'following') {
					this.userList = this.userList.filter(u => u.id !== user.id)
					this.totalCount = Math.max(0, this.totalCount - 1)
				}

				uni.showToast({
					title: user.isFollowing ? '关注成功' : '取消关注',
					icon: 'success'
				})
			} catch (error) {
				console.error('关注操作失败:', error)
				uni.showToast({
					title: '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 发送私信
		 * @param {Object} user - 用户对象
		 */
		sendMessage(user) {
			if (!this.currentUserId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			uni.navigateTo({
				url: `/pages/chat/chat?userId=${user.id}&username=${user.username}`
			})
		},

		/**
		 * 跳转到用户资料页
		 * @param {number} userId - 用户ID
		 */
		goToUserProfile(userId) {
			uni.navigateTo({
				url: `/pages/user-profile/user-profile?userId=${userId}`
			})
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
		 * 获取关注按钮文本
		 * @param {Object} user - 用户对象
		 * @returns {string} 按钮文本
		 */
		getFollowButtonText(user) {
			if (user.isMutualFollow) {
				return '互关'
			} else if (user.isFollowing) {
				return '已关注'
			} else {
				return '关注'
			}
		},

		/**
		 * 获取空状态图标
		 * @returns {string} 图标
		 */
		getEmptyIcon() {
			return this.listType === 'following' ? '👥' : '🙋‍♀️'
		},

		/**
		 * 获取空状态文本
		 * @returns {string} 文本
		 */
		getEmptyText() {
			if (this.listType === 'following') {
				return this.userId === this.currentUserId ? '还没有关注任何人' : 'Ta还没有关注任何人'
			} else {
				return this.userId === this.currentUserId ? '还没有粉丝' : 'Ta还没有粉丝'
			}
		},

		/**
		 * 获取空状态提示
		 * @returns {string} 提示文本
		 */
		getEmptyTip() {
			if (this.listType === 'following') {
				return '发现感兴趣的用户，建立连接吧'
			} else {
				return '发布优质内容，吸引更多关注者'
			}
		},

		/**
		 * 模拟关注/取消关注API
		 * @param {number} userId - 用户ID
		 * @param {boolean} isFollow - 是否关注
		 */
		async mockToggleFollow(userId, isFollow) {
			await new Promise(resolve => setTimeout(resolve, 300))
			return {
				code: 200,
				message: isFollow ? '关注成功' : '取消关注成功'
			}
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
}

/* 用户项 */
.user-item {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
	position: relative;
}

/* 用户信息 */
.user-info {
	display: flex;
	align-items: flex-start;
	margin-bottom: 20rpx;
}

.user-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	margin-right: 24rpx;
}

.user-details {
	flex: 1;
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

.message-btn {
	background-color: #f5f5f5;
	color: #666;
	font-size: 24rpx;
	border: none;
	border-radius: 20rpx;
	padding: 12rpx;
	width: 60rpx;
	height: 60rpx;
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