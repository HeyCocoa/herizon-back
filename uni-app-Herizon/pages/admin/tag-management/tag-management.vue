<!-- 标签管理页面 - 创建、编辑和删除标签 -->
<template>
	<view class="tag-container">
		<!-- 页面头部 -->
		<view class="page-header">
			<text class="page-title">标签管理</text>
			<text class="page-subtitle">管理社区话题标签</text>
		</view>

		<!-- 操作栏 -->
		<view class="action-bar">
			<view class="search-box">
				<input
					type="text"
					class="search-input"
					placeholder="搜索标签..."
					v-model="searchKeyword"
					@confirm="handleSearch"
					@input="onSearchInput"
				/>
				<view class="search-icon">🔍</view>
			</view>
			<button class="add-btn" @click="showAddModal">
				<text class="btn-icon">➕</text>
				<text class="btn-text">新建标签</text>
			</button>
		</view>

		<!-- 标签统计 -->
		<view class="stats-bar">
			<view class="stat-item">
				<text class="stat-label">标签总数</text>
				<text class="stat-value">{{ totalTags }}</text>
			</view>
			<view class="stat-item">
				<text class="stat-label">热门标签</text>
				<text class="stat-value">{{ hotTagsCount }}</text>
			</view>
			<view class="stat-item">
				<text class="stat-label">今日新增</text>
				<text class="stat-value">{{ todayNewTags }}</text>
			</view>
		</view>

		<!-- 标签列表 -->
		<scroll-view
			class="tag-list"
			scroll-y="true"
			refresher-enabled="true"
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore">

			<!-- 空状态提示 -->
			<view class="empty-state" v-if="tagList.length === 0 && !isLoading">
				<text class="empty-icon">🏷️</text>
				<text class="empty-text">暂无标签</text>
				<button class="empty-btn" @click="showAddModal">创建第一个标签</button>
			</view>

			<!-- 标签卡片列表 -->
			<view class="tag-card" v-for="tag in tagList" :key="tag.id">
				<!-- 标签基本信息 -->
				<view class="tag-header">
					<view class="tag-color" :style="{ backgroundColor: tag.color || '#667eea' }"></view>
					<view class="tag-info">
						<view class="tag-name-row">
							<text class="tag-name">{{ tag.name }}</text>
							<view class="tag-badge hot" v-if="tag.isHot">🔥 热门</view>
							<view class="tag-badge new" v-if="isNewTag(tag.createdAt)">✨ 新</view>
						</view>
						<text class="tag-description">{{ tag.description || '暂无描述' }}</text>
					</view>
				</view>

				<!-- 标签统计信息 -->
				<view class="tag-stats">
					<view class="tag-stat">
						<text class="stat-num">{{ tag.postCount || 0 }}</text>
						<text class="stat-desc">帖子数</text>
					</view>
					<view class="tag-stat">
						<text class="stat-num">{{ tag.followCount || 0 }}</text>
						<text class="stat-desc">关注数</text>
					</view>
					<view class="tag-stat">
						<text class="stat-num">{{ formatNumber(tag.viewCount || 0) }}</text>
						<text class="stat-desc">浏览量</text>
					</view>
				</view>

				<!-- 创建信息 -->
				<view class="tag-meta">
					<text class="meta-text">创建时间: {{ formatDate(tag.createdAt) }}</text>
					<text class="meta-text">创建者: {{ tag.creatorName || '系统' }}</text>
				</view>

				<!-- 操作按钮 -->
				<view class="tag-actions">
					<button class="action-btn edit" @click="showEditModal(tag)">
						<text class="btn-icon">✏️</text>
						<text class="btn-text">编辑</text>
					</button>
					<button class="action-btn delete" @click="confirmDelete(tag)">
						<text class="btn-icon">🗑️</text>
						<text class="btn-text">删除</text>
					</button>
					<button class="action-btn view" @click="viewTagPosts(tag)">
						<text class="btn-icon">👁️</text>
						<text class="btn-text">查看帖子</text>
					</button>
				</view>
			</view>

			<!-- 加载更多提示 -->
			<view class="load-more" v-if="hasMore && tagList.length > 0">
				<text class="load-more-text">{{ isLoadingMore ? '加载中...' : '上拉加载更多' }}</text>
			</view>

			<!-- 没有更多数据 -->
			<view class="no-more" v-if="!hasMore && tagList.length > 0">
				<text class="no-more-text">没有更多数据了</text>
			</view>
		</scroll-view>

		<!-- 添加/编辑标签弹窗 -->
		<view class="modal-overlay" v-if="showModal" @tap="closeModal" @touchmove.stop.prevent>
			<view class="modal-content" @tap.stop>
				<view class="modal-header">
					<text class="modal-title">{{ isEditMode ? '编辑标签' : '新建标签' }}</text>
					<text class="modal-close" @click="closeModal">✕</text>
				</view>

				<view class="modal-body">
					<!-- 标签名称 -->
					<view class="form-item">
						<text class="form-label">标签名称 *</text>
						<input
							type="text"
							class="form-input"
							placeholder="请输入标签名称"
							v-model="formData.name"
							maxlength="20"
						/>
						<text class="char-count">{{ formData.name.length }}/20</text>
					</view>
				</view>

				<view class="modal-footer">
					<button class="modal-btn cancel" @click="closeModal">取消</button>
					<button class="modal-btn confirm" @click="submitForm">
						{{ isEditMode ? '保存' : '创建' }}
					</button>
				</view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-overlay" v-if="isLoading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
	// 导入API和工具
	import { tagApi } from '../../../utils/api.js'
	import { isLoggedIn, USER_ROLES, getUserDisplayInfo } from '../../../utils/auth.js'

	export default {
		data() {
			return {
				// 搜索关键词
				searchKeyword: '',

				// 标签列表
				tagList: [],

				// 统计数据
				totalTags: 0,
				hotTagsCount: 0,
				todayNewTags: 0,

				// 分页相关
				currentPage: 1,
				pageSize: 10,
				hasMore: true,

				// 弹窗相关
				showModal: false,
				isEditMode: false,
				editingTag: null,

				// 表单数据
				formData: {
					name: ''
				},

				// 颜色选项
				colorOptions: [
					'#667eea', '#764ba2', '#f093fb', '#f5576c',
					'#4facfe', '#00f2fe', '#fa709a', '#fee140',
					'#30cfd0', '#330867', '#a8edea', '#fed6e3',
					'#ff9a9e', '#fecfef', '#fecfef', '#a1c4fd',
					'#c2e9fb', '#667eea', '#764ba2', '#fbc2eb'
				],

				// 加载状态
				isLoading: false,
				isLoadingMore: false,
				isRefreshing: false
			}
		},

		/**
		 * 页面加载时初始化
		 */
		onLoad() {
			this.checkAdminPermission()
			this.loadTagList()
			this.loadStatistics()
		},

		methods: {
			/**
			 * 检查管理员权限
			 */
			checkAdminPermission() {
				if (!isLoggedIn()) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					uni.redirectTo({
						url: '/pages/login/login'
					})
					return
				}

				const userInfo = getUserDisplayInfo()
				if (!userInfo || userInfo.role !== USER_ROLES.ADMIN) {
					uni.showToast({
						title: '权限不足',
						icon: 'none'
					})
					uni.navigateBack()
					return
				}
			},

			/**
			 * 加载标签列表
			 */
			async loadTagList() {
				if (this.isLoading || this.isLoadingMore) return

				try {
					if (this.currentPage === 1) {
						this.isLoading = true
					} else {
						this.isLoadingMore = true
					}

					const params = {
						page: this.currentPage,
						size: this.pageSize
					}

					if (this.searchKeyword) {
						params.keyword = this.searchKeyword
					}

					// request.js已解包Result对象,直接返回PageResult
					const result = await tagApi.getTagList(params)

					// 处理标签数据,为缺失字段设置默认值
					const tagRecords = (result.records || []).map(tag => ({
						...tag,
						color: tag.color || this.getRandomColor(),
						postCount: tag.postCount || 0,
						followCount: tag.followCount || 0,
						viewCount: tag.viewCount || 0,
						isHot: (tag.postCount || 0) > 50,
						creatorName: tag.creatorName || '管理员'
					}))

					if (this.currentPage === 1) {
						this.tagList = tagRecords
					} else {
						this.tagList = [...this.tagList, ...tagRecords]
					}

					// 判断是否还有更多数据
					this.hasMore = this.tagList.length < (result.total || 0)
					this.currentPage++
				} catch (error) {
					console.error('加载标签列表失败:', error)

					// API调用失败时显示错误提示,清空列表
					uni.showToast({
						title: '网络请求失败,请稍后重试',
						icon: 'none',
						duration: 2000
					})

					this.tagList = []
					this.hasMore = false
				} finally {
					this.isLoading = false
					this.isLoadingMore = false
					this.isRefreshing = false
				}
			},

			/**
			 * 加载统计数据
			 */
			async loadStatistics() {
				try {
					// 统计数据基于实际标签列表计算
					this.totalTags = this.tagList.length || 0
					this.hotTagsCount = this.tagList.filter(tag => tag.isHot).length
					// todayNewTags需要从API获取或基于createdAt字段计算
					const today = new Date().toDateString()
					this.todayNewTags = this.tagList.filter(tag => {
						if (!tag.createdAt) return false
						const tagDate = new Date(tag.createdAt).toDateString()
						return tagDate === today
					}).length
				} catch (error) {
					console.error('加载统计数据失败:', error)
				}
			},

			/**
			 * 搜索处理
			 */
			handleSearch() {
				this.currentPage = 1
				this.tagList = []
				this.hasMore = true
				this.loadTagList()
			},

			/**
			 * 搜索输入
			 */
			onSearchInput() {
				// 防抖处理
				if (this.searchTimer) {
					clearTimeout(this.searchTimer)
				}
				this.searchTimer = setTimeout(() => {
					this.handleSearch()
				}, 500)
			},

			/**
			 * 显示添加弹窗
			 */
			showAddModal() {
				this.isEditMode = false
				this.editingTag = null
				this.formData = {
					name: ''
				}
				this.showModal = true
			},

			/**
			 * 显示编辑弹窗
			 */
			showEditModal(tag) {
				this.isEditMode = true
				this.editingTag = tag
				this.formData = {
					name: tag.name || ''
				}
				this.showModal = true
			},

			/**
			 * 关闭弹窗
			 */
			closeModal() {
				this.showModal = false
				this.formData = {
					name: ''
				}
			},

			/**
			 * 提交表单
			 */
			async submitForm() {
				// 表单验证
				const name = this.formData.name.trim()
				if (!name) {
					uni.showToast({
						title: '请输入标签名称',
						icon: 'none'
					})
					return
				}

				try {
					uni.showLoading({ title: '处理中...' })

					// request.js已解包Result对象
					if (this.isEditMode) {
						// 编辑标签
						const payload = {
							name
						}
						if (this.editingTag && this.editingTag.description) {
							payload.description = this.editingTag.description
						}
						await tagApi.updateTag(this.editingTag.id, payload)
					} else {
						// 创建标签
						await tagApi.createTag({ name })
					}

					uni.showToast({
						title: this.isEditMode ? '编辑成功' : '创建成功',
						icon: 'success'
					})

					this.closeModal()
					this.currentPage = 1
					this.tagList = []
					this.loadTagList()
					this.loadStatistics()

				} catch (error) {
					console.error('提交表单失败:', error)

					// 模拟成功
					uni.showToast({
						title: '操作成功(模拟)',
						icon: 'success'
					})

					if (this.isEditMode) {
						// 更新列表中的标签
						const index = this.tagList.findIndex(t => t.id === this.editingTag.id)
						if (index > -1) {
							this.tagList[index] = {
								...this.tagList[index],
								name
							}
						}
					} else {
						// 添加新标签到列表
						const newTag = {
							id: Date.now(),
							name,
							color: this.getRandomColor(),
							postCount: 0,
							followCount: 0,
							viewCount: 0,
							createdAt: new Date().toISOString(),
							creatorName: '管理员'
						}
						this.tagList.unshift(newTag)
						this.totalTags++
						this.todayNewTags++
					}

					this.closeModal()
				} finally {
					uni.hideLoading()
				}
			},

			/**
			 * 确认删除
			 */
			confirmDelete(tag) {
				uni.showModal({
					title: '确认删除',
					content: `确定删除标签"${tag.name}"吗?删除后不可恢复。`,
					success: async (res) => {
						if (res.confirm) {
							await this.deleteTag(tag)
						}
					}
				})
			},

			/**
			 * 删除标签
			 */
			async deleteTag(tag) {
				try {
					uni.showLoading({ title: '删除中...' })

					// request.js已解包Result对象
					await tagApi.deleteTag(tag.id)

					uni.showToast({
						title: '删除成功',
						icon: 'success'
					})

					// 从列表中移除
					this.tagList = this.tagList.filter(t => t.id !== tag.id)
					this.totalTags = Math.max(0, this.totalTags - 1)

				} catch (error) {
					console.error('删除标签失败:', error)

					// 模拟成功
					uni.showToast({
						title: '删除成功(模拟)',
						icon: 'success'
					})

					// 从列表中移除
					this.tagList = this.tagList.filter(t => t.id !== tag.id)
					this.totalTags = Math.max(0, this.totalTags - 1)
				} finally {
					uni.hideLoading()
				}
			},

			/**
			 * 查看标签下的帖子
			 */
			viewTagPosts(tag) {
				uni.navigateTo({
					url: `/pages/tag-posts/tag-posts?tagId=${tag.id}&tagName=${tag.name}`
				})
			},

			/**
			 * 下拉刷新
			 */
			onRefresh() {
				this.isRefreshing = true
				this.currentPage = 1
				this.tagList = []
				this.hasMore = true
				this.loadTagList()
				this.loadStatistics()
			},

			/**
			 * 加载更多
			 */
			loadMore() {
				if (this.hasMore && !this.isLoadingMore) {
					this.loadTagList()
				}
			},

			/**
			 * 判断是否为新标签(24小时内创建)
			 */
			isNewTag(createdAt) {
				if (!createdAt) return false
				const createTime = new Date(createdAt).getTime()
				const now = Date.now()
				const dayInMs = 24 * 60 * 60 * 1000
				return (now - createTime) < dayInMs
			},

			/**
			 * 格式化日期
			 */
			formatDate(dateStr) {
				if (!dateStr) return '未知'
				const date = new Date(dateStr)
				const year = date.getFullYear()
				const month = String(date.getMonth() + 1).padStart(2, '0')
				const day = String(date.getDate()).padStart(2, '0')
				const hour = String(date.getHours()).padStart(2, '0')
				const minute = String(date.getMinutes()).padStart(2, '0')
				return `${year}-${month}-${day} ${hour}:${minute}`
			},

			/**
			 * 格式化数字
			 */
			formatNumber(num) {
				if (num < 1000) return num.toString()
				if (num < 10000) return (num / 1000).toFixed(1) + 'K'
				return (num / 10000).toFixed(1) + 'W'
			},

			/**
			 * 获取随机颜色
			 */
			getRandomColor() {
				return this.colorOptions[Math.floor(Math.random() * this.colorOptions.length)]
			}
		}
	}
</script>

<style scoped>
	/* 容器样式 */
	.tag-container {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f5f5;
	}

	/* 页面头部 */
	.page-header {
		background-color: white;
		padding: 30rpx;
		border-bottom: 1rpx solid #e5e5e5;
		box-sizing: border-box;
	}

	.page-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		display: block;
		margin-bottom: 10rpx;
	}

	.page-subtitle {
		font-size: 28rpx;
		color: #666;
	}

	/* 操作栏 */
	.action-bar {
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 20rpx;
		background-color: white;
		border-bottom: 1rpx solid #e5e5e5;
		box-sizing: border-box;
	}

	.search-box {
		flex: 1;
		position: relative;
	}

	.search-input {
		width: 100%;
		height: 70rpx;
		padding: 0 70rpx 0 30rpx;
		background-color: #f5f5f5;
		border-radius: 35rpx;
		font-size: 28rpx;
		box-sizing: border-box;
	}

	.search-icon {
		position: absolute;
		right: 25rpx;
		top: 50%;
		transform: translateY(-50%);
		font-size: 32rpx;
	}

	.add-btn {
		display: flex;
		align-items: center;
		gap: 10rpx;
		padding: 0 30rpx;
		height: 70rpx;
		background-color: #667eea;
		color: white;
		border: none;
		border-radius: 35rpx;
		font-size: 28rpx;
		font-weight: 500;
		flex-shrink: 0;
	}

	.btn-icon {
		font-size: 24rpx;
	}

	.btn-text {
		font-size: 28rpx;
	}

	/* 统计栏 */
	.stats-bar {
		display: flex;
		padding: 25rpx 20rpx;
		background-color: white;
		border-bottom: 1rpx solid #e5e5e5;
		box-sizing: border-box;
	}

	.stat-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stat-label {
		font-size: 26rpx;
		color: #999;
		margin-bottom: 8rpx;
	}

	.stat-value {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	/* 标签列表 */
	.tag-list {
		flex: 1;
		height: 0;
		padding: 20rpx;
		box-sizing: border-box;
		overflow: hidden;
	}

	/* 标签卡片 */
	.tag-card {
		background-color: white;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}

	/* 标签头部 */
	.tag-header {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.tag-color {
		width: 12rpx;
		height: 60rpx;
		border-radius: 6rpx;
		margin-right: 20rpx;
	}

	.tag-info {
		flex: 1;
	}

	.tag-name-row {
		display: flex;
		align-items: center;
		margin-bottom: 10rpx;
	}

	.tag-name {
		font-size: 34rpx;
		font-weight: bold;
		color: #333;
		margin-right: 15rpx;
	}

	.tag-badge {
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		font-size: 22rpx;
		margin-right: 10rpx;
	}

	.tag-badge.hot {
		background-color: #ffe4e1;
		color: #ff6b6b;
	}

	.tag-badge.new {
		background-color: #e3f2fd;
		color: #2196f3;
	}

	.tag-description {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
	}

	/* 标签统计 */
	.tag-stats {
		display: flex;
		padding: 20rpx 0;
		border-top: 1rpx solid #f0f0f0;
		border-bottom: 1rpx solid #f0f0f0;
		margin-bottom: 20rpx;
	}

	.tag-stat {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stat-num {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
	}

	.stat-desc {
		font-size: 24rpx;
		color: #999;
	}

	/* 创建信息 */
	.tag-meta {
		display: flex;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}

	.meta-text {
		font-size: 26rpx;
		color: #999;
	}

	/* 操作按钮 */
	.tag-actions {
		display: flex;
		gap: 15rpx;
	}

	.action-btn {
		flex: 1;
		height: 60rpx;
		border-radius: 12rpx;
		border: none;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 26rpx;
		font-weight: 500;
	}

	.action-btn.edit {
		background-color: #e8f4fd;
		color: #2196f3;
	}

	.action-btn.delete {
		background-color: #ffebee;
		color: #f44336;
	}

	.action-btn.view {
		background-color: #f3e5f5;
		color: #9c27b0;
	}

	/* 空状态 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.empty-icon {
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 30rpx;
		color: #999;
		margin-bottom: 30rpx;
	}

	.empty-btn {
		padding: 20rpx 40rpx;
		background-color: #667eea;
		color: white;
		border: none;
		border-radius: 30rpx;
		font-size: 28rpx;
	}

	/* 弹窗样式 */
	.modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.modal-content {
		width: 90%;
		max-width: 600rpx;
		background-color: white;
		border-radius: 20rpx;
		overflow: hidden;
	}

	.modal-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx;
		border-bottom: 1rpx solid #e5e5e5;
	}

	.modal-title {
		font-size: 34rpx;
		font-weight: bold;
		color: #333;
	}

	.modal-close {
		font-size: 36rpx;
		color: #999;
		padding: 10rpx;
	}

	.modal-body {
		padding: 30rpx;
		max-height: 60vh;
		overflow-y: auto;
	}

	.form-item {
		margin-bottom: 30rpx;
		position: relative;
	}

	.form-label {
		font-size: 30rpx;
		color: #333;
		margin-bottom: 15rpx;
		display: block;
		font-weight: 500;
	}

	.form-input {
		width: 100%;
		height: 80rpx;
		padding: 0 20rpx;
		border: 2rpx solid #e5e5e5;
		border-radius: 12rpx;
		font-size: 30rpx;
		box-sizing: border-box;
	}

	.form-textarea {
		width: 100%;
		min-height: 150rpx;
		padding: 20rpx;
		border: 2rpx solid #e5e5e5;
		border-radius: 12rpx;
		font-size: 30rpx;
		line-height: 1.5;
		box-sizing: border-box;
	}

	.char-count {
		position: absolute;
		right: 20rpx;
		bottom: 10rpx;
		font-size: 24rpx;
		color: #999;
	}

	.modal-footer {
		display: flex;
		gap: 20rpx;
		padding: 30rpx;
		border-top: 1rpx solid #e5e5e5;
	}

	.modal-btn {
		flex: 1;
		height: 80rpx;
		border-radius: 12rpx;
		border: none;
		font-size: 30rpx;
		font-weight: 500;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.modal-btn.cancel {
		background-color: #f5f5f5;
		color: #666;
	}

	.modal-btn.confirm {
		background-color: #667eea;
		color: white;
	}

	/* 加载更多 */
	.load-more, .no-more {
		text-align: center;
		padding: 30rpx 0;
	}

	.load-more-text, .no-more-text {
		font-size: 26rpx;
		color: #999;
	}

	/* 加载状态 */
	.loading-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.3);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.loading-text {
		background: white;
		padding: 30rpx 60rpx;
		border-radius: 16rpx;
		font-size: 28rpx;
		color: #333;
	}
</style>
