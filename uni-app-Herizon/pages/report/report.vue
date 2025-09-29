<!-- 举报页面 - 举报内容和用户 -->
<template>
	<!-- 主容器：举报表单 -->
	<view class="report-container">
		<!-- 举报对象信息 -->
		<view class="report-target">
			<view class="target-header">
				<text class="target-title">举报{{ getTargetTypeText() }}</text>
			</view>
			<view class="target-info" v-if="targetInfo">
				<!-- 用户举报 -->
				<view class="user-target" v-if="targetType === 'user'">
					<image class="target-avatar" :src="targetInfo.avatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
					<view class="target-details">
						<text class="target-name">{{ targetInfo.nickname }}</text>
						<text class="target-username">@{{ targetInfo.username }}</text>
					</view>
				</view>
				<!-- 帖子/评论举报 -->
				<view class="content-target" v-else>
					<view class="target-content">
						<text class="content-preview">{{ getContentPreview() }}</text>
					</view>
					<view class="target-author">
						<text class="author-text">来自 @{{ targetInfo.authorName }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 举报原因选择 -->
		<view class="report-reasons">
			<view class="section-title">
				<text class="title-text">举报原因</text>
				<text class="required-mark">*</text>
			</view>
			<view class="reasons-list">
				<view class="reason-item"
					  v-for="reason in reportReasons"
					  :key="reason.id"
					  :class="{ 'selected': selectedReason === reason.id }"
					  @click="selectReason(reason.id)">
					<view class="reason-content">
						<text class="reason-title">{{ reason.title }}</text>
						<text class="reason-desc">{{ reason.description }}</text>
					</view>
					<text class="select-indicator">{{ selectedReason === reason.id ? '✓' : '' }}</text>
				</view>
			</view>
		</view>

		<!-- 详细描述 -->
		<view class="report-description">
			<view class="section-title">
				<text class="title-text">详细说明</text>
				<text class="optional-mark">（选填）</text>
			</view>
			<textarea class="description-input"
					  v-model="reportDescription"
					  placeholder="请详细描述您举报的具体原因，这将有助于我们更好地处理您的举报"
					  :maxlength="500"
					  auto-height>
			</textarea>
			<view class="char-count">
				<text class="count-text">{{ reportDescription.length }}/500</text>
			</view>
		</view>

		<!-- 证据上传 -->
		<view class="report-evidence">
			<view class="section-title">
				<text class="title-text">相关证据</text>
				<text class="optional-mark">（选填）</text>
			</view>
			<view class="evidence-tip">
				<text class="tip-text">您可以上传相关截图作为举报证据，最多3张</text>
			</view>
			<view class="evidence-upload">
				<view class="uploaded-images">
					<view class="image-item" v-for="(image, index) in evidenceImages" :key="index">
						<image class="evidence-image" :src="image" mode="aspectFill"></image>
						<text class="remove-image" @click="removeImage(index)">✕</text>
					</view>
				</view>
				<view class="upload-btn" v-if="evidenceImages.length < 3" @click="chooseImage">
					<text class="upload-icon">📷</text>
					<text class="upload-text">添加图片</text>
				</view>
			</view>
		</view>

		<!-- 举报须知 -->
		<view class="report-notice">
			<view class="notice-title">
				<text class="notice-text">举报须知</text>
			</view>
			<view class="notice-content">
				<text class="notice-item">• 请确保举报内容真实有效，恶意举报将被处罚</text>
				<text class="notice-item">• 我们会在24小时内处理您的举报</text>
				<text class="notice-item">• 处理结果会通过私信通知您</text>
				<text class="notice-item">• 您的举报信息将严格保密</text>
			</view>
		</view>

		<!-- 提交按钮 -->
		<view class="submit-section">
			<button class="submit-btn" :disabled="!canSubmit" @click="submitReport">
				提交举报
			</button>
		</view>
	</view>
</template>

<script>
// 引入API和工具函数
import { actionApi, userApi, postApi, commentApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 举报对象信息
			targetType: '', // user | post | comment
			targetId: null,
			targetInfo: null,

			// 举报表单
			selectedReason: null,
			reportDescription: '',
			evidenceImages: [],

			// 举报原因列表
			reportReasons: [],

			// 用户信息
			currentUser: null,

			// 页面状态
			loading: false
		}
	},

	computed: {
		/**
		 * 是否可以提交举报
		 */
		canSubmit() {
			return this.selectedReason && !this.loading
		}
	},

	onLoad(options) {
		// 获取举报参数
		this.targetType = options.targetType
		this.targetId = parseInt(options.targetId)

		if (!this.targetType || !this.targetId) {
			uni.showToast({ title: '参数错误', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 获取当前用户信息
		this.currentUser = getAuthInfo()
		if (!this.currentUser?.userId) {
			uni.showToast({ title: '请先登录', icon: 'error' })
			uni.navigateBack()
			return
		}

		// 设置页面标题
		uni.setNavigationBarTitle({
			title: `举报${this.getTargetTypeText()}`
		})

		// 初始化数据
		this.initReportReasons()
		this.loadTargetInfo()
	},

	methods: {
		/**
		 * 初始化举报原因列表
		 */
		initReportReasons() {
			// 根据举报类型设置不同的举报原因
			if (this.targetType === 'user') {
				this.reportReasons = [
					{
						id: 'harassment',
						title: '骚扰或恶意行为',
						description: '发送骚扰信息、恶意评论或人身攻击'
					},
					{
						id: 'spam',
						title: '垃圾信息',
						description: '发布广告、垃圾内容或重复信息'
					},
					{
						id: 'fake',
						title: '虚假账号',
						description: '冒充他人或使用虚假身份'
					},
					{
						id: 'inappropriate',
						title: '不当内容',
						description: '发布不适宜的图片或内容'
					},
					{
						id: 'other',
						title: '其他原因',
						description: '其他违反社区规范的行为'
					}
				]
			} else {
				this.reportReasons = [
					{
						id: 'inappropriate',
						title: '不当内容',
						description: '包含色情、暴力、仇恨言论等不适宜内容'
					},
					{
						id: 'spam',
						title: '垃圾信息',
						description: '广告、刷屏、无意义内容'
					},
					{
						id: 'fake',
						title: '虚假信息',
						description: '传播谣言、虚假新闻或误导信息'
					},
					{
						id: 'copyright',
						title: '侵犯版权',
						description: '未经授权使用他人作品或内容'
					},
					{
						id: 'privacy',
						title: '侵犯隐私',
						description: '泄露他人个人信息或隐私'
					},
					{
						id: 'harassment',
						title: '骚扰或攻击',
						description: '针对特定用户的骚扰、威胁或人身攻击'
					},
					{
						id: 'other',
						title: '其他原因',
						description: '其他违反社区规范的内容'
					}
				]
			}
		},

		/**
		 * 加载举报对象信息
		 */
		async loadTargetInfo() {
			try {
				this.loading = true

				switch (this.targetType) {
					case 'user':
						await this.loadUserInfo()
						break
					case 'post':
						await this.loadPostInfo()
						break
					case 'comment':
						await this.loadCommentInfo()
						break
				}
			} catch (error) {
				console.error('加载举报对象信息失败:', error)
				uni.showToast({
					title: '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 加载用户信息
		 */
		async loadUserInfo() {
			const response = await userApi.getUserProfile(this.targetId)
			if (response.code === 200) {
				this.targetInfo = response.data
			} else {
				throw new Error('获取用户信息失败')
			}
		},

		/**
		 * 加载帖子信息
		 */
		async loadPostInfo() {
			const response = await postApi.getPostDetail(this.targetId)
			if (response.code === 200) {
				this.targetInfo = {
					title: response.data.title,
					content: response.data.content,
					authorName: response.data.username
				}
			} else {
				throw new Error('获取帖子信息失败')
			}
		},

		/**
		 * 加载评论信息
		 */
		async loadCommentInfo() {
			const response = await commentApi.getCommentDetail(this.targetId)
			if (response.code === 200) {
				this.targetInfo = {
					content: response.data.content,
					authorName: response.data.username
				}
			} else {
				throw new Error('获取评论信息失败')
			}
		},

		/**
		 * 选择举报原因
		 * @param {string} reasonId - 原因ID
		 */
		selectReason(reasonId) {
			this.selectedReason = reasonId
		},

		/**
		 * 选择图片
		 */
		chooseImage() {
			uni.chooseImage({
				count: 3 - this.evidenceImages.length,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.evidenceImages = [...this.evidenceImages, ...res.tempFilePaths]
				},
				fail: (error) => {
					console.error('选择图片失败:', error)
					uni.showToast({
						title: '选择图片失败',
						icon: 'error'
					})
				}
			})
		},

		/**
		 * 移除图片
		 * @param {number} index - 图片索引
		 */
		removeImage(index) {
			this.evidenceImages.splice(index, 1)
		},

		/**
		 * 提交举报
		 */
		async submitReport() {
			if (!this.canSubmit) return

			try {
				this.loading = true

				// 上传证据图片（如果有）
				const evidenceUrls = await this.uploadEvidenceImages()

				// 构建举报数据
				const reportData = {
					targetId: this.targetId,
					targetType: this.targetType,
					reason: this.selectedReason,
					description: this.reportDescription.trim(),
					evidenceUrls: evidenceUrls
				}

				// 提交举报
				const response = await actionApi.reportContent(reportData)

				if (response.code === 200) {
					uni.showToast({
						title: '举报提交成功',
						icon: 'success'
					})

					// 延时返回上一页
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} else {
					throw new Error(response.message || '举报提交失败')
				}
			} catch (error) {
				console.error('提交举报失败:', error)
				uni.showToast({
					title: error.message || '提交失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 上传证据图片
		 * @returns {Array} 图片URL数组
		 */
		async uploadEvidenceImages() {
			if (this.evidenceImages.length === 0) {
				return []
			}

			try {
				// 这里应该调用文件上传API
				// 暂时返回原始路径
				return this.evidenceImages
			} catch (error) {
				console.error('上传证据图片失败:', error)
				// 上传失败不影响举报提交
				return []
			}
		},

		/**
		 * 获取举报对象类型文本
		 * @returns {string} 类型文本
		 */
		getTargetTypeText() {
			const typeMap = {
				user: '用户',
				post: '帖子',
				comment: '评论'
			}
			return typeMap[this.targetType] || '内容'
		},

		/**
		 * 获取内容预览
		 * @returns {string} 内容预览
		 */
		getContentPreview() {
			if (!this.targetInfo) return ''

			if (this.targetType === 'post') {
				return this.targetInfo.title || this.targetInfo.content.substring(0, 100)
			} else if (this.targetType === 'comment') {
				return this.targetInfo.content.substring(0, 100)
			}

			return ''
		}
	}
}
</script>

<style scoped>
/* 主容器样式 */
.report-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 30rpx;
	padding-bottom: 120rpx;
}

/* 举报对象信息 */
.report-target {
	background-color: white;
	border-radius: 12rpx;
	margin-bottom: 30rpx;
	overflow: hidden;
}

.target-header {
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.target-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.target-info {
	padding: 30rpx;
}

/* 用户举报目标 */
.user-target {
	display: flex;
	align-items: center;
}

.target-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.target-details {
	flex: 1;
}

.target-name {
	display: block;
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.target-username {
	font-size: 24rpx;
	color: #666;
}

/* 内容举报目标 */
.content-target {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.target-content {
	background-color: #f8f9fa;
	border-radius: 8rpx;
	padding: 20rpx;
}

.content-preview {
	font-size: 26rpx;
	color: #333;
	line-height: 1.5;
}

.target-author {
	text-align: right;
}

.author-text {
	font-size: 24rpx;
	color: #666;
}

/* 举报原因 */
.report-reasons {
	background-color: white;
	border-radius: 12rpx;
	margin-bottom: 30rpx;
	padding: 30rpx;
}

.section-title {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
}

.title-text {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.required-mark {
	color: #ff4757;
	font-size: 30rpx;
	margin-left: 6rpx;
}

.optional-mark {
	color: #999;
	font-size: 24rpx;
	margin-left: 12rpx;
}

.reasons-list {
	display: flex;
	flex-direction: column;
	gap: 1rpx;
}

.reason-item {
	display: flex;
	align-items: center;
	padding: 30rpx 20rpx;
	background-color: #fafafa;
	border-radius: 8rpx;
	margin-bottom: 16rpx;
}

.reason-item.selected {
	background-color: #f0f8ff;
	border: 1rpx solid #1890ff;
}

.reason-content {
	flex: 1;
}

.reason-title {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 8rpx;
}

.reason-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

.select-indicator {
	font-size: 32rpx;
	color: #1890ff;
	width: 40rpx;
	text-align: center;
}

/* 详细描述 */
.report-description {
	background-color: white;
	border-radius: 12rpx;
	margin-bottom: 30rpx;
	padding: 30rpx;
}

.description-input {
	width: 100%;
	min-height: 200rpx;
	background-color: #f8f9fa;
	border-radius: 8rpx;
	padding: 20rpx;
	font-size: 26rpx;
	color: #333;
	line-height: 1.5;
	margin-bottom: 16rpx;
}

.char-count {
	text-align: right;
}

.count-text {
	font-size: 22rpx;
	color: #999;
}

/* 证据上传 */
.report-evidence {
	background-color: white;
	border-radius: 12rpx;
	margin-bottom: 30rpx;
	padding: 30rpx;
}

.evidence-tip {
	margin-bottom: 20rpx;
}

.tip-text {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

.evidence-upload {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.uploaded-images {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.image-item {
	position: relative;
	width: 160rpx;
	height: 160rpx;
}

.evidence-image {
	width: 100%;
	height: 100%;
	border-radius: 8rpx;
}

.remove-image {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	width: 40rpx;
	height: 40rpx;
	background-color: #ff4757;
	color: white;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
}

.upload-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 160rpx;
	height: 160rpx;
	background-color: #f8f9fa;
	border: 2rpx dashed #ddd;
	border-radius: 8rpx;
}

.upload-icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}

.upload-text {
	font-size: 22rpx;
	color: #666;
}

/* 举报须知 */
.report-notice {
	background-color: #fff7e6;
	border: 1rpx solid #ffd591;
	border-radius: 12rpx;
	margin-bottom: 30rpx;
	padding: 30rpx;
}

.notice-title {
	margin-bottom: 20rpx;
}

.notice-text {
	font-size: 28rpx;
	font-weight: bold;
	color: #d46b08;
}

.notice-content {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.notice-item {
	font-size: 24rpx;
	color: #d46b08;
	line-height: 1.4;
}

/* 提交按钮 */
.submit-section {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: white;
	border-top: 1rpx solid #e0e0e0;
	padding: 30rpx;
}

.submit-btn {
	width: 100%;
	height: 80rpx;
	background-color: #1890ff;
	color: white;
	font-size: 28rpx;
	border: none;
	border-radius: 40rpx;
}

.submit-btn[disabled] {
	background-color: #ccc;
	color: #999;
}
</style>