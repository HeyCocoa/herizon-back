<!-- 个人中心页面 - 用户信息和功能管理 -->
<template>
	<!-- 主容器：个人中心 -->
	<view class="profile-container">
		<!-- 统一的用户界面（适配登录和未登录状态） -->
		<scroll-view class="profile-content" scroll-y="true" refresher-enabled="true" :refresher-triggered="isRefreshing" @refresherrefresh="refreshUserData">
			<!-- 用户信息卡片 -->
			<view class="user-info-card">
				<!-- 背景图 -->
				<view class="profile-bg">
					<view class="bg-image" :style="{ backgroundColor: userInfo.backgroundImage ? 'transparent' : '#f33e54' }">
						<image v-if="userInfo.backgroundImage" class="bg-image-real" :src="userInfo.backgroundImage" mode="aspectFill"></image>
					</view>
					<view class="bg-overlay"></view>
				</view>

				<!-- 用户基本信息 -->
				<view class="user-basic-info">
					<view class="avatar-section">
						<view class="user-avatar" :style="{ backgroundColor: userInfo.avatar ? 'transparent' : '#e5e5e5' }" @click="handleAvatarClick">
							<image v-if="userInfo.avatar && isLoggedIn" class="user-avatar-real" :src="userInfo.avatar" mode="aspectFill"></image>
							<text v-else class="avatar-placeholder">👤</text>
						</view>
						<view class="role-badge" :class="getRoleClass(isLoggedIn ? userInfo.role : -1)">
							{{ getRoleText(isLoggedIn ? userInfo.role : -1) }}
						</view>
					</view>

					<view class="user-details">
						<view class="username-row">
							<text class="username">{{ isLoggedIn ? (userInfo.nickname || userInfo.username) : '访客用户' }}</text>
							<view class="verified-icon" v-if="isLoggedIn && userInfo.role >= 1">✓</view>
						</view>
						<text class="user-bio" v-if="isLoggedIn && userInfo.bio">{{ userInfo.bio }}</text>
						<text class="user-bio placeholder" v-if="isLoggedIn && !userInfo.bio" @click="verifyLoginAndExecute(0, editProfile)">点击添加个人简介</text>
						<text class="user-bio placeholder" v-if="!isLoggedIn" @click="triggerLogin">登录后可设置个人简介</text>
						<text class="join-date" v-if="isLoggedIn">{{ formatJoinDate(userInfo.createdAt) }}加入</text>
						<text class="join-date" v-else>点击登录享受完整功能</text>
					</view>

					<!-- 编辑按钮 -->
					<view class="edit-btn" @click="verifyLoginAndExecute(0, editProfile)">
						<text class="edit-icon">✏️</text>
					</view>
				</view>

				<!-- 用户数据统计 -->
				<view class="user-stats">
					<view class="stat-item" @click="verifyLoginAndExecute(0, viewMyPosts)">
						<text class="stat-number">{{ isLoggedIn ? (userStats.postsCount || 0) : '-' }}</text>
						<text class="stat-label">帖子</text>
					</view>
					<view class="stat-item" @click="verifyLoginAndExecute(0, viewFollowing)">
						<text class="stat-number">{{ isLoggedIn ? (userStats.followingCount || 0) : '-' }}</text>
						<text class="stat-label">关注</text>
					</view>
					<view class="stat-item" @click="verifyLoginAndExecute(0, viewFollowers)">
						<text class="stat-number">{{ isLoggedIn ? (userStats.followersCount || 0) : '-' }}</text>
						<text class="stat-label">粉丝</text>
					</view>
					<view class="stat-item" @click="verifyLoginAndExecute(0, viewLikes)">
						<text class="stat-number">{{ isLoggedIn ? (userStats.likesCount || 0) : '-' }}</text>
						<text class="stat-label">获赞</text>
					</view>
				</view>

				<!-- 身份认证提示 -->
				<view class="verification-prompt" v-if="isLoggedIn && userInfo.role === 0" @click="goToVerification">
					<view class="prompt-icon">🔒</view>
					<view class="prompt-content">
						<text class="prompt-title">完成身份认证</text>
						<text class="prompt-desc">解锁更多功能，享受完整体验</text>
					</view>
					<view class="prompt-arrow">未开发</view>
				</view>
			</view>

			<!-- 功能菜单（系统变更后精简版） -->
			<view class="menu-section">
				<view class="menu-title">我的</view>
				<view class="menu-list">
					<view class="menu-item" @click="verifyLoginAndExecute(0, viewCollections)">
						<view class="menu-icon">⭐</view>
						<text class="menu-text">我的收藏</text>
						<view class="menu-arrow">未开发</view>
					</view>
				</view>
			</view>

			<!-- 设置菜单（系统变更后精简版） -->
			<view class="menu-section">
				<view class="menu-title">设置</view>
				<view class="menu-list">
					<view class="menu-item" @click="verifyLoginAndExecute(0, accountSettings)">
						<view class="menu-icon">⚙️</view>
						<text class="menu-text">账号设置</text>
						<view class="menu-arrow">未开发</view>
					</view>
					<view class="menu-item" @click="verifyLoginAndExecute(0, notificationSettings)">
						<view class="menu-icon">🔔</view>
						<text class="menu-text">通知设置</text>
						<view class="menu-arrow">未开发</view>
					</view>
					<view class="menu-item" @click="helpCenter">
						<view class="menu-icon">❓</view>
						<text class="menu-text">帮助中心</text>
						<view class="menu-arrow">√</view>
					</view>
					<view class="menu-item" @click="aboutApp">
						<view class="menu-icon">ℹ️</view>
						<text class="menu-text">关于Herizon</text>
						<view class="menu-arrow">√</view>
					</view>
				</view>
			</view>

			<!-- 登录/退出 -->
			<view class="logout-section">
				<button class="logout-btn" v-if="isLoggedIn" @click="confirmLogout">退出登录</button>
				<button class="login-btn-main" v-else @click="triggerLogin">登录</button>
			</view>

			<!-- 底部间距 -->
			<view class="bottom-space"></view>
		</scroll-view>

		<!-- 加载状态 -->
		<view class="loading-state" v-if="isLoading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
	// 导入必要的工具和API
	import { userApi, postApi } from '../../../utils/api.js'
	import { isLoggedIn, USER_ROLES, getUserDisplayInfo, handleLogout, verifyAndExecute } from '../../../utils/auth.js'

	export default {
		data() {
			return {
				// 登录状态
				isLoggedIn: false,

				// 用户信息
				userInfo: {
					id: null,
					username: '',
					nickname: '',
					avatar: '',
					bio: '',
					role: 0,
					backgroundImage: '',
					createdAt: ''
				},

				// 用户数据统计
				userStats: {
					postsCount: 0,
					followingCount: 0,
					followersCount: 0,
					likesCount: 0
				},

				// 加载状态
				isLoading: false,
				isRefreshing: false
			}
		},

		/**
		 * 页面加载时初始化
		 */
		onLoad() {
			this.checkLoginStatus()
			this.loadUserData()
		},

		/**
		 * 页面显示时刷新数据
		 */
		onShow() {
			this.checkLoginStatus()
			if (this.isLoggedIn) {
				this.loadUserData()
			}
		},

		methods: {
			/**
			 * 检查用户登录状态
			 */
			checkLoginStatus() {
				this.isLoggedIn = isLoggedIn()
				if (this.isLoggedIn) {
					const userDisplayInfo = getUserDisplayInfo()
					this.userInfo = {
						...this.userInfo,
						...userDisplayInfo
					}
				}
			},

			/**
			 * 权限验证并执行操作
			 * @param {number} requiredRole - 所需角色级别
			 * @param {Function} action - 要执行的操作
			 */
			verifyLoginAndExecute(requiredRole, action) {
				verifyAndExecute(requiredRole, action, {
					loginPrompt: '请先登录后使用此功能'
				})
			},

			/**
			 * 触发登录流程
			 */
			triggerLogin() {
				uni.navigateTo({
					url: '/pages/login/login?redirect=' + encodeURIComponent('/pages/tabbar/tabbar-5/tabbar-5')
				})
			},

			/**
			 * 处理头像点击
			 */
			handleAvatarClick() {
				if (this.isLoggedIn && this.userInfo.avatar) {
					this.previewAvatar()
				} else {
					this.triggerLogin()
				}
			},

			/**
			 * 加载用户数据
			 */
			async loadUserData() {
				if (!this.isLoggedIn) return

				try {
					this.isLoading = true

					// 尝试获取用户详细信息，如果失败则使用本地存储的基本信息
					try {
						const userResponse = await userApi.getMyProfile()
						if (userResponse.code === 200) {
							this.userInfo = {
								...this.userInfo,
								...userResponse.data
							}
						}
					} catch (apiError) {
						console.warn('API调用失败，使用本地用户信息:', apiError)
						// 如果API调用失败，继续使用已有的用户信息（来自getUserDisplayInfo）
					}

					// 获取用户统计数据
					await this.loadUserStats()

				} catch (error) {
					console.error('加载用户数据失败:', error)
					// 不显示错误提示，以免影响用户体验
					// 继续使用现有的用户信息
				} finally {
					this.isLoading = false
					this.isRefreshing = false
				}
			},

			/**
			 * 加载用户统计数据
			 * 模拟实现，实际应该调用专门的统计API
			 */
			async loadUserStats() {
				try {
					// 模拟统计数据
					this.userStats = {
						postsCount: Math.floor(Math.random() * 50) + 5,
						followingCount: Math.floor(Math.random() * 100) + 10,
						followersCount: Math.floor(Math.random() * 200) + 20,
						likesCount: Math.floor(Math.random() * 500) + 50
					}
				} catch (error) {
					console.error('加载统计数据失败:', error)
				}
			},

			/**
			 * 刷新用户数据
			 */
			refreshUserData() {
				this.isRefreshing = true
				this.loadUserData()
			},

			/**
			 * 跳转到登录页面（保留兼容性）
			 */
			goToLogin() {
				this.triggerLogin()
			},

			/**
			 * 跳转到注册页面
			 */
			goToRegister() {
				uni.navigateTo({
					url: '/pages/register/register'
				})
			},

			/**
			 * 预览头像
			 */
			previewAvatar() {
				if (this.userInfo.avatar) {
					uni.previewImage({
						urls: [this.userInfo.avatar],
						current: 0
					})
				}
			},

			/**
			 * 编辑个人资料
			 */
			editProfile() {
				// TODO: 实现个人资料编辑页面
				uni.showToast({
					title: '个人资料编辑页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 查看我的帖子
			 */
			viewMyPosts() {
				// TODO: 实现我的帖子页面
				uni.showToast({
					title: '我的帖子页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 查看关注列表
			 */
			viewFollowing() {
				// TODO: 实现关注列表页面
				uni.showToast({
					title: '关注列表页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 查看粉丝列表
			 */
			viewFollowers() {
				// TODO: 实现粉丝列表页面
				uni.showToast({
					title: '粉丝列表页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 查看获赞记录
			 */
			viewLikes() {
				// TODO: 实现获赞记录页面
				uni.showToast({
					title: '获赞记录页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 跳转到身份认证页面
			 */
			goToVerification() {
				// TODO: 实现身份认证页面
				uni.showToast({
					title: '身份认证页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 查看收藏
			 */
			viewCollections() {
				// TODO: 实现收藏列表页面
				uni.showToast({
					title: '收藏列表页面开发中',
					icon: 'none'
				})
			},


			/**
			 * 账号设置
			 */
			accountSettings() {
				// TODO: 实现账号设置页面
				uni.showToast({
					title: '账号设置页面开发中',
					icon: 'none'
				})
			},


			/**
			 * 通知设置
			 */
			notificationSettings() {
				// TODO: 实现通知设置页面
				uni.showToast({
					title: '通知设置页面开发中',
					icon: 'none'
				})
			},

			/**
			 * 帮助中心
			 */
			helpCenter() {
				uni.navigateTo({
					url: '/pages/help-center/help-center'
				})
			},

			/**
			 * 关于应用
			 */
			aboutApp() {
				uni.navigateTo({
					url: '/pages/about/about'
				})
			},

			/**
			 * 确认退出登录
			 */
			confirmLogout() {
				uni.showModal({
					title: '确认退出',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							this.doLogout()
						}
					}
				})
			},

			/**
			 * 执行退出登录
			 */
			doLogout() {
				try {
					// 调用认证工具的退出方法
					handleLogout()

					// 重置页面状态
					this.isLoggedIn = false
					this.userInfo = {
						id: null,
						username: '',
						nickname: '',
						avatar: '',
						bio: '',
						role: 0,
						backgroundImage: '',
						createdAt: ''
					}
					this.userStats = {
						postsCount: 0,
						followingCount: 0,
						followersCount: 0,
						likesCount: 0
					}

					// 跳转到首页
					uni.switchTab({
						url: '/pages/tabbar/tabbar-1/tabbar-1'
					})
				} catch (error) {
					console.error('退出登录失败:', error)
					uni.showToast({
						title: '退出失败',
						icon: 'none'
					})
				}
			},

			/**
			 * 格式化加入时间
			 * @param {string} timestamp - 时间戳
			 * @returns {string} 格式化后的时间
			 */
			formatJoinDate(timestamp) {
				if (!timestamp) return ''
				const date = new Date(timestamp)
				const year = date.getFullYear()
				const month = date.getMonth() + 1
				return `${year}年${month}月`
			},

			/**
			 * 获取用户角色样式类
			 * @param {number} role - 用户角色
			 * @returns {string} CSS类名
			 */
			getRoleClass(role) {
				switch(role) {
					case USER_ROLES.ADMIN: return 'role-admin'
					case USER_ROLES.VERIFIED: return 'role-verified'
					case USER_ROLES.TRIAL: return 'role-trial'
					default: return 'role-guest'
				}
			},

			/**
			 * 获取用户角色文本
			 * @param {number} role - 用户角色
			 * @returns {string} 角色文本
			 */
			getRoleText(role) {
				switch(role) {
					case USER_ROLES.ADMIN: return '管理员'
					case USER_ROLES.VERIFIED: return '认证用户'
					case USER_ROLES.TRIAL: return '体验用户'
					default: return '游客'
				}
			}
		}
	}
</script>

<style scoped>
	/* 主容器样式 */
	.profile-container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
	}

	/* 未登录状态 */
	.login-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		min-height: 100vh;
		padding: 0 60rpx;
	}

	.login-prompt {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 80rpx;
	}

	.guest-avatar {
		width: 160rpx;
		height: 160rpx;
		border-radius: 50%;
		background-color: #e5e5e5;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 80rpx;
		margin-bottom: 30rpx;
	}

	.guest-text {
		font-size: 36rpx;
		color: #333;
		margin-bottom: 15rpx;
		font-weight: bold;
	}

	.login-hint {
		font-size: 28rpx;
		color: #999;
	}

	.login-buttons {
		display: flex;
		gap: 30rpx;
	}

	.login-btn, .register-btn {
		width: 200rpx;
		height: 80rpx;
		border-radius: 40rpx;
		font-size: 30rpx;
		border: none;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.login-btn {
		background-color: #f33e54;
		color: white;
	}

	.register-btn {
		background-color: white;
		color: #f33e54;
		border: 2rpx solid #f33e54;
	}

	/* 已登录内容 */
	.profile-content {
		height: 100vh;
		padding: 20rpx 0;
	}

	/* 用户信息卡片 */
	.user-info-card {
		background-color: white;
		margin: 0 20rpx 30rpx;
		border-radius: 20rpx;
		overflow: hidden;
		position: relative;
	}

	/* 背景图 */
	.profile-bg {
		height: 200rpx;
		position: relative;
		overflow: hidden;
	}

	.bg-image {
		width: 100%;
		height: 100%;
		position: relative;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.bg-image-real {
		width: 100%;
		height: 100%;
	}

	.bg-overlay {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.1));
	}

	/* 用户基本信息 */
	.user-basic-info {
		display: flex;
		align-items: flex-start;
		padding: 30rpx;
		margin-top: -80rpx;
		position: relative;
		z-index: 2;
	}

	.avatar-section {
		position: relative;
		margin-right: 25rpx;
	}

	.user-avatar {
		width: 160rpx;
		height: 160rpx;
		border-radius: 50%;
		border: 6rpx solid white;
		background-color: #f0f0f0;
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
	}

	.user-avatar-real {
		width: 100%;
		height: 100%;
		border-radius: 50%;
	}

	.avatar-placeholder {
		font-size: 60rpx;
		color: #999;
	}

	.role-badge {
		position: absolute;
		bottom: 10rpx;
		right: 10rpx;
		padding: 6rpx 12rpx;
		border-radius: 20rpx;
		font-size: 20rpx;
		color: white;
		border: 2rpx solid white;
	}

	.role-admin {
		background-color: #ff6b35;
	}

	.role-verified {
		background-color: #4CAF50;
	}

	.role-trial {
		background-color: #2196F3;
	}

	.role-guest {
		background-color: #9E9E9E;
	}

	.user-details {
		flex: 1;
		margin-top: 50rpx;
	}

	.username-row {
		display: flex;
		align-items: center;
		margin-bottom: 10rpx;
	}

	.username {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-right: 10rpx;
	}

	.verified-icon {
		width: 36rpx;
		height: 36rpx;
		background-color: #4CAF50;
		color: white;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 20rpx;
		font-weight: bold;
	}

	.user-bio {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
		margin-bottom: 15rpx;
	}

	.user-bio.placeholder {
		color: #999;
		font-style: italic;
	}

	.join-date {
		font-size: 24rpx;
		color: #999;
	}

	.edit-btn {
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		background-color: #f8f8f8;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 50rpx;
	}

	.edit-icon {
		font-size: 30rpx;
	}

	/* 用户数据统计 */
	.user-stats {
		display: flex;
		padding: 30rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.stat-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stat-number {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 10rpx;
	}

	.stat-label {
		font-size: 26rpx;
		color: #666;
	}

	/* 身份认证提示 */
	.verification-prompt {
		display: flex;
		align-items: center;
		padding: 25rpx 30rpx;
		margin: 20rpx;
		background: linear-gradient(135deg, #f33e54, #ff6b35);
		border-radius: 16rpx;
		color: white;
	}

	.prompt-icon {
		font-size: 40rpx;
		margin-right: 20rpx;
	}

	.prompt-content {
		flex: 1;
	}

	.prompt-title {
		font-size: 30rpx;
		font-weight: bold;
		margin-bottom: 5rpx;
		display: block;
	}

	.prompt-desc {
		font-size: 24rpx;
		opacity: 0.9;
	}

	.prompt-arrow {
		font-size: 32rpx;
		font-weight: bold;
	}

	/* 菜单部分 */
	.menu-section {
		background-color: white;
		margin: 0 20rpx 30rpx;
		border-radius: 20rpx;
		overflow: hidden;
	}

	.menu-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		padding: 30rpx 30rpx 20rpx;
	}

	.menu-list {
		padding-bottom: 10rpx;
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 25rpx 30rpx;
		border-bottom: 1rpx solid #f8f8f8;
	}

	.menu-item:last-child {
		border-bottom: none;
	}

	.menu-icon {
		width: 50rpx;
		height: 50rpx;
		margin-right: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 36rpx;
	}

	.menu-text {
		flex: 1;
		font-size: 30rpx;
		color: #333;
	}

	.menu-arrow {
		font-size: 28rpx;
		color: #ccc;
	}

	/* 退出登录部分 */
	.logout-section {
		margin: 0 20rpx 50rpx;
	}

	.logout-btn {
		width: 100%;
		height: 80rpx;
		background-color: white;
		color: #f33e54;
		border: 2rpx solid #f33e54;
		border-radius: 16rpx;
		font-size: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.login-btn-main {
		width: 100%;
		height: 80rpx;
		background-color: #f33e54;
		color: white;
		border: none;
		border-radius: 16rpx;
		font-size: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	/* 底部间距 */
	.bottom-space {
		height: 100rpx;
	}

	/* 加载状态 */
	.loading-state {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 40rpx 0;
	}

	.loading-text {
		font-size: 28rpx;
		color: #999;
	}
</style>
