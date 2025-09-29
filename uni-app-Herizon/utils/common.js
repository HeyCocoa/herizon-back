/**
 * 通用工具函数模块
 *
 * 功能：
 * - 时间格式化处理
 * - 数据验证工具
 * - 字符串处理工具
 * - 数字格式化工具
 */

/**
 * 格式化时间显示
 * @param {String|Date} time - 时间戳或时间字符串
 * @returns {String} 格式化后的时间字符串
 */
export function formatTime(time) {
	if (!time) return ''

	const now = new Date()
	const target = new Date(time)
	const diff = now - target

	// 小于1分钟显示"刚刚"
	if (diff < 60 * 1000) {
		return '刚刚'
	}

	// 小于1小时显示"X分钟前"
	if (diff < 60 * 60 * 1000) {
		const minutes = Math.floor(diff / (60 * 1000))
		return `${minutes}分钟前`
	}

	// 小于1天显示"X小时前"
	if (diff < 24 * 60 * 60 * 1000) {
		const hours = Math.floor(diff / (60 * 60 * 1000))
		return `${hours}小时前`
	}

	// 小于7天显示"X天前"
	if (diff < 7 * 24 * 60 * 60 * 1000) {
		const days = Math.floor(diff / (24 * 60 * 60 * 1000))
		return `${days}天前`
	}

	// 超过7天显示具体日期
	const year = target.getFullYear()
	const month = target.getMonth() + 1
	const date = target.getDate()

	// 如果是当年，不显示年份
	if (year === now.getFullYear()) {
		return `${month}月${date}日`
	}

	return `${year}年${month}月${date}日`
}

/**
 * 格式化数字显示（如点赞数、浏览数等）
 * @param {Number} num - 数字
 * @returns {String} 格式化后的数字字符串
 */
export function formatNumber(num) {
	if (!num || num === 0) return '0'

	if (num < 1000) {
		return num.toString()
	}

	if (num < 10000) {
		return `${(num / 1000).toFixed(1)}k`
	}

	if (num < 100000) {
		return `${(num / 10000).toFixed(1)}w`
	}

	return `${Math.floor(num / 10000)}w`
}

/**
 * 验证邮箱格式
 * @param {String} email - 邮箱地址
 * @returns {Boolean} 是否为有效邮箱
 */
export function isValidEmail(email) {
	const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
	return emailReg.test(email)
}

/**
 * 验证手机号格式
 * @param {String} phone - 手机号
 * @returns {Boolean} 是否为有效手机号
 */
export function isValidPhone(phone) {
	const phoneReg = /^1[3-9]\d{9}$/
	return phoneReg.test(phone)
}

/**
 * 截取字符串并添加省略号
 * @param {String} str - 原字符串
 * @param {Number} maxLength - 最大长度
 * @returns {String} 截取后的字符串
 */
export function truncateString(str, maxLength = 50) {
	if (!str || str.length <= maxLength) {
		return str
	}
	return str.substring(0, maxLength) + '...'
}

/**
 * 深拷贝对象
 * @param {Object} obj - 要拷贝的对象
 * @returns {Object} 拷贝后的对象
 */
export function deepClone(obj) {
	if (obj === null || typeof obj !== 'object') {
		return obj
	}

	if (obj instanceof Date) {
		return new Date(obj.getTime())
	}

	if (obj instanceof Array) {
		return obj.map(item => deepClone(item))
	}

	if (typeof obj === 'object') {
		const cloned = {}
		Object.keys(obj).forEach(key => {
			cloned[key] = deepClone(obj[key])
		})
		return cloned
	}
}

/**
 * 防抖函数
 * @param {Function} fn - 要防抖的函数
 * @param {Number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300) {
	let timer = null
	return function(...args) {
		clearTimeout(timer)
		timer = setTimeout(() => {
			fn.apply(this, args)
		}, delay)
	}
}

/**
 * 节流函数
 * @param {Function} fn - 要节流的函数
 * @param {Number} interval - 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(fn, interval = 300) {
	let last = 0
	return function(...args) {
		const now = Date.now()
		if (now - last >= interval) {
			last = now
			fn.apply(this, args)
		}
	}
}

/**
 * 生成随机字符串
 * @param {Number} length - 字符串长度
 * @returns {String} 随机字符串
 */
export function generateRandomString(length = 8) {
	const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
	let result = ''
	for (let i = 0; i < length; i++) {
		result += chars.charAt(Math.floor(Math.random() * chars.length))
	}
	return result
}

/**
 * 获取文件扩展名
 * @param {String} filename - 文件名
 * @returns {String} 扩展名
 */
export function getFileExtension(filename) {
	if (!filename) return ''
	const lastDotIndex = filename.lastIndexOf('.')
	return lastDotIndex > -1 ? filename.substring(lastDotIndex + 1).toLowerCase() : ''
}

/**
 * 格式化文件大小
 * @param {Number} bytes - 字节数
 * @returns {String} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
	if (bytes === 0) return '0 B'

	const k = 1024
	const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
	const i = Math.floor(Math.log(bytes) / Math.log(k))

	return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 检查是否为移动设备
 * @returns {Boolean} 是否为移动设备
 */
export function isMobile() {
	// uni-app环境检测
	// #ifdef H5
	return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
	// #endif

	// #ifdef APP-PLUS
	return true
	// #endif

	// #ifdef MP-WEIXIN
	return true
	// #endif

	return false
}

/**
 * 显示成功提示
 * @param {String} message - 提示消息
 */
export function showSuccess(message) {
	uni.showToast({
		title: message,
		icon: 'success',
		duration: 2000
	})
}

/**
 * 显示错误提示
 * @param {String} message - 错误消息
 */
export function showError(message) {
	uni.showToast({
		title: message,
		icon: 'none',
		duration: 2000
	})
}

/**
 * 显示加载提示
 * @param {String} message - 加载消息
 */
export function showLoading(message = '加载中...') {
	uni.showLoading({
		title: message,
		mask: true
	})
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
	uni.hideLoading()
}