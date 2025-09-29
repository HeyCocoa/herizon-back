<!-- 关于页面 - 展示应用信息和团队介绍 -->
<template>
	<view class="about-container">
		<!-- 应用头部 -->
		<view class="app-header">
			<view class="app-logo-section">
				<view class="app-logo">
					<text class="logo-text">Herizon</text>
				</view>
				<text class="app-tagline">女性职场成长社区</text>
				<text class="version-info">版本 v1.0.0</text>
			</view>
		</view>

		<!-- 应用介绍 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">🌟</text>
				<text class="title-text">关于Herizon</text>
			</view>
			<view class="content-text">
				<text class="description">
					Herizon是一个专为女性职场人士打造的交流和成长平台。我们致力于为女性创造一个安全、包容、互助的社区环境，帮助每位女性在职场上发现自己的无限可能。
				</text>
				<text class="description">
					在这里，你可以分享职场经验、探讨行业趋势、寻求建议和支持，与来自各行各业的优秀女性建立联系，共同成长。
				</text>
			</view>
		</view>

		<!-- 核心价值 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">💎</text>
				<text class="title-text">核心价值</text>
			</view>
			<view class="value-list">
				<view class="value-item">
					<view class="value-icon">🚀</view>
					<view class="value-content">
						<text class="value-title">赋能成长</text>
						<text class="value-desc">通过知识分享和经验交流，帮助每位女性实现职业突破</text>
					</view>
				</view>
				<view class="value-item">
					<view class="value-icon">🤝</view>
					<view class="value-content">
						<text class="value-title">互助支持</text>
						<text class="value-desc">建立互相支持的网络，在职场挑战中提供温暖的力量</text>
					</view>
				</view>
				<view class="value-item">
					<view class="value-icon">🎯</view>
					<view class="value-content">
						<text class="value-title">专业品质</text>
						<text class="value-desc">维护高质量的内容和讨论，创造有价值的交流体验</text>
					</view>
				</view>
				<view class="value-item">
					<view class="value-icon">🔒</view>
					<view class="value-content">
						<text class="value-title">安全包容</text>
						<text class="value-desc">营造安全、包容的环境，让每个人都能自由表达观点</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 功能特色 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">✨</text>
				<text class="title-text">功能特色</text>
			</view>
			<view class="feature-grid">
				<view class="feature-card">
					<view class="feature-icon">💬</view>
					<text class="feature-title">专业交流</text>
					<text class="feature-desc">深度的职场话题讨论</text>
				</view>
				<view class="feature-card">
					<view class="feature-icon">📚</view>
					<text class="feature-title">知识分享</text>
					<text class="feature-desc">实用的职场经验和技能</text>
				</view>
				<view class="feature-card">
					<view class="feature-icon">🌐</view>
					<text class="feature-title">人脉网络</text>
					<text class="feature-desc">连接各行业优秀女性</text>
				</view>
				<view class="feature-card">
					<view class="feature-icon">🎓</view>
					<text class="feature-title">职业发展</text>
					<text class="feature-desc">个人成长和能力提升</text>
				</view>
			</view>
		</view>

		<!-- 发展历程 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">📈</text>
				<text class="title-text">发展历程</text>
			</view>
			<view class="timeline">
				<view class="timeline-item">
					<view class="timeline-dot"></view>
					<view class="timeline-content">
						<text class="timeline-date">2025年Q1</text>
						<text class="timeline-title">项目启动</text>
						<text class="timeline-desc">确立产品定位，开始团队组建和技术选型</text>
					</view>
				</view>
				<view class="timeline-item">
					<view class="timeline-dot"></view>
					<view class="timeline-content">
						<text class="timeline-date">2025年Q2</text>
						<text class="timeline-title">产品设计</text>
						<text class="timeline-desc">完成用户调研，设计产品架构和用户体验</text>
					</view>
				</view>
				<view class="timeline-item">
					<view class="timeline-dot"></view>
					<view class="timeline-content">
						<text class="timeline-date">2025年Q3</text>
						<text class="timeline-title">技术开发</text>
						<text class="timeline-desc">核心功能开发，建立基础设施和安全体系</text>
					</view>
				</view>
				<view class="timeline-item">
					<view class="timeline-dot active"></view>
					<view class="timeline-content">
						<text class="timeline-date">2025年Q4</text>
						<text class="timeline-title">beta测试</text>
						<text class="timeline-desc">内测阶段，收集用户反馈并优化产品体验</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 团队介绍 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">👥</text>
				<text class="title-text">团队介绍</text>
			</view>
			<view class="content-text">
				<text class="description">
					我们是一支充满激情和创新精神的团队，由来自技术、设计、产品和运营等不同背景的专业人士组成。团队成员都深刻理解女性在职场面临的挑战，致力于通过技术和产品创新来解决这些问题。
				</text>
				<text class="description">
					我们相信多元化的团队能够创造更好的产品，每个人都为Herizon的发展贡献着独特的价值和视角。
				</text>
			</view>
		</view>

		<!-- 联系信息 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">📞</text>
				<text class="title-text">联系我们</text>
			</view>
			<view class="contact-grid">
				<view class="contact-item" @click="copyEmail">
					<view class="contact-icon">📧</view>
					<text class="contact-label">邮箱</text>
					<text class="contact-value">support@herizon.com</text>
				</view>
				<view class="contact-item" @click="openFeedback">
					<view class="contact-icon">💬</view>
					<text class="contact-label">反馈</text>
					<text class="contact-value">意见建议</text>
				</view>
			</view>
		</view>

		<!-- 法律信息 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">📋</text>
				<text class="title-text">法律信息</text>
			</view>
			<view class="legal-links">
				<view class="legal-item" @click="openTerms">
					<text class="legal-text">服务条款</text>
					<text class="arrow-icon">></text>
				</view>
				<view class="legal-item" @click="openPrivacy">
					<text class="legal-text">隐私政策</text>
					<text class="arrow-icon">></text>
				</view>
			</view>
		</view>

		<!-- 致谢 -->
		<view class="section">
			<view class="section-title">
				<text class="title-icon">🙏</text>
				<text class="title-text">特别感谢</text>
			</view>
			<view class="content-text">
				<text class="description">
					感谢所有参与beta测试的用户，您们的反馈和建议对产品的完善起到了至关重要的作用。
				</text>
				<text class="description">
					感谢开源社区的贡献者们，Herizon的技术栈建立在众多优秀的开源项目之上。
				</text>
				<text class="description">
					感谢每一位相信Herizon理念并支持我们的朋友，让我们一起为女性职场发展创造更美好的未来！
				</text>
			</view>
		</view>

		<!-- 底部版权 -->
		<view class="footer">
			<text class="copyright">© 2025 Herizon. All rights reserved.</text>
			<text class="build-info">构建于 {{ getBuildDate() }}</text>
		</view>

		<!-- 底部间距 -->
		<view class="bottom-space"></view>
	</view>
</template>

<script>
/**
 * 关于页面
 *
 * 功能特性：
 * - 应用介绍和价值观展示
 * - 功能特色说明
 * - 发展历程时间线
 * - 团队和联系信息
 * - 法律条款链接
 */

export default {
	data() {
		return {
			// 页面数据可以在这里定义
		}
	},

	methods: {
		/**
		 * 获取构建日期
		 */
		getBuildDate() {
			const now = new Date()
			const year = now.getFullYear()
			const month = String(now.getMonth() + 1).padStart(2, '0')
			const day = String(now.getDate()).padStart(2, '0')
			return `${year}-${month}-${day}`
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
		 * 打开意见反馈页面
		 */
		openFeedback() {
			uni.navigateTo({
				url: '/pages/feedback/feedback'
			})
		},

		/**
		 * 打开服务条款
		 */
		openTerms() {
			uni.navigateTo({
				url: '/pages/terms-of-service/terms-of-service'
			})
		},

		/**
		 * 打开隐私政策
		 */
		openPrivacy() {
			// 可以跳转到隐私政策页面，或在服务条款页面中包含
			uni.navigateTo({
				url: '/pages/terms-of-service/terms-of-service?section=privacy'
			})
		}
	}
}
</script>

<style scoped>
/* 主容器 */
.about-container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

/* 应用头部 */
.app-header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 60rpx 30rpx 80rpx;
	color: white;
	text-align: center;
}

.app-logo-section {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.app-logo {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.2);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 30rpx;
	backdrop-filter: blur(10rpx);
	border: 3rpx solid rgba(255, 255, 255, 0.3);
}

.logo-text {
	font-size: 48rpx;
	font-weight: bold;
	color: white;
}

.app-tagline {
	font-size: 32rpx;
	margin-bottom: 15rpx;
	opacity: 0.9;
}

.version-info {
	font-size: 24rpx;
	opacity: 0.7;
}

/* 章节样式 */
.section {
	background-color: white;
	margin: 20rpx;
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

/* 内容文本 */
.content-text {
	padding: 30rpx;
}

.description {
	display: block;
	font-size: 28rpx;
	line-height: 1.8;
	color: #666;
	margin-bottom: 30rpx;
}

.description:last-child {
	margin-bottom: 0;
}

/* 价值观列表 */
.value-list {
	padding: 30rpx;
}

.value-item {
	display: flex;
	align-items: flex-start;
	margin-bottom: 35rpx;
}

.value-item:last-child {
	margin-bottom: 0;
}

.value-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
	margin-right: 25rpx;
	flex-shrink: 0;
}

.value-content {
	flex: 1;
	padding-top: 10rpx;
}

.value-title {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.value-desc {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}

/* 功能特色网格 */
.feature-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 20rpx;
	padding: 30rpx;
}

.feature-card {
	background-color: #f8f9fa;
	border-radius: 12rpx;
	padding: 30rpx 20rpx;
	text-align: center;
}

.feature-icon {
	font-size: 48rpx;
	margin-bottom: 15rpx;
}

.feature-title {
	display: block;
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.feature-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

/* 时间线 */
.timeline {
	padding: 30rpx;
	position: relative;
}

.timeline::before {
	content: '';
	position: absolute;
	left: 60rpx;
	top: 30rpx;
	bottom: 30rpx;
	width: 4rpx;
	background-color: #e9ecef;
}

.timeline-item {
	display: flex;
	align-items: flex-start;
	margin-bottom: 40rpx;
	position: relative;
}

.timeline-item:last-child {
	margin-bottom: 0;
}

.timeline-dot {
	width: 24rpx;
	height: 24rpx;
	border-radius: 50%;
	background-color: #dee2e6;
	margin-right: 30rpx;
	margin-top: 10rpx;
	flex-shrink: 0;
	position: relative;
	z-index: 2;
}

.timeline-dot.active {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.timeline-content {
	flex: 1;
}

.timeline-date {
	display: block;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 5rpx;
}

.timeline-title {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.timeline-desc {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}

/* 联系信息网格 */
.contact-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 20rpx;
	padding: 30rpx;
}

.contact-item {
	background-color: #f8f9fa;
	border-radius: 12rpx;
	padding: 25rpx;
	text-align: center;
	transition: all 0.3s;
}

.contact-item:active {
	background-color: #e9ecef;
	transform: scale(0.95);
}

.contact-icon {
	font-size: 40rpx;
	margin-bottom: 15rpx;
}

.contact-label {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 5rpx;
}

.contact-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

/* 法律链接 */
.legal-links {
	padding: 0 30rpx 30rpx;
}

.legal-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	transition: all 0.3s;
}

.legal-item:last-child {
	border-bottom: none;
}

.legal-item:active {
	background-color: #f8f9fa;
	margin: 0 -30rpx;
	padding: 25rpx 30rpx;
}

.legal-text {
	font-size: 30rpx;
	color: #333;
}

.arrow-icon {
	font-size: 28rpx;
	color: #ccc;
}

/* 底部版权 */
.footer {
	background-color: white;
	margin: 20rpx;
	border-radius: 16rpx;
	padding: 40rpx 30rpx;
	text-align: center;
	border-top: 1rpx solid #f0f0f0;
}

.copyright {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 10rpx;
}

.build-info {
	font-size: 24rpx;
	color: #999;
}

/* 底部间距 */
.bottom-space {
	height: 100rpx;
}
</style>