<!-- 登录页面 -->
<template>
	<view class="login-container">
		<!-- 顶部装饰区域 -->
		<view class="header-section">
			<!-- 返回按钮 -->
			<view class="back-button" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="logo-area">
				<text class="app-logo">Herizon</text>
				<text class="app-slogan">女性职场成长社区</text>
			</view>
		</view>

		<!-- 登录表单区域 -->
		<view class="form-section">
			<view class="form-container">
				<view class="form-title">
					<text class="title-text">欢迎回来</text>
					<text class="subtitle-text">请登录您的账号</text>
				</view>

				<!-- 登录表单 -->
				<view class="form-content">
					<!-- 用户名/邮箱输入 -->
					<view class="input-group">
						<input
							class="form-input"
							type="text"
							placeholder="请输入用户名或邮箱"
							v-model="formData.username"
							@input="onUsernameInput"
							:maxlength="50"
						/>
						<text class="input-icon">👤</text>
					</view>

					<!-- 密码输入 -->
					<view class="input-group">
						<input
							class="form-input"
							:type="showPassword ? 'text' : 'password'"
							placeholder="请输入密码"
							v-model="formData.password"
							@input="onPasswordInput"
							:maxlength="20"
						/>
						<text class="input-icon toggle-password" @click="togglePasswordVisible">
							{{ showPassword ? '🙈' : '👁️' }}
						</text>
					</view>

					<!-- 登录按钮 -->
					<button
						class="login-btn"
						:class="{ 'active': canLogin }"
						:disabled="!canLogin"
						@click="handleLogin"
					>
						<text v-if="!isLogging">登录</text>
						<text v-else>登录中...</text>
					</button>

					<!-- 分隔符 -->
					<view class="divider" v-if="supportWechatLogin">
						<view class="divider-line"></view>
						<text class="divider-text">或</text>
						<view class="divider-line"></view>
					</view>

					<!-- 微信登录按钮 -->
					<button
						class="wechat-login-btn"
						v-if="supportWechatLogin"
						:disabled="isWechatLogging"
						@click="handleWechatLogin"
					>
						<text class="wechat-icon">💬</text>
						<text class="wechat-text" v-if="!isWechatLogging">微信登录</text>
						<text class="wechat-text" v-else>登录中...</text>
					</button>

					<!-- 快速操作 -->
					<view class="quick-actions">
						<text class="action-link" @click="goToRegister">还没有账号？立即注册</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部区域 -->
		<view class="footer-section">
			<text class="footer-text">继续使用即表示您同意我们的服务条款</text>
		</view>
	</view>
</template>

<script>
/**
 * 登录页面
 *
 * 功能特性：
 * - 用户名/邮箱登录
 * - 密码可见性切换
 * - 表单验证
 * - 自动跳转处理
 */

import { userApi } from '../../utils/api.js'
import { handleLoginSuccess, wechatLogin, handleWechatLoginSuccess, checkWechatLoginSupport } from '../../utils/auth.js'

export default {
	data() {
		return {
			// 表单数据
			formData: {
				username: '',  // 用户名或邮箱
				password: ''   // 密码
			},

			// UI状态
			showPassword: false,  // 是否显示密码
			isLogging: false,     // 登录中状态
			isWechatLogging: false, // 微信登录中状态

			// 微信登录支持
			supportWechatLogin: false, // 是否支持微信登录

			// 跳转相关
			redirectUrl: ''       // 登录成功后跳转的页面
		}
	},

	computed: {
		/**
		 * 是否可以登录
		 * 用户名和密码都不为空
		 */
		canLogin() {
			return this.formData.username.trim() && this.formData.password.trim()
		}
	},

	async onLoad(options) {
		// 获取跳转URL参数
		this.redirectUrl = decodeURIComponent(options.redirect || '/pages/tabbar/tabbar-1/tabbar-1')

		// 检查微信登录支持
		this.supportWechatLogin = await checkWechatLoginSupport()
	},

	methods: {
		/**
		 * 返回上一页
		 */
		goBack() {
			// 检查是否有上一页
			const pages = getCurrentPages()
			if (pages.length > 1) {
				// 如果有上一页，则返回
				uni.navigateBack()
			} else {
				// 如果没有上一页（比如直接通过链接访问），则跳转到首页
				uni.switchTab({
					url: '/pages/tabbar/tabbar-1/tabbar-1'
				})
			}
		},

		/**
		 * 用户名输入处理
		 */
		onUsernameInput(e) {
			this.formData.username = e.detail.value.trim()
		},

		/**
		 * 密码输入处理
		 */
		onPasswordInput(e) {
			this.formData.password = e.detail.value
		},

		/**
		 * 切换密码可见性
		 */
		togglePasswordVisible() {
			this.showPassword = !this.showPassword
		},

		/**
		 * 处理登录
		 */
		async handleLogin() {
			if (!this.canLogin || this.isLogging) {
				return
			}

			// 简单的表单验证
			if (this.formData.username.length < 3) {
				uni.showToast({
					title: '用户名至少3个字符',
					icon: 'none'
				})
				return
			}

			if (this.formData.password.length < 6) {
				uni.showToast({
					title: '密码至少6个字符',
					icon: 'none'
				})
				return
			}

			this.isLogging = true

			try {
				// 调用登录API
				const result = await userApi.login({
					username: this.formData.username,
					password: this.formData.password
				})

				// 处理登录成功
				handleLoginSuccess(result)

				// 延迟跳转，让用户看到成功提示
				setTimeout(() => {
					if (this.redirectUrl.startsWith('/pages/tabbar/')) {
						// 如果是tabbar页面，使用switchTab
						uni.switchTab({
							url: this.redirectUrl
						})
					} else {
						// 其他页面使用redirectTo或navigateBack
						uni.redirectTo({
							url: this.redirectUrl
						})
					}
				}, 1500)

			} catch (error) {
				console.error('登录失败:', error)

				// 根据错误类型显示不同提示
				let errorMessage = '登录失败，请重试'
				if (error.message) {
					if (error.message.includes('用户不存在')) {
						errorMessage = '用户不存在，请检查用户名'
					} else if (error.message.includes('密码错误')) {
						errorMessage = '密码错误，请重新输入'
					} else if (error.message.includes('账号被禁用')) {
						errorMessage = '账号已被禁用，请联系管理员'
					}
				}

				uni.showToast({
					title: errorMessage,
					icon: 'none',
					duration: 2000
				})
			} finally {
				this.isLogging = false
			}
		},

		/**
		 * 跳转到注册页面
		 */
		goToRegister() {
			uni.navigateTo({
				url: '/pages/register/register?redirect=' + encodeURIComponent(this.redirectUrl)
			})
		},

		/**
		 * 处理微信登录
		 */
		async handleWechatLogin() {
			if (this.isWechatLogging) return

			this.isWechatLogging = true

			try {
				// 执行微信登录
				const wechatData = await wechatLogin()

				// 处理微信登录成功
				await handleWechatLoginSuccess(
					wechatData,
					(userData) => {
						// 登录成功回调
						console.log('微信登录成功:', userData)

						// 延迟跳转，让用户看到成功提示
						setTimeout(() => {
							if (this.redirectUrl.startsWith('/pages/tabbar/')) {
								// 如果是tabbar页面，使用switchTab
								uni.switchTab({
									url: this.redirectUrl
								})
							} else {
								// 其他页面使用redirectTo
								uni.redirectTo({
									url: this.redirectUrl
								})
							}
						}, 1500)
					},
					(error) => {
						// 登录失败回调
						console.error('微信登录处理失败:', error)
					}
				)

			} catch (error) {
				console.error('微信登录失败:', error)

				// 根据错误类型显示不同提示
				let errorMessage = '微信登录失败'
				if (error.message) {
					if (error.message.includes('用户取消')) {
						errorMessage = '用户取消登录'
					} else if (error.message.includes('网络')) {
						errorMessage = '网络连接失败，请重试'
					} else if (error.message.includes('不支持')) {
						errorMessage = '当前环境不支持微信登录'
					}
				}

				uni.showToast({
					title: errorMessage,
					icon: 'none',
					duration: 2000
				})
			} finally {
				this.isWechatLogging = false
			}
		}
	}
}
</script>

<style lang="scss" scoped>
/* 页面容器 */
.login-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	display: flex;
	flex-direction: column;
}

/* 顶部装饰区域 */
.header-section {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 100upx 60upx 60upx 60upx;
	position: relative;

	/* 返回按钮 */
	.back-button {
		position: absolute;
		left: 30upx;
		top: 50upx;
		width: 80upx;
		height: 80upx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.1);
		border-radius: 50%;
		backdrop-filter: blur(10upx);
		transition: all 0.3s;

		&:active {
			background: rgba(255, 255, 255, 0.2);
			transform: scale(0.95);
		}

		.back-icon {
			font-size: 40upx;
			color: #fff;
			font-weight: bold;
		}
	}

	.logo-area {
		text-align: center;

		.app-logo {
			display: block;
			font-size: 72upx;
			font-weight: bold;
			color: #fff;
			margin-bottom: 20upx;
			text-shadow: 0 4upx 8upx rgba(0, 0, 0, 0.2);
		}

		.app-slogan {
			font-size: 28upx;
			color: rgba(255, 255, 255, 0.9);
		}
	}
}

/* 表单区域 */
.form-section {
	background-color: #fff;
	border-radius: 40upx 40upx 0 0;
	min-height: 60vh;
	padding: 60upx 60upx 40upx 60upx;

	.form-container {
		.form-title {
			text-align: center;
			margin-bottom: 60upx;

			.title-text {
				display: block;
				font-size: 48upx;
				font-weight: bold;
				color: #333;
				margin-bottom: 15upx;
			}

			.subtitle-text {
				font-size: 28upx;
				color: #666;
			}
		}
	}
}

/* 表单内容 */
.form-content {
	.input-group {
		position: relative;
		margin-bottom: 40upx;

		.form-input {
			width: 100%;
			height: 100upx;
			padding: 0 60upx 0 20upx;
			background-color: #f8f9fa;
			border: 2upx solid #e9ecef;
			border-radius: 20upx;
			font-size: 32upx;
			color: #333;
			box-sizing: border-box;
			transition: all 0.3s;

			&:focus {
				border-color: #667eea;
				background-color: #fff;
			}
		}

		.input-icon {
			position: absolute;
			right: 25upx;
			top: 50%;
			transform: translateY(-50%);
			font-size: 32upx;
			color: #999;

			&.toggle-password {
				cursor: pointer;
				user-select: none;
			}
		}
	}

	.login-btn {
		width: 100%;
		height: 100upx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		border: none;
		border-radius: 20upx;
		font-size: 32upx;
		font-weight: bold;
		color: #fff;
		margin: 60upx 0 40upx 0;
		transition: all 0.3s;
		opacity: 0.6;

		&.active {
			opacity: 1;
			transform: translateY(-2upx);
			box-shadow: 0 8upx 20upx rgba(102, 126, 234, 0.3);
		}

		&:disabled {
			opacity: 0.6;
			transform: none;
			box-shadow: none;
		}
	}

	/* 分隔符 */
	.divider {
		display: flex;
		align-items: center;
		margin: 40upx 0;

		.divider-line {
			flex: 1;
			height: 1upx;
			background-color: #e9ecef;
		}

		.divider-text {
			margin: 0 30upx;
			font-size: 28upx;
			color: #999;
		}
	}

	/* 微信登录按钮 */
	.wechat-login-btn {
		width: 100%;
		height: 100upx;
		background-color: #07c160;
		border: none;
		border-radius: 20upx;
		font-size: 32upx;
		font-weight: bold;
		color: #fff;
		margin-bottom: 40upx;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: all 0.3s;

		&:hover {
			background-color: #06ad56;
			transform: translateY(-2upx);
			box-shadow: 0 8upx 20upx rgba(7, 193, 96, 0.3);
		}

		&:disabled {
			opacity: 0.6;
			transform: none;
			box-shadow: none;
		}

		.wechat-icon {
			font-size: 36upx;
			margin-right: 15upx;
		}

		.wechat-text {
			font-size: 32upx;
		}
	}

	.quick-actions {
		text-align: center;

		.action-link {
			font-size: 28upx;
			color: #667eea;
			text-decoration: underline;
		}
	}
}

/* 底部区域 */
.footer-section {
	padding: 40upx 60upx;
	text-align: center;

	.footer-text {
		font-size: 24upx;
		color: rgba(255, 255, 255, 0.7);
	}
}
</style>