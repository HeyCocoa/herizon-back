<!-- 首页 - 帖子浏览页面 -->
<template>
	<!-- 主容器:帖子列表展示 -->
	<view class="home-container">
		<!-- 顶部导航栏:仅保留搜索功能(系统变更后简化版) -->
		<view class="top-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
			<!-- 应用标题 -->
			<view class="app-title">Herizon</view>

			<!-- 搜索框 -->
			<view class="search-box" @click="handleSearch">
				<text class="search-placeholder">搜索帖子、话题...</text>
				<text class="search-icon">🔍</text>
			</view>
		</view>

		<!-- 帖子列表 - 简化属性,参考话题页面设计 -->
		<scroll-view class="posts-list"
					 scroll-y="true"
					 enable-back-to-top="true"
					 refresher-enabled="true"
					 :refresher-triggered="isRefreshing"
					 @refresherrefresh="refreshPosts"
					 @scrolltolower="loadMorePosts">

			<!-- 帖子卡片 -->
			<view class="post-item"
				  v-for="post in postList"
				  :key="post.id"
				  @click="goToPostDetail(post.id)">

				<!-- 用户信息栏 -->
				<view class="post-header">
					<view class="user-info">
						<image class="user-avatar" :src="post.userAvatar || '/static/img/default-avatar.png'" mode="aspectFill"></image>
						<view class="user-details">
							<text class="username">{{ post.nickname || post.authorNickname || post.username || '匿名用户' }}</text>
							<text class="post-time">{{ formatTime(post.createdAt) }}</text>
						</view>
					</view>

					<!-- 用户角色标识 -->
					<view class="user-role" :class="getRoleClass(post.userRole)">
						{{ getRoleText(post.userRole) }}
					</view>
				</view>

				<!-- 帖子内容 -->
				<view class="post-content">
					<!-- 标题 -->
					<text class="post-title" v-if="post.title">{{ post.title }}</text>

					<!-- 内容预览 -->
					<text class="post-text">{{ getContentPreview(post.content) }}</text>

					<!-- 图片预览 -->
					<view class="post-images" v-if="post.imageUrls && post.imageUrls.length > 0">
						<image class="post-image"
							   v-for="(img, index) in post.imageUrls.slice(0, 3)"
							   :key="index"
							   :src="img"
							   mode="aspectFill"
							   @click.stop="previewImage(post.imageUrls, index)">
						</image>
						<view class="more-images" v-if="post.imageUrls.length > 3">
							+{{ post.imageUrls.length - 3 }}
						</view>
					</view>

					<!-- 视频预览 -->
					<view class="post-video" v-if="post.videoUrl">
						<video class="video-player"
							   :src="post.videoUrl"
							   :poster="post.videoCover"
							   controls="true">
						</video>
					</view>
				</view>

				<!-- 话题标签 -->
				<view class="post-tags" v-if="post.tags && post.tags.length > 0">
					<text class="tag" v-for="tag in post.tags" :key="tag.id">
						#{{ tag.name }}
					</text>
				</view>

				<!-- 互动栏 -->
				<view class="post-actions">
					<view class="action-item" @click.stop="toggleLike(post)">
						<text class="action-icon" :class="{ 'liked': post.isLiked }">❤️</text>
						<text class="action-count">{{ post.likeCount || 0 }}</text>
					</view>

					<view class="action-item" @click.stop="goToPostDetail(post.id)">
						<text class="action-icon">💬</text>
						<text class="action-count">{{ post.commentCount || 0 }}</text>
					</view>

					<view class="action-item" @click.stop="toggleCollect(post)">
						<text class="action-icon" :class="{ 'collected': post.isCollected }">⭐</text>
						<text class="action-count">{{ post.collectCount || 0 }}</text>
					</view>

					<view class="action-item" @click.stop="sharePost(post)">
						<text class="action-icon">📤</text>
						<text class="action-count">{{ post.shareCount || 0 }}</text>
					</view>
				</view>
			</view>

			<!-- 加载更多提示 -->
			<view class="load-more" v-if="hasMore">
				<text v-if="!isLoadingMore">上拉加载更多</text>
				<text v-else>加载中...</text>
			</view>

			<!-- 没有更多数据提示 -->
			<view class="no-more" v-if="!hasMore && postList.length > 0">
				<text>没有更多内容了</text>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="!isLoading && postList.length === 0">
				<text class="empty-icon">📝</text>
				<text class="empty-text">还没有帖子,快来发布第一条吧!</text>
			</view>
		</scroll-view>

		<!-- 浮动发布按钮 -->
		<view class="fab-button" @click="goToPublish">
			<text class="fab-icon">✏️</text>
		</view>
	</view>
</template>

<script>
/**
 * 首页 - 帖子浏览页面(系统变更后简化版)
 *
 * 功能特性:
 * - Feed流展示全部帖子(按推荐算法排序)
 * - 下拉刷新、上拉加载更多
 * - 点赞、收藏、分享等互动功能
 * - 搜索功能入口
 *
 * 系统变更说明:
 * - 移除了话题标签筛选功能
 * - 移除了排序切换功能
 * - 使用简单推荐算法统一排序
 */

import { postApi, actionApi } from '../../../utils/api.js'
import { isLoggedIn, verifyAndExecute, USER_ROLES } from '../../../utils/auth.js'

export default {
	data() {
		return {
			// 系统状态栏高度
			statusBarHeight: 0,

			// 帖子列表数据
			postList: [],

			// 分页参数(简化后只需要分页信息)
			pageParams: {
				current: 1,
				size: 10
			},

			// 加载状态
			isLoading: false,
			isLoadingMore: false,
			isRefreshing: false,
			hasMore: true,

		}
	},

	onLoad() {
		// 获取系统状态栏高度
		this.getSystemInfo()

		// 页面加载时初始化数据
		this.initPage()
	},

	onShow() {
		// 页面显示时可能需要刷新数据(比如从发布页面返回)
		if (this.postList.length > 0) {
			this.refreshPosts()
		}
	},

	methods: {
		/**
		 * 获取系统信息
		 */
		getSystemInfo() {
			const systemInfo = uni.getSystemInfoSync()
			this.statusBarHeight = systemInfo.statusBarHeight || 0
		},

		/**
		 * 初始化页面数据(系统变更后的简化版)
		 * 只加载帖子列表,移除标签加载
		 */
		async initPage() {
			try {
				// 直接加载帖子列表,移除标签加载
				await this.loadPostList(true)
			} catch (error) {
				console.error('页面初始化失败:', error)
				uni.showToast({
					title: '加载失败,请重试',
					icon: 'none'
				})
			}
		},

		/**
		 * 加载帖子列表(系统变更后的简化版)
		 * 使用新的首页API,按推荐算法排序,无筛选条件
		 * @param {boolean} reset - 是否重置列表(刷新时为true)
		 */
		async loadPostList(reset = false) {
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
				// 调用新的简化API,只传递分页参数
				const params = {
					current: this.pageParams.current,
					size: this.pageParams.size
				}

				const response = await postApi.getHomePostList(params)
				const currentPage = this.pageParams.current
				const pageSize = this.pageParams.size || 1
				const records = response.records || []

				// 处理分页数据
				if (reset) {
					this.postList = records
				} else {
					this.postList.push(...records)
				}

				const pagesRaw = response && typeof response.pages !== 'undefined' ? response.pages : undefined
				let totalPages = Number(pagesRaw)
				if (!Number.isFinite(totalPages) || totalPages <= 0) {
					const totalRaw = response && typeof response.total !== 'undefined' ? response.total : undefined
					const total = Number(totalRaw)
					if (Number.isFinite(total) && total > 0) {
						totalPages = Math.max(1, Math.ceil(total / pageSize))
					} else if (records.length === pageSize) {
						totalPages = currentPage + 1
					} else {
						totalPages = currentPage
					}
				}

				// 更新分页状态
				this.hasMore = currentPage < totalPages
				this.pageParams.current = this.hasMore ? currentPage + 1 : Math.max(totalPages, 1)

			} catch (error) {
				console.error('加载帖子列表失败:', error)
				console.warn('API调用失败,使用模拟数据:', error)
				// 如果API调用失败,使用模拟数据
				this.loadMockData(reset)
			} finally {
				this.isLoading = false
				this.isLoadingMore = false
				this.isRefreshing = false
			}
		},


		/**
		 * 下拉刷新
		 */
		refreshPosts() {
			this.isRefreshing = true
			this.loadPostList(true)
		},

		/**
		 * 上拉加载更多
		 */
		loadMorePosts() {
			if (this.hasMore && !this.isLoadingMore) {
				this.loadPostList(false)
			}
		},


		/**
		 * 切换帖子点赞状态
		 * @param {Object} post - 帖子对象
		 */
		async toggleLike(post) {
			// 需要登录才能点赞
			verifyAndExecute(USER_ROLES.TRIAL, async () => {
				try {
					const result = await actionApi.toggleLike({
						targetId: post.id,
						targetType: 'post'
					})

					// 更新本地状态(乐观更新)
					post.isLiked = !post.isLiked
					post.likeCount = post.isLiked ?
						(post.likeCount || 0) + 1 :
						Math.max((post.likeCount || 0) - 1, 0)

					// 触发页面更新
					this.$forceUpdate()

				} catch (error) {
					console.error('点赞操作失败:', error)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			}, {
				loginPrompt: '请先登录后再点赞'
			})
		},

		/**
		 * 切换帖子收藏状态
		 * @param {Object} post - 帖子对象
		 */
		async toggleCollect(post) {
			// 需要登录才能收藏
			verifyAndExecute(USER_ROLES.TRIAL, async () => {
				try {
					const result = await actionApi.toggleCollect({
						targetId: post.id,
						targetType: 'post'
					})

					// 更新本地状态
					post.isCollected = !post.isCollected
					post.collectCount = post.isCollected ?
						(post.collectCount || 0) + 1 :
						Math.max((post.collectCount || 0) - 1, 0)

					this.$forceUpdate()

					uni.showToast({
						title: post.isCollected ? '已收藏' : '已取消收藏',
						icon: 'success'
					})

				} catch (error) {
					console.error('收藏操作失败:', error)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			}, {
				loginPrompt: '请先登录后再收藏'
			})
		},

		/**
		 * 分享帖子
		 * @param {Object} post - 帖子对象
		 */
		async sharePost(post) {
			try {
				// 记录分享行为
				if (isLoggedIn()) {
					await actionApi.recordShare({
						targetId: post.id,
						targetType: 'post',
						platform: 'wechat'
					})
				}

				// 调用微信分享API
				uni.share({
					provider: 'weixin',
					scene: 'WXSceneSession',
					type: 0,
					href: `https://yourapp.com/post/${post.id}`,
					title: post.title || post.content.substring(0, 50),
					summary: post.content.substring(0, 100),
					imageUrl: post.imageUrls && post.imageUrls[0] || '',
					success: () => {
						// 更新分享数
						post.shareCount = (post.shareCount || 0) + 1
						this.$forceUpdate()
					}
				})

			} catch (error) {
				console.error('分享失败:', error)
				uni.showToast({
					title: '分享失败',
					icon: 'none'
				})
			}
		},

		/**
		 * 跳转到帖子详情页
		 * @param {number} postId - 帖子ID
		 */
		goToPostDetail(postId) {
			// 记录浏览行为
			postApi.addPostView(postId).catch(console.warn)

			// 跳转到详情页
			uni.navigateTo({
				url: `/pages/post-detail/post-detail?id=${postId}`
			})
		},

		/**
		 * 跳转到发布页面
		 */
		goToPublish() {
			uni.switchTab({
				url: '/pages/tabbar/tabbar-3/tabbar-3'
			})
		},

		/**
		 * 处理搜索点击
		 */
		handleSearch() {
			uni.navigateTo({
				url: '/pages/search/search'
			})
		},

		/**
		 * 预览图片
		 * @param {Array} images - 图片数组
		 * @param {number} current - 当前图片索引
		 */
		previewImage(images, current) {
			uni.previewImage({
				urls: images,
				current: current
			})
		},

		/**
		 * 格式化时间显示
		 * @param {string} dateTime - 时间字符串
		 * @returns {string} 格式化后的时间
		 */
		formatTime(dateTime) {
			if (!dateTime) return ''

			const now = new Date()
			const time = new Date(dateTime)
			const diff = now - time

			const minutes = Math.floor(diff / 60000)
			const hours = Math.floor(diff / 3600000)
			const days = Math.floor(diff / 86400000)

			if (minutes < 1) return '刚刚'
			if (minutes < 60) return `${minutes}分钟前`
			if (hours < 24) return `${hours}小时前`
			if (days < 7) return `${days}天前`

			// 超过7天显示具体日期
			return time.toLocaleDateString()
		},

		/**
		 * 获取内容预览文本
		 * @param {string} content - 完整内容
		 * @returns {string} 预览文本
		 */
		getContentPreview(content) {
			if (!content) return ''

			// 移除HTML标签和多余空格
			const text = content.replace(/<[^>]*>/g, '').trim()

			// 限制长度,超出显示省略号
			return text.length > 150 ? text.substring(0, 150) + '...' : text
		},

		/**
		 * 获取用户角色样式类
		 * @param {number} role - 用户角色
		 * @returns {string} CSS类名
		 */
		getRoleClass(role) {
			switch (role) {
				case 0: return 'role-trial'     // 体验用户
				case 1: return 'role-verified'  // 正式用户
				case 2: return 'role-admin'     // 管理员
				default: return ''
			}
		},

		/**
		 * 获取用户角色文本
		 * @param {number} role - 用户角色
		 * @returns {string} 角色文本
		 */
		getRoleText(role) {
			switch (role) {
				case 0: return '体验'
				case 1: return '认证'
				case 2: return '管理'
				default: return ''
			}
		},

		/**
		 * 加载模拟数据
		 * 基于2025年最新女性职场、生活平衡、学习成长等真实内容创建的丰富模拟数据
		 * 数据来源:网络调研女性职场发展趋势、AIGC技能培训、工作生活平衡等热点话题
		 * 注意:所有帖子作者都设为正式用户(role=1)或管理员(role=2),体验用户(role=0)不能发帖
		 * @param {boolean} reset - 是否重置列表
		 */
		loadMockData(reset = false) {
			// 统一的完整帖子数据源(基于2025年最新网络调研数据)
			const allPosts = [
				{
					id: 101,
					userId: 1001,
					username: 'AIGC培训师Lisa',
					userAvatar: '',
					userRole: 1,
					title: '2025年职场女性月薪达8978元!分享我的AIGC技能提升之路',
					content: '根据最新调研,2025年职场女性平均月薪达8978元,比去年增长10.3%!我刚通过全国生成式人工智能(AIGC)技术应用职业培训考试,这个证书真的很有含金量。现在50%的工作都会涉及提示词工程,掌握ChatGPT、Midjourney等工具已成必备技能。分享学习要点:1.参加复旦AIGC研修班 2.考取官方职业证书 3.实操练习提示词编写 4.学会AI视频制作。女性在AI接受度上比男性更高,我们要抓住这个优势!',
					imageUrls: [],
					videoUrl: '',
					tags: [{ id: 1, name: 'AIGC' }, { id: 2, name: '职业证书' }, { id: 3, name: '薪资增长' }],
					createdAt: '2025-01-29T10:30:00Z',
					likeCount: 289,
					commentCount: 76,
					shareCount: 45,
					viewCount: 2156,
					isLiked: false,
					isCollected: false
				},
				{
					id: 102,
					userId: 1002,
					username: '职场妈妈Annie',
					userAvatar: '',
					userRole: 1,
					title: '职场妈妈的数字化转型:35%的我们已在使用AI工具平衡工作与家庭',
					content: '智联招聘报告显示,35%的职场妈妈已开始使用数字化工具,比去年提升6.4%!作为二宝妈+部门主管,分享我的数字化平衡术:1.用AI助手制定每日计划和优先级排序 2.利用智能家居减少家务时间 3.通过远程办公工具实现灵活工作 4.AI育儿助手帮助教育规划。65.3%职场妈妈做过全职妈妈,56.7%重返职场,我们要学会拥抱技术变革。记住:技能迭代→效率跃迁→职业韧性!',
					imageUrls: [],
					tags: [{ id: 4, name: '职场妈妈' }, { id: 5, name: '数字化转型' }, { id: 6, name: '工作生活平衡' }],
					createdAt: '2025-01-28T16:20:00Z',
					likeCount: 356,
					commentCount: 92,
					shareCount: 67,
					viewCount: 2743,
					isLiked: true,
					isCollected: false
				},
				{
					id: 103,
					userId: 1003,
					username: '学习达人Sophia',
					userAvatar: '',
					userRole: 1,
					title: '女性择业意愿比男性更强!50.5%的我们愿意重新择业进入新行业',
					content: '最新数据震撼:50.5%女性愿意重新择业进入新行业,明显高于男性的43.9%!42.7%女性倾向通过考证提升竞争力,而男性只有33.9%。我就是活生生的例子:30岁从传统行业转入AIGC领域。分享转型心得:1.选择有前景的细分赛道(如AI+教育)2.系统学习而非碎片化(报名专业培训班)3.找到靠谱导师或社群 4.保持学习节奏不急躁 5.准备充足资金支撑。现在薪资翻倍,女性的学习适应力真的很强!',
					imageUrls: [],
					tags: [{ id: 7, name: '职业转型' }, { id: 8, name: '终身学习' }, { id: 9, name: '择业意愿' }],
					createdAt: '2025-01-27T14:45:00Z',
					likeCount: 234,
					commentCount: 58,
					shareCount: 41,
					viewCount: 1876,
					isLiked: false,
					isCollected: true
				},
				{
					id: 104,
					userId: 1004,
					username: '平衡专家Grace',
					userAvatar: '',
					userRole: 2,
					title: '【管理员分享】职场女性升职信心断崖式下滑:从21.5%跌至7.9%',
					content: '作为平台管理员,分享一个值得关注的数据:仅有7.9%女性对升职有十足把握,相较去年21.5%断崖式下滑!主要原因:15.3%在婚育阶段被动失去晋升机会,10.2%因照顾家庭精力分散。但我们不能气馁!36.6%女性认为自己工作表现比同职级男同事更佳,说明我们的实力是被认可的。建议:1.主动争取高价值项目展示能力 2.建立职场导师关系 3.学会向上管理 4.适时表达晋升意愿。记住:专业实力+主动争取=职场突破!',
					imageUrls: [],
					tags: [{ id: 10, name: '升职信心' }, { id: 11, name: '职场晋升' }, { id: 12, name: '管理建议' }],
					createdAt: '2025-01-26T09:15:00Z',
					likeCount: 445,
					commentCount: 127,
					shareCount: 89,
					viewCount: 3214,
					isLiked: true,
					isCollected: true
				},
				{
					id: 105,
					userId: 1005,
					username: '时间管理师Rachel',
					userAvatar: '',
					userRole: 1,
					title: '职场妈妈时间管理新策略:利用AI工具实现效率翻倍',
					content: '分享我作为职场妈妈的时间管理进化史!传统方法vs AI赋能方法对比:过去手写计划表→现在AI智能规划;过去凭经验排优先级→现在算法辅助决策;过去手动跟踪进度→现在自动化监控。具体工具推荐:1.Motion AI做日程优化 2.Notion AI整理会议纪要 3.ChatGPT生成邮件模板 4.小爱同学管理家庭事务。结果:工作效率提升40%,亲子时间增加1小时/天。建议制定ABC优先级:A=必须做且重要,B=应该做,C=可以推迟。记住:工具是放大器,方法是根本!',
					imageUrls: [],
					tags: [{ id: 13, name: '时间管理' }, { id: 14, name: 'AI工具' }, { id: 15, name: '效率提升' }],
					createdAt: '2025-01-25T11:30:00Z',
					likeCount: 278,
					commentCount: 64,
					shareCount: 52,
					viewCount: 2089,
					isLiked: false,
					isCollected: false
				},
				// 新增帖子数据(时间较新的内容)
				{
					id: 201,
					userId: 2001,
					username: '新人导师Ivy',
					userAvatar: '',
					userRole: 1,
					title: '刚入职3个月的感悟:职场新人避坑指南',
					content: '作为应届毕业生,分享这3个月的真实体验。最大的感受是沟通真的很重要!之前觉得把事情做好就行,但发现主动汇报、及时反馈、积极参与讨论同样关键。还有就是要学会管理期望值,不要给自己太大压力。导师说:"打败你的不是AI,而是掌握了AI的人",所以我也在学习相关技能。',
					imageUrls: [],
					tags: [{ id: 16, name: '职场新人' }, { id: 17, name: '工作感悟' }],
					createdAt: '2025-01-20T15:30:00Z',
					likeCount: 45,
					commentCount: 12,
					shareCount: 8,
					viewCount: 234,
					isLiked: false,
					isCollected: false
				},
				{
					id: 202,
					userId: 2002,
					username: '转行成功Jessica',
					userAvatar: '',
					userRole: 1,
					title: '30岁转行互联网:我的血泪教训和成功经验',
					content: '从传统制造业转到互联网,用了2年时间。最困难的不是技能学习,而是克服年龄焦虑和重新建立信心。分享几个关键点:1.选择有前景的细分领域 2.系统性学习而非碎片化 3.找到靠谱的导师或社群 4.保持学习节奏不要急躁 5.准备充足的资金支撑。现在薪资比之前翻了一倍,虽然过程很艰难但很值得!',
					imageUrls: [],
					tags: [{ id: 18, name: '职业转型' }, { id: 19, name: '年龄焦虑' }, { id: 20, name: '学习方法' }],
					createdAt: '2025-01-20T12:45:00Z',
					likeCount: 67,
					commentCount: 23,
					shareCount: 15,
					viewCount: 456,
					isLiked: true,
					isCollected: false
				},
				{
					id: 203,
					userId: 2003,
					username: '效率达人Grace',
					userAvatar: '',
					userRole: 1,
					title: '用AI工具提升工作效率300%:我的实战工具箱分享',
					content: '作为产品经理,这半年来深度使用各种AI工具,效率提升明显。分享我的工具箱:1.ChatGPT:写方案、会议纪要、邮件 2.Midjourney:原型设计、PPT配图 3.Notion AI:整理笔记、生成大纲 4.剪映:视频剪辑和字幕 5.百度文心一言:中文内容优化。关键是要学会写好提示词,这真的是核心技能!',
					imageUrls: [],
					tags: [{ id: 21, name: 'AI工具' }, { id: 22, name: '效率提升' }, { id: 23, name: '产品经理' }],
					createdAt: '2025-01-20T10:20:00Z',
					likeCount: 89,
					commentCount: 31,
					shareCount: 19,
					viewCount: 678,
					isLiked: false,
					isCollected: true
				},
				{
					id: 204,
					userId: 2004,
					username: '自由工作者Luna',
					userAvatar: '',
					userRole: 1,
					title: '从公司员工到自由职业者:收入翻倍的背后真相',
					content: '离职做自由职业者2年,收入确实翻倍了,但背后的挑战也不少。真实分享:优势是时间自由、收入上限高、可以选择项目;挑战是收入不稳定、需要自己找客户、缺乏团队支持。建议:1.在职期间积累客户资源 2.建立稳定的收入来源 3.保持学习和技能更新 4.做好财务规划。不是所有人都适合,要评估自己的风险承受能力。',
					imageUrls: [],
					tags: [{ id: 24, name: '自由职业' }, { id: 25, name: '收入规划' }, { id: 26, name: '职业选择' }],
					createdAt: '2025-01-20T08:15:00Z',
					likeCount: 123,
					commentCount: 45,
					shareCount: 27,
					viewCount: 892,
					isLiked: true,
					isCollected: false
				},
				{
					id: 205,
					userId: 2005,
					username: '团队领导Rachel',
					userAvatar: '',
					userRole: 1,
					title: '带团队一年心得:女性领导力的独特优势',
					content: '从个人贡献者到团队leader,最大的感受是女性在领导力方面有独特优势:1.更善于倾听和共情 2.注重团队协作和关系建设 3.决策时考虑更全面 4.更容易获得团队信任。但也要注意:不要过度照顾他人情绪而忽略目标达成,适度的强势和决断力很重要。推荐大家看《精益创业》和《关键对话》这两本书。',
					imageUrls: [],
					tags: [{ id: 27, name: '团队管理' }, { id: 28, name: '女性领导力' }, { id: 29, name: '管理技巧' }],
					createdAt: '2025-01-19T20:30:00Z',
					likeCount: 156,
					commentCount: 52,
					shareCount: 34,
					viewCount: 1123,
					isLiked: false,
					isCollected: true
				}
			]

			// 系统变更后的简化版:统一使用推荐算法排序(修复模拟数据显示问题)
			let sourcePosts = [...allPosts] // 创建副本避免原数据被修改

			// 使用简化的推荐算法排序,与后端API保持一致
			// 优先级:点赞数 > 收藏数 > 分享数 > 时间
			sourcePosts.sort((a, b) => {
				// 1. 点赞数比较
				if (a.likeCount !== b.likeCount) {
					return (b.likeCount || 0) - (a.likeCount || 0)
				}
				// 2. 收藏数比较
				if (a.collectCount !== b.collectCount) {
					return (b.collectCount || 0) - (a.collectCount || 0)
				}
				// 3. 分享数比较
				if (a.shareCount !== b.shareCount) {
					return (b.shareCount || 0) - (a.shareCount || 0)
				}
				// 4. 时间比较(最新优先)
				return new Date(b.createdAt) - new Date(a.createdAt)
			})

			// 系统变更后移除标签筛选功能,显示全部帖子

			// 分页处理 - 作为API失败时的fallback
			const pageSize = 10
			let mockPosts = []

			if (reset) {
				// 重置时加载第一页数据
				mockPosts = sourcePosts.slice(0, pageSize)
				this.postList = mockPosts
				this.pageParams.current = 2
			} else {
				// 加载更多时,从统一数据源获取下一页数据
				const startIndex = (this.pageParams.current - 1) * pageSize
				mockPosts = sourcePosts.slice(startIndex, startIndex + pageSize)

				if (mockPosts.length > 0) {
					this.postList.push(...mockPosts)
					this.pageParams.current++
				}
			}

			// 更新分页状态
			const totalPages = Math.ceil(sourcePosts.length / pageSize)
			this.hasMore = this.pageParams.current <= totalPages && mockPosts.length > 0
		}
	}
}
</script>

<style lang="scss" scoped>
/* 页面容器 - 修复滚动布局 */
.home-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f7f7f7;
	overflow: hidden; /* 防止整个页面出现滚动条,确保滚动只在posts-list内 */
}

/* 顶部导航栏(包含状态栏适配) */
.top-nav {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20upx 30upx;
	background-color: #fff;
	border-bottom: 2upx solid #f0f0f0;
	position: relative;
	z-index: 10;

	.app-title {
		font-size: 36upx;
		font-weight: bold;
		color: #333;
	}

	.search-box {
		flex: 1;
		margin-left: 30upx;
		padding: 15upx 20upx;
		background-color: #f5f5f5;
		border-radius: 25upx;
		display: flex;
		align-items: center;
		justify-content: space-between;

		.search-placeholder {
			font-size: 28upx;
			color: #999;
		}

		.search-icon {
			font-size: 28upx;
			color: #666;
		}
	}
}

/* 删除无用的标签栏和排序标签样式(系统变更后已移除相关功能) */

/* 帖子列表 - 修复滚动问题,参考话题页面设计 */
.posts-list {
	flex: 1;
	height: 0; /* 确保flex子元素正确计算高度 */
	padding: 20upx 0;
	box-sizing: border-box;
	overflow: hidden; /* 确保scroll-view正确工作 */
}

/* 帖子卡片 */
.post-item {
	background-color: #fff;
	margin: 0 20upx 20upx 20upx;
	border-radius: 16upx;
	padding: 30upx;
	box-shadow: 0 4upx 12upx rgba(0, 0, 0, 0.05);
}

/* 帖子头部 */
.post-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20upx;

	.user-info {
		display: flex;
		align-items: center;

		.user-avatar {
			width: 60upx;
			height: 60upx;
			border-radius: 50%;
			margin-right: 20upx;
		}

		.user-details {
			.username {
				display: block;
				font-size: 28upx;
				font-weight: bold;
				color: #333;
				margin-bottom: 4upx;
			}

			.post-time {
				font-size: 22upx;
				color: #999;
			}
		}
	}

	.user-role {
		padding: 6upx 12upx;
		border-radius: 8upx;
		font-size: 20upx;

		&.role-trial {
			background-color: #fff2e8;
			color: #ff8800;
		}

		&.role-verified {
			background-color: #e8f4ff;
			color: #007aff;
		}

		&.role-admin {
			background-color: #ffe8e8;
			color: #ff4444;
		}
	}
}

/* 帖子内容 */
.post-content {
	margin-bottom: 20upx;

	.post-title {
		display: block;
		font-size: 32upx;
		font-weight: bold;
		color: #333;
		margin-bottom: 12upx;
		line-height: 1.4;
	}

	.post-text {
		font-size: 28upx;
		color: #666;
		line-height: 1.6;
		margin-bottom: 15upx;
	}
}

/* 图片预览 */
.post-images {
	display: flex;
	flex-wrap: wrap;
	gap: 10upx;
	margin-bottom: 15upx;

	.post-image {
		width: 200upx;
		height: 200upx;
		border-radius: 12upx;
	}

	.more-images {
		width: 200upx;
		height: 200upx;
		border-radius: 12upx;
		background-color: rgba(0, 0, 0, 0.6);
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 28upx;
	}
}

/* 视频预览 */
.post-video {
	position: relative;
	border-radius: 12upx;
	overflow: hidden;
	margin-bottom: 15upx;

	.video-player {
		width: 100%;
		height: 300upx;
	}

}

/* 标签 */
.post-tags {
	margin-bottom: 20upx;

	.tag {
		display: inline-block;
		padding: 6upx 12upx;
		margin-right: 10upx;
		background-color: #f0f7ff;
		color: #007aff;
		border-radius: 8upx;
		font-size: 22upx;
	}
}

/* 互动栏 */
.post-actions {
	display: flex;
	align-items: center;

	.action-item {
		display: flex;
		align-items: center;
		margin-right: 40upx;

		.action-icon {
			font-size: 32upx;
			margin-right: 8upx;
			transition: all 0.3s;

			&.liked {
				color: #ff4444;
			}

			&.collected {
				color: #ff8800;
			}
		}

		.action-count {
			font-size: 24upx;
			color: #666;
		}
	}
}

/* 加载状态 */
.load-more, .no-more {
	text-align: center;
	padding: 30upx;
	color: #999;
	font-size: 26upx;
}

/* 空状态 */
.empty-state {
	text-align: center;
	padding: 100upx 30upx;

	.empty-icon {
		font-size: 80upx;
		margin-bottom: 20upx;
		display: block;
	}

	.empty-text {
		font-size: 28upx;
		color: #999;
	}
}

/* 浮动发布按钮 */
.fab-button {
	position: fixed;
	right: 40upx;
	bottom: 120upx;
	width: 100upx;
	height: 100upx;
	border-radius: 50%;
	background: linear-gradient(135deg, #007aff, #5ac8fa);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8upx 20upx rgba(0, 122, 255, 0.3);
	z-index: 100;

	.fab-icon {
		font-size: 40upx;
		color: #fff;
	}
}
</style>
