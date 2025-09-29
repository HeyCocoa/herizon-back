/**
 * 用户认证和权限管理工具
 *
 * 处理用户登录状态、权限验证、角色管理等功能
 * 基于三级权限系统：0=体验用户, 1=正式用户, 2=管理员
 */

import { getUserInfo, setUserInfo as setUserInfoInternal, clearUserInfo } from './request.js'
import { authApi } from './api.js'

// 重新导出setUserInfo函数以便其他模块使用
export { setUserInfoInternal as setUserInfo }

/**
 * 用户角色枚举
 */
export const USER_ROLES = {
	TRIAL: 0,        // 体验用户
	VERIFIED: 1,     // 正式用户（已认证）
	ADMIN: 2         // 管理员
}

/**
 * 用户角色描述
 */
export const ROLE_DESCRIPTIONS = {
	[USER_ROLES.TRIAL]: '体验用户',
	[USER_ROLES.VERIFIED]: '正式用户',
	[USER_ROLES.ADMIN]: '管理员'
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export const isLoggedIn = () => {
	const userInfo = getUserInfo()
	return !!(userInfo && userInfo.id)
}

/**
 * 获取当前用户角色
 * @returns {number} 用户角色，未登录返回-1
 */
export const getCurrentUserRole = () => {
	const userInfo = getUserInfo()
	return userInfo ? userInfo.role : -1
}

/**
 * 检查用户是否为体验用户
 * @returns {boolean} 是否为体验用户
 */
export const isTrialUser = () => {
	return getCurrentUserRole() === USER_ROLES.TRIAL
}

/**
 * 检查用户是否为正式用户
 * @returns {boolean} 是否为正式用户
 */
export const isVerifiedUser = () => {
	const role = getCurrentUserRole()
	return role === USER_ROLES.VERIFIED || role === USER_ROLES.ADMIN
}

/**
 * 检查用户是否为管理员
 * @returns {boolean} 是否为管理员
 */
export const isAdmin = () => {
	return getCurrentUserRole() === USER_ROLES.ADMIN
}

/**
 * 检查用户是否有特定权限
 * @param {number} requiredRole - 所需的最低角色级别
 * @returns {boolean} 是否有权限
 */
export const hasPermission = (requiredRole) => {
	const currentRole = getCurrentUserRole()
	return currentRole >= requiredRole
}

/**
 * 权限验证装饰器函数
 * @param {number} requiredRole - 所需的最低角色级别
 * @param {Function} callback - 有权限时执行的回调
 * @param {Function} fallback - 无权限时执行的回调
 */
export const requirePermission = (requiredRole, callback, fallback) => {
	if (hasPermission(requiredRole)) {
		callback && callback()
	} else {
		fallback && fallback()
	}
}

/**
 * 登录状态验证
 * @param {Function} callback - 已登录时执行的回调
 * @param {Function} fallback - 未登录时执行的回调
 */
export const requireLogin = (callback, fallback) => {
	if (isLoggedIn()) {
		callback && callback()
	} else {
		fallback && fallback()
	}
}

/**
 * 处理用户登录成功
 * @param {Object} userData - 登录返回的用户数据
 */
export const handleLoginSuccess = (userData) => {
	// 保存用户信息到本地存储
	setUserInfoInternal(userData)

	// 显示欢迎信息
	const roleDesc = ROLE_DESCRIPTIONS[userData.role] || '用户'
	uni.showToast({
		title: `欢迎回来，${roleDesc}`,
		icon: 'success'
	})
}

/**
 * 处理用户退出登录
 */
export const handleLogout = () => {
	// 清除本地用户信息
	clearUserInfo()

	// 显示退出提示
	uni.showToast({
		title: '已退出登录',
		icon: 'success'
	})

	// 跳转到登录页面（如果需要）
	// uni.navigateTo({
	//   url: '/pages/login/login'
	// })
}

/**
 * 导航到登录页面
 */
export const navigateToLogin = () => {
	uni.navigateTo({
		url: '/pages/login/login'
	})
}

/**
 * 权限不足提示
 * @param {string} message - 自定义提示信息
 */
export const showPermissionDenied = (message = '权限不足') => {
	uni.showToast({
		title: message,
		icon: 'none'
	})
}

/**
 * 体验用户升级提示
 */
export const showTrialUserUpgradePrompt = () => {
	uni.showModal({
		title: '身份认证',
		content: '体验用户功能有限，请完成身份认证成为正式用户以享受完整功能。',
		confirmText: '去认证',
		cancelText: '继续体验',
		success: (res) => {
			if (res.confirm) {
				// 跳转到身份认证页面
				uni.navigateTo({
					url: '/pages/verification/verification'
				})
			}
		}
	})
}

/**
 * 验证操作权限并执行
 * @param {number} requiredRole - 所需角色级别
 * @param {Function} action - 要执行的操作
 * @param {Object} options - 配置选项
 * @param {string} options.loginPrompt - 未登录提示
 * @param {string} options.permissionPrompt - 权限不足提示
 * @param {boolean} options.showTrialUpgrade - 是否显示体验用户升级提示
 */
export const verifyAndExecute = (requiredRole, action, options = {}) => {
	const {
		loginPrompt = '请先登录',
		permissionPrompt = '权限不足',
		showTrialUpgrade = true
	} = options

	// 检查登录状态
	if (!isLoggedIn()) {
		uni.showToast({
			title: loginPrompt,
			icon: 'none'
		})
		navigateToLogin()
		return
	}

	// 检查权限
	const currentRole = getCurrentUserRole()
	if (currentRole < requiredRole) {
		// 如果是体验用户且需要升级提示
		if (currentRole === USER_ROLES.TRIAL && showTrialUpgrade) {
			showTrialUserUpgradePrompt()
		} else {
			showPermissionDenied(permissionPrompt)
		}
		return
	}

	// 执行操作
	action && action()
}

/**
 * 获取用户认证信息
 * @returns {Object|null} 当前用户的认证信息，未登录返回null
 */
export const getAuthInfo = () => {
	return getUserInfo()
}

/**
 * 获取用户显示信息
 * @returns {Object} 用户显示信息
 */
export const getUserDisplayInfo = () => {
	const userInfo = getUserInfo()
	if (!userInfo) {
		return {
			nickname: '未登录',
			role: -1,
			roleDesc: '游客'
		}
	}

	return {
		nickname: userInfo.nickname || userInfo.username,
		role: userInfo.role,
		roleDesc: ROLE_DESCRIPTIONS[userInfo.role] || '未知'
	}
}

/**
 * 微信登录
 * <p>
 * 微信小程序登录流程：
 * 1. 调用wx.login()获取临时登录凭证code
 * 2. 可选：获取用户基本信息（昵称、头像等）
 * 3. 将code和用户信息发送给后端进行验证和登录
 *
 * @param {Object} options - 配置选项
 * @param {Function} options.success - 成功回调
 * @param {Function} options.fail - 失败回调
 * @param {boolean} options.withUserInfo - 是否同时获取用户信息，默认false
 */
export const wechatLogin = (options = {}) => {
	return new Promise((resolve, reject) => {
		// 在微信小程序环境中直接使用wx.login
		// #ifdef MP-WEIXIN
		wx.login({
			timeout: 10000, // 10秒超时
			success: (loginRes) => {
				console.log('微信登录成功，获取到code:', loginRes.code)

				if (!loginRes.code) {
					const error = new Error('微信登录失败：未获取到code')
					console.error(error.message)
					options.fail && options.fail(error)
					reject(error)
					return
				}

				// 构建返回结果
				const result = {
					code: loginRes.code,
					nickname: '',
					avatar: ''
				}

				// 如果需要获取用户信息
				if (options.withUserInfo) {
					// 在微信小程序中，获取用户信息需要用户主动授权
					// 这里可以添加获取用户信息的逻辑
					// 注意：微信小程序2021年4月13日后，getUserInfo不再弹出授权框
					// 建议使用getUserProfile或通过button组件的open-type="getUserInfo"

					// 暂时使用默认值，实际项目中需要通过其他方式获取用户信息
					result.nickname = '微信用户'
					result.avatar = ''
				}

				console.log('微信登录数据准备完成:', result)
				options.success && options.success(result)
				resolve(result)
			},
			fail: (error) => {
				console.error('微信登录失败:', error)
				const errorMsg = error.errMsg || '微信登录失败'
				const finalError = new Error(errorMsg)
				options.fail && options.fail(finalError)
				reject(finalError)
			}
		})
		// #endif

		// 在非微信小程序环境中，使用uni-app的通用API（主要用于调试）
		// #ifndef MP-WEIXIN
		uni.getProvider({
			service: 'oauth',
			success: (res) => {
				if (res.provider.includes('weixin')) {
					// 在APP环境中使用uni.login
					uni.login({
						provider: 'weixin',
						success: (loginRes) => {
							console.log('uni-app微信登录成功:', loginRes)

							const result = {
								code: loginRes.code || 'mock_code_' + Date.now(),
								nickname: options.withUserInfo ? '微信用户' : '',
								avatar: options.withUserInfo ? '' : ''
							}

							options.success && options.success(result)
							resolve(result)
						},
						fail: (error) => {
							console.error('uni-app微信登录失败:', error)
							options.fail && options.fail(error)
							reject(error)
						}
					})
				} else {
					const error = new Error('当前环境不支持微信登录')
					options.fail && options.fail(error)
					reject(error)
				}
			},
			fail: (error) => {
				console.error('获取登录服务失败:', error)
				options.fail && options.fail(error)
				reject(error)
			}
		})
		// #endif
	})
}

/**
 * 处理微信登录成功
 * <p>
 * 基于微信登录2.md规范的完整登录处理流程
 * 将微信登录数据发送到后端进行验证和用户创建/登录
 * 成功后保存JWT Token和用户信息到本地存储
 *
 * @param {Object} wechatData - 微信登录数据
 * @param {string} wechatData.code - 微信登录临时凭证
 * @param {string} wechatData.nickname - 用户昵称（可选）
 * @param {string} wechatData.avatar - 用户头像（可选）
 * @param {Function} successCallback - 成功回调
 * @param {Function} failCallback - 失败回调
 */
export const handleWechatLoginSuccess = async (wechatData, successCallback, failCallback) => {
	try {
		// 验证必要参数
		if (!wechatData || !wechatData.code) {
			throw new Error('微信登录数据不完整')
		}

		// 构建后端API请求数据
		const requestData = {
			code: wechatData.code,
			nickname: wechatData.nickname || '',
			avatar: wechatData.avatar || '',
			registerSource: 2, // 微信小程序
			questionnaireData: '' // 可选的身份认证问卷数据
		}

		console.log('调用新版微信登录API:', requestData)

		// 调用新的认证API
		const response = await authApi.wechatLogin(requestData)

		console.log('微信登录API响应:', response)

		// 检查响应是否成功
		if (!response || typeof response !== 'object') {
			throw new Error('服务器响应格式错误')
		}

		// 后端返回的标准格式：{ token, userInfo, isNewUser, tokenExpiration, loginTime }
		const loginResult = response
		if (loginResult.token && loginResult.userInfo) {
			// 保存Token
			uni.setStorageSync('token', loginResult.token)

			// 保存用户信息
			uni.setStorageSync('userInfo', loginResult.userInfo)

			// 保存Token过期时间
			if (loginResult.tokenExpiration) {
				uni.setStorageSync('tokenExpiration', loginResult.tokenExpiration)
			}

			// 显示登录成功提示
			const userInfo = loginResult.userInfo
			const roleDesc = userInfo.roleDesc || ROLE_DESCRIPTIONS[userInfo.role] || '用户'
			const welcomeMessage = loginResult.isNewUser ?
				`欢迎加入，${roleDesc}` :
				`欢迎回来，${roleDesc}`

			uni.showToast({
				title: welcomeMessage,
				icon: 'success',
				duration: 2000
			})

			console.log(`微信登录成功 - ${loginResult.isNewUser ? '新用户注册' : '老用户登录'}，用户ID: ${userInfo.userId}`)

			// 调用成功回调，传递标准化的用户数据
			successCallback && successCallback(loginResult)
		} else {
			throw new Error('登录响应数据不完整')
		}

	} catch (error) {
		console.error('微信登录处理失败:', error)

		// 根据错误类型提供具体的错误信息
		let errorMessage = '微信登录失败'
		if (error.message) {
			// 根据微信登录2.md中的错误处理规范
			if (error.message.includes('40029')) {
				errorMessage = 'code无效，请重新登录'
			} else if (error.message.includes('45011')) {
				errorMessage = 'API调用太频繁，请稍后重试'
			} else if (error.message.includes('40013')) {
				errorMessage = 'AppID无效，请联系管理员'
			} else if (error.message.includes('网络')) {
				errorMessage = '网络连接失败，请检查网络后重试'
			} else if (error.message.includes('code')) {
				errorMessage = '微信授权失败，请重新登录'
			} else if (error.message.includes('配置')) {
				errorMessage = '微信登录配置错误，请联系管理员'
			} else {
				errorMessage = error.message
			}
		}

		// 显示错误提示
		uni.showToast({
			title: errorMessage,
			icon: 'none',
			duration: 3000
		})

		// 调用失败回调
		failCallback && failCallback(error)
	}
}

/**
 * 检查是否支持微信登录
 * <p>
 * 检测当前运行环境是否支持微信登录功能
 * 微信小程序环境原生支持，APP环境需要配置，H5环境暂不支持
 *
 * @returns {Promise<boolean>} 是否支持微信登录
 */
export const checkWechatLoginSupport = () => {
	return new Promise((resolve) => {
		// 在微信小程序环境中，原生支持微信登录
		// #ifdef MP-WEIXIN
		console.log('检测到微信小程序环境，支持微信登录')
		resolve(true)
		// #endif

		// 在APP环境中，需要检查是否配置了微信登录
		// #ifdef APP-PLUS
		console.log('检测到APP环境，检查微信登录配置...')
		uni.getProvider({
			service: 'oauth',
			success: (res) => {
				const supportsWechat = res.provider.includes('weixin')
				console.log('APP环境微信登录支持状态:', supportsWechat)
				resolve(supportsWechat)
			},
			fail: (error) => {
				console.error('检查APP微信登录配置失败:', error)
				resolve(false)
			}
		})
		// #endif

		// 在H5环境中，暂不支持微信登录（可以扩展支持微信网页授权）
		// #ifdef H5
		console.log('检测到H5环境，暂不支持微信登录')
		resolve(false)
		// #endif

		// 其他环境默认不支持
		// #ifndef MP-WEIXIN || APP-PLUS || H5
		console.log('检测到其他环境，不支持微信登录')
		resolve(false)
		// #endif
	})
}

/**
 * 检查登录状态
 * <p>
 * 基于微信登录2.md规范的完整登录状态检查
 * 包括本地Token检查、微信登录态检查、服务器Token验证
 *
 * @param {Function} successCallback - 登录有效时的回调
 * @param {Function} failCallback - 登录失效时的回调
 */
export const checkLoginStatus = async (successCallback, failCallback) => {
	try {
		// 步骤1：检查本地是否有Token
		const token = uni.getStorageSync('token')
		const userInfo = uni.getStorageSync('userInfo')

		if (!token || !userInfo) {
			console.log('本地无登录态，需要重新登录')
			failCallback && failCallback('本地无登录态')
			return
		}

		// 步骤2：检查Token是否即将过期
		const tokenExpiration = uni.getStorageSync('tokenExpiration')
		if (tokenExpiration && Date.now() > tokenExpiration - 24 * 60 * 60 * 1000) { // 提前1天刷新
			console.log('Token即将过期，尝试刷新')
			try {
				const newToken = await authApi.refreshToken(token)
				if (newToken) {
					uni.setStorageSync('token', newToken)
					console.log('Token刷新成功')
				}
			} catch (error) {
				console.warn('Token刷新失败:', error)
			}
		}

		// 步骤3：在微信小程序环境中检查微信登录态
		// #ifdef MP-WEIXIN
		wx.checkSession({
			success() {
				console.log('微信登录态有效')
				// 验证服务器Token
				validateTokenWithServer(token, userInfo, successCallback, failCallback)
			},
			fail() {
				console.log('微信登录态过期，需要重新登录')
				// 清除本地登录态
				clearUserInfo()
				failCallback && failCallback('微信登录态过期')
			}
		})
		// #endif

		// 在非微信小程序环境中直接验证服务器Token
		// #ifndef MP-WEIXIN
		validateTokenWithServer(token, userInfo, successCallback, failCallback)
		// #endif

	} catch (error) {
		console.error('检查登录状态失败:', error)
		failCallback && failCallback(error)
	}
}

/**
 * 验证服务器端Token有效性
 * <p>
 * 调用后端API验证Token是否有效
 *
 * @param {string} token - JWT Token
 * @param {Object} userInfo - 本地用户信息
 * @param {Function} successCallback - 验证成功回调
 * @param {Function} failCallback - 验证失败回调
 */
const validateTokenWithServer = async (token, userInfo, successCallback, failCallback) => {
	try {
		await authApi.validateToken(token)

		console.log('服务器Token验证成功')
		successCallback && successCallback(userInfo)

	} catch (error) {
		console.warn('服务器Token验证失败:', error)

		// Token无效，清除本地登录态
		clearUserInfo()
		failCallback && failCallback('Token验证失败')
	}
}

/**
 * 自动登录
 * <p>
 * 基于微信登录2.md规范的自动登录逻辑
 * 优先检查现有登录态，失效时自动执行微信登录
 *
 * @param {Object} options - 配置选项
 * @param {boolean} options.forceWechatLogin - 是否强制微信登录
 * @param {Function} options.success - 成功回调
 * @param {Function} options.fail - 失败回调
 */
export const autoLogin = async (options = {}) => {
	const {
		forceWechatLogin = false,
		success,
		fail
	} = options

	if (forceWechatLogin) {
		// 强制微信登录
		console.log('执行强制微信登录')
		await performWechatLogin(success, fail)
		return
	}

	// 检查现有登录状态
	await checkLoginStatus(
		// 登录状态有效
		(userInfo) => {
			console.log('自动登录检查通过，当前用户:', userInfo.nickname || userInfo.username)
			success && success(userInfo)
		},
		// 登录状态失效，尝试微信登录
		async (reason) => {
			console.log('登录状态失效，原因:', reason)

			// 在微信小程序环境中自动执行微信登录
			// #ifdef MP-WEIXIN
			console.log('在微信小程序中执行自动微信登录')
			await performWechatLogin(success, fail)
			// #endif

			// 在其他环境中，提示用户登录
			// #ifndef MP-WEIXIN
			console.log('非微信小程序环境，需要手动登录')
			fail && fail(reason)
			// #endif
		}
	)
}

/**
 * 执行微信登录
 * <p>
 * 完整的微信登录流程执行
 *
 * @param {Function} successCallback - 成功回调
 * @param {Function} failCallback - 失败回调
 */
const performWechatLogin = async (successCallback, failCallback) => {
	try {
		// 获取微信code
		const wechatData = await wechatLogin()

		// 处理微信登录
		await handleWechatLoginSuccess(
			wechatData,
			(loginResult) => {
				console.log('微信自动登录成功')
				successCallback && successCallback(loginResult.userInfo)
			},
			(error) => {
				console.error('微信自动登录失败:', error)
				failCallback && failCallback(error)
			}
		)

	} catch (error) {
		console.error('执行微信登录失败:', error)
		failCallback && failCallback(error)
	}
}

/**
 * 重定向到登录页面
 * <p>
 * 当登录状态失效时，跳转到登录页面
 *
 * @param {string} redirectUrl - 登录成功后的跳转地址
 */
export const redirectToLogin = (redirectUrl) => {
	const currentPage = getCurrentPages()
	const currentRoute = currentPage.length > 0 ? currentPage[currentPage.length - 1].route : ''

	// 如果当前不是登录页面，则跳转到登录页面
	if (currentRoute !== 'pages/login/login') {
		const loginUrl = redirectUrl ?
			`/pages/login/login?redirect=${encodeURIComponent(redirectUrl)}` :
			'/pages/login/login'

		uni.navigateTo({
			url: loginUrl
		})
	}
}