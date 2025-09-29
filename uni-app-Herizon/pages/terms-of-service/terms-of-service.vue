<!-- 服务协议页面 - 用户协议和隐私政策 -->
<template>
	<view class="terms-container">
		<!-- 顶部导航 -->
		<view class="nav-tabs">
			<view
				class="tab-item"
				:class="{ 'active': currentTab === 'terms' }"
				@click="switchTab('terms')"
			>
				<text class="tab-text">服务条款</text>
			</view>
			<view
				class="tab-item"
				:class="{ 'active': currentTab === 'privacy' }"
				@click="switchTab('privacy')"
			>
				<text class="tab-text">隐私政策</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-scroll" scroll-y="true">
			<!-- 服务条款 -->
			<view class="content-section" v-if="currentTab === 'terms'">
				<view class="document-header">
					<text class="document-title">Herizon 用户服务协议</text>
					<text class="update-date">最后更新：2025年{{ getCurrentMonth() }}月</text>
				</view>

				<view class="content-body">
					<view class="article">
						<view class="article-title">
							<text class="title-text">1. 服务条款的接受</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								欢迎使用Herizon！本服务协议（以下简称"协议"）是您与Herizon之间关于使用Herizon服务的法律协议。通过访问或使用我们的服务，您表示同意受本协议的约束。
							</text>
							<text class="content-text">
								如果您不同意本协议的任何条款，请不要使用我们的服务。我们保留随时修改本协议的权利，修改后的协议将在发布后立即生效。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">2. 服务描述</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								Herizon是一个专为女性职场人士设计的社交平台，提供以下服务：
							</text>
							<view class="list-content">
								<text class="list-item">• 内容发布与分享功能</text>
								<text class="list-item">• 社交互动与网络建设</text>
								<text class="list-item">• 职场知识与经验交流</text>
								<text class="list-item">• 个人资料管理与展示</text>
								<text class="list-item">• 搜索与发现相关内容</text>
							</view>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">3. 用户账户与注册</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								要使用Herizon的全部功能，您需要创建一个账户。注册时，您必须：
							</text>
							<view class="list-content">
								<text class="list-item">• 提供真实、准确的个人信息</text>
								<text class="list-item">• 选择一个独特的用户名</text>
								<text class="list-item">• 设置安全的密码</text>
								<text class="list-item">• 负责维护账户信息的机密性</text>
								<text class="list-item">• 对账户下的所有活动承担责任</text>
							</view>
							<text class="content-text">
								我们保留拒绝注册或终止账户的权利，特别是在违反社区规范的情况下。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">4. 用户行为准则</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								使用Herizon时，您同意不会：
							</text>
							<view class="list-content">
								<text class="list-item">• 发布虚假、误导性或恶意内容</text>
								<text class="list-item">• 骚扰、威胁或恶意攻击其他用户</text>
								<text class="list-item">• 侵犯他人的知识产权或隐私权</text>
								<text class="list-item">• 发布垃圾邮件或进行商业推广</text>
								<text class="list-item">• 传播病毒或恶意软件</text>
								<text class="list-item">• 违反任何适用的法律法规</text>
							</view>
							<text class="content-text">
								我们致力于维护一个安全、包容的社区环境，对违反行为准则的用户将采取适当的措施，包括但不限于警告、限制功能或终止账户。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">5. 内容所有权与许可</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								您保留对发布内容的所有权，但通过使用我们的服务，您授予Herizon非排他性、全球性、免版税的许可，以便我们：
							</text>
							<view class="list-content">
								<text class="list-item">• 存储、显示和分发您的内容</text>
								<text class="list-item">• 为改善服务质量而处理内容</text>
								<text class="list-item">• 防止滥用和维护平台安全</text>
							</view>
							<text class="content-text">
								您有权随时删除自己的内容，但请注意，其他用户可能已经分享或引用了您的内容。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">6. 服务变更与终止</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们保留随时修改、暂停或终止服务的权利，包括：
							</text>
							<view class="list-content">
								<text class="list-item">• 添加新功能或改进现有功能</text>
								<text class="list-item">• 出于安全或法律原因暂停服务</text>
								<text class="list-item">• 在合理通知后终止服务</text>
							</view>
							<text class="content-text">
								您也可以随时停止使用我们的服务并删除您的账户。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">7. 免责声明</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								Herizon服务按"现状"提供，我们不对以下情况承担责任：
							</text>
							<view class="list-content">
								<text class="list-item">• 服务的连续性、可靠性或安全性</text>
								<text class="list-item">• 用户生成内容的准确性或质量</text>
								<text class="list-item">• 因使用服务而产生的任何损失</text>
								<text class="list-item">• 第三方服务或链接的可用性</text>
							</view>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">8. 联系我们</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								如果您对本服务协议有任何疑问或建议，请通过以下方式联系我们：
							</text>
							<view class="contact-info">
								<text class="contact-item">邮箱：legal@herizon.com</text>
								<text class="contact-item">客服：support@herizon.com</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 隐私政策 -->
			<view class="content-section" v-if="currentTab === 'privacy'">
				<view class="document-header">
					<text class="document-title">Herizon 隐私政策</text>
					<text class="update-date">最后更新：2025年{{ getCurrentMonth() }}月</text>
				</view>

				<view class="content-body">
					<view class="article">
						<view class="article-title">
							<text class="title-text">1. 信息收集</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们收集以下类型的信息以提供更好的服务：
							</text>
							<view class="sub-section">
								<text class="sub-title">个人信息：</text>
								<view class="list-content">
									<text class="list-item">• 注册时提供的基本信息（用户名、邮箱等）</text>
									<text class="list-item">• 个人资料信息（头像、简介、职业信息等）</text>
									<text class="list-item">• 身份认证时提供的额外信息</text>
								</view>
							</view>
							<view class="sub-section">
								<text class="sub-title">使用信息：</text>
								<view class="list-content">
									<text class="list-item">• 您发布的内容和互动记录</text>
									<text class="list-item">• 浏览历史和搜索记录</text>
									<text class="list-item">• 设备信息和技术数据</text>
								</view>
							</view>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">2. 信息使用</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们使用收集的信息用于：
							</text>
							<view class="list-content">
								<text class="list-item">• 提供和改善服务功能</text>
								<text class="list-item">• 个性化内容推荐</text>
								<text class="list-item">• 维护平台安全和防止滥用</text>
								<text class="list-item">• 发送重要通知和更新</text>
								<text class="list-item">• 分析服务使用情况以优化体验</text>
							</view>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">3. 信息分享</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们不会出售您的个人信息。我们可能在以下情况下分享信息：
							</text>
							<view class="list-content">
								<text class="list-item">• 获得您的明确同意</text>
								<text class="list-item">• 遵守法律要求或法院命令</text>
								<text class="list-item">• 保护我们或他人的合法权益</text>
								<text class="list-item">• 与可信的服务提供商合作（在严格保密协议下）</text>
							</view>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">4. 数据安全</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们采取多种安全措施保护您的信息：
							</text>
							<view class="list-content">
								<text class="list-item">• 数据加密传输和存储</text>
								<text class="list-item">• 定期安全审计和漏洞扫描</text>
								<text class="list-item">• 严格的员工访问控制</text>
								<text class="list-item">• 及时的安全更新和补丁</text>
							</view>
							<text class="content-text">
								尽管我们采取了合理的安全措施，但请注意没有任何系统是完全安全的。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">5. 您的权利</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								关于您的个人信息，您有以下权利：
							</text>
							<view class="list-content">
								<text class="list-item">• 访问和查看我们收集的您的信息</text>
								<text class="list-item">• 更正不准确或过时的信息</text>
								<text class="list-item">• 删除您的个人信息（在某些条件下）</text>
								<text class="list-item">• 限制或反对某些处理活动</text>
								<text class="list-item">• 导出您的数据</text>
							</view>
							<text class="content-text">
								要行使这些权利，请通过privacy@herizon.com联系我们。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">6. Cookie和跟踪技术</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们使用Cookie和类似技术来：
							</text>
							<view class="list-content">
								<text class="list-item">• 保持您的登录状态</text>
								<text class="list-item">• 记住您的偏好设置</text>
								<text class="list-item">• 分析网站使用情况</text>
								<text class="list-item">• 提供个性化体验</text>
							</view>
							<text class="content-text">
								您可以通过浏览器设置管理Cookie偏好，但这可能影响某些功能的使用。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">7. 儿童隐私</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								Herizon面向成年用户，我们不会故意收集13岁以下儿童的个人信息。如果我们发现收集了此类信息，将立即删除。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">8. 政策更新</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								我们可能会不时更新此隐私政策。重大变更将通过应用内通知或电子邮件的方式告知您。继续使用服务即表示您接受更新后的政策。
							</text>
						</view>
					</view>

					<view class="article">
						<view class="article-title">
							<text class="title-text">9. 联系我们</text>
						</view>
						<view class="article-content">
							<text class="content-text">
								如果您对隐私政策有任何疑问，请联系我们：
							</text>
							<view class="contact-info">
								<text class="contact-item">隐私问题：privacy@herizon.com</text>
								<text class="contact-item">一般咨询：support@herizon.com</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 底部间距 -->
			<view class="bottom-space"></view>
		</scroll-view>
	</view>
</template>

<script>
/**
 * 服务协议页面
 *
 * 功能特性：
 * - 服务条款展示
 * - 隐私政策展示
 * - 标签切换功能
 * - 响应式布局
 */

export default {
	data() {
		return {
			// 当前选中的标签
			currentTab: 'terms'
		}
	},

	onLoad(options) {
		// 支持从URL参数切换到隐私政策
		if (options.section === 'privacy') {
			this.currentTab = 'privacy'
		}
	},

	methods: {
		/**
		 * 切换标签
		 */
		switchTab(tab) {
			this.currentTab = tab
		},

		/**
		 * 获取当前月份
		 */
		getCurrentMonth() {
			return new Date().getMonth() + 1
		}
	}
}
</script>

<style scoped>
/* 主容器 */
.terms-container {
	height: 100vh;
	background-color: #f5f5f5;
	display: flex;
	flex-direction: column;
}

/* 导航标签 */
.nav-tabs {
	display: flex;
	background-color: white;
	border-bottom: 1rpx solid #e5e5e5;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 30rpx 0;
	position: relative;
	transition: all 0.3s;
}

.tab-item.active {
	color: #f33e54;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background-color: #f33e54;
	border-radius: 2rpx;
}

.tab-text {
	font-size: 32rpx;
	font-weight: 500;
}

/* 内容滚动区域 */
.content-scroll {
	flex: 1;
}

/* 内容区域 */
.content-section {
	background-color: white;
	margin: 20rpx;
	border-radius: 16rpx;
	overflow: hidden;
}

/* 文档头部 */
.document-header {
	padding: 40rpx 30rpx 30rpx;
	text-align: center;
	border-bottom: 1rpx solid #f0f0f0;
	background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.document-title {
	display: block;
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 15rpx;
}

.update-date {
	font-size: 24rpx;
	color: #666;
}

/* 内容主体 */
.content-body {
	padding: 30rpx;
}

/* 文章块 */
.article {
	margin-bottom: 50rpx;
}

.article:last-child {
	margin-bottom: 0;
}

/* 文章标题 */
.article-title {
	margin-bottom: 20rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

/* 文章内容 */
.article-content {
	line-height: 1.8;
}

.content-text {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 25rpx;
	line-height: 1.8;
}

/* 子标题 */
.sub-section {
	margin: 25rpx 0;
}

.sub-title {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 15rpx;
}

/* 列表内容 */
.list-content {
	margin: 20rpx 0;
	padding-left: 20rpx;
}

.list-item {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 12rpx;
	line-height: 1.6;
}

/* 联系信息 */
.contact-info {
	margin-top: 25rpx;
	padding: 25rpx;
	background-color: #f8f9fa;
	border-radius: 12rpx;
}

.contact-item {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 10rpx;
}

.contact-item:last-child {
	margin-bottom: 0;
}

/* 底部间距 */
.bottom-space {
	height: 100rpx;
}
</style>