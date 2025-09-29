<!-- 话题页面 - 显示标签列表和话题帖子（系统变更后新页面） -->
<template>
	<!-- 主容器：话题展示 -->
	<view class="topics-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-title">话题</view>
			<!-- 搜索按钮 -->
			<view class="search-btn" @click="handleSearch">
				<text class="search-icon">🔍</text>
			</view>
		</view>

		<!-- 话题标签列表 -->
		<scroll-view class="content-scroll"
					 scroll-y="true"
					 enable-back-to-top="true"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshTags"
					 @scrolltolower="loadMoreTags">

			<!-- 标签卡片 -->
			<view class="tag-item"
				  v-for="tag in tagList"
				  :key="tag.id"
				  @click="goToTagPosts(tag)">

				<!-- 标签信息 -->
				<view class="tag-header">
					<view class="tag-name">#{{ tag.name }}</view>
					<view class="tag-count">{{ tag.postCount || 0 }}篇内容</view>
				</view>

				<!-- 标签描述 -->
				<text class="tag-description" v-if="tag.description">{{ tag.description }}</text>

				<!-- 最新帖子预览 -->
				<view class="tag-preview" v-if="tag.latestPostTitle">
					<text class="preview-label">最新：</text>
					<text class="preview-title">{{ tag.latestPostTitle }}</text>
				</view>

				<!-- 热度指示器 -->
				<view class="tag-stats">
					<view class="stat-item">
						<text class="stat-icon">🔥</text>
						<text class="stat-label">热度</text>
					</view>
					<view class="stat-item" v-if="tag.lastUsedAt">
						<text class="stat-icon">⏰</text>
						<text class="stat-label">{{ formatTime(tag.lastUsedAt) }}</text>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="!isLoading && tagList.length === 0">
				<text class="empty-icon">🏷️</text>
				<text class="empty-text">暂无话题标签</text>
				<text class="empty-hint">管理员可以创建新话题</text>
			</view>

			<!-- 加载更多提示 -->
			<view class="load-more" v-if="hasMore && tagList.length > 0">
				<text v-if="!isLoadingMore">上拉加载更多话题</text>
				<text v-else>加载中...</text>
			</view>

			<!-- 没有更多数据提示 -->
			<view class="no-more" v-if="!hasMore && tagList.length > 0">
				<text>已加载所有话题</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
/**
 * 话题页面（系统变更后的新页面）
 *
 * 功能特性：
 * - 展示所有标签列表
 * - 显示标签统计信息（帖子数量、热度等）
 * - 点击标签进入该标签下的帖子列表
 * - 下拉刷新标签列表
 * - 上拉加载更多标签
 *
 * 系统变更说明：
 * - 从"关注"页面改为"话题"页面
 * - 移除用户关注功能，改为标签浏览功能
 * - 点击标签后跳转到标签帖子列表页面
 */

import { tagApi } from '../../../utils/api.js'
// import { formatTime } from '../../../utils/common.js'

export default {
	data() {
		return {
			// 状态栏高度
			statusBarHeight: 0,

			// 标签列表数据
			tagList: [],

			// 分页参数
			pageParams: {
				current: 1,
				size: 20,
				sortBy: 'count'  // 按帖子数量排序
			},

			// 加载状态
			isLoading: false,
			isLoadingMore: false,
			isRefreshing: false,
			hasMore: true
		}
	},

	onLoad() {
		// 获取系统状态栏高度
		const systemInfo = uni.getSystemInfoSync()
		this.statusBarHeight = systemInfo.statusBarHeight || 0

		// 页面加载时初始化数据
		this.loadTagList(true)
	},

	onShow() {
		// 页面显示时可能需要刷新数据
		if (this.tagList.length > 0) {
			this.refreshTags()
		}
	},

	methods: {
		/**
		 * 加载标签列表
		 * @param {boolean} reset - 是否重置列表（刷新时为true）
		 */
		async loadTagList(reset = false) {
			// 防止重复请求
			if (this.isLoading || (!reset && this.isLoadingMore)) {
				return
			}

			// 重置时从第一页开始
			if (reset) {
				this.pageParams.current = 1
				this.isLoading = true
				this.hasMore = true
			} else {
				this.isLoadingMore = true
			}

			try {
				const params = {
					current: this.pageParams.current,
					size: this.pageParams.size,
					sortBy: this.pageParams.sortBy
				}

				const response = await tagApi.getTagList(params)

				// 处理分页数据
				if (reset) {
					this.tagList = response.records || []
				} else {
					this.tagList.push(...(response.records || []))
				}

				// 更新分页状态
				this.hasMore = this.pageParams.current < (response.pages || 1)
				this.pageParams.current++

			} catch (error) {
				console.error('加载标签列表失败:', error)
				console.warn('API调用失败，使用模拟数据:', error)
				// 如果API调用失败，使用模拟数据
				this.loadMockData(reset)
			} finally {
				this.isLoading = false
				this.isLoadingMore = false
				this.isRefreshing = false
			}
		},

		/**
		 * 加载模拟标签数据（API调用失败时使用）
		 */
		loadMockData(reset) {
			const mockTags = [
				{
					id: 1,
					name: 'AIGC',
					description: '人工智能生成内容相关话题',
					postCount: 128,
					latestPostTitle: '如何利用AI工具提高工作效率',
					lastUsedAt: new Date().toISOString()
				},
				{
					id: 2,
					name: '技能提升',
					description: '职业技能和专业能力提升',
					postCount: 245,
					latestPostTitle: '职场新人必备的10个软技能',
					lastUsedAt: new Date(Date.now() - 3600000).toISOString()
				},
				{
					id: 3,
					name: '薪资谈判',
					description: '薪资谈判技巧和经验分享',
					postCount: 89,
					latestPostTitle: '年终调薪谈判的黄金法则',
					lastUsedAt: new Date(Date.now() - 7200000).toISOString()
				},
				{
					id: 4,
					name: '职业发展',
					description: '职业规划和发展路径讨论',
					postCount: 156,
					latestPostTitle: '30岁女性的职业转型之路',
					lastUsedAt: new Date(Date.now() - 10800000).toISOString()
				},
				{
					id: 5,
					name: '面试技巧',
					description: '求职面试经验和技巧分享',
					postCount: 203,
					latestPostTitle: '技术面试中最容易忽略的细节',
					lastUsedAt: new Date(Date.now() - 14400000).toISOString()
				}
			]

			if (reset) {
				this.tagList = mockTags
			} else {
				this.tagList.push(...mockTags)
			}

			this.hasMore = false
		},

		/**
		 * 下拉刷新标签列表
		 */
		refreshTags() {
			this.isRefreshing = true
			this.loadTagList(true)
		},

		/**
		 * 上拉加载更多标签
		 */
		loadMoreTags() {
			if (this.hasMore && !this.isLoadingMore) {
				this.loadTagList(false)
			}
		},

		/**
		 * 点击标签，进入该标签的帖子列表
		 * @param {Object} tag - 标签对象
		 */
		goToTagPosts(tag) {
			// 跳转到标签帖子列表页面（需要新创建）
			uni.navigateTo({
				url: `/pages/tag-posts/tag-posts?tagId=${tag.id}&tagName=${encodeURIComponent(tag.name)}`
			})
		},

		/**
		 * 搜索功能
		 */
		handleSearch() {
			uni.navigateTo({
				url: '/pages/search/search'
			})
		},

		/**
		 * 格式化时间
		 */
		formatTime(time) {
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
	}
}
</script>

<style lang="scss" scoped>
.topics-container {
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

.search-btn {
	padding: 8px;
	border-radius: 20px;
	background-color: #f5f5f5;
}

.search-icon {
	font-size: 16px;
}

.content-scroll {
	flex: 1;
	height: 0; /* 确保flex子元素正确计算高度 */
	padding: 20upx 0;
	box-sizing: border-box;
	overflow: hidden; /* 确保scroll-view正确工作 */
}

.tag-item {
	background-color: #ffffff;
	border-radius: 12px;
	padding: 30upx;
	margin: 20upx;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tag-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 8px;
}

.tag-name {
	font-size: 18px;
	font-weight: 600;
	color: #f33e54;
}

.tag-count {
	font-size: 12px;
	color: #999999;
	background-color: #f5f5f5;
	padding: 2px 8px;
	border-radius: 10px;
}

.tag-description {
	font-size: 14px;
	color: #666666;
	line-height: 1.4;
	margin-bottom: 10px;
}

.tag-preview {
	background-color: #f8f9fa;
	padding: 8px 12px;
	border-radius: 8px;
	margin-bottom: 10px;
}

.preview-label {
	font-size: 12px;
	color: #999999;
}

.preview-title {
	font-size: 13px;
	color: #333333;
	margin-left: 5px;
}

.tag-stats {
	display: flex;
	align-items: center;
	gap: 15px;
}

.stat-item {
	display: flex;
	align-items: center;
	gap: 4px;
}

.stat-icon {
	font-size: 12px;
}

.stat-label {
	font-size: 12px;
	color: #999999;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60px 20px;
}

.empty-icon {
	font-size: 48px;
	margin-bottom: 16px;
}

.empty-text {
	font-size: 16px;
	color: #666666;
	margin-bottom: 8px;
}

.empty-hint {
	font-size: 14px;
	color: #999999;
}

.load-more, .no-more {
	display: flex;
	justify-content: center;
	padding: 20px;
	color: #999999;
	font-size: 14px;
}
</style>