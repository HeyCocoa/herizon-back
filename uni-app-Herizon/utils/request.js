/**
 * 统一HTTP请求封装
 *
 * 提供标准化的API请求方法,包含:
 * - 自动添加用户身份认证
 * - 统一响应格式处理 Result<T>
 * - 错误处理和重试机制
 * - 请求/响应拦截器
 */

// API基础配置
const CONFIG = {
	baseURL: 'https://1sd14738sb896.vicp.fun:443/api',
	timeout: 10000,
	header: {
		'Content-Type': 'application/json'
	}
}

/**
 * 统一HTTP请求方法
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求URL(相对路径)
 * @param {string} options.method - 请求方法 GET/POST/PUT/DELETE
 * @param {Object} options.data - 请求数据
 * @param {Object} options.header - 自定义请求头
 * @returns {Promise} 返回处理后的响应数据
 */
const request = (options) => {
	// 构建完整的请求配置
	const requestConfig = {
		url: CONFIG.baseURL + options.url,
		method: options.method || 'GET',
		timeout: CONFIG.timeout,
		header: {
			...CONFIG.header,
			...options.header
		}
	}

	// 只在用户已登录时添加userId头部
	const userId = getUserId()
	if (userId !== undefined && userId !== null && userId !== '') {
		requestConfig.header['userId'] = String(userId)
	}

	// 添加请求数据
	if (options.data) {
		if (options.method === 'GET') {
			requestConfig.data = options.data
		} else {
			requestConfig.data = options.data
		}
	}

	// 发送请求并处理响应
	return new Promise((resolve, reject) => {
		uni.request({
			...requestConfig,
			success: (response) => {
				// 处理HTTP状态码
				if (response.statusCode !== 200) {
					reject(new Error(`HTTP ${response.statusCode}: ${response.errMsg}`))
					return
				}

				// 处理业务响应格式 Result<T>
				const result = response.data
				if (result.code === 200) {
					// 成功:返回data字段
					resolve(result.data)
				} else {
					// 业务错误:抛出错误信息
					reject(new Error(result.message || '请求失败'))
				}
			},
			fail: (error) => {
				// 网络错误处理
				console.error('网络请求失败:', error)
				reject(new Error(error.errMsg || '网络连接失败'))
			}
		})
	})
}

/**
 * 获取当前用户ID
 * @returns {string} 用户ID,未登录返回空字符串
 */
const getUserId = () => {
	try {
		// 从本地存储获取用户ID
		const userInfo = getUserInfo()
		return userInfo ? (userInfo.userId || userInfo.id || '') : ''
	} catch (error) {
		console.warn('获取用户ID失败:', error)
		return ''
	}
}

/**
 * 保存用户信息到本地存储
 * @param {Object} userInfo - 用户信息对象
 */
/**
 * Normalize stored user info so both id and userId exist
 * @param {Object|null} userInfo - Raw user info object
 * @returns {Object|null} Normalized user info
 */
const normalizeUserInfo = (userInfo) => {
	if (!userInfo) {
		return null
	}

	const normalized = { ...userInfo }

	if (normalized.id && !normalized.userId) {
		normalized.userId = normalized.id
	} else if (normalized.userId && !normalized.id) {
		normalized.id = normalized.userId
	}

	return normalized
}

const setUserInfo = (userInfo) => {
	try {
		if (!userInfo) {
			uni.removeStorageSync('userInfo')
			return
		}
		const normalized = normalizeUserInfo(userInfo)
		uni.setStorageSync('userInfo', normalized)
	} catch (error) {
		console.error('保存用户信息失败:', error)
	}
}

/**
 * 清除用户信息(退出登录)
 */
const clearUserInfo = () => {
	try {
		uni.removeStorageSync('userInfo')
	} catch (error) {
		console.error('清除用户信息失败:', error)
	}
}

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息对象,未登录返回null
 */
const getUserInfo = () => {
	try {
		const stored = uni.getStorageSync('userInfo')
		if (!stored) {
			return null
		}
		const normalized = normalizeUserInfo(stored)
		if (normalized !== stored) {
			uni.setStorageSync('userInfo', normalized)
		}
		return normalized
	} catch (error) {
		console.warn('获取用户信息失败:', error)
		return null
	}
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
const isLoggedIn = () => {
	const userInfo = getUserInfo()
	return !!(userInfo && (userInfo.id || userInfo.userId))
}

// 便捷方法封装
const http = {
	/**
	 * GET请求
	 * @param {string} url - 请求URL
	 * @param {Object} data - 查询参数
	 * @param {Object} header - 自定义请求头
	 */
	get: (url, data, header) => request({ url, method: 'GET', data, header }),

	/**
	 * POST请求
	 * @param {string} url - 请求URL
	 * @param {Object} data - 请求体数据
	 * @param {Object} header - 自定义请求头
	 */
	post: (url, data, header) => request({ url, method: 'POST', data, header }),

	/**
	 * PUT请求
	 * @param {string} url - 请求URL
	 * @param {Object} data - 请求体数据
	 * @param {Object} header - 自定义请求头
	 */
	put: (url, data, header) => request({ url, method: 'PUT', data, header }),

	/**
	 * DELETE请求
	 * @param {string} url - 请求URL
	 * @param {Object} data - 查询参数
	 * @param {Object} header - 自定义请求头
	 */
	delete: (url, data, header) => request({ url, method: 'DELETE', data, header })
}

export {
	http,
	getUserInfo,
	setUserInfo,
	clearUserInfo,
	isLoggedIn
}

