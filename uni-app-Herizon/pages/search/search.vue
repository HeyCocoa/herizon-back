<!-- 搜索页面 - 内容搜索和用户搜索 -->
<template>
	<!-- 主容器：搜索功能 -->
	<view class="search-container">
		<!-- 状态栏占位 -->
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="nav-center">
				<text class="nav-title">搜索内容</text>
			</view>
			<view class="nav-right">
				<!-- 可以放置更多按钮 -->
			</view>
		</view>

		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-input-wrapper">
				<text class="search-icon">🔍</text>
				<input
					class="search-input"
					v-model="searchKeyword"
					placeholder="搜索帖子、用户、话题..."
					:focus="true"
					@input="onSearchInput"
					@confirm="performSearch"
					confirm-type="search">
				</input>
				<text class="clear-btn" v-if="searchKeyword" @click="clearSearch">✕</text>
			</view>
			<text class="cancel-btn" @click="goBack">取消</text>
		</view>

		<!-- 搜索历史和热门推荐 -->
		<view class="search-suggestions" v-if="!searchKeyword && !hasSearched">
			<!-- 搜索历史 -->
			<view class="suggestion-section" v-if="searchHistory.length > 0">
				<view class="section-header">
					<text class="section-title">搜索历史</text>
					<text class="clear-history" @click="clearHistory">清空</text>
				</view>
				<view class="history-tags">
					<text class="history-tag"
						  v-for="(item, index) in searchHistory"
						  :key="index"
						  @click="searchHistoryItem(item)">
						{{ item }}
					</text>
				</view>
			</view>

			<!-- 热门搜索 -->
			<view class="suggestion-section">
				<view class="section-header">
					<text class="section-title">热门搜索</text>
				</view>
				<view class="hot-tags">
					<text class="hot-tag"
						  v-for="(item, index) in hotSearches"
						  :key="index"
						  @click="searchHotItem(item)">
						{{ item }}
					</text>
				</view>
			</view>

			<!-- 热门话题 -->
			<view class="suggestion-section">
				<view class="section-header">
					<text class="section-title">热门话题</text>
				</view>
				<view class="topic-list">
					<view class="topic-item"
						  v-for="topic in hotTopics"
						  :key="topic.id"
						  @click="searchByTopic(topic)">
						<text class="topic-name">#{{ topic.name }}</text>
						<text class="topic-count">{{ topic.postCount }}条内容</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 搜索结果容器 -->
		<view class="search-results-container" v-if="hasSearched">
			<!-- 搜索结果导航 -->
			<view class="result-tabs">
				<view class="result-tab"
					  :class="{ 'active': currentTab === 'posts' }"
					  @click="switchResultTab('posts')">
					<text class="tab-text">帖子</text>
					<text class="tab-count">({{ searchResults.posts.total || 0 }})</text>
				</view>
				<view class="result-tab"
					  :class="{ 'active': currentTab === 'users' }"
					  @click="switchResultTab('users')">
					<text class="tab-text">用户</text>
					<text class="tab-count">({{ searchResults.users.total || 0 }})</text>
				</view>
				<view class="result-tab"
					  :class="{ 'active': currentTab === 'topics' }"
					  @click="switchResultTab('topics')">
					<text class="tab-text">话题</text>
					<text class="tab-count">({{ searchResults.topics.total || 0 }})</text>
				</view>
			</view>

			<!-- 搜索结果内容 -->
			<scroll-view class="results-scroll"
						 scroll-y="true"
						 @scrolltolower="loadMoreResults"
						 refresher-enabled="true"
						 :refresher-triggered="isRefreshing"
						 @refresherrefresh="refreshResults"
						 :style="{ height: scrollViewHeight + 'px' }">

				<!-- 帖子搜索结果 -->
				<view class="posts-results" v-if="currentTab === 'posts'">
					<view class="post-result-item"
						  v-for="post in searchResults.posts.list"
						  :key="post.id"
						  @click="goToPostDetail(post.id)">
						<!-- 帖子作者信息 -->
						<view class="post-author">
							<image class="author-avatar" :src="post.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
							<text class="author-name">{{ post.username }}</text>
							<text class="post-time">{{ formatTime(post.createdAt) }}</text>
						</view>

						<!-- 帖子标题（高亮搜索关键词） -->
						<view class="post-title" v-if="post.title">
							<rich-text class="title-content" :nodes="highlightText(post.title)"></rich-text>
						</view>

						<!-- 帖子内容（高亮搜索关键词） -->
						<view class="post-content">
							<rich-text class="content-text" :nodes="highlightText(post.content.substring(0, 150))"></rich-text>
							<text class="read-more" v-if="post.content.length > 150">...阅读全文</text>
						</view>

						<!-- 帖子图片 -->
						<view class="post-images" v-if="post.imageUrls && post.imageUrls.length">
							<image class="result-image"
								   :src="post.imageUrls[0]"
								   mode="aspectFill">
							</image>
							<text class="more-images" v-if="post.imageUrls.length > 1">+{{ post.imageUrls.length - 1 }}</text>
						</view>

						<!-- 帖子统计 -->
						<view class="post-stats">
							<text class="stat-item">👁 {{ post.viewCount || 0 }}</text>
							<text class="stat-item">👍 {{ post.likeCount || 0 }}</text>
							<text class="stat-item">💬 {{ post.commentCount || 0 }}</text>
						</view>

						<!-- 帖子标签 -->
						<view class="post-tags" v-if="post.tags && post.tags.length">
							<text class="post-tag" v-for="tag in post.tags.slice(0, 3)" :key="tag.id">
								#{{ tag.name }}
							</text>
						</view>
					</view>
				</view>

				<!-- 用户搜索结果 -->
				<view class="users-results" v-if="currentTab === 'users'">
					<view class="user-result-item"
						  v-for="user in searchResults.users.list"
						  :key="user.id"
						  @click="goToUserProfile(user.id)">
						<!-- 用户头像和信息 -->
						<view class="user-info">
							<image class="user-avatar" :src="user.avatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
							<view class="user-details">
								<rich-text class="user-nickname" :nodes="highlightText(user.nickname)"></rich-text>
								<rich-text class="user-username" :nodes="highlightText('@' + user.username)"></rich-text>
								<text class="user-bio" v-if="user.bio">{{ user.bio.substring(0, 60) }}{{ user.bio.length > 60 ? '...' : '' }}</text>
							</view>
						</view>

						<!-- 用户验证状态 -->
						<view class="user-badges" v-if="user.isVerified">
							<text class="verified-badge">✓ 已认证</text>
						</view>

						<!-- 用户统计 -->
						<view class="user-stats">
							<text class="user-stat">{{ user.postCount || 0 }}条帖子</text>
							<text class="user-stat">{{ user.followerCount || 0 }}位粉丝</text>
						</view>

						<!-- 关注按钮 -->
						<view class="follow-action" v-if="user.id !== currentUserId">
							<button class="follow-btn" :class="{ 'following': user.isFollowing }" @click.stop="toggleUserFollow(user)">
								{{ user.isFollowing ? '已关注' : '关注' }}
							</button>
						</view>
					</view>
				</view>

				<!-- 话题搜索结果 -->
				<view class="topics-results" v-if="currentTab === 'topics'">
					<view class="topic-result-item"
						  v-for="topic in searchResults.topics.list"
						  :key="topic.id"
						  @click="goToTopicDetail(topic.id)">
						<!-- 话题信息 -->
						<view class="topic-info">
							<rich-text class="topic-name" :nodes="highlightText('#' + topic.name)"></rich-text>
							<text class="topic-description" v-if="topic.description">{{ topic.description }}</text>
						</view>

						<!-- 话题统计 -->
						<view class="topic-stats">
							<text class="topic-stat">{{ topic.postCount || 0 }}条内容</text>
							<text class="topic-stat">{{ topic.followCount || 0 }}人关注</text>
						</view>

						<!-- 关注话题按钮 -->
						<view class="topic-action">
							<button class="follow-topic-btn" :class="{ 'following': topic.isFollowing }" @click.stop="toggleTopicFollow(topic)">
								{{ topic.isFollowing ? '已关注' : '关注' }}
							</button>
						</view>
					</view>
				</view>

				<!-- 空状态 -->
				<view class="empty-results" v-if="!hasResults && !loading">
					<text class="empty-icon">🔍</text>
					<text class="empty-text">没有找到相关内容</text>
					<text class="empty-tip">试试其他关键词或浏览热门内容</text>
				</view>

				<!-- 加载更多 -->
				<view class="load-more" v-if="hasMoreResults">
					<text class="load-more-text">{{ loading ? '加载中...' : '加载更多' }}</text>
				</view>
			</scroll-view>
		</view>

		<!-- 加载指示器 -->
		<view class="loading-overlay" v-if="loading">
			<text class="loading-text">搜索中...</text>
		</view>
	</view>
</template>

<script>
// 引入API和工具函数
import { postApi, userApi, tagApi } from '@/utils/api.js'
import { getAuthInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 系统信息
			statusBarHeight: 0,
			scrollViewHeight: 0,

			// 搜索状态
			searchKeyword: '',
			hasSearched: false,
			loading: false,
			isRefreshing: false,

			// 当前标签
			currentTab: 'posts', // posts | users | topics

			// 搜索结果
			searchResults: {
				posts: { list: [], total: 0 },
				users: { list: [], total: 0 },
				topics: { list: [], total: 0 }
			},

			// 分页状态
			currentPage: {
				posts: 1,
				users: 1,
				topics: 1
			},
			pageSize: 20,
			hasMoreResults: false,

			// 搜索历史
			searchHistory: [],

			// 热门搜索
			hotSearches: [
				'职场发展', '创业经验', '女性权益', '技能提升',
				'工作平衡', '投资理财', '健康生活', '学习成长'
			],

			// 热门话题
			hotTopics: [
				{ id: 1, name: '职场女性', postCount: 1250 },
				{ id: 2, name: '创业故事', postCount: 890 },
				{ id: 3, name: '技能分享', postCount: 756 },
				{ id: 4, name: '生活感悟', postCount: 642 },
				{ id: 5, name: '投资理财', postCount: 523 }
			],

			// 用户信息
			currentUserId: null,

			// 搜索防抖定时器
			searchTimer: null
		}
	},

	computed: {
		/**
		 * 是否有搜索结果
		 */
		hasResults() {
			return this.searchResults.posts.list.length > 0 ||
				   this.searchResults.users.list.length > 0 ||
				   this.searchResults.topics.list.length > 0
		}
	},

	onLoad(options) {
		// 获取系统信息，设置状态栏高度和页面高度
		this.getSystemInfo()

		// 获取传入的搜索参数
		if (options.keyword) {
			this.searchKeyword = decodeURIComponent(options.keyword)
			this.performSearch()
		}

		if (options.tagId) {
			// 如果是通过标签进入，设置为话题搜索
			this.currentTab = 'topics'
			this.searchByTagId(options.tagId)
		}

		// 获取当前用户信息
		const userInfo = getAuthInfo()
		this.currentUserId = userInfo?.userId

		// 加载搜索历史
		this.loadSearchHistory()
	},

	onUnload() {
		// 清理定时器
		if (this.searchTimer) {
			clearTimeout(this.searchTimer)
		}
	},

	methods: {
		/**
		 * 获取系统信息，设置页面布局
		 */
		getSystemInfo() {
			try {
				const systemInfo = uni.getSystemInfoSync()
				this.statusBarHeight = systemInfo.statusBarHeight || 20

				// 计算滚动视图高度，简单处理
				this.scrollViewHeight = systemInfo.windowHeight - 200 // 预留200px给其他元素

				console.log('状态栏高度:', this.statusBarHeight)
			} catch (error) {
				console.error('获取系统信息失败:', error)
				this.statusBarHeight = 20
				this.scrollViewHeight = 600
			}
		},

		/**
		 * 搜索输入处理（防抖）
		 * @param {Object} event - 输入事件
		 */
		onSearchInput(event) {
			// 清除之前的定时器
			if (this.searchTimer) {
				clearTimeout(this.searchTimer)
			}

			// 设置防抖延时
			this.searchTimer = setTimeout(() => {
				if (this.searchKeyword.trim()) {
					// 实时搜索建议（可选功能）
					// this.getSearchSuggestions()
				}
			}, 300)
		},

		/**
		 * 执行搜索
		 */
		async performSearch() {
			const keyword = this.searchKeyword.trim()
			if (!keyword) {
				uni.showToast({ title: '请输入搜索关键词', icon: 'error' })
				return
			}

			this.loading = true
			this.hasSearched = true

			// 保存搜索历史
			this.saveSearchHistory(keyword)

			// 重置搜索结果和分页
			this.resetSearchResults()

			try {
				// 同时搜索所有类型的内容
				await Promise.all([
					this.searchPosts(keyword),
					this.searchUsers(keyword),
					this.searchTopics(keyword)
				])
			} catch (error) {
				console.error('搜索失败:', error)
				uni.showToast({
					title: error.message || '搜索失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},

		/**
		 * 搜索帖子
		 * @param {string} keyword - 搜索关键词
		 * @param {boolean} loadMore - 是否为加载更多
		 */
		async searchPosts(keyword, loadMore = false) {
			try {
				const params = {
					current: loadMore ? this.currentPage.posts : 1,
					size: this.pageSize,
					keyword: keyword,
					sort: 'relevance' // 按相关性排序
				}

				// 由于后端可能还没有搜索API，这里使用模拟数据
				const response = await this.mockSearchPosts(params)

				if (response.code === 200) {
					const newPosts = response.data.records || []

					if (loadMore) {
						this.searchResults.posts.list = [...this.searchResults.posts.list, ...newPosts]
					} else {
						this.searchResults.posts.list = newPosts
						this.searchResults.posts.total = response.data.total || 0
					}

					// 更新分页状态
					if (!loadMore) {
						this.currentPage.posts = 1
					}
					this.updateHasMoreResults('posts', response.data)
				}
			} catch (error) {
				console.error('搜索帖子失败:', error)
				throw error
			}
		},

		/**
		 * 搜索用户
		 * @param {string} keyword - 搜索关键词
		 * @param {boolean} loadMore - 是否为加载更多
		 */
		async searchUsers(keyword, loadMore = false) {
			try {
				const params = {
					current: loadMore ? this.currentPage.users : 1,
					size: this.pageSize,
					keyword: keyword
				}

				// 使用模拟数据
				const response = await this.mockSearchUsers(params)

				if (response.code === 200) {
					const newUsers = response.data.records || []

					if (loadMore) {
						this.searchResults.users.list = [...this.searchResults.users.list, ...newUsers]
					} else {
						this.searchResults.users.list = newUsers
						this.searchResults.users.total = response.data.total || 0
					}

					if (!loadMore) {
						this.currentPage.users = 1
					}
					this.updateHasMoreResults('users', response.data)
				}
			} catch (error) {
				console.error('搜索用户失败:', error)
				throw error
			}
		},

		/**
		 * 搜索话题
		 * @param {string} keyword - 搜索关键词
		 * @param {boolean} loadMore - 是否为加载更多
		 */
		async searchTopics(keyword, loadMore = false) {
			try {
				// 使用真实的标签搜索API
				const response = await tagApi.searchTags(keyword)

				if (response.code === 200) {
					const newTopics = response.data || []

					if (loadMore) {
						this.searchResults.topics.list = [...this.searchResults.topics.list, ...newTopics]
					} else {
						this.searchResults.topics.list = newTopics
						this.searchResults.topics.total = newTopics.length
					}

					if (!loadMore) {
						this.currentPage.topics = 1
					}
					this.updateHasMoreResults('topics', { total: newTopics.length, size: this.pageSize })
				}
			} catch (error) {
				console.error('搜索话题失败:', error)
				// 搜索话题失败不影响整体搜索体验
			}
		},

		/**
		 * 模拟帖子搜索API
		 * @param {Object} params - 搜索参数
		 */
		async mockSearchPosts(params) {
			// 模拟API延时
			await new Promise(resolve => setTimeout(resolve, 500))

			// 模拟搜索结果
			const mockPosts = [
				{
					id: 1,
					title: '职场女性如何平衡工作与生活',
					content: '在现代社会中，职场女性面临着前所未有的挑战。如何在追求事业成功的同时，保持工作与生活的平衡，是每个职场女性都需要思考的问题...',
					username: '张小美',
					userAvatar: '/static/img/avatar1.jpg',
					createdAt: '2025-01-15T10:30:00Z',
					viewCount: 1250,
					likeCount: 89,
					commentCount: 23,
					imageUrls: ['/static/img/post1.jpg'],
					tags: [{ id: 1, name: '职场女性' }, { id: 2, name: '工作平衡' }]
				},
				{
					id: 2,
					title: '创业路上的那些坑',
					content: '作为一名女性创业者，我想分享一些创业路上遇到的挑战和经验。希望能够帮助到更多有创业想法的姐妹们...',
					username: '李创业',
					userAvatar: '/static/img/avatar2.jpg',
					createdAt: '2025-01-14T15:20:00Z',
					viewCount: 2100,
					likeCount: 156,
					commentCount: 45,
					imageUrls: [],
					tags: [{ id: 3, name: '创业经验' }, { id: 4, name: '女性创业' }]
				}
			]

			return {
				code: 200,
				data: {
					records: params.keyword ? mockPosts.filter(post =>
						post.title.includes(params.keyword) ||
						post.content.includes(params.keyword)
					) : mockPosts,
					total: 25,
					current: params.current,
					size: params.size
				}
			}
		},

		/**
		 * 模拟用户搜索API
		 * @param {Object} params - 搜索参数
		 */
		async mockSearchUsers(params) {
			await new Promise(resolve => setTimeout(resolve, 300))

			const mockUsers = [
				{
					id: 1,
					username: 'zhangxiaomei',
					nickname: '张小美',
					avatar: '/static/img/avatar1.jpg',
					bio: '职场女性，专注于工作生活平衡的探索',
					isVerified: true,
					postCount: 45,
					followerCount: 1250,
					isFollowing: false
				},
				{
					id: 2,
					username: 'lichuangye',
					nickname: '李创业',
					avatar: '/static/img/avatar2.jpg',
					bio: '女性创业者，分享创业路上的心得体会',
					isVerified: true,
					postCount: 32,
					followerCount: 890,
					isFollowing: true
				}
			]

			return {
				code: 200,
				data: {
					records: params.keyword ? mockUsers.filter(user =>
						user.username.includes(params.keyword) ||
						user.nickname.includes(params.keyword) ||
						(user.bio && user.bio.includes(params.keyword))
					) : mockUsers,
					total: 15,
					current: params.current,
					size: params.size
				}
			}
		},

		/**
		 * 切换搜索结果标签
		 * @param {string} tab - 标签名称
		 */
		switchResultTab(tab) {
			if (this.currentTab !== tab) {
				this.currentTab = tab
				this.updateHasMoreResults(tab)
			}
		},

		/**
		 * 加载更多搜索结果
		 */
		async loadMoreResults() {
			if (!this.hasMoreResults || this.loading) return

			const tab = this.currentTab
			this.currentPage[tab]++

			try {
				switch (tab) {
					case 'posts':
						await this.searchPosts(this.searchKeyword, true)
						break
					case 'users':
						await this.searchUsers(this.searchKeyword, true)
						break
					case 'topics':
						await this.searchTopics(this.searchKeyword, true)
						break
				}
			} catch (error) {
				console.error('加载更多失败:', error)
				this.currentPage[tab]-- // 回滚页码
			}
		},

		/**
		 * 刷新搜索结果
		 */
		async refreshResults() {
			this.isRefreshing = true
			try {
				await this.performSearch()
			} finally {
				this.isRefreshing = false
			}
		},

		/**
		 * 更新是否有更多结果的状态
		 * @param {string} tab - 当前标签
		 * @param {Object} data - 响应数据
		 */
		updateHasMoreResults(tab, data = null) {
			if (data) {
				const currentList = this.searchResults[tab].list
				this.hasMoreResults = currentList.length < (data.total || 0)
			} else {
				// 根据当前标签更新状态
				const currentData = this.searchResults[this.currentTab]
				this.hasMoreResults = currentData.list.length < currentData.total
			}
		},

		/**
		 * 重置搜索结果
		 */
		resetSearchResults() {
			this.searchResults = {
				posts: { list: [], total: 0 },
				users: { list: [], total: 0 },
				topics: { list: [], total: 0 }
			}
			this.currentPage = { posts: 1, users: 1, topics: 1 }
			this.hasMoreResults = false
		},

		/**
		 * 清空搜索
		 */
		clearSearch() {
			this.searchKeyword = ''
			this.hasSearched = false
			this.resetSearchResults()
		},

		/**
		 * 返回上一页
		 */
		goBack() {
			uni.navigateBack()
		},

		/**
		 * 搜索历史项目
		 * @param {string} keyword - 历史搜索词
		 */
		searchHistoryItem(keyword) {
			this.searchKeyword = keyword
			this.performSearch()
		},

		/**
		 * 搜索热门项目
		 * @param {string} keyword - 热门搜索词
		 */
		searchHotItem(keyword) {
			this.searchKeyword = keyword
			this.performSearch()
		},

		/**
		 * 按话题搜索
		 * @param {Object} topic - 话题对象
		 */
		searchByTopic(topic) {
			this.searchKeyword = topic.name
			this.currentTab = 'posts'
			this.performSearch()
		},

		/**
		 * 按标签ID搜索
		 * @param {number} tagId - 标签ID
		 */
		async searchByTagId(tagId) {
			try {
				const response = await tagApi.getTagDetail(tagId)
				if (response.code === 200) {
					this.searchKeyword = response.data.name
					this.performSearch()
				}
			} catch (error) {
				console.error('获取标签详情失败:', error)
			}
		},

		/**
		 * 跳转到帖子详情
		 * @param {number} postId - 帖子ID
		 */
		goToPostDetail(postId) {
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?id=${postId}`
			})
		},

		/**
		 * 跳转到用户资料
		 * @param {number} userId - 用户ID
		 */
		goToUserProfile(userId) {
			uni.navigateTo({
				url: `/pages/user-profile/user-profile?userId=${userId}`
			})
		},

		/**
		 * 跳转到话题详情
		 * @param {number} topicId - 话题ID
		 */
		goToTopicDetail(topicId) {
			uni.navigateTo({
				url: `/pages/topic-detail/topic-detail?topicId=${topicId}`
			})
		},

		/**
		 * 切换用户关注状态
		 * @param {Object} user - 用户对象
		 */
		async toggleUserFollow(user) {
			if (!this.currentUserId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			try {
				// 这里需要调用关注/取消关注API
				// 暂时模拟操作
				user.isFollowing = !user.isFollowing
				user.followerCount += user.isFollowing ? 1 : -1

				uni.showToast({
					title: user.isFollowing ? '关注成功' : '取消关注',
					icon: 'success'
				})
			} catch (error) {
				console.error('关注操作失败:', error)
				uni.showToast({
					title: '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 切换话题关注状态
		 * @param {Object} topic - 话题对象
		 */
		async toggleTopicFollow(topic) {
			if (!this.currentUserId) {
				uni.showToast({ title: '请先登录', icon: 'error' })
				return
			}

			try {
				// 这里需要调用关注/取消关注话题的API
				topic.isFollowing = !topic.isFollowing
				topic.followCount += topic.isFollowing ? 1 : -1

				uni.showToast({
					title: topic.isFollowing ? '关注成功' : '取消关注',
					icon: 'success'
				})
			} catch (error) {
				console.error('关注话题失败:', error)
				uni.showToast({
					title: '操作失败',
					icon: 'error'
				})
			}
		},

		/**
		 * 高亮搜索关键词
		 * @param {string} text - 原始文本
		 * @returns {Array} rich-text节点数组
		 */
		highlightText(text) {
			if (!this.searchKeyword || !text) {
				return [{ type: 'text', text: text }]
			}

			const keyword = this.searchKeyword.toLowerCase()
			const lowerText = text.toLowerCase()
			const nodes = []
			let lastIndex = 0

			let index = lowerText.indexOf(keyword)
			while (index !== -1) {
				// 添加关键词前的文本
				if (index > lastIndex) {
					nodes.push({
						type: 'text',
						text: text.substring(lastIndex, index)
					})
				}

				// 添加高亮的关键词
				nodes.push({
					name: 'span',
					attrs: {
						style: 'color: #1890ff; font-weight: bold; background-color: #fff3cd;'
					},
					children: [{
						type: 'text',
						text: text.substring(index, index + keyword.length)
					}]
				})

				lastIndex = index + keyword.length
				index = lowerText.indexOf(keyword, lastIndex)
			}

			// 添加剩余文本
			if (lastIndex < text.length) {
				nodes.push({
					type: 'text',
					text: text.substring(lastIndex)
				})
			}

			return nodes
		},

		/**
		 * 加载搜索历史
		 */
		loadSearchHistory() {
			try {
				const history = uni.getStorageSync('searchHistory') || []
				this.searchHistory = history.slice(0, 10) // 最多显示10个
			} catch (error) {
				console.error('加载搜索历史失败:', error)
				this.searchHistory = []
			}
		},

		/**
		 * 保存搜索历史
		 * @param {string} keyword - 搜索关键词
		 */
		saveSearchHistory(keyword) {
			try {
				let history = uni.getStorageSync('searchHistory') || []

				// 移除重复项
				history = history.filter(item => item !== keyword)

				// 添加到开头
				history.unshift(keyword)

				// 限制数量
				history = history.slice(0, 20)

				uni.setStorageSync('searchHistory', history)
				this.searchHistory = history.slice(0, 10)
			} catch (error) {
				console.error('保存搜索历史失败:', error)
			}
		},

		/**
		 * 清空搜索历史
		 */
		clearHistory() {
			uni.showModal({
				title: '确认清空',
				content: '确定要清空所有搜索历史吗？',
				success: (res) => {
					if (res.confirm) {
						uni.removeStorageSync('searchHistory')
						this.searchHistory = []
						uni.showToast({ title: '已清空', icon: 'success' })
					}
				}
			})
		},

		/**
		 * 格式化时间显示
		 * @param {string} timeString - 时间字符串
		 * @returns {string} 格式化后的时间
		 */
		formatTime(timeString) {
			if (!timeString) return ''

			const now = new Date()
			const time = new Date(timeString)
			const diff = now - time

			const minute = 60 * 1000
			const hour = 60 * minute
			const day = 24 * hour

			if (diff < hour) {
				return `${Math.floor(diff / minute)}分钟前`
			} else if (diff < day) {
				return `${Math.floor(diff / hour)}小时前`
			} else {
				return time.toLocaleDateString('zh-CN', {
					month: 'short',
					day: 'numeric'
				})
			}
		}
	}
}
</script>

<style scoped>
/* 主容器样式 */
.search-container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

/* 状态栏占位 */
.status-bar {
	background-color: white;
	width: 100%;
}

/* 顶部导航栏 */
.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
	padding: 0 30rpx;
}

.nav-left {
	width: 80rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 36rpx;
	color: #333;
	font-weight: bold;
}

.nav-center {
	flex: 1;
	text-align: center;
}

.nav-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.nav-right {
	width: 80rpx;
	height: 60rpx;
}

/* 搜索栏 */
.search-bar {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: white;
	border-bottom: 1rpx solid #f0f0f0;
}

.search-input-wrapper {
	flex: 1;
	display: flex;
	align-items: center;
	background-color: #f5f5f5;
	border-radius: 25rpx;
	padding: 16rpx 24rpx;
	margin-right: 20rpx;
}

.search-icon {
	font-size: 28rpx;
	color: #999;
	margin-right: 12rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.clear-btn {
	font-size: 24rpx;
	color: #999;
	padding: 8rpx;
}

.cancel-btn {
	font-size: 28rpx;
	color: #1890ff;
	padding: 8rpx;
}

/* 搜索建议 */
.search-suggestions {
	padding: 30rpx;
}

.suggestion-section {
	margin-bottom: 40rpx;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.clear-history {
	font-size: 26rpx;
	color: #666;
}

/* 历史标签 */
.history-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.history-tag {
	padding: 12rpx 20rpx;
	background-color: white;
	color: #666;
	font-size: 26rpx;
	border-radius: 20rpx;
	border: 1rpx solid #e0e0e0;
}

/* 热门标签 */
.hot-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.hot-tag {
	padding: 12rpx 20rpx;
	background-color: #fff3cd;
	color: #856404;
	font-size: 26rpx;
	border-radius: 20rpx;
	border: 1rpx solid #ffeaa7;
}

/* 话题列表 */
.topic-list {
	background-color: white;
	border-radius: 12rpx;
}

.topic-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx;
	border-bottom: 1rpx solid #f8f8f8;
}

.topic-item:last-child {
	border-bottom: none;
}

.topic-name {
	font-size: 28rpx;
	color: #1890ff;
	font-weight: bold;
}

.topic-count {
	font-size: 24rpx;
	color: #999;
}

/* 搜索结果容器 */
.search-results-container {
	background-color: #f5f5f5;
}

/* 结果标签栏 */
.result-tabs {
	display: flex;
	background-color: white;
	flex-shrink: 0;
}

.result-tab {
	flex: 1;
	display: flex;
	justify-content: center;
	align-items: center;
	gap: 8rpx;
	padding: 30rpx 20rpx;
	border-bottom: 4rpx solid transparent;
}

.result-tab.active {
	border-bottom-color: #1890ff;
}

.tab-text {
	font-size: 28rpx;
	color: #666;
}

.result-tab.active .tab-text {
	color: #1890ff;
	font-weight: bold;
}

.tab-count {
	font-size: 24rpx;
	color: #999;
}

.result-tab.active .tab-count {
	color: #1890ff;
}

/* 结果滚动视图 */
.results-scroll {
	background-color: #f5f5f5;
}

/* 帖子搜索结果 */
.posts-results {
	padding: 0 30rpx;
}

.post-result-item {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
}

/* 帖子作者 */
.post-author {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.author-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}

.author-name {
	flex: 1;
	font-size: 26rpx;
	color: #333;
	margin-right: 16rpx;
}

.post-time {
	font-size: 22rpx;
	color: #999;
}

/* 帖子标题 */
.post-title {
	margin-bottom: 16rpx;
}

.title-content {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.4;
}

/* 帖子内容 */
.post-content {
	margin-bottom: 20rpx;
}

.content-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
}

.read-more {
	color: #1890ff;
	font-size: 26rpx;
	margin-left: 8rpx;
}

/* 帖子图片 */
.post-images {
	position: relative;
	margin-bottom: 20rpx;
}

.result-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 8rpx;
}

.more-images {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	background-color: rgba(0, 0, 0, 0.6);
	color: white;
	font-size: 20rpx;
	padding: 4rpx 8rpx;
	border-radius: 8rpx;
}

/* 帖子统计 */
.post-stats {
	display: flex;
	gap: 30rpx;
	margin-bottom: 16rpx;
}

.stat-item {
	font-size: 24rpx;
	color: #666;
}

/* 帖子标签 */
.post-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.post-tag {
	font-size: 22rpx;
	color: #1890ff;
	background-color: #f0f8ff;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

/* 用户搜索结果 */
.users-results {
	padding: 0 30rpx;
}

.user-result-item {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
}

/* 用户信息 */
.user-info {
	display: flex;
	align-items: flex-start;
	margin-bottom: 20rpx;
}

.user-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.user-details {
	flex: 1;
}

.user-nickname {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 8rpx;
}

.user-username {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 12rpx;
}

.user-bio {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

/* 用户徽章 */
.user-badges {
	margin-bottom: 16rpx;
}

.verified-badge {
	background-color: #f0f8ff;
	color: #1890ff;
	font-size: 22rpx;
	padding: 6rpx 12rpx;
	border-radius: 12rpx;
	border: 1rpx solid #d6e4ff;
}

/* 用户统计 */
.user-stats {
	display: flex;
	gap: 30rpx;
	margin-bottom: 20rpx;
}

.user-stat {
	font-size: 24rpx;
	color: #666;
}

/* 关注操作 */
.follow-action {
	text-align: right;
}

.follow-btn {
	background-color: #1890ff;
	color: white;
	font-size: 24rpx;
	border: none;
	border-radius: 20rpx;
	padding: 12rpx 24rpx;
}

.follow-btn.following {
	background-color: #f5f5f5;
	color: #666;
	border: 1rpx solid #d9d9d9;
}

/* 话题搜索结果 */
.topics-results {
	padding: 0 30rpx;
}

.topic-result-item {
	background-color: white;
	margin-bottom: 20rpx;
	padding: 30rpx;
	border-radius: 12rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

/* 话题信息 */
.topic-info {
	flex: 1;
	margin-right: 20rpx;
}

.topic-name {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: #1890ff;
	margin-bottom: 8rpx;
}

.topic-description {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
}

/* 话题统计 */
.topic-stats {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 8rpx;
	margin-right: 20rpx;
}

.topic-stat {
	font-size: 22rpx;
	color: #999;
}

/* 关注话题 */
.topic-action {
	flex-shrink: 0;
}

.follow-topic-btn {
	background-color: #1890ff;
	color: white;
	font-size: 24rpx;
	border: none;
	border-radius: 20rpx;
	padding: 12rpx 20rpx;
}

.follow-topic-btn.following {
	background-color: #f5f5f5;
	color: #666;
	border: 1rpx solid #d9d9d9;
}

/* 空状态 */
.empty-results {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 400rpx;
	background-color: white;
	margin: 20rpx 30rpx;
	border-radius: 12rpx;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 30rpx;
	color: #666;
	margin-bottom: 12rpx;
}

.empty-tip {
	font-size: 24rpx;
	color: #999;
}

/* 加载更多 */
.load-more {
	text-align: center;
	padding: 40rpx;
	background-color: white;
	margin: 20rpx 30rpx;
	border-radius: 12rpx;
}

.load-more-text {
	font-size: 26rpx;
	color: #666;
}

/* 加载指示器 */
.loading-overlay {
	position: fixed;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	background-color: rgba(0, 0, 0, 0.7);
	color: white;
	padding: 20rpx 40rpx;
	border-radius: 12rpx;
	z-index: 1000;
}

.loading-text {
	font-size: 28rpx;
}
</style>