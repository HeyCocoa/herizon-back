<!-- 反馈页面 - 意见反馈 -->
<template>
	<!-- 主容器：反馈表单 -->
	<view class="feedback-container">
		<!-- 反馈类型选择 -->
		<view class="feedback-types">
			<view class="section-title">
				<text class="title-text">反馈类型</text>
				<text class="required-mark">*</text>
			</view>
			<view class="types-grid">
				<view class="type-item"
					  v-for="type in feedbackTypes"
					  :key="type.id"
					  :class="{ 'selected': selectedType === type.id }"
					  @click="selectType(type.id)">
					<text class="type-icon">{{ type.icon }}</text>
					<text class="type-title">{{ type.title }}</text>
				</view>
			</view>
		</view>

		<!-- 反馈内容 -->
		<view class="feedback-content">
			<view class="section-title">
				<text class="title-text">反馈内容</text>
				<text class="required-mark">*</text>
			</view>
			<textarea class="content-input"
					  v-model="feedbackContent"
					  :placeholder="getContentPlaceholder()"
					  :maxlength="1000"
					  auto-height>
			</textarea>
			<view class="char-count">
				<text class="count-text">{{ feedbackContent.length }}/1000</text>
			</view>
		</view>

		<!-- 联系方式 -->
		<view class="contact-info">
			<view class="section-title">
				<text class="title-text">联系方式</text>
				<text class="optional-mark">（选填）</text>
			</view>
			<view class="contact-tip">
				<text class="tip-text">提供联系方式可以帮助我们更好地跟进您的反馈</text>
			</view>
			<view class="contact-inputs">
				<view class="input-row">
					<text class="input-label">邮箱</text>
					<input class="contact-input"
						   v-model="contactEmail"
						   placeholder="请输入邮箱地址"
						   type="email">
					</input>
				</view>
				<view class="input-row">
					<text class="input-label">微信号</text>
					<input class="contact-input"
						   v-model="contactWechat"
						   placeholder="请输入微信号">
					</input>
				</view>
			</view>
		</view>

		<!-- 附件上传 -->
		<view class="feedback-attachments">
			<view class="section-title">
				<text class="title-text">相关截图</text>
				<text class="optional-mark">（选填）</text>
			</view>
			<view class="attachments-tip">
				<text class="tip-text">上传相关截图有助于我们更好地理解问题，最多5张</text>
			</view>
			<view class="attachments-upload">
				<view class="uploaded-images">
					<view class="image-item" v-for="(image, index) in attachmentImages" :key="index">
						<image class="attachment-image" :src="image" mode="aspectFill"></image>
						<text class="remove-image" @click="removeImage(index)">✕</text>
					</view>
				</view>
				<view class="upload-btn" v-if="attachmentImages.length < 5" @click="chooseImage">
					<text class="upload-icon">📷</text>
					<text class="upload-text">添加截图</text>
				</view>
			</view>
		</view>

		<!-- 系统信息 -->
		<view class="system-info">
			<view class="section-title">
				<text class="title-text">系统信息</text>
				<text class="collect-tip">（自动收集，有助于问题定位）</text>
			</view>
			<view class="info-list">
				<view class="info-item">
					<text class="info-label">设备型号</text>
					<text class="info-value">{{ systemInfo.model }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">系统版本</text>
					<text class="info-value">{{ systemInfo.system }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">应用版本</text>
					<text class="info-value">{{ systemInfo.appVersion }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">平台</text>
					<text class="info-value">{{ systemInfo.platform }}</text>
				</view>
			</view>
		</view>

		<!-- 常见问题 -->
		<view class="common-questions">
			<view class="section-title">
				<text class="title-text">常见问题</text>
			</view>
			<view class="questions-list">
				<view class="question-item" v-for="(faq, index) in commonFAQs" :key="index" @click="toggleFAQ(index)">
					<view class="question-header">
						<text class="question-text">{{ faq.question }}</text>
						<text class="expand-icon" :class="{ 'expanded': faq.expanded }">▼</text>
					</view>
					<view class="answer-content" v-if="faq.expanded">
						<text class="answer-text">{{ faq.answer }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 提交按钮 -->
		<view class="submit-section">
			<view class="submit-tip">
				<text class="tip-text">提交后我们会尽快处理您的反馈，感谢您的支持！</text>
			</view>
			<button class="submit-btn" :disabled="!canSubmit" @click="submitFeedback">
				{{ loading ? '提交中...' : '提交反馈' }}
			</button>
		</view>
	</view>
</template>

<script>
// 引入工具函数
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 反馈表单
			selectedType: null,
			feedbackContent: '',
			contactEmail: '',
			contactWechat: '',
			attachmentImages: [],

			// 反馈类型
			feedbackTypes: [
				{
					id: 'bug',
					icon: '🐛',
					title: '功能异常'
				},
				{
					id: 'feature',
					icon: '💡',
					title: '功能建议'
				},
				{
					id: 'content',
					icon: '📝',
					title: '内容问题'
				},
				{
					id: 'performance',
					icon: '⚡',
					title: '性能问题'
				},
				{
					id: 'ui',
					icon: '🎨',
					title: '界面优化'
				},
				{
					id: 'other',
					icon: '💬',
					title: '其他问题'
				}
			],

			// 系统信息
			systemInfo: {
				model: '',
				system: '',
				appVersion: '1.0.0',
				platform: ''
			},

			// 常见问题
			commonFAQs: [
				{
					question: '如何进行身份认证？',
					answer: '在个人资料页面点击"申请认证"，填写认证信息并上传相关证明材料，我们会在3个工作日内完成审核。',
					expanded: false
				},
				{
					question: '为什么我的帖子没有显示？',
					answer: '可能是因为内容正在审核中，或者违反了社区规范。审核通过后会自动显示，如有疑问可联系客服。',
					expanded: false
				},
				{
					question: '如何删除或修改已发布的帖子？',
					answer: '在"我的帖子"页面找到对应帖子，点击"编辑"或"删除"即可。已发布的帖子只能在24小时内修改。',
					expanded: false
				},
				{
					question: '忘记密码怎么办？',
					answer: '在登录页面点击"忘记密码"，通过注册邮箱重置密码。如果邮箱无法收到邮件，请检查垃圾邮件或联系客服。',
					expanded: false
				}
			],

			// 页面状态
			loading: false,

			// 用户信息
			currentUser: null
		}
	},

	computed: {
		/**
		 * 是否可以提交反馈
		 */
		canSubmit() {
			return this.selectedType && this.feedbackContent.trim() && !this.loading
		}
	},

	onLoad() {
		// 获取当前用户信息
		this.currentUser = getAuthInfo()

		// 获取系统信息
		this.getSystemInfo()

		// 如果用户已登录，自动填充邮箱
		if (this.currentUser?.email) {
			this.contactEmail = this.currentUser.email
		}
	},

	methods: {
		/**
		 * 获取系统信息
		 */
		getSystemInfo() {
			uni.getSystemInfo({
				success: (res) => {
					this.systemInfo = {
						model: res.model || '未知',
						system: `${res.platform} ${res.system}`,
						appVersion: '1.0.0', // 这里应该从配置中获取
						platform: res.platform || '未知'
					}
				},
				fail: () => {
					this.systemInfo = {
						model: '获取失败',
						system: '获取失败',
						appVersion: '1.0.0',
						platform: '获取失败'
					}
				}
			})
		},

		/**
		 * 选择反馈类型
		 * @param {string} typeId - 类型ID
		 */
		selectType(typeId) {
			this.selectedType = typeId
		},

		/**
		 * 获取内容占位符
		 * @returns {string} 占位符文本
		 */
		getContentPlaceholder() {
			const placeholders = {
				bug: '请详细描述遇到的问题，包括操作步骤、异常现象等...',
				feature: '请描述您希望新增或改进的功能，以及使用场景...',
				content: '请说明具体的内容问题，如不当内容、版权问题等...',
				performance: '请描述遇到的性能问题，如卡顿、加载慢等...',
				ui: '请描述界面上需要优化的地方，提供具体建议...',
				other: '请详细描述您的问题或建议...'
			}
			return placeholders[this.selectedType] || '请详细描述您的反馈内容...'
		},

		/**
		 * 选择图片
		 */
		chooseImage() {
			uni.chooseImage({
				count: 5 - this.attachmentImages.length,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.attachmentImages = [...this.attachmentImages, ...res.tempFilePaths]
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
			this.attachmentImages.splice(index, 1)
		},

		/**
		 * 切换FAQ展开状态
		 * @param {number} index - FAQ索引
		 */
		toggleFAQ(index) {
			this.commonFAQs[index].expanded = !this.commonFAQs[index].expanded
		},

		/**
		 * 提交反馈
		 */
		async submitFeedback() {
			if (!this.canSubmit) return

			try {
				this.loading = true

				// 上传附件（如果有）
				const attachmentUrls = await this.uploadAttachments()

				// 构建反馈数据
				const feedbackData = {
					type: this.selectedType,
					content: this.feedbackContent.trim(),
					contactEmail: this.contactEmail.trim(),
					contactWechat: this.contactWechat.trim(),
					attachmentUrls: attachmentUrls,
					systemInfo: this.systemInfo,
					userId: this.currentUser?.userId || null
				}

				// 提交反馈（模拟API调用）
				await this.mockSubmitFeedback(feedbackData)

				uni.showToast({
					title: '反馈提交成功',
					icon: 'success'
				})

				// 延时返回上一页
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} catch (error) {
				console.error('提交反馈失败:', error)
				uni.showToast({
					title: error.message || '提交失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 上传附件
		 * @returns {Array} 附件URL数组
		 */
		async uploadAttachments() {
			if (this.attachmentImages.length === 0) {
				return []
			}

			try {
				// 这里应该调用文件上传API
				// 暂时返回原始路径
				return this.attachmentImages
			} catch (error) {
				console.error('上传附件失败:', error)
				// 上传失败不影响反馈提交
				return []
			}
		},

		/**
		 * 模拟提交反馈API
		 * @param {Object} feedbackData - 反馈数据
		 */
		async mockSubmitFeedback(feedbackData) {
			// 模拟API延时
			await new Promise(resolve => setTimeout(resolve, 1000))

			console.log('提交反馈数据:', feedbackData)

			// 模拟成功响应
			return {
				code: 200,
				message: '反馈提交成功',
				data: {
					feedbackId: Date.now()
				}
			}
		}
	}
}
</script>

<style scoped>
/* 主容器样式 */
.feedback-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 30rpx;
	padding-bottom: 120rpx;
}

/* 通用区块样式 */
.feedback-types,
.feedback-content,
.contact-info,
.feedback-attachments,
.system-info,
.common-questions {
	background-color: white;
	border-radius: 12rpx;
	margin-bottom: 30rpx;
	padding: 30rpx;
}

/* 标题样式 */
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

.collect-tip {
	color: #666;
	font-size: 22rpx;
	margin-left: 12rpx;
}

/* 反馈类型 */
.types-grid {
	display: grid;
	grid-template-columns: 1fr 1fr 1fr;
	gap: 20rpx;
}

.type-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 30rpx 20rpx;
	background-color: #f8f9fa;
	border-radius: 12rpx;
	border: 2rpx solid transparent;
}

.type-item.selected {
	background-color: #f0f8ff;
	border-color: #1890ff;
}

.type-icon {
	font-size: 40rpx;
	margin-bottom: 12rpx;
}

.type-title {
	font-size: 24rpx;
	color: #333;
	text-align: center;
}

/* 反馈内容 */
.content-input {
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

/* 联系方式 */
.contact-tip,
.attachments-tip {
	margin-bottom: 20rpx;
}

.tip-text {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

.contact-inputs {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.input-row {
	display: flex;
	align-items: center;
}

.input-label {
	width: 120rpx;
	font-size: 26rpx;
	color: #333;
	margin-right: 20rpx;
}

.contact-input {
	flex: 1;
	background-color: #f8f9fa;
	border-radius: 8rpx;
	padding: 20rpx;
	font-size: 26rpx;
	color: #333;
}

/* 附件上传 */
.attachments-upload {
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

.attachment-image {
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

/* 系统信息 */
.info-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.info-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx;
	background-color: #f8f9fa;
	border-radius: 8rpx;
}

.info-label {
	font-size: 26rpx;
	color: #666;
}

.info-value {
	font-size: 26rpx;
	color: #333;
}

/* 常见问题 */
.questions-list {
	display: flex;
	flex-direction: column;
	gap: 1rpx;
}

.question-item {
	background-color: #f8f9fa;
	border-radius: 8rpx;
	margin-bottom: 16rpx;
	overflow: hidden;
}

.question-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx;
}

.question-text {
	flex: 1;
	font-size: 26rpx;
	color: #333;
	line-height: 1.4;
	margin-right: 16rpx;
}

.expand-icon {
	font-size: 20rpx;
	color: #666;
	transition: transform 0.3s ease;
}

.expand-icon.expanded {
	transform: rotate(180deg);
}

.answer-content {
	padding: 0 24rpx 24rpx;
	border-top: 1rpx solid #e8e9ea;
	margin-top: -1rpx;
}

.answer-text {
	font-size: 24rpx;
	color: #666;
	line-height: 1.5;
}

/* 提交区域 */
.submit-section {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: white;
	border-top: 1rpx solid #e0e0e0;
	padding: 30rpx;
}

.submit-tip {
	margin-bottom: 20rpx;
	text-align: center;
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