<!-- 问答发布页面 - 创建问答帖子 -->
<template>
	<view class="qa-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-item" @click="handleCancel">
				<text class="nav-text">取消</text>
			</view>
			<view class="nav-title">提问</view>
			<view class="nav-item" @click="handlePublish">
				<text class="nav-text publish-btn" :class="{ 'active': canPublish }">发布</text>
			</view>
		</view>

		<!-- 内容编辑区域 -->
		<scroll-view class="content-area" scroll-y="true">
			<!-- 问题标题输入 -->
			<view class="title-section">
				<view class="section-header">
					<text class="section-title">问题标题</text>
					<text class="required-mark">*</text>
				</view>
				<textarea
					class="title-input"
					placeholder="请简明扼要地描述你的问题..."
					v-model="formData.title"
					:maxlength="100"
					auto-height
					@input="onTitleInput"
				/>
				<text class="char-count">{{ formData.title.length }}/100</text>
				<text class="input-tip">清晰明确的标题有助于获得更好的回答</text>
			</view>

			<!-- 问题描述输入 -->
			<view class="content-section">
				<view class="section-header">
					<text class="section-title">问题描述</text>
					<text class="optional-mark">可选</text>
				</view>
				<textarea
					class="content-input"
					placeholder="详细描述你的问题背景、遇到的困难、期望的帮助等..."
					v-model="formData.content"
					:maxlength="2000"
					auto-height
					@input="onContentInput"
					:focus="autoFocus"
				/>
				<text class="char-count">{{ formData.content.length }}/2000</text>
				<text class="input-tip">提供更多细节有助于获得针对性的回答</text>
			</view>

			<!-- 问题分类选择 -->
			<view class="category-section">
				<view class="section-header">
					<text class="section-title">问题分类</text>
					<text class="required-mark">*</text>
				</view>
				<view class="category-list">
					<view
						class="category-item"
						v-for="category in questionCategories"
						:key="category.id"
						:class="{ 'selected': formData.categoryId === category.id }"
						@click="selectCategory(category.id)"
					>
						<text class="category-icon">{{ category.icon }}</text>
						<text class="category-name">{{ category.name }}</text>
					</view>
				</view>
			</view>

			<!-- 悬赏设置 -->
			<view class="reward-section">
				<view class="section-header">
					<text class="section-title">悬赏设置</text>
					<text class="optional-mark">可选</text>
					<switch
						class="reward-switch"
						:checked="formData.hasReward"
						@change="toggleReward"
						color="#f33e54"
					/>
				</view>

				<view class="reward-content" v-if="formData.hasReward">
					<view class="reward-options">
						<view
							class="reward-option"
							v-for="option in rewardOptions"
							:key="option.value"
							:class="{ 'selected': formData.rewardPoints === option.value }"
							@click="selectReward(option.value)"
						>
							<text class="reward-points">{{ option.value }}</text>
							<text class="reward-label">积分</text>
						</view>
					</view>
					<text class="reward-tip">设置悬赏可以吸引更多专业人士回答你的问题</text>
				</view>
			</view>

			<!-- 图片上传区域 -->
			<view class="images-section">
				<view class="section-header">
					<text class="section-title">补充图片</text>
					<text class="optional-mark">可选</text>
				</view>
				<view class="images-grid">
					<!-- 已上传的图片 -->
					<view
						class="image-item"
						v-for="(image, index) in formData.images"
						:key="index"
					>
						<image class="uploaded-image" :src="image.url" mode="aspectFill" @click="previewImage(index)"/>
						<view class="image-actions">
							<text class="action-btn delete-btn" @click="removeImage(index)">✕</text>
						</view>
					</view>

					<!-- 添加图片按钮 -->
					<view
						class="add-image-btn"
						v-if="formData.images.length < 6"
						@click="selectImages"
					>
						<text class="add-icon">📷</text>
						<text class="add-text">添加图片</text>
					</view>
				</view>
				<text class="image-tip">最多可上传6张图片，用于辅助说明问题</text>
			</view>

			<!-- 标签选择区域 -->
			<view class="tags-section">
				<view class="section-header">
					<text class="section-title">相关话题</text>
					<text class="optional-mark">最多选择3个</text>
				</view>

				<!-- 已选标签 -->
				<view class="selected-tags" v-if="formData.selectedTags.length > 0">
					<view
						class="selected-tag"
						v-for="tag in formData.selectedTags"
						:key="tag.id"
						@click="removeTag(tag.id)"
					>
						<text class="tag-name">#{{ tag.name }}</text>
						<text class="remove-icon">✕</text>
					</view>
				</view>

				<!-- 推荐标签 -->
				<view class="hot-tags">
					<view class="tags-title">推荐话题</view>
					<view class="tags-list">
						<view
							class="tag-item"
							v-for="tag in recommendTags"
							:key="tag.id"
							:class="{ 'selected': isTagSelected(tag.id) }"
							@click="toggleTag(tag)"
						>
							<text class="tag-text">#{{ tag.name }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 隐私设置 -->
			<view class="privacy-section">
				<view class="section-header">
					<text class="section-title">隐私设置</text>
				</view>
				<view class="privacy-options">
					<view class="privacy-item" @click="toggleAnonymous">
						<view class="privacy-info">
							<text class="privacy-title">匿名提问</text>
							<text class="privacy-desc">开启后其他用户无法看到你的身份信息</text>
						</view>
						<switch
							:checked="formData.isAnonymous"
							@change="toggleAnonymous"
							color="#f33e54"
						/>
					</view>
				</view>
			</view>

			<!-- 底部间距 -->
			<view class="bottom-space"></view>
		</scroll-view>

		<!-- 发布确认弹窗 -->
		<view class="modal-overlay" v-if="showPublishModal" @click="hidePublishModal">
			<view class="publish-modal" @click.stop>
				<view class="modal-header">
					<text class="modal-title">发布确认</text>
				</view>
				<view class="modal-content">
					<text class="modal-text">确定要发布这个问题吗？</text>
					<view class="question-preview">
						<text class="preview-title">{{ formData.title }}</text>
						<text class="preview-category" v-if="selectedCategory">{{ selectedCategory.name }}</text>
						<text class="preview-reward" v-if="formData.hasReward">悬赏 {{ formData.rewardPoints }} 积分</text>
					</view>
				</view>
				<view class="modal-actions">
					<button class="modal-btn cancel-btn" @click="hidePublishModal">取消</button>
					<button class="modal-btn confirm-btn" @click="confirmPublish">确认发布</button>
				</view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-overlay" v-if="isUploading">
			<view class="loading-content">
				<text class="loading-text">发布中...</text>
				<view class="loading-progress">
					<view class="progress-bar" :style="{ width: uploadProgress + '%' }"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	// 导入必要的工具和API
	import { postApi, tagApi, fileApi } from '../../../utils/api.js'
	import { verifyAndExecute, USER_ROLES } from '../../../utils/auth.js'

	export default {
		data() {
			return {
				// 表单数据
				formData: {
					title: '',           // 问题标题
					content: '',         // 问题描述
					categoryId: null,    // 问题分类ID
					hasReward: false,    // 是否设置悬赏
					rewardPoints: 0,     // 悬赏积分
					isAnonymous: false,  // 是否匿名
					selectedTags: [],    // 已选标签
					images: []           // 上传的图片
				},

				// 问题分类列表
				questionCategories: [
					{ id: 1, name: '职场发展', icon: '💼' },
					{ id: 2, name: '技能学习', icon: '📚' },
					{ id: 3, name: '人际关系', icon: '👥' },
					{ id: 4, name: '工作压力', icon: '😰' },
					{ id: 5, name: '薪资谈判', icon: '💰' },
					{ id: 6, name: '转行跳槽', icon: '🚀' },
					{ id: 7, name: '领导力', icon: '👑' },
					{ id: 8, name: '其他', icon: '❓' }
				],

				// 悬赏选项
				rewardOptions: [
					{ value: 10, label: '10积分' },
					{ value: 20, label: '20积分' },
					{ value: 50, label: '50积分' },
					{ value: 100, label: '100积分' }
				],

				// 推荐标签
				recommendTags: [],

				// 界面状态
				autoFocus: false,
				showPublishModal: false,
				isUploading: false,
				uploadProgress: 0
			}
		},

		computed: {
			/**
			 * 是否可以发布
			 * 需要标题不为空且选择了分类
			 */
			canPublish() {
				return this.formData.title.trim().length > 0 && this.formData.categoryId !== null
			},

			/**
			 * 当前选中的分类
			 */
			selectedCategory() {
				return this.questionCategories.find(cat => cat.id === this.formData.categoryId)
			}
		},

		/**
		 * 页面加载时初始化
		 */
		onLoad() {
			this.initializePage()
		},

		/**
		 * 页面显示时自动聚焦到内容输入框
		 */
		onShow() {
			setTimeout(() => {
				this.autoFocus = true
			}, 300)
		},

		methods: {
			/**
			 * 初始化页面数据
			 */
			async initializePage() {
				try {
					// 加载推荐标签
					await this.loadRecommendTags()
				} catch (error) {
					console.error('页面初始化失败:', error)
				}
			},

			/**
			 * 加载推荐标签
			 */
			async loadRecommendTags() {
				try {
					const response = await tagApi.getHotTags(15)
					if (response.code === 200) {
						this.recommendTags = response.data
					}
				} catch (error) {
					console.error('加载推荐标签失败:', error)
					// 使用默认标签
					this.recommendTags = [
						{ id: 1, name: '职场新人' },
						{ id: 2, name: '求职面试' },
						{ id: 3, name: '职业规划' },
						{ id: 4, name: '工作经验' },
						{ id: 5, name: '团队管理' },
						{ id: 6, name: '技能提升' }
					]
				}
			},

			/**
			 * 标题输入处理
			 */
			onTitleInput() {
				// 可以在这里添加实时校验逻辑
			},

			/**
			 * 内容输入处理
			 */
			onContentInput() {
				// 可以在这里添加实时校验逻辑
			},

			/**
			 * 选择问题分类
			 * @param {number} categoryId - 分类ID
			 */
			selectCategory(categoryId) {
				this.formData.categoryId = categoryId
			},

			/**
			 * 切换悬赏开关
			 */
			toggleReward() {
				this.formData.hasReward = !this.formData.hasReward
				if (!this.formData.hasReward) {
					this.formData.rewardPoints = 0
				} else if (this.formData.rewardPoints === 0) {
					this.formData.rewardPoints = 10
				}
			},

			/**
			 * 选择悬赏积分
			 * @param {number} points - 积分数量
			 */
			selectReward(points) {
				this.formData.rewardPoints = points
			},

			/**
			 * 选择图片
			 */
			selectImages() {
				uni.chooseImage({
					count: 6 - this.formData.images.length,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						this.uploadImages(res.tempFilePaths)
					}
				})
			},

			/**
			 * 上传图片
			 * @param {Array} filePaths - 图片路径数组
			 */
			async uploadImages(filePaths) {
				for (const filePath of filePaths) {
					try {
						uni.showLoading({ title: '上传中...' })

						const imageUrl = await fileApi.uploadImage(filePath, 'post')

						this.formData.images.push({
							url: imageUrl,
							path: filePath
						})
					} catch (error) {
						console.error('图片上传失败:', error)
						uni.showToast({
							title: '图片上传失败',
							icon: 'none'
						})
					} finally {
						uni.hideLoading()
					}
				}
			},

			/**
			 * 预览图片
			 * @param {number} index - 图片索引
			 */
			previewImage(index) {
				const urls = this.formData.images.map(img => img.url)
				uni.previewImage({
					urls: urls,
					current: index
				})
			},

			/**
			 * 移除图片
			 * @param {number} index - 图片索引
			 */
			removeImage(index) {
				this.formData.images.splice(index, 1)
			},

			/**
			 * 检查标签是否已选择
			 * @param {number} tagId - 标签ID
			 * @returns {boolean} 是否已选择
			 */
			isTagSelected(tagId) {
				return this.formData.selectedTags.some(tag => tag.id === tagId)
			},

			/**
			 * 切换标签选择状态
			 * @param {Object} tag - 标签对象
			 */
			toggleTag(tag) {
				const existingIndex = this.formData.selectedTags.findIndex(t => t.id === tag.id)

				if (existingIndex >= 0) {
					// 移除标签
					this.formData.selectedTags.splice(existingIndex, 1)
				} else {
					// 添加标签
					if (this.formData.selectedTags.length < 3) {
						this.formData.selectedTags.push(tag)
					} else {
						uni.showToast({
							title: '最多只能选择3个话题',
							icon: 'none'
						})
					}
				}
			},

			/**
			 * 移除已选标签
			 * @param {number} tagId - 标签ID
			 */
			removeTag(tagId) {
				const index = this.formData.selectedTags.findIndex(tag => tag.id === tagId)
				if (index >= 0) {
					this.formData.selectedTags.splice(index, 1)
				}
			},

			/**
			 * 切换匿名状态
			 */
			toggleAnonymous() {
				this.formData.isAnonymous = !this.formData.isAnonymous
			},

			/**
			 * 处理取消操作
			 */
			handleCancel() {
				// 检查是否有未保存的内容
				if (this.hasUnsavedContent()) {
					uni.showModal({
						title: '确认退出',
						content: '退出后内容将不会保存，确定要退出吗？',
						success: (res) => {
							if (res.confirm) {
								this.navigateBack()
							}
						}
					})
				} else {
					this.navigateBack()
				}
			},

			/**
			 * 检查是否有未保存的内容
			 * @returns {boolean} 是否有未保存内容
			 */
			hasUnsavedContent() {
				return this.formData.title.trim().length > 0 ||
					   this.formData.content.trim().length > 0 ||
					   this.formData.images.length > 0 ||
					   this.formData.selectedTags.length > 0
			},

			/**
			 * 返回上一页
			 */
			navigateBack() {
				uni.navigateBack({
					delta: 1
				})
			},

			/**
			 * 处理发布操作
			 */
			handlePublish() {
				// 验证权限并执行发布
				verifyAndExecute(USER_ROLES.TRIAL, () => {
					if (this.canPublish) {
						this.showPublishModal = true
					} else {
						this.showValidationErrors()
					}
				})
			},

			/**
			 * 显示验证错误信息
			 */
			showValidationErrors() {
				if (!this.formData.title.trim()) {
					uni.showToast({
						title: '请输入问题标题',
						icon: 'none'
					})
					return
				}

				if (!this.formData.categoryId) {
					uni.showToast({
						title: '请选择问题分类',
						icon: 'none'
					})
					return
				}
			},

			/**
			 * 隐藏发布确认弹窗
			 */
			hidePublishModal() {
				this.showPublishModal = false
			},

			/**
			 * 确认发布问题
			 */
			async confirmPublish() {
				try {
					this.isUploading = true
					this.uploadProgress = 0
					this.showPublishModal = false

					// 模拟上传进度
					const progressInterval = setInterval(() => {
						if (this.uploadProgress < 90) {
							this.uploadProgress += 10
						}
					}, 200)

					// 准备发布数据
					const publishData = {
						title: this.formData.title.trim(),
						content: this.formData.content.trim(),
						type: 1, // 问答类型
						categoryId: this.formData.categoryId,
						tagIds: this.formData.selectedTags.map(tag => tag.id),
						imageUrls: this.formData.images.map(img => img.url),
						isAnonymous: this.formData.isAnonymous,
						rewardPoints: this.formData.hasReward ? this.formData.rewardPoints : 0
					}

					// 调用发布API
					const response = await postApi.createPost(publishData)

					clearInterval(progressInterval)
					this.uploadProgress = 100

					if (response.code === 200) {
						uni.showToast({
							title: '发布成功',
							icon: 'success'
						})

						// 跳转到首页
						setTimeout(() => {
							uni.switchTab({
								url: '/pages/tabbar/tabbar-1/tabbar-1'
							})
						}, 1500)
					} else {
						throw new Error(response.message || '发布失败')
					}

				} catch (error) {
					console.error('发布问题失败:', error)
					uni.showToast({
						title: error.message || '发布失败',
						icon: 'none'
					})
				} finally {
					this.isUploading = false
					this.uploadProgress = 0
				}
			}
		}
	}
</script>

<style scoped>
	/* 主容器样式 */
	.qa-container {
		width: 100%;
		height: 100vh;
		background-color: #f5f5f5;
		display: flex;
		flex-direction: column;
	}

	/* 顶部导航栏 */
	.nav-bar {
		height: 88rpx;
		background-color: #fff;
		border-bottom: 1rpx solid #e5e5e5;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 30rpx;
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 999;
	}

	.nav-item {
		min-width: 80rpx;
	}

	.nav-text {
		font-size: 32rpx;
		color: #666;
	}

	.nav-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
	}

	.publish-btn {
		color: #ccc;
		transition: all 0.3s;
	}

	.publish-btn.active {
		color: #f33e54;
		font-weight: bold;
	}

	/* 内容区域 */
	.content-area {
		flex: 1;
		margin-top: 88rpx;
		padding: 30rpx;
	}

	/* 通用部分样式 */
	.section-header {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-right: 10rpx;
	}

	.required-mark {
		color: #f33e54;
		font-size: 30rpx;
		font-weight: bold;
	}

	.optional-mark {
		font-size: 24rpx;
		color: #999;
		background-color: #f8f8f8;
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
	}

	.char-count {
		font-size: 24rpx;
		color: #999;
		text-align: right;
		margin-top: 10rpx;
	}

	.input-tip {
		font-size: 24rpx;
		color: #999;
		margin-top: 10rpx;
		line-height: 1.4;
	}

	/* 标题输入区域 */
	.title-section {
		margin-bottom: 40rpx;
	}

	.title-input {
		width: 100%;
		min-height: 80rpx;
		background-color: #fff;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 32rpx;
		line-height: 1.5;
		border: 2rpx solid #e5e5e5;
		transition: border-color 0.3s;
	}

	.title-input:focus {
		border-color: #f33e54;
	}

	/* 内容输入区域 */
	.content-section {
		margin-bottom: 40rpx;
	}

	.content-input {
		width: 100%;
		min-height: 200rpx;
		background-color: #fff;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 30rpx;
		line-height: 1.6;
		border: 2rpx solid #e5e5e5;
		transition: border-color 0.3s;
	}

	.content-input:focus {
		border-color: #f33e54;
	}

	/* 分类选择区域 */
	.category-section {
		margin-bottom: 40rpx;
	}

	.category-list {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.category-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 150rpx;
		padding: 20rpx 10rpx;
		background-color: #fff;
		border-radius: 12rpx;
		border: 2rpx solid #e5e5e5;
		transition: all 0.3s;
	}

	.category-item.selected {
		border-color: #f33e54;
		background-color: #fff0f1;
	}

	.category-icon {
		font-size: 40rpx;
		margin-bottom: 10rpx;
	}

	.category-name {
		font-size: 26rpx;
		color: #333;
		text-align: center;
	}

	/* 悬赏设置区域 */
	.reward-section {
		margin-bottom: 40rpx;
	}

	.reward-switch {
		margin-left: auto;
	}

	.reward-content {
		margin-top: 20rpx;
	}

	.reward-options {
		display: flex;
		gap: 20rpx;
		margin-bottom: 15rpx;
	}

	.reward-option {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 20rpx;
		background-color: #fff;
		border-radius: 12rpx;
		border: 2rpx solid #e5e5e5;
		min-width: 120rpx;
		transition: all 0.3s;
	}

	.reward-option.selected {
		border-color: #f33e54;
		background-color: #fff0f1;
	}

	.reward-points {
		font-size: 32rpx;
		font-weight: bold;
		color: #f33e54;
		margin-bottom: 5rpx;
	}

	.reward-label {
		font-size: 24rpx;
		color: #666;
	}

	.reward-tip {
		font-size: 24rpx;
		color: #999;
		line-height: 1.4;
	}

	/* 图片上传区域 */
	.images-section {
		margin-bottom: 40rpx;
	}

	.images-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
		margin-bottom: 15rpx;
	}

	.image-item {
		position: relative;
		width: 200rpx;
		height: 200rpx;
		border-radius: 12rpx;
		overflow: hidden;
	}

	.uploaded-image {
		width: 100%;
		height: 100%;
		background-color: #f0f0f0;
	}

	.image-actions {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		display: flex;
		gap: 10rpx;
	}

	.action-btn {
		width: 40rpx;
		height: 40rpx;
		background-color: rgba(0, 0, 0, 0.6);
		color: white;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
	}

	.delete-btn {
		background-color: #f33e54;
	}

	.add-image-btn {
		width: 200rpx;
		height: 200rpx;
		border: 2rpx dashed #ccc;
		border-radius: 12rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background-color: #fafafa;
	}

	.add-icon {
		font-size: 48rpx;
		color: #999;
		margin-bottom: 10rpx;
	}

	.add-text {
		font-size: 24rpx;
		color: #999;
	}

	.image-tip {
		font-size: 24rpx;
		color: #999;
	}

	/* 标签选择区域 */
	.tags-section {
		margin-bottom: 40rpx;
	}

	.selected-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 15rpx;
		margin-bottom: 25rpx;
	}

	.selected-tag {
		display: flex;
		align-items: center;
		padding: 8rpx 16rpx;
		background-color: #f33e54;
		color: white;
		border-radius: 20rpx;
		font-size: 26rpx;
	}

	.tag-name {
		margin-right: 10rpx;
	}

	.remove-icon {
		font-size: 20rpx;
		font-weight: bold;
	}

	.hot-tags {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 25rpx;
	}

	.tags-title {
		font-size: 28rpx;
		color: #333;
		margin-bottom: 20rpx;
		font-weight: bold;
	}

	.tags-list {
		display: flex;
		flex-wrap: wrap;
		gap: 15rpx;
	}

	.tag-item {
		padding: 10rpx 20rpx;
		border: 2rpx solid #e5e5e5;
		border-radius: 20rpx;
		background-color: #fff;
		transition: all 0.3s;
	}

	.tag-item.selected {
		border-color: #f33e54;
		background-color: #fff0f1;
	}

	.tag-text {
		font-size: 26rpx;
		color: #666;
	}

	.tag-item.selected .tag-text {
		color: #f33e54;
	}

	/* 隐私设置区域 */
	.privacy-section {
		margin-bottom: 40rpx;
	}

	.privacy-options {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 25rpx;
	}

	.privacy-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.privacy-info {
		flex: 1;
	}

	.privacy-title {
		font-size: 30rpx;
		color: #333;
		margin-bottom: 8rpx;
		font-weight: bold;
	}

	.privacy-desc {
		font-size: 24rpx;
		color: #999;
		line-height: 1.4;
	}

	/* 发布确认弹窗 */
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
	}

	.publish-modal {
		width: 600rpx;
		background-color: white;
		border-radius: 20rpx;
		overflow: hidden;
	}

	.modal-header {
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		text-align: center;
	}

	.modal-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	.modal-content {
		padding: 30rpx;
	}

	.modal-text {
		font-size: 28rpx;
		color: #666;
		margin-bottom: 20rpx;
		text-align: center;
	}

	.question-preview {
		background-color: #f8f8f8;
		border-radius: 12rpx;
		padding: 20rpx;
	}

	.preview-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 10rpx;
		display: block;
	}

	.preview-category {
		font-size: 24rpx;
		color: #f33e54;
		background-color: #fff0f1;
		padding: 4rpx 12rpx;
		border-radius: 10rpx;
		margin-bottom: 10rpx;
		display: inline-block;
	}

	.preview-reward {
		font-size: 24rpx;
		color: #ff9500;
		background-color: #fff7e6;
		padding: 4rpx 12rpx;
		border-radius: 10rpx;
		display: inline-block;
	}

	.modal-actions {
		display: flex;
		border-top: 1rpx solid #f0f0f0;
	}

	.modal-btn {
		flex: 1;
		height: 88rpx;
		border: none;
		font-size: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.cancel-btn {
		background-color: #f8f8f8;
		color: #666;
	}

	.confirm-btn {
		background-color: #f33e54;
		color: white;
	}

	/* 加载状态 */
	.loading-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.7);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 1001;
	}

	.loading-content {
		background-color: white;
		border-radius: 20rpx;
		padding: 60rpx 80rpx;
		text-align: center;
		min-width: 400rpx;
	}

	.loading-text {
		font-size: 32rpx;
		color: #333;
		margin-bottom: 30rpx;
		font-weight: bold;
	}

	.loading-progress {
		width: 100%;
		height: 8rpx;
		background-color: #f0f0f0;
		border-radius: 4rpx;
		overflow: hidden;
	}

	.progress-bar {
		height: 100%;
		background-color: #f33e54;
		transition: width 0.3s;
	}

	/* 底部间距 */
	.bottom-space {
		height: 100rpx;
	}
</style>
