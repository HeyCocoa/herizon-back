<!-- 个人中心页面 - 用户信息和功能管理 -->
<template>
	<!-- 主容器:个人中心 -->
	<view class="profile-container">
		<!-- 统一的用户界面(适配登录和未登录状态) -->
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
							<text class="username">{{ isLoggedIn ? (userInfo.nickname || '未设置昵称') : '访客用户' }}</text>
							<view class="verified-icon" v-if="isLoggedIn && userInfo.role >= 1">✓</view>
						</view>
						<text class="user-bio" v-if="isLoggedIn && userInfo.bio">{{ userInfo.bio }}</text>
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
					<view class="stat-item">
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
				<view class="stat-item">
					<text class="stat-number">{{ isLoggedIn ? (userStats.likesCount || 0) : '-' }}</text>
					<text class="stat-label">获赞</text>
				</view>
				</view>

				<!-- 身份认证提示 -->
				<view class="verification-prompt" v-if="isLoggedIn && userInfo.role === 0" @click="goToVerification">
					<view class="prompt-icon">🔒</view>
					<view class="prompt-content">
						<text class="prompt-title">完成身份认证</text>
						<text class="prompt-desc">解锁更多功能,享受完整体验</text>
					</view>
					<view class="prompt-arrow">›</view>
				</view>
			</view>

			<!-- 功能菜单(系统变更后精简版) -->
			<view class="menu-section">
				<view class="menu-title">我的</view>
				<view class="menu-list">
					<view class="menu-item" @click="verifyLoginAndExecute(0, viewMyPosts)">
						<view class="menu-icon">📝</view>
						<text class="menu-text">我的帖子</text>
						<view class="menu-arrow">›</view>
					</view>
					<view class="menu-item" @click="verifyLoginAndExecute(0, viewCollections)">
						<view class="menu-icon">⭐</view>
						<text class="menu-text">我的收藏</text>
						<view class="menu-arrow">›</view>
					</view>
				</view>
			</view>

			<!-- 管理员菜单(仅管理员可见) -->
			<view class="menu-section" v-if="isLoggedIn && userInfo.role === 2">
				<view class="menu-title">管理员</view>
				<view class="menu-list">
					<view class="menu-item" @click="goToAdminPage">
						<view class="menu-icon">👨‍💼</view>
						<text class="menu-text">管理后台</text>
						<view class="menu-arrow">›</view>
					</view>
				</view>
			</view>

			<!-- 设置菜单(系统功能快捷入口) -->
			<view class="menu-section">
				<view class="menu-title">设置</view>
				<view class="menu-list">
					<view class="menu-item" @click="openFeedback">
						<view class="menu-icon">💬</view>
						<text class="menu-text">意见反馈</text>
						<view class="menu-arrow">›</view>
					</view>
					<view class="menu-item" @click="aboutApp">
						<view class="menu-icon">ℹ️</view>
						<text class="menu-text">关于Herizon</text>
						<view class="menu-arrow">›</view>
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
  // 基础工具与 API
  import { userApi } from '../../../utils/api.js'
  import { isLoggedIn, USER_ROLES, getAuthInfo, getUserId, handleLogout, verifyAndExecute } from '../../../utils/auth.js'

  const DEFAULT_USER_INFO = {
    id: null,
    username: '',
    nickname: '',
    avatar: '',
    bio: '',
    role: 0,
    backgroundImage: '',
    createdAt: ''
  }

  const DEFAULT_USER_STATS = {
    postsCount: 0,
    followingCount: 0,
    followersCount: 0,
    likesCount: 0
  }

  export default {
    data() {
      return {
        isLoggedIn: false,
        userInfo: { ...DEFAULT_USER_INFO },
        userStats: { ...DEFAULT_USER_STATS },
        isLoading: false,
        isRefreshing: false
      }
    },

    onShow() {
      this.loadUserData({ showSkeleton: true })
    },

    methods: {
      resetUserState() {
        this.userInfo = { ...DEFAULT_USER_INFO }
        this.userStats = { ...DEFAULT_USER_STATS }
      },

      triggerLogin() {
        uni.navigateTo({
          url: '/pages/login/login'
        })
      },

      verifyLoginAndExecute(requiredRole, action, options = {}) {
        verifyAndExecute(requiredRole, () => {
          if (typeof action === 'function') {
            action.call(this)
          }
        }, {
          loginPrompt: '请先登录',
          permissionPrompt: '权限不足',
          showTrialUpgrade: true,
          ...options
        })
      },

      handleAvatarClick() {
        if (this.isLoggedIn) {
          this.verifyLoginAndExecute(USER_ROLES.TRIAL, this.editProfile)
        } else {
          this.triggerLogin()
        }
      },

      editProfile() {
        uni.navigateTo({
          url: '/pages/edit-profile/edit-profile'
        })
      },

      viewFollowing() {
        const title = encodeURIComponent('关注列表')
        const userId = this.userInfo.id ? `&userId=${this.userInfo.id}` : ''
        uni.navigateTo({
          url: `/pages/follow-list/follow-list?type=following&title=${title}${userId}`
        })
      },

      viewFollowers() {
        const title = encodeURIComponent('粉丝列表')
        const userId = this.userInfo.id ? `&userId=${this.userInfo.id}` : ''
        uni.navigateTo({
          url: `/pages/follow-list/follow-list?type=followers&title=${title}${userId}`
        })
      },

      viewMyPosts() {
        uni.navigateTo({
          url: '/pages/my-posts/my-posts'
        })
      },

      viewCollections() {
        uni.navigateTo({
          url: '/pages/collect-list/collect-list'
        })
      },

      goToVerification() {
        uni.navigateTo({
          url: '/pages/verification/verification'
        })
      },

      goToAdminPage() {
        uni.navigateTo({
          url: '/pages/admin/admin-main/admin-main'
        })
      },

      openFeedback() {
        this.verifyLoginAndExecute(0, () => {
          uni.navigateTo({
            url: '/pages/feedback/feedback'
          })
        })
      },


      aboutApp() {
        uni.navigateTo({
          url: '/pages/about/about'
        })
      },

      confirmLogout() {
        uni.showModal({
          title: '确认退出',
          content: '确认要退出登录吗?',
          success: (res) => {
            if (res.confirm) {
              this.doLogout()
            }
          }
        })
      },

      doLogout() {
        try {
          handleLogout()
          this.isLoggedIn = false
          this.resetUserState()
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

      async loadUserData({ showSkeleton = false } = {}) {
        if (showSkeleton) {
          this.isLoading = true
        }
        try {
          this.isLoggedIn = isLoggedIn()
          if (!this.isLoggedIn) {
            this.resetUserState()
            return
          }

          const userId = getUserId()
          const [profile, stats] = await Promise.all([
            userApi.getMyProfile().catch((error) => {
              console.error('获取用户信息失败:', error)
              return null
            }),
            userId ? userApi.getUserStats(userId).catch((error) => {
              console.error('获取用户统计失败:', error)
              return null
            }) : Promise.resolve(null)
          ])

          const storedAuthInfo = getAuthInfo()
          const fallbackInfo = {
            ...DEFAULT_USER_INFO,
            id: storedAuthInfo?.id ?? storedAuthInfo?.userId ?? null,
            username: storedAuthInfo?.username ?? DEFAULT_USER_INFO.username,
            nickname: storedAuthInfo?.nickname ?? storedAuthInfo?.username ?? DEFAULT_USER_INFO.nickname,
            avatar: storedAuthInfo?.avatar ?? DEFAULT_USER_INFO.avatar,
            bio: storedAuthInfo?.bio ?? DEFAULT_USER_INFO.bio,
            role: storedAuthInfo?.role ?? DEFAULT_USER_INFO.role,
            backgroundImage: storedAuthInfo?.backgroundImage ?? DEFAULT_USER_INFO.backgroundImage,
            createdAt: storedAuthInfo?.createdAt ?? DEFAULT_USER_INFO.createdAt
          }

          const displayInfo = profile ? { ...fallbackInfo, ...profile } : fallbackInfo
          this.userInfo = displayInfo
          if (!this.userInfo.id && userId) {
            this.userInfo.id = userId
          }

          const statsSources = []
          if (storedAuthInfo) {
            statsSources.push({
              postsCount: storedAuthInfo.postsCount ?? storedAuthInfo.postCount ?? 0,
              followingCount: storedAuthInfo.followingCount ?? 0,
              followersCount: storedAuthInfo.followersCount ?? 0,
              likesCount: storedAuthInfo.likesCount ?? storedAuthInfo.totalLikes ?? 0
            })
          }
          if (profile) {
            statsSources.push({
              postsCount: profile.postsCount ?? profile.postCount ?? 0,
              followingCount: profile.followingCount ?? 0,
              followersCount: profile.followersCount ?? 0,
              likesCount: profile.likesCount ?? profile.totalLikes ?? 0
            })
          }
          if (stats) {
            statsSources.push(stats)
          }

          let mergedStats = { ...DEFAULT_USER_STATS }
          for (const source of statsSources) {
            if (!source) {
              continue
            }
            mergedStats = {
              ...mergedStats,
              postsCount: source.postsCount ?? source.postCount ?? mergedStats.postsCount,
              followingCount: source.followingCount ?? mergedStats.followingCount,
              followersCount: source.followersCount ?? mergedStats.followersCount,
              likesCount: source.likesCount ?? source.totalLikes ?? mergedStats.likesCount
            }
          }

          this.userStats = mergedStats

        } catch (error) {
          console.error('加载用户数据失败:', error)
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          })
        } finally {
          if (showSkeleton) {
            this.isLoading = false
          }
          this.isRefreshing = false
        }
      },

      refreshUserData() {
        if (this.isRefreshing) {
          return
        }
        this.isRefreshing = true
        this.loadUserData()
      },

      formatJoinDate(timestamp) {
        if (!timestamp) {
          return ''
        }
        const date = new Date(timestamp)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        return `${year}年${month}月`
      },

      getRoleClass(role) {
        switch (role) {
          case USER_ROLES.ADMIN:
            return 'role-admin'
          case USER_ROLES.VERIFIED:
            return 'role-verified'
          case USER_ROLES.TRIAL:
            return 'role-trial'
          default:
            return 'role-guest'
        }
      },

      getRoleText(role) {
        switch (role) {
          case USER_ROLES.ADMIN:
            return '管理员'
          case USER_ROLES.VERIFIED:
            return '认证用户'
          case USER_ROLES.TRIAL:
            return '体验用户'
          default:
            return '访客'
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



















