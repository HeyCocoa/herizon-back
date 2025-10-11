/**
 * API接口定义
 *
 * 集中管理所有后端API接口调用
 * 基于42个REST API接口的完整封装
 */

import { http } from './request.js'

/**
 * 用户相关API - /api/users
 */
export const userApi = {
	/**
	 * 用户注册
	 * @param {Object} data - 注册信息
	 * @param {string} data.username - 用户名
	 * @param {string} data.email - 邮箱
	 * @param {string} data.password - 密码
	 * @param {string} data.nickname - 昵称
	 * @param {Object} data.questionnaire - 身份认证问卷
	 */
	register: (data) => http.post('/users/register', data),

	/**
	 * 用户登录
	 * @param {Object} data - 登录信息
	 * @param {string} data.username - 用户名或邮箱
	 * @param {string} data.password - 密码
	 */
	login: (data) => http.post('/users/login', data),

	/**
	 * 获取用户公开资料
	 * @param {number} userId - 用户ID
	 */
	getUserProfile: (userId) => http.get(`/users/${userId}`),

	/**
	 * 获取当前用户完整资料
	 */
	getMyProfile: () => http.get('/users/me'),

	/**
	 * 更新用户资料
	 * @param {Object} data - 更新的用户信息
	 */
	updateProfile: (data) => http.put('/users/me', data),

	/**
	 * 申请身份认证
	 * @param {Object} data - 认证材料
	 */
	applyVerification: (data) => http.post('/users/verify', data),

	/**
	 * 检查用户名可用性
	 * @param {string} username - 用户名
	 */
	checkUsername: (username) => http.get('/users/check-username', { username }),

	/**
	 * 检查邮箱可用性
	 * @param {string} email - 邮箱
	 */
	checkEmail: (email) => http.get('/users/check-email', { email }),

	/**
	 * 微信登录(已迁移到authApi)
	 * @deprecated 请使用 authApi.wechatLogin
	 */
	wechatLogin: (data) => {
		// 解决循环引用问题:直接调用HTTP请求而非引用authApi
		return http.post('/auth/wechat-login', data)
	},

	/**
	 * 绑定微信账号
	 * @param {Object} data - 绑定数据
	 * @param {string} data.code - 微信登录临时凭证(通过wx.login获取)
	 * @param {string} data.nickname - 微信用户昵称(可选)
	 * @param {string} data.avatar - 微信用户头像URL(可选)
	 */
	bindWechat: (data) => http.post('/users/bind-wechat', data),

	/**
	 * 获取用户统计数据
	 * @param {number} userId - 用户ID
	 */
	getUserStats: (userId) => http.get(`/users/${userId}/stats`)
}

/**
 * 帖子相关API - /api/posts
 */
export const postApi = {
	/**
	 * 首页帖子列表(系统变更后的简化版)
	 * @param {Object} params - 查询参数
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 */
	getHomePostList: (params) => http.get('/posts', params),

	/**
	 * 根据标签查询帖子列表(话题页面专用)
	 * @param {Object} params - 查询参数
	 * @param {number} tagId - 标签ID
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 */
	getPostsByTag: (tagId, params) => http.get(`/posts/by-tag/${tagId}`, params),

	/**
	 * 分页查询帖子列表(已废弃,保留向后兼容)
	 * @deprecated 请使用 getHomePostList 或 getPostsByTag
	 */
	getPostList: (params) => http.get('/posts', params),

	/**
	 * 查询帖子详情
	 * @param {number} postId - 帖子ID
	 */
	getPostDetail: (postId) => http.get(`/posts/${postId}`),

	/**
	 * 创建新帖子
	 * @param {Object} data - 帖子数据
	 * @param {string} data.title - 标题
	 * @param {string} data.content - 内容
	 * @param {number} data.type - 帖子类型 (0=普通, 1=投票, 2=违规公示)
	 * @param {Array} data.tagIds - 标签ID数组
	 * @param {Array} data.imageUrls - 图片URL数组
	 * @param {string} data.videoUrl - 视频URL
	 */
	createPost: (data) => http.post('/posts', data),

	/**
	 * 增加帖子浏览量
	 * @param {number} postId - 帖子ID
	 */
	addPostView: (postId) => http.post(`/posts/${postId}/view`),

	/**
	 * 在投票帖子中提交投票
	 * @param {number} postId - 帖子ID
	 * @param {number} optionId - 投票选项ID
	 */
	vote: (postId, optionId) => http.post(`/posts/${postId}/vote`, { optionId }),

	/**
	 * 查询用户帖子列表(我的帖子页面)
	 * @param {number} userId - 用户ID
	 * @param {Object} params - 查询参数
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 */
	getUserPosts: (userId, params) => http.get(`/posts/user/${userId}`, params),

	/**
	 * 删除帖子（作者或管理员）
	 * @param {number} postId - 帖子ID
	 */
	deleteMyPost: (postId) => http.delete(`/posts/${postId}`),

	/**
	 * 统计指定标签下的帖子数量(实时查询)
	 * @param {number} tagId - 标签ID
	 * @returns {Promise<number>} 帖子数量
	 */
	getPostCountByTag: (tagId) => http.get(`/posts/by-tag/${tagId}/count`),

	/**
	 * 搜索帖子(模糊匹配)
	 * <p>
	 * 根据关键词搜索帖子,支持标题和内容的模糊匹配
	 * 搜索结果按创建时间倒序排列(最新的在前)
	 *
	 * API路径:GET /api/posts/search
	 *
	 * @param {Object} params - 搜索参数
	 * @param {string} params.keyword - 搜索关键词(必需)
	 * @param {number} params.current - 当前页码,默认1
	 * @param {number} params.size - 每页数量,默认10
	 * @returns {Promise<PageResult<PostDTO>>} 分页的帖子列表
	 */
	searchPosts: (params) => http.get('/posts/search', params)
}

/**
 * 标签相关API - /api/tags
 */
export const tagApi = {
	/**
	 * 分页查询标签列表
	 * @param {Object} params - 查询参数
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 * @param {string} params.sort - 排序方式 (hot/time)
	 */
	getTagList: (params) => http.get('/tags', params),

	/**
	 * 查询标签详情
	 * @param {number} tagId - 标签ID
	 */
	getTagDetail: (tagId) => http.get(`/tags/${tagId}`),

	/**
	 * 创建新标签
	 * @param {Object} data - 标签数据
	 * @param {string} data.name - 标签名称
	 * @param {string} data.description - 标签描述
	 * @param {string} data.color - 标签颜色
	 */
	createTag: (data) => http.post('/tags', data),

	/**
	 * 更新标签信息
	 * @param {number} tagId - 标签ID
	 * @param {Object} data - 更新数据
	 */
	updateTag: (tagId, data) => http.put(`/tags/${tagId}`, data),

	/**
	 * 搜索标签(模糊匹配)
	 * @param {string} keyword - 搜索关键词
	 */
	searchTags: (keyword) => http.get('/tags/search', { keyword }),

	/**
	 * 获取热门标签
	 * @param {number} limit - 返回数量限制
	 */
	getHotTags: (limit = 10) => http.get('/tags/hot', { limit }),

	/**
	 * 删除标签(管理员)
	 * @param {number} tagId - 标签ID
	 */
	deleteTag: (tagId) => http.delete(`/tags/${tagId}`)
}

/**
 * 评论相关API - /api/comments
 */
export const commentApi = {
	/**
	 * 分页查询帖子评论
	 * @param {number} postId - 帖子ID
	 * @param {Object} params - 分页参数
	 */
	getPostComments: (postId, params) => http.get(`/comments/post/${postId}`, params),

	/**
	 * 获取子评论列表
	 * @param {number} parentId - 父评论ID
	 */
	getReplies: (parentId, params) => http.get(`/comments/${parentId}/replies`, params),

	/**
	 * 创建新评论
	 * @param {Object} data - 评论数据
	 * @param {number} data.postId - 帖子ID
	 * @param {string} data.content - 评论内容
	 * @param {number} data.parentId - 父评论ID(可选)
	 */
	createComment: (data) => http.post('/comments', data),

	/**
	 * 删除评论
	 * @param {number} commentId - 评论ID
	 */
	deleteComment: (commentId) => http.delete(`/comments/${commentId}`),

	/**
	 * 获取用户评论历史
	 * @param {number} userId - 用户ID
	 * @param {Object} params - 分页参数
	 */
	getUserComments: (userId, params) => http.get(`/comments/user/${userId}`, params),

	/**
	 * 获取评论详情
	 * @param {number} commentId - 评论ID
	 */
	getCommentDetail: (commentId) => http.get(`/comments/${commentId}`),

	/**
	 * 获取最新评论
	 * @param {number} limit - 返回数量限制
	 */
	getLatestComments: (limit = 10) => http.get('/comments/latest', { limit })
}

/**
 * 用户行为相关API - /api/actions
 */
export const actionApi = {
	/**
	 * 切换点赞状态
	 * @param {Object} data - 点赞数据
	 * @param {number} data.targetId - 目标ID(帖子或评论)
	 * @param {string} data.targetType - 目标类型 (post/comment)
	 */
	toggleLike: (data) => http.post(`/actions/like?targetId=${data.targetId}&targetType=${data.targetType || 'post'}`, null),

	/**
	 * 切换收藏状态
	 * @param {Object} data - 收藏数据
	 * @param {number} data.targetId - 目标ID
	 * @param {string} data.targetType - 目标类型
	 */
	toggleCollect: (data) => http.post(`/actions/collect?targetId=${data.targetId}&targetType=${data.targetType || 'post'}`, null),

	/**
	 * 提交内容举报
	 * @param {Object} data - 举报数据
	 * @param {number} data.targetId - 目标ID
	 * @param {string} data.targetType - 目标类型
	 * @param {string} data.reason - 举报原因
	 * @param {string} data.description - 详细描述
	 */
	reportContent: (data) => http.post(`/actions/report?targetId=${data.targetId}&targetType=${data.targetType || 'post'}&reason=${encodeURIComponent(data.reason)}&description=${encodeURIComponent(data.description || '')}`, null),

	/**
	 * 切换关注状态
	 * @param {Object} data - 关注数据
	 * @param {number} data.targetUserId - 目标用户ID
	 */
	toggleFollow: (data) => http.post(`/actions/follow?targetUserId=${data.targetUserId}`, null),

	/**
	 * 获取关注列表
	 * @param {number} userId - 用户ID
	 * @param {Object} params - 分页参数
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 */
	getFollowing: (userId, params) => http.get('/actions/following', { userId, ...params }),

	/**
	 * 获取粉丝列表
	 * @param {number} userId - 用户ID
	 * @param {Object} params - 分页参数
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 */
	getFollowers: (userId, params) => http.get('/actions/followers', { userId, ...params }),

	/**
	 * 获取收藏列表
	 * @param {number} userId - 用户ID
	 * @param {Object} params - 分页参数
	 * @param {number} params.current - 当前页码
	 * @param {number} params.size - 每页数量
	 */
	getCollections: (userId, params) => http.get('/actions/collections', { userId, ...params })
}

/**
 * 管理员相关API - /api/admin
 */
export const adminApi = {
	/**
	 * 获取待审核用户列表
	 */
	getPendingUsers: (params) => http.get('/admin/users/pending', params),

	/**
	 * 审核用户身份认证
	 * @param {number} userId - 用户ID
	 * @param {Object} data - 审核结果
	 */
	verifyUser: (userId, data) => http.post(`/admin/users/${userId}/verify`, data),

	/**
	 * 将用户提升为管理员
	 * @param {number} userId - 用户ID
	 */
	promoteUser: (userId) => http.post(`/admin/users/${userId}/promote`),

	/**
	 * 管理员删除用户
	 * @param {number} userId - 用户ID
	 */
	deleteUser: (userId) => http.delete(`/admin/users/${userId}`),

	/**
	 * 获取平台统计数据
	 */
	getStatistics: () => http.get('/admin/statistics'),

	/**
	 * 获取所有用户列表
	 */
	getAllUsers: (params) => http.get('/admin/users', params),

	/**
	 * 获取所有帖子列表
	 */
	getAllPosts: (params) => http.get('/admin/posts', params),

	/**
	 * 管理员删除帖子
	 * @param {number} postId - 帖子ID
	 */
	deletePost: (postId) => http.delete(`/admin/posts/${postId}`),

	/**
	 * 获取用户反馈列表
	 * @param {Object} params - 查询参数（可选status）
	 */
	getFeedbackList: (params) => http.get('/admin/feedback', params),

	/**
	 * 回复用户反馈
	 * @param {number} feedbackId - 反馈ID
	 * @param {Object} data - 回复数据 { reply }
	 */
	replyFeedback: (feedbackId, data) => http.post(`/admin/feedback/${feedbackId}/reply`, data)
}


/**
 * 文件上传相关API - /api/files
 */
export const fileApi = {
	/**
	 * 上传图片
	 * @param {File} file - 图片文件
	 * @param {string} type - 图片类型 (avatar/post)
	 */
	uploadImage: (file, type = 'post', onProgress) => {
		return new Promise((resolve, reject) => {
			const headers = {
				'userId': getUserId()
			}
			const token = getStoredToken()
			if (token) {
				headers['Authorization'] = 'Bearer ' + token
			}

			const uploadTask = uni.uploadFile({
				url: 'http://localhost:8080/api/files/image',
				filePath: file,
				name: 'file',
				formData: { type },
				header: headers,
				success: (response) => {
					const result = JSON.parse(response.data)
					if (result.code === 200) {
						let url = result.data
						if (url && typeof url === 'object') {
							url = url.url
						}
						if (url) {
							resolve(url)
						} else {
							reject(new Error('����URL����ȷ��Ӧ�ṹ'))
						}
					} else {
						reject(new Error(result.message))
					}
				},
				fail: (error) => {
					reject(new Error(error.errMsg || '�ϴ�ʧ��'))
				}
			})

			if (uploadTask && typeof onProgress === 'function' && typeof uploadTask.onProgressUpdate === 'function') {
				uploadTask.onProgressUpdate((progressEvent) => {
					onProgress(progressEvent.progress)
				})
			}
		})
	},

	/**
	 * 上传视频
	 * @param {File} file - 视频文件
	 */
	uploadVideo: (file) => {
		return new Promise((resolve, reject) => {
			uni.uploadFile({
				url: 'http://localhost:8080/api/files/video',
				filePath: file,
				name: 'file',
				header: {
					'userId': getUserId()
				},
				success: (response) => {
					const result = JSON.parse(response.data)
					if (result.code === 200) {
						resolve(result.data)
					} else {
						reject(new Error(result.message))
					}
				},
				fail: (error) => {
					reject(new Error(error.errMsg))
				}
			})
		})
	}
}

/**
 * 认证相关API - /api/auth
 * <p>
 * 基于微信登录2.md规范的完整认证API
 */
export const authApi = {
	/**
	 * 微信小程序登录
	 * @param {Object} data - 微信登录数据
	 * @param {string} data.code - 微信登录临时凭证(通过wx.login获取)
	 * @param {string} data.nickname - 微信用户昵称(可选)
	 * @param {string} data.avatar - 微信用户头像URL(可选)
	 * @param {number} data.registerSource - 注册来源(2=微信小程序,3=微信App)
	 * @param {string} data.questionnaireData - 身份认证问卷数据(可选,JSON格式)
	 */
	wechatLogin: (data) => http.post('/auth/wechat-login', data),

	/**
	 * 验证Token有效性
	 */
	validateToken: () => {
		return http.post('/auth/validate-token')
	},

	/**
	 * 刷新Token
	 */
	refreshToken: () => {
		return http.post('/auth/refresh-token')
	},

	/**
	 * 获取当前登录用户信息
	 */
	getCurrentUser: () => {
		return http.get('/auth/current-user')
	},

	/**
	 * 检查微信配置状态
	 */
	getWechatConfigStatus: () => http.get('/auth/wechat-config-status')
}

/**
 * 用户设置相关API
 * 基于 PUT /users/me 端点的封装
 */
export const feedbackApi = {
	/**
	 * 提交意见反馈
	 * @param {Object} data - 反馈数据
	 * @param {string} data.type - 反馈类型 (bug/feature/experience/complaint/other)
	 * @param {string} data.content - 反馈内容
	 * @param {string} [data.contact] - 联系方式(选填)
	 */
	submitFeedback: (data) => http.post('/feedback', data),

	/**
	 * 获取当前用户的反馈记录
	 * @returns {Promise<Array>} 反馈列表
	 */
	getMyFeedback: () => http.get('/feedback/me')
}

// 获取用户ID的辅助函数(需要导入)
function getUserId() {
	try {
		const userInfo = uni.getStorageSync('userInfo')
		return userInfo ? userInfo.userId || userInfo.id : ''
	} catch (error) {
		return ''
	}
}

// 获取存储的Token
function getStoredToken() {
	try {
		return uni.getStorageSync('token') || ''
	} catch (error) {
		return ''
	}
}
