<!-- 工具页面 - 职业测评与工具集合（系统变更后新页面） -->
<template>
	<!-- 主容器：工具中心 -->
	<view class="tools-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-title">工具</view>
			<!-- 更多工具按钮 -->
			<view class="more-btn" @click="showMoreTools">
				<text class="more-icon">⋯</text>
			</view>
		</view>

		<!-- 工具内容区域 -->
		<scroll-view class="content-scroll" scroll-y="true" enable-back-to-top="true" refresher-enabled="true" :refresher-triggered="isRefreshing" @refresherrefresh="refreshTools">

			<!-- 推荐工具模块 -->
			<view class="section">
				<view class="section-header">
					<text class="section-title">🔥 精选工具</text>
					<text class="section-subtitle">为女性职场发展量身定制</text>
				</view>

				<view class="tools-list">
					<!-- MBTI职业测评 -->
					<view class="tool-item" @click="handleToolClick('mbti')">
						<view class="tool-left">
							<view class="tool-icon-small">🧠</view>
							<view class="tool-info">
								<text class="tool-name">MBTI职业测评</text>
								<text class="tool-desc">发现你的职业性格类型</text>
							</view>
						</view>
						<view class="tool-right">
							<text class="tool-status">即将推出</text>
							<text class="tool-arrow">→</text>
						</view>
					</view>

					<!-- 薪资数据查询 -->
					<view class="tool-item" @click="handleToolClick('salary')">
						<view class="tool-left">
							<view class="tool-icon-small">💰</view>
							<view class="tool-info">
								<text class="tool-name">薪资数据查询</text>
								<text class="tool-desc">了解行业薪资水平</text>
							</view>
						</view>
						<view class="tool-right">
							<text class="tool-status">即将推出</text>
							<text class="tool-arrow">→</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 开发提示 -->
			<view class="dev-notice">
				<view class="notice-icon">🚧</view>
				<text class="notice-text">更多职业工具正在开发中，敬请期待！</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
/**
 * 工具页面（系统变更后的新页面）
 *
 * 功能特性：
 * - 职业测评工具集合（MBTI、职业兴趣等）
 * - 薪资数据查询工具
 * - 行业分析工具
 * - 职业规划工具
 *
 * 系统变更说明：
 * - 从"消息"页面改为"工具"页面
 * - 预留各类职业测评和数据工具的框架
 * - 所有工具当前为"开发中"状态
 * - 为后续功能开发提供统一入口
 */

import { isLoggedIn, verifyAndExecute, USER_ROLES } from '../../../utils/auth.js'

export default {
	data() {
		return {
			// 状态栏高度
			statusBarHeight: 0,

			// 刷新状态
			isRefreshing: false,

			// 工具数据
			featuredTools: [],
			allTools: []
		}
	},

	onLoad() {
		// 获取系统状态栏高度
		const systemInfo = uni.getSystemInfoSync()
		this.statusBarHeight = systemInfo.statusBarHeight || 0

		// 页面加载时初始化数据
		this.initToolsData()
	},

	methods: {
		/**
		 * 初始化工具数据
		 */
		initToolsData() {
			// 当前所有工具都是开发中状态
			console.log('工具页面初始化完成')
		},

		/**
		 * 处理工具点击事件
		 * @param {string} toolType - 工具类型
		 */
		handleToolClick(toolType) {
			// 根据不同工具类型处理点击事件
			switch (toolType) {
				case 'mbti':
					this.showComingSoon('MBTI职业测评')
					break
				case 'salary':
					this.showComingSoon('薪资数据查询')
					break
				default:
					this.showComingSoon('该工具')
					break
			}
		},

		/**
		 * 显示即将推出提示
		 * @param {string} toolName - 工具名称
		 */
		showComingSoon(toolName) {
			uni.showModal({
				title: '即将推出',
				content: `${toolName}功能正在开发中，敬请期待！\n\n我们将为您提供专业、实用的职场工具，帮助您更好地规划和发展职业生涯。`,
				showCancel: false,
				confirmText: '期待中'
			})
		},

		/**
		 * 显示更多工具
		 */
		showMoreTools() {
			uni.showActionSheet({
				itemList: ['工具建议', '意见反馈', '关于工具'],
				success: (res) => {
					if (res.tapIndex === 0) {
						this.goToFeedback('工具建议')
					} else if (res.tapIndex === 1) {
						this.goToFeedback('意见反馈')
					} else if (res.tapIndex === 2) {
						this.showAboutTools()
					}
				}
			})
		},

		/**
		 * 跳转到反馈页面
		 */
		goToFeedback(type) {
			uni.navigateTo({
				url: `/pages/feedback/feedback?type=${type}`
			})
		},

		/**
		 * 显示工具说明
		 */
		showAboutTools() {
			uni.showModal({
				title: '关于工具',
				content: 'Herizon工具中心致力于为女性用户提供专业的职业发展工具，包括性格测评、薪资分析、技能评估等功能。\n\n所有工具都经过专业设计，旨在帮助您更好地了解自己、规划职业发展路径。',
				showCancel: false,
				confirmText: '了解了'
			})
		},

		/**
		 * 下拉刷新
		 */
		refreshTools() {
			this.isRefreshing = true
			setTimeout(() => {
				this.isRefreshing = false
				uni.showToast({
					title: '已是最新',
					icon: 'success',
					duration: 1500
				})
			}, 1000)
		}
	}
}
</script>

<style lang="scss" scoped>
.tools-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f7f7f7;
}

.top-nav {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 10px 15px;
	background-color: #ffffff;
	border-bottom: 1px solid #e5e5e5;
	z-index: 100;
}

.nav-title {
	font-size: 18px;
	font-weight: 600;
	color: #333333;
}

.more-btn {
	padding: 8px;
	border-radius: 20px;
	background-color: #f5f5f5;
}

.more-icon {
	font-size: 16px;
	color: #666666;
}

.content-scroll {
	flex: 1;
	height: 0; /* 确保flex子元素正确计算高度 */
	padding: 20upx 0;
	box-sizing: border-box;
	overflow: hidden; /* 确保scroll-view正确工作 */
}

.section {
	margin-bottom: 50upx;
}

.section-header {
	margin-bottom: 30upx;
	text-align: center;
	padding: 0 20upx;
}

.section-title {
	font-size: 36upx;
	font-weight: 600;
	color: #333333;
	display: block;
	margin-bottom: 8upx;
}

.section-subtitle {
	font-size: 26upx;
	color: #999999;
}


.tools-list {
	display: flex;
	flex-direction: column;
	gap: 20upx;
}

.tool-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 30upx;
	margin: 0 20upx;
	background-color: #ffffff;
	border-radius: 20upx;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tool-left {
	display: flex;
	align-items: center;
}

.tool-icon-small {
	font-size: 24px;
	margin-right: 12px;
}

.tool-info {
	display: flex;
	flex-direction: column;
}

.tool-item .tool-name {
	font-size: 15px;
	font-weight: 500;
	color: #333333;
	margin-bottom: 2px;
}

.tool-item .tool-desc {
	font-size: 13px;
	color: #666666;
}

.tool-right {
	display: flex;
	align-items: center;
	gap: 8px;
}

.tool-item .tool-status {
	font-size: 12px;
	color: #999999;
	background-color: #f5f5f5;
	padding: 2px 8px;
	border-radius: 8px;
}

.tool-item .tool-arrow {
	font-size: 14px;
	color: #cccccc;
}

.dev-notice {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 20px;
	background-color: #ffffff;
	border-radius: 10px;
	margin-top: 20px;
}

.notice-icon {
	font-size: 20px;
	margin-right: 8px;
}

.notice-text {
	font-size: 14px;
	color: #666666;
}
</style>