<!-- 注册页面 -->
<template>
	<view class="register-container">
		<!-- 顶部导航 -->
		<view class="nav-section">
			<text class="back-btn" @click="goBack">←</text>
			<text class="nav-title">创建账号</text>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 注册表单区域 -->
		<scroll-view class="form-section" scroll-y="true">
			<view class="form-container">
				<view class="form-title">
					<text class="title-text">加入 Herizon</text>
					<text class="subtitle-text">开启您的职场成长之旅</text>
				</view>

				<!-- 注册表单 -->
				<view class="form-content">
					<!-- 用户名输入 -->
					<view class="input-group">
						<text class="input-label">用户名</text>
						<input
							class="form-input"
							type="text"
							placeholder="请输入用户名（3-20个字符）"
							v-model="formData.username"
							@input="onUsernameInput"
							@blur="checkUsername"
							:maxlength="20"
						/>
						<text class="input-hint" :class="{ 'error': usernameError, 'success': usernameValid }">
							{{ usernameMessage }}
						</text>
					</view>

					<!-- 邮箱输入 -->
					<view class="input-group">
						<text class="input-label">邮箱</text>
						<input
							class="form-input"
							type="email"
							placeholder="请输入邮箱地址"
							v-model="formData.email"
							@input="onEmailInput"
							@blur="checkEmail"
							:maxlength="50"
						/>
						<text class="input-hint" :class="{ 'error': emailError, 'success': emailValid }">
							{{ emailMessage }}
						</text>
					</view>

					<!-- 密码输入 -->
					<view class="input-group">
						<text class="input-label">密码</text>
						<view class="password-input">
							<input
								class="form-input"
								:type="showPassword ? 'text' : 'password'"
								placeholder="请输入密码（6-20个字符）"
								v-model="formData.password"
								@input="onPasswordInput"
								:maxlength="20"
							/>
							<text class="toggle-password" @click="togglePasswordVisible">
								{{ showPassword ? '🙈' : '👁️' }}
							</text>
						</view>
						<text class="input-hint" :class="{ 'error': passwordError, 'success': passwordValid }">
							{{ passwordMessage }}
						</text>
					</view>

					<!-- 确认密码 -->
					<view class="input-group">
						<text class="input-label">确认密码</text>
						<input
							class="form-input"
							type="password"
							placeholder="请再次输入密码"
							v-model="formData.confirmPassword"
							@input="onConfirmPasswordInput"
							:maxlength="20"
						/>
						<text class="input-hint" :class="{ 'error': confirmPasswordError, 'success': confirmPasswordValid }">
							{{ confirmPasswordMessage }}
						</text>
					</view>

					<!-- 昵称输入 -->
					<view class="input-group">
						<text class="input-label">昵称</text>
						<input
							class="form-input"
							type="text"
							placeholder="请输入昵称（用于显示）"
							v-model="formData.nickname"
							@input="onNicknameInput"
							:maxlength="20"
						/>
						<text class="input-hint">{{ nicknameMessage }}</text>
					</view>

					<!-- 身份认证问卷 -->
					<view class="questionnaire-section">
						<text class="section-title">身份认证</text>
						<text class="section-desc">为了营造安全的女性社区环境，请完成简单的身份认证</text>

						<view class="question-item">
							<text class="question-text">您的职业身份是？</text>
							<view class="options-list">
								<label
									class="option-item"
									v-for="option in careerOptions"
									:key="option.value"
								>
									<radio
										:value="option.value"
										:checked="formData.questionnaire.career === option.value"
										@change="onCareerChange"
									/>
									<text class="option-text">{{ option.label }}</text>
								</label>
							</view>
						</view>

						<view class="question-item">
							<text class="question-text">您希望在社区中获得什么？</text>
							<view class="options-list">
								<label
									class="option-item"
									v-for="option in purposeOptions"
									:key="option.value"
								>
									<checkbox
										:value="option.value"
										:checked="formData.questionnaire.purposes.includes(option.value)"
										@change="onPurposeChange"
									/>
									<text class="option-text">{{ option.label }}</text>
								</label>
							</view>
						</view>
					</view>

					<!-- 用户协议 -->
					<view class="agreement-section">
						<label class="agreement-item">
							<checkbox
								:checked="agreedToTerms"
								@change="onAgreementChange"
							/>
							<text class="agreement-text">
								我已阅读并同意
								<text class="link-text" @click="showTerms">《用户协议》</text>
								和
								<text class="link-text" @click="showPrivacy">《隐私政策》</text>
							</text>
						</label>
					</view>

					<!-- 注册按钮 -->
					<button
						class="register-btn"
						:class="{ 'active': canRegister }"
						:disabled="!canRegister"
						@click="handleRegister"
					>
						<text v-if="!isRegistering">创建账号</text>
						<text v-else>创建中...</text>
					</button>

					<!-- 登录链接 -->
					<view class="login-link">
						<text class="link-text" @click="goToLogin">已有账号？立即登录</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
/**
 * 注册页面
 *
 * 功能特性：
 * - 完整的注册表单验证
 * - 用户名和邮箱可用性检查
 * - 身份认证问卷
 * - 用户协议确认
 */

import { userApi } from '../../utils/api.js'
import { handleLoginSuccess } from '../../utils/auth.js'

export default {
	data() {
		return {
			// 表单数据
			formData: {
				username: '',
				email: '',
				password: '',
				confirmPassword: '',
				nickname: '',
				questionnaire: {
					career: '',           // 职业身份
					purposes: []          // 使用目的
				}
			},

			// 验证状态
			usernameError: false,
			usernameValid: false,
			usernameMessage: '用户名将作为您的唯一标识',

			emailError: false,
			emailValid: false,
			emailMessage: '用于登录和接收重要通知',

			passwordError: false,
			passwordValid: false,
			passwordMessage: '密码长度6-20个字符',

			confirmPasswordError: false,
			confirmPasswordValid: false,
			confirmPasswordMessage: '请再次输入密码',

			nicknameMessage: '昵称将在社区中显示',

			// UI状态
			showPassword: false,
			agreedToTerms: false,
			isRegistering: false,

			// 跳转相关
			redirectUrl: '',

			// 选项数据
			careerOptions: [
				{ value: 'student', label: '学生' },
				{ value: 'employee', label: '职场人士' },
				{ value: 'entrepreneur', label: '创业者' },
				{ value: 'freelancer', label: '自由职业者' },
				{ value: 'other', label: '其他' }
			],

			purposeOptions: [
				{ value: 'networking', label: '职场社交' },
				{ value: 'learning', label: '技能学习' },
				{ value: 'mentorship', label: '寻求指导' },
				{ value: 'sharing', label: '经验分享' },
				{ value: 'opportunities', label: '职业机会' }
			]
		}
	},

	computed: {
		/**
		 * 是否可以注册
		 */
		canRegister() {
			return this.usernameValid &&
				   this.emailValid &&
				   this.passwordValid &&
				   this.confirmPasswordValid &&
				   this.formData.nickname.trim() &&
				   this.formData.questionnaire.career &&
				   this.formData.questionnaire.purposes.length > 0 &&
				   this.agreedToTerms &&
				   !this.isRegistering
		}
	},

	onLoad(options) {
		// 获取跳转URL参数
		this.redirectUrl = decodeURIComponent(options.redirect || '/pages/tabbar/tabbar-1/tabbar-1')
	},

	methods: {
		/**
		 * 返回上一页
		 */
		goBack() {
			uni.navigateBack()
		},

		/**
		 * 用户名输入处理
		 */
		onUsernameInput(e) {
			this.formData.username = e.detail.value.trim()
			this.usernameError = false
			this.usernameValid = false
			this.usernameMessage = '用户名将作为您的唯一标识'
		},

		/**
		 * 检查用户名可用性
		 */
		async checkUsername() {
			const username = this.formData.username
			if (!username) return

			if (username.length < 3) {
				this.usernameError = true
				this.usernameMessage = '用户名至少需要3个字符'
				return
			}

			if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(username)) {
				this.usernameError = true
				this.usernameMessage = '用户名只能包含字母、数字、下划线和中文'
				return
			}

			try {
				const result = await userApi.checkUsername(username)
				if (result.available) {
					this.usernameValid = true
					this.usernameMessage = '用户名可用'
				} else {
					this.usernameError = true
					this.usernameMessage = '用户名已被使用'
				}
			} catch (error) {
				this.usernameError = true
				this.usernameMessage = '检查用户名失败，请重试'
			}
		},

		/**
		 * 邮箱输入处理
		 */
		onEmailInput(e) {
			this.formData.email = e.detail.value.trim()
			this.emailError = false
			this.emailValid = false
			this.emailMessage = '用于登录和接收重要通知'
		},

		/**
		 * 检查邮箱可用性
		 */
		async checkEmail() {
			const email = this.formData.email
			if (!email) return

			const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
			if (!emailRegex.test(email)) {
				this.emailError = true
				this.emailMessage = '请输入有效的邮箱地址'
				return
			}

			try {
				const result = await userApi.checkEmail(email)
				if (result.available) {
					this.emailValid = true
					this.emailMessage = '邮箱可用'
				} else {
					this.emailError = true
					this.emailMessage = '邮箱已被注册'
				}
			} catch (error) {
				this.emailError = true
				this.emailMessage = '检查邮箱失败，请重试'
			}
		},

		/**
		 * 密码输入处理
		 */
		onPasswordInput(e) {
			this.formData.password = e.detail.value
			this.validatePassword()
		},

		/**
		 * 验证密码
		 */
		validatePassword() {
			const password = this.formData.password
			if (!password) {
				this.passwordError = false
				this.passwordValid = false
				this.passwordMessage = '密码长度6-20个字符'
				return
			}

			if (password.length < 6) {
				this.passwordError = true
				this.passwordMessage = '密码至少需要6个字符'
			} else if (password.length > 20) {
				this.passwordError = true
				this.passwordMessage = '密码最多20个字符'
			} else {
				this.passwordError = false
				this.passwordValid = true
				this.passwordMessage = '密码强度良好'
			}

			// 重新验证确认密码
			this.validateConfirmPassword()
		},

		/**
		 * 确认密码输入处理
		 */
		onConfirmPasswordInput(e) {
			this.formData.confirmPassword = e.detail.value
			this.validateConfirmPassword()
		},

		/**
		 * 验证确认密码
		 */
		validateConfirmPassword() {
			const confirmPassword = this.formData.confirmPassword
			if (!confirmPassword) {
				this.confirmPasswordError = false
				this.confirmPasswordValid = false
				this.confirmPasswordMessage = '请再次输入密码'
				return
			}

			if (confirmPassword !== this.formData.password) {
				this.confirmPasswordError = true
				this.confirmPasswordMessage = '两次输入的密码不一致'
			} else {
				this.confirmPasswordError = false
				this.confirmPasswordValid = true
				this.confirmPasswordMessage = '密码确认正确'
			}
		},

		/**
		 * 昵称输入处理
		 */
		onNicknameInput(e) {
			this.formData.nickname = e.detail.value.trim()
		},

		/**
		 * 切换密码可见性
		 */
		togglePasswordVisible() {
			this.showPassword = !this.showPassword
		},

		/**
		 * 职业选择改变
		 */
		onCareerChange(e) {
			this.formData.questionnaire.career = e.detail.value
		},

		/**
		 * 目的选择改变
		 */
		onPurposeChange(e) {
			const purposes = e.detail.value
			this.formData.questionnaire.purposes = purposes
		},

		/**
		 * 协议同意状态改变
		 */
		onAgreementChange(e) {
			this.agreedToTerms = e.detail.value.length > 0
		},

		/**
		 * 显示用户协议
		 */
		showTerms() {
			uni.showModal({
				title: '用户协议',
				content: '这里是用户协议内容...',
				showCancel: false
			})
		},

		/**
		 * 显示隐私政策
		 */
		showPrivacy() {
			uni.showModal({
				title: '隐私政策',
				content: '这里是隐私政策内容...',
				showCancel: false
			})
		},

		/**
		 * 处理注册
		 */
		async handleRegister() {
			if (!this.canRegister) return

			this.isRegistering = true

			try {
				// 构建注册数据
				const registerData = {
					username: this.formData.username,
					email: this.formData.email,
					password: this.formData.password,
					nickname: this.formData.nickname,
					questionnaire: this.formData.questionnaire
				}

				// 调用注册API
				const result = await userApi.register(registerData)

				// 注册成功，自动登录
				handleLoginSuccess(result)

				uni.showToast({
					title: '注册成功！',
					icon: 'success'
				})

				// 延迟跳转
				setTimeout(() => {
					if (this.redirectUrl.startsWith('/pages/tabbar/')) {
						uni.switchTab({
							url: this.redirectUrl
						})
					} else {
						uni.redirectTo({
							url: this.redirectUrl
						})
					}
				}, 1500)

			} catch (error) {
				console.error('注册失败:', error)

				let errorMessage = '注册失败，请重试'
				if (error.message) {
					errorMessage = error.message
				}

				uni.showToast({
					title: errorMessage,
					icon: 'none',
					duration: 2000
				})
			} finally {
				this.isRegistering = false
			}
		},

		/**
		 * 跳转到登录页面
		 */
		goToLogin() {
			uni.redirectTo({
				url: '/pages/login/login?redirect=' + encodeURIComponent(this.redirectUrl)
			})
		}
	}
}
</script>

<style lang="scss" scoped>
/* 页面容器 */
.register-container {
	min-height: 100vh;
	background-color: #f8f9fa;
	display: flex;
	flex-direction: column;
}

/* 导航区域 */
.nav-section {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20upx 30upx;
	background-color: #fff;
	border-bottom: 2upx solid #f0f0f0;

	.back-btn {
		font-size: 40upx;
		color: #333;
		width: 80upx;
	}

	.nav-title {
		font-size: 36upx;
		font-weight: bold;
		color: #333;
	}

	.nav-placeholder {
		width: 80upx;
	}
}

/* 表单区域 */
.form-section {
	flex: 1;
	padding: 40upx 30upx;

	.form-container {
		background-color: #fff;
		border-radius: 20upx;
		padding: 40upx 30upx;

		.form-title {
			text-align: center;
			margin-bottom: 50upx;

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
		margin-bottom: 35upx;

		.input-label {
			display: block;
			font-size: 28upx;
			font-weight: bold;
			color: #333;
			margin-bottom: 15upx;
		}

		.form-input {
			width: 100%;
			height: 80upx;
			padding: 0 20upx;
			background-color: #f8f9fa;
			border: 2upx solid #e9ecef;
			border-radius: 12upx;
			font-size: 28upx;
			color: #333;
			box-sizing: border-box;
			transition: all 0.3s;

			&:focus {
				border-color: #667eea;
				background-color: #fff;
			}
		}

		.password-input {
			position: relative;

			.toggle-password {
				position: absolute;
				right: 20upx;
				top: 50%;
				transform: translateY(-50%);
				font-size: 28upx;
				color: #999;
			}
		}

		.input-hint {
			display: block;
			font-size: 24upx;
			color: #999;
			margin-top: 10upx;
			min-height: 36upx;

			&.error {
				color: #dc3545;
			}

			&.success {
				color: #28a745;
			}
		}
	}
}

/* 问卷区域 */
.questionnaire-section {
	margin: 50upx 0;
	padding: 30upx;
	background-color: #f8f9fa;
	border-radius: 12upx;

	.section-title {
		display: block;
		font-size: 32upx;
		font-weight: bold;
		color: #333;
		margin-bottom: 10upx;
	}

	.section-desc {
		display: block;
		font-size: 24upx;
		color: #666;
		margin-bottom: 30upx;
	}

	.question-item {
		margin-bottom: 30upx;

		.question-text {
			display: block;
			font-size: 28upx;
			font-weight: bold;
			color: #333;
			margin-bottom: 20upx;
		}

		.options-list {
			.option-item {
				display: flex;
				align-items: center;
				margin-bottom: 15upx;

				.option-text {
					font-size: 26upx;
					color: #333;
					margin-left: 15upx;
				}
			}
		}
	}
}

/* 协议区域 */
.agreement-section {
	margin: 40upx 0;

	.agreement-item {
		display: flex;
		align-items: flex-start;

		.agreement-text {
			font-size: 26upx;
			color: #666;
			margin-left: 15upx;
			line-height: 1.6;

			.link-text {
				color: #667eea;
				text-decoration: underline;
			}
		}
	}
}

/* 注册按钮 */
.register-btn {
	width: 100%;
	height: 100upx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border: none;
	border-radius: 20upx;
	font-size: 32upx;
	font-weight: bold;
	color: #fff;
	margin: 40upx 0;
	opacity: 0.6;
	transition: all 0.3s;

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

/* 登录链接 */
.login-link {
	text-align: center;
	margin-top: 30upx;

	.link-text {
		font-size: 28upx;
		color: #667eea;
		text-decoration: underline;
	}
}
</style>