<!-- 投票发布页面 - 创建投票帖子 -->
<template>
	<view class="poll-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-item" @click="handleCancel">
				<text class="nav-text">取消</text>
			</view>
			<view class="nav-title">发起投票</view>
			<view class="nav-item" @click="handlePublish">
				<text class="nav-text publish-btn" :class="{ 'active': canPublish }">发布</text>
			</view>
		</view>

		<!-- 内容编辑区域 -->
		<scroll-view class="content-area" scroll-y="true">
			<!-- 投票标题输入 -->
			<view class="title-section">
				<view class="section-header">
					<text class="section-title">投票标题</text>
					<text class="required-mark">*</text>
				</view>
				<textarea
					class="title-input"
					placeholder="请输入投票主题..."
					v-model="formData.title"
					:maxlength="100"
					auto-height
					@input="onTitleInput"
				/>
				<text class="char-count">{{ formData.title.length }}/100</text>
				<text class="input-tip">清晰的标题有助于获得更多参与</text>
			</view>

			<!-- 投票描述输入 -->
			<view class="content-section">
				<view class="section-header">
					<text class="section-title">投票说明</text>
					<text class="optional-mark">可选</text>
				</view>
				<textarea
					class="content-input"
					placeholder="详细描述投票背景、目的等..."
					v-model="formData.content"
					:maxlength="2000"
					auto-height
					@input="onContentInput"
					:focus="autoFocus"
				/>
				<text class="char-count">{{ formData.content.length }}/2000</text>
				<text class="input-tip">提供更多信息有助于用户做出选择</text>
			</view>

			<!-- 投票选项输入 -->
			<view class="options-section">
				<view class="section-header">
					<text class="section-title">投票选项</text>
					<text class="required-mark">*</text>
					<text class="option-count">{{ formData.pollOptions.length }}/5</text>
				</view>
				<text class="input-tip option-tip">至少需要2个选项,最多5个选项</text>

				<!-- 已添加的选项 -->
				<view class="options-list">
					<view
						class="option-item"
						v-for="(option, index) in formData.pollOptions"
						:key="index"
					>
						<view class="option-number">{{ index + 1 }}</view>
						<input
							class="option-input"
							v-model="formData.pollOptions[index]"
							:placeholder="'选项 ' + (index + 1)"
							maxlength="50"
							@input="onOptionInput(index)"
						/>
						<view class="option-actions">
							<text class="action-icon delete-icon" @click="removeOption(index)" v-if="formData.pollOptions.length > 2">✕</text>
						</view>
					</view>
				</view>

				<!-- 添加选项按钮 -->
				<view
					class="add-option-btn"
					v-if="formData.pollOptions.length < 5"
					@click="addOption"
				>
					<text class="add-icon">+</text>
					<text class="add-text">添加选项</text>
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
						v-if="formData.images.length < 3"
						@click="selectImages"
					>
						<text class="add-icon">📷</text>
						<text class="add-text">添加图片</text>
					</view>
				</view>
				<text class="image-tip">最多可上传3张图片,用于辅助说明投票内容</text>
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

				<!-- 所有可用标签 -->
				<view class="available-tags">
					<view class="tags-title">可选话题</view>
					<view class="tags-list" v-if="allTags.length > 0">
						<view
							class="tag-item"
							v-for="tag in allTags"
							:key="tag.id"
							:class="{ 'selected': isTagSelected(tag.id) }"
							@click="toggleTag(tag)"
						>
							<text class="tag-text">#{{ tag.name }}</text>
						</view>
					</view>
					<view class="no-result" v-else>
						<text class="no-result-text">暂无可选话题</text>
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
							<text class="privacy-title">匿名发起</text>
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
					<text class="modal-text">确定要发布这个投票吗?</text>
					<view class="poll-preview">
						<text class="preview-title">{{ formData.title }}</text>
						<view class="preview-options">
							<text class="preview-option" v-for="(option, index) in formData.pollOptions" :key="index">
								{{ index + 1 }}. {{ option }}
							</text>
						</view>
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
	/**
	 * 投票发布页面 - TabBar第3页的详情页
	 *
	 * 提供投票帖发布功能:
	 * - 投票标题和说明
	 * - 2-5个投票选项
	 * - 可选图片(最多3张)
	 * - 可选标签(最多3个)
	 * - 匿名发起开关
	 *
	 * 后端接口:POST /api/posts
	 * 字段要求:type=1, pollOptions=[...], 其他字段同普通帖子
	 */

	// 导入必要的工具和API
	import { postApi, tagApi, fileApi } from '../../../utils/api.js'
	import { verifyAndExecute, USER_ROLES } from '../../../utils/auth.js'

	export default {
		data() {
			return {
				// 表单数据
				formData: {
					title: '',           // 投票标题
					content: '',         // 投票说明
					pollOptions: ['', ''], // 投票选项列表(默认2个空选项)
					isAnonymous: false,  // 是否匿名
					selectedTags: [],    // 已选标签
					images: []           // 上传的图片
				},

				// 推荐标签
				recommendTags: [],
				allTags: [],              // 所有可用标签

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
			 * 需要标题不为空且至少有2个非空选项
			 */
			canPublish() {
				const hasTitle = this.formData.title.trim().length > 0
				const validOptions = this.formData.pollOptions.filter(opt => opt.trim().length > 0)
				return hasTitle && validOptions.length >= 2
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
					// 加载推荐标签和所有标签
					await this.loadRecommendTags()
					await this.loadAllTags()
				} catch (error) {
					console.error('页面初始化失败:', error)
				}
			},

			/**
			 * 加载推荐标签
			 */
			async loadRecommendTags() {
				try {
					// request.js已解包Result对象
					const result = await tagApi.getHotTags(15)
					this.recommendTags = result
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
			 * 加载所有标签(用于搜索)
			 */
			async loadAllTags() {
				try {
					// request.js 已经解包了 Result,直接返回 PageResult 对象
					const pageResult = await tagApi.getTagList({
						current: 1,
						size: 100 // 加载前100个标签供搜索
					})
					if (pageResult && Array.isArray(pageResult.records)) {
						this.allTags = pageResult.records
					}
				} catch (error) {
					console.error('加载标签列表失败:', error)
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
			 * 选项输入处理
			 * @param {number} index - 选项索引
			 */
			onOptionInput(index) {
				// 可以在这里添加实时校验逻辑
			},

			/**
			 * 添加投票选项
			 */
			addOption() {
				if (this.formData.pollOptions.length < 5) {
					this.formData.pollOptions.push('')
				} else {
					uni.showToast({
						title: '最多只能添加5个选项',
						icon: 'none'
					})
				}
			},

			/**
			 * 移除投票选项
			 * @param {number} index - 选项索引
			 */
			removeOption(index) {
				if (this.formData.pollOptions.length > 2) {
					this.formData.pollOptions.splice(index, 1)
				} else {
					uni.showToast({
						title: '至少需要保留2个选项',
						icon: 'none'
					})
				}
			},

			/**
			 * 选择图片
			 */
			selectImages() {
				uni.chooseImage({
					count: 3 - this.formData.images.length,
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
						content: '退出后内容将不会保存,确定要退出吗?',
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
				const hasOptions = this.formData.pollOptions.some(opt => opt.trim().length > 0)
				return this.formData.title.trim().length > 0 ||
					   this.formData.content.trim().length > 0 ||
					   hasOptions ||
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
						title: '请输入投票标题',
						icon: 'none'
					})
					return
				}

				const validOptions = this.formData.pollOptions.filter(opt => opt.trim().length > 0)
				if (validOptions.length < 2) {
					uni.showToast({
						title: '至少需要2个投票选项',
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
			 * 确认发布投票
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

					// 过滤空选项
					const validOptions = this.formData.pollOptions
						.map(opt => opt.trim())
						.filter(opt => opt.length > 0)

					if (validOptions.length < 2 || validOptions.length > 5) {
						throw new Error('投票选项必须在2-5个之间')
					}

					// 准备发布数据
					const publishData = {
						title: this.formData.title.trim(),
						content: this.formData.content.trim(),
						type: 1, // 投票类型
						tagIds: this.formData.selectedTags.map(tag => tag.id),
						imageUrls: this.formData.images.map(img => img.url),
						isAnonymous: this.formData.isAnonymous,
						pollOptions: validOptions // 投票选项列表
					}

					// 调用发布API
					// request.js已解包Result对象,直接返回Post对象
					const result = await postApi.createPost(publishData)

					clearInterval(progressInterval)
					this.uploadProgress = 100

					// 如果执行到这里说明API调用成功(失败会在拦截器抛出异常)
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

				} catch (error) {
					console.error('发布投票失败:', error)
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
	/* 主容器样式 - 参照图文发布页的布局模式 */
	.poll-container {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f5f5;
	}

	/* 顶部导航栏 */
	.nav-bar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx 30rpx;
		border-bottom: 2rpx solid #f0f0f0;
		background-color: #fff;
		z-index: 100;
	}

	.nav-item {
		min-width: 100rpx;
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

	/* 内容区域 - 使用flex: 1而不是固定高度,移除padding通过section的margin控制间距 */
	.content-area {
		flex: 1;
		/* 不设置padding,通过各个section的margin来控制间距,避免width: 100%溢出 */
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

	.option-count {
		margin-left: auto;
		font-size: 24rpx;
		color: #999;
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

	.option-tip {
		margin-bottom: 15rpx;
	}

	/* 标题输入区域 - 通过margin控制外部间距 */
	.title-section {
		margin: 30rpx 30rpx 40rpx 30rpx;  /* 顶部、左右30rpx,底部40rpx */
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
		box-sizing: border-box;  /* 确保padding和border不会导致元素溢出 */
	}

	.title-input:focus {
		border-color: #f33e54;
	}

	/* 内容输入区域 - 通过margin控制外部间距 */
	.content-section {
		margin: 0 30rpx 40rpx 30rpx;  /* 左右30rpx,底部40rpx */
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
		box-sizing: border-box;  /* 确保padding和border不会导致元素溢出 */
	}

	.content-input:focus {
		border-color: #f33e54;
	}

	/* 投票选项区域 - 通过margin控制外部间距 */
	.options-section {
		margin: 0 30rpx 40rpx 30rpx;  /* 左右30rpx,底部40rpx */
	}

	.options-list {
		display: flex;
		flex-direction: column;
		gap: 15rpx;
		margin-bottom: 20rpx;
	}

	.option-item {
		display: flex;
		align-items: center;
		background-color: #fff;
		border-radius: 12rpx;
		padding: 20rpx;
		border: 2rpx solid #e5e5e5;
		box-sizing: border-box;  /* 确保padding和border不会导致元素溢出 */
	}

	.option-number {
		width: 50rpx;
		height: 50rpx;
		background-color: #f33e54;
		color: white;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		font-weight: bold;
		margin-right: 20rpx;
		flex-shrink: 0;
	}

	.option-input {
		flex: 1;
		font-size: 30rpx;
		color: #333;
	}

	.option-actions {
		margin-left: 20rpx;
		flex-shrink: 0;
	}

	.action-icon {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: #999;
	}

	.delete-icon {
		color: #f33e54;
	}

	.add-option-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 25rpx;
		background-color: #fff;
		border-radius: 12rpx;
		border: 2rpx dashed #ccc;
		box-sizing: border-box;  /* 确保padding和border不会导致元素溢出 */
	}

	.add-icon {
		font-size: 36rpx;
		color: #999;
		margin-right: 10rpx;
	}

	.add-text {
		font-size: 28rpx;
		color: #999;
	}

	/* 图片上传区域 - 通过margin控制外部间距 */
	.images-section {
		margin: 0 30rpx 40rpx 30rpx;  /* 左右30rpx,底部40rpx */
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

	/* 标签选择区域 - 通过margin控制外部间距 */
	.tags-section {
		margin: 0 30rpx 40rpx 30rpx;  /* 左右30rpx,底部40rpx */
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

	/* 可用标签列表样式 */
	.available-tags {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 25rpx;
		margin-bottom: 20rpx;
		box-sizing: border-box;  /* 确保padding不会导致元素溢出 */

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

		.no-result {
			padding: 40rpx 0;
			text-align: center;

			.no-result-text {
				display: block;
				font-size: 28rpx;
				color: #999;
			}
		}
	}

	.tag-item {
		padding: 10rpx 20rpx;
		border: 2rpx solid #e5e5e5;
		border-radius: 20rpx;
		background-color: #fff;
		transition: all 0.3s;
		box-sizing: border-box;  /* 确保padding和border不会导致元素溢出 */
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

	/* 隐私设置区域 - 通过margin控制外部间距 */
	.privacy-section {
		margin: 0 30rpx 40rpx 30rpx;  /* 左右30rpx,底部40rpx */
	}

	.privacy-options {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 25rpx;
		box-sizing: border-box;  /* 确保padding不会导致元素溢出 */
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

	.poll-preview {
		background-color: #f8f8f8;
		border-radius: 12rpx;
		padding: 20rpx;
	}

	.preview-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 15rpx;
		display: block;
	}

	.preview-options {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.preview-option {
		font-size: 26rpx;
		color: #666;
		line-height: 1.5;
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
