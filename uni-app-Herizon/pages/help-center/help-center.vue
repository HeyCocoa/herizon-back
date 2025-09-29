<!-- 帮助中心页面 - 提供使用指南和常见问题解答 -->
<template>
	<view class="help-container">
		<!-- 顶部搜索区域 -->
		<view class="search-section">
			<view class="search-box">
				<input
					class="search-input"
					type="text"
					placeholder="搜索帮助内容..."
					v-model="searchKeyword"
					@input="onSearchInput"
				/>
				<text class="search-icon">🔍</text>
			</view>
		</view>

		<!-- 快速入门 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">🚀</text>
				<text class="title-text">快速入门</text>
			</view>
			<view class="guide-grid">
				<view class="guide-item" @click="showGuideDetail('register')">
					<view class="guide-icon">👤</view>
					<text class="guide-title">新用户注册</text>
					<text class="guide-desc">如何注册成为Herizon用户</text>
				</view>
				<view class="guide-item" @click="showGuideDetail('post')">
					<view class="guide-icon">✍️</view>
					<text class="guide-title">发布内容</text>
					<text class="guide-desc">发布帖子、视频和问答</text>
				</view>
				<view class="guide-item" @click="showGuideDetail('interact')">
					<view class="guide-icon">💬</view>
					<text class="guide-title">互动交流</text>
					<text class="guide-desc">点赞、评论、收藏和分享</text>
				</view>
				<view class="guide-item" @click="showGuideDetail('verification')">
					<view class="guide-icon">✅</view>
					<text class="guide-title">身份认证</text>
					<text class="guide-desc">完成身份认证获得更多权限</text>
				</view>
			</view>
		</view>

		<!-- 常见问题 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">❓</text>
				<text class="title-text">常见问题</text>
			</view>
			<view class="faq-list">
				<view
					class="faq-item"
					v-for="(faq, index) in filteredFaqs"
					:key="index"
					@click="toggleFaq(index)"
				>
					<view class="faq-question">
						<text class="question-text">{{ faq.question }}</text>
						<text class="expand-icon" :class="{ 'expanded': faq.expanded }">▼</text>
					</view>
					<view class="faq-answer" v-if="faq.expanded">
						<text class="answer-text">{{ faq.answer }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 功能说明 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">📚</text>
				<text class="title-text">功能说明</text>
			</view>
			<view class="feature-list">
				<view
					class="feature-item"
					v-for="(feature, index) in features"
					:key="index"
					@click="showFeatureDetail(feature)"
				>
					<view class="feature-icon">{{ feature.icon }}</view>
					<view class="feature-content">
						<text class="feature-title">{{ feature.title }}</text>
						<text class="feature-desc">{{ feature.description }}</text>
					</view>
					<text class="arrow-icon">></text>
				</view>
			</view>
		</view>

		<!-- 联系我们 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">📞</text>
				<text class="title-text">联系我们</text>
			</view>
			<view class="contact-list">
				<view class="contact-item" @click="openFeedback">
					<view class="contact-icon">📝</view>
					<view class="contact-content">
						<text class="contact-title">意见反馈</text>
						<text class="contact-desc">向我们反馈问题和建议</text>
					</view>
					<text class="arrow-icon">></text>
				</view>
				<view class="contact-item" @click="copyEmail">
					<view class="contact-icon">📧</view>
					<view class="contact-content">
						<text class="contact-title">邮箱联系</text>
						<text class="contact-desc">support@herizon.com</text>
					</view>
					<text class="arrow-icon">📋</text>
				</view>
			</view>
		</view>

		<!-- 详情弹窗 -->
		<view class="modal-overlay" v-if="showModal" @click="closeModal">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">{{ modalData.title }}</text>
					<text class="close-btn" @click="closeModal">×</text>
				</view>
				<scroll-view class="modal-body" scroll-y="true">
					<view class="content-wrapper">
						<text class="content-text">{{ modalData.content }}</text>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 底部间距 -->
		<view class="bottom-space"></view>
	</view>
</template>

<script>
/**
 * 帮助中心页面
 *
 * 功能特性：
 * - 搜索帮助内容
 * - 快速入门指南
 * - 常见问题解答
 * - 功能说明
 * - 联系方式
 */

export default {
	data() {
		return {
			// 搜索关键词
			searchKeyword: '',

			// 弹窗状态
			showModal: false,
			modalData: {
				title: '',
				content: ''
			},

			// 常见问题列表
			faqs: [
				{
					question: '如何注册Herizon账号？',
					answer: '点击登录页面的"立即注册"按钮，填写用户名、邮箱和密码，完成身份认证问卷即可注册。建议使用真实邮箱以便接收重要通知。',
					expanded: false
				},
				{
					question: '忘记密码怎么办？',
					answer: '在登录页面点击"忘记密码"，输入注册邮箱，系统会发送重置密码邮件到您的邮箱。请检查垃圾邮件文件夹。',
					expanded: false
				},
				{
					question: '如何完成身份认证？',
					answer: '进入个人中心，点击"完成身份认证"，填写详细的身份认证问卷。认证通过后可享受完整功能，包括发布内容、参与讨论等。',
					expanded: false
				},
				{
					question: '体验用户和正式用户有什么区别？',
					answer: '体验用户功能有限，只能浏览内容和基础互动。正式用户（已认证）可以发布内容、参与完整讨论、使用高级功能。',
					expanded: false
				},
				{
					question: '如何发布高质量的内容？',
					answer: '1. 选择合适的标签 2. 使用清晰的标题 3. 提供有价值的内容 4. 添加相关图片或视频 5. 积极回复评论互动',
					expanded: false
				},
				{
					question: '什么内容会被举报或删除？',
					answer: '违反社区规范的内容，包括：虚假信息、恶意攻击、垃圾广告、侵犯他人权益等。请遵守社区规范，共建良好环境。',
					expanded: false
				},
				{
					question: '如何保护账号安全？',
					answer: '1. 使用复杂密码 2. 不要分享账号信息 3. 定期检查账号活动 4. 发现异常及时联系客服 5. 谨慎点击外部链接',
					expanded: false
				},
				{
					question: '能否修改用户名？',
					answer: '目前暂不支持修改用户名，请在注册时谨慎选择。您可以修改昵称、头像、个人简介等其他信息。',
					expanded: false
				}
			],

			// 功能说明列表
			features: [
				{
					icon: '🏠',
					title: '首页动态',
					description: '浏览最新内容和热门话题',
					detail: '首页展示最新发布的帖子、热门内容和个性化推荐。可以通过排序和筛选找到感兴趣的内容。'
				},
				{
					icon: '👥',
					title: '关注动态',
					description: '查看关注用户的最新动态',
					detail: '关注页面显示您关注的用户发布的最新内容，帮您及时了解感兴趣的人的动态。'
				},
				{
					icon: '✨',
					title: '内容发布',
					description: '发布图文、视频和问答',
					detail: '支持发布多种类型的内容：文字+图片、视频、问答。可以添加标签、设置话题，让更多人看到您的内容。'
				},
				{
					icon: '💬',
					title: '互动功能',
					description: '点赞、评论、收藏和分享',
					detail: '可以对喜欢的内容点赞、评论交流、收藏保存、分享给朋友。积极互动有助于建立社交关系。'
				},
				{
					icon: '🔍',
					title: '搜索功能',
					description: '搜索内容、用户和标签',
					detail: '强大的搜索功能，可以搜索帖子内容、用户昵称、标签等。支持关键词搜索和筛选。'
				},
				{
					icon: '📋',
					title: '个人中心',
					description: '管理个人信息和内容',
					detail: '查看和编辑个人资料、管理发布的内容、查看收藏和浏览历史、设置账号偏好。'
				}
			]
		}
	},

	computed: {
		/**
		 * 根据搜索关键词筛选常见问题
		 */
		filteredFaqs() {
			if (!this.searchKeyword.trim()) {
				return this.faqs
			}

			const keyword = this.searchKeyword.toLowerCase()
			return this.faqs.filter(faq =>
				faq.question.toLowerCase().includes(keyword) ||
				faq.answer.toLowerCase().includes(keyword)
			)
		}
	},

	methods: {
		/**
		 * 搜索输入处理
		 */
		onSearchInput(e) {
			this.searchKeyword = e.detail.value
		},

		/**
		 * 显示快速入门详情
		 */
		showGuideDetail(type) {
			const guides = {
				register: {
					title: '新用户注册指南',
					content: `步骤1：访问注册页面
点击首页右上角的"注册"按钮，或在登录页面点击"立即注册"。

步骤2：填写基本信息
• 用户名：3-20个字符，支持中英文和数字
• 邮箱：用于账号验证和找回密码
• 密码：至少6个字符，建议包含字母和数字

步骤3：完成身份认证问卷
填写简单的认证问卷，帮助我们了解您的背景，提供更好的服务。

步骤4：开始使用
注册成功后即可登录使用，建议先完善个人资料。`
				},
				post: {
					title: '内容发布指南',
					content: `发布图文帖子：
• 点击底部"+"按钮选择"图文"
• 编写标题和内容
• 添加相关图片（最多9张）
• 选择合适的标签
• 点击发布

发布视频：
• 选择本地视频文件
• 添加视频标题和描述
• 设置封面图片
• 选择相关标签

发布问答：
• 提出清晰的问题
• 提供背景信息
• 选择相关分类
• 设置问题标签

注意事项：
• 内容应真实、有价值
• 遵守社区规范
• 合理使用标签
• 积极回复评论`
				},
				interact: {
					title: '互动交流指南',
					content: `点赞：
对喜欢的内容点赞，表达支持和认同。点赞可以提升内容的热度。

评论：
• 发表有意义的评论
• 可以回复其他用户的评论
• 支持表情和图片
• 保持礼貌和理性

收藏：
收藏有价值的内容，方便以后查看。收藏的内容在个人中心可以找到。

分享：
将精彩内容分享给朋友，传播有价值的信息。

关注：
关注感兴趣的用户，及时了解他们的最新动态。

私信：
与其他用户进行私密交流，建立更深入的联系。`
				},
				verification: {
					title: '身份认证指南',
					content: `为什么要认证：
身份认证确保社区的真实性和安全性，认证用户享有更多权限和功能。

认证流程：
1. 进入个人中心
2. 点击"完成身份认证"
3. 填写详细的认证问卷
4. 等待审核（通常1-3个工作日）
5. 认证通过后获得正式用户权限

认证要求：
• 真实身份信息
• 完整的问卷填写
• 符合社区定位
• 无违规记录

认证后权限：
• 发布所有类型内容
• 参与高级讨论
• 使用完整互动功能
• 申请成为话题专家`
				}
			}

			this.modalData = guides[type] || { title: '', content: '' }
			this.showModal = true
		},

		/**
		 * 显示功能详情
		 */
		showFeatureDetail(feature) {
			this.modalData = {
				title: feature.title,
				content: feature.detail
			}
			this.showModal = true
		},

		/**
		 * 切换FAQ展开状态
		 */
		toggleFaq(index) {
			this.faqs[index].expanded = !this.faqs[index].expanded
		},

		/**
		 * 打开意见反馈页面
		 */
		openFeedback() {
			uni.navigateTo({
				url: '/pages/feedback/feedback'
			})
		},

		/**
		 * 复制邮箱地址
		 */
		copyEmail() {
			uni.setClipboardData({
				data: 'support@herizon.com',
				success: () => {
					uni.showToast({
						title: '邮箱地址已复制',
						icon: 'success'
					})
				}
			})
		},

		/**
		 * 关闭弹窗
		 */
		closeModal() {
			this.showModal = false
			this.modalData = { title: '', content: '' }
		}
	}
}
</script>

<style scoped>
/* 主容器 */
.help-container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 20rpx;
}

/* 搜索区域 */
.search-section {
	background-color: white;
	padding: 30rpx 20rpx;
	margin-bottom: 20rpx;
}

.search-box {
	position: relative;
	background-color: #f8f9fa;
	border-radius: 25rpx;
	overflow: hidden;
}

.search-input {
	width: 100%;
	height: 70rpx;
	padding: 0 60rpx 0 20rpx;
	font-size: 28rpx;
	background-color: transparent;
	border: none;
}

.search-icon {
	position: absolute;
	right: 20rpx;
	top: 50%;
	transform: translateY(-50%);
	font-size: 30rpx;
	color: #999;
}

/* 章节样式 */
.section {
	background-color: white;
	margin: 0 20rpx 20rpx;
	border-radius: 16rpx;
	overflow: hidden;
}

.section-title {
	display: flex;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.title-icon {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

/* 快速入门网格 */
.guide-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 20rpx;
	padding: 30rpx;
}

.guide-item {
	background-color: #f8f9fa;
	border-radius: 12rpx;
	padding: 25rpx;
	text-align: center;
	transition: all 0.3s;
}

.guide-item:active {
	background-color: #e9ecef;
	transform: scale(0.95);
}

.guide-icon {
	font-size: 48rpx;
	margin-bottom: 15rpx;
}

.guide-title {
	display: block;
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.guide-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

/* 常见问题列表 */
.faq-list {
	padding: 0 30rpx 30rpx;
}

.faq-item {
	border-bottom: 1rpx solid #f0f0f0;
	padding: 25rpx 0;
}

.faq-item:last-child {
	border-bottom: none;
}

.faq-question {
	display: flex;
	align-items: center;
	justify-content: space-between;
	cursor: pointer;
}

.question-text {
	flex: 1;
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.expand-icon {
	font-size: 24rpx;
	color: #999;
	transition: transform 0.3s;
}

.expand-icon.expanded {
	transform: rotate(180deg);
}

.faq-answer {
	margin-top: 20rpx;
	padding-left: 0;
}

.answer-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

/* 功能列表 */
.feature-list, .contact-list {
	padding: 0 30rpx 30rpx;
}

.feature-item, .contact-item {
	display: flex;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	transition: all 0.3s;
}

.feature-item:last-child, .contact-item:last-child {
	border-bottom: none;
}

.feature-item:active, .contact-item:active {
	background-color: #f8f9fa;
	margin: 0 -30rpx;
	padding: 25rpx 30rpx;
}

.feature-icon, .contact-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	background-color: #f0f0f0;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
	margin-right: 20rpx;
}

.feature-content, .contact-content {
	flex: 1;
}

.feature-title, .contact-title {
	display: block;
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
	margin-bottom: 5rpx;
}

.feature-desc, .contact-desc {
	font-size: 26rpx;
	color: #666;
	line-height: 1.4;
}

.arrow-icon {
	font-size: 28rpx;
	color: #ccc;
}

/* 弹窗样式 */
.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
	padding: 40rpx;
}

.modal-content {
	background-color: white;
	border-radius: 20rpx;
	max-height: 80vh;
	width: 100%;
	max-width: 600rpx;
	overflow: hidden;
}

.modal-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.close-btn {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background-color: #f0f0f0;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
	color: #666;
}

.modal-body {
	max-height: 60vh;
	padding: 30rpx;
}

/* 底部间距 */
.bottom-space {
	height: 100rpx;
}

/* 模态框内容样式 - 兼容小程序端 */
.content-wrapper {
	padding: 0;
}

.content-text {
	display: block;
	margin-bottom: 20rpx;
	line-height: 1.8;
	font-size: 28rpx;
	color: #333;
	white-space: pre-line; /* 保持换行符 */
}
</style>