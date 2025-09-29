<!-- 发图文页面 - 创建图文帖子 -->
<template>
	<view class="release-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-item" @click="handleCancel">
				<text class="nav-text">取消</text>
			</view>
			<view class="nav-title">发图文</view>
			<view class="nav-item" @click="handlePublish">
				<text class="nav-text publish-btn" :class="{ 'active': canPublish }">发布</text>
			</view>
		</view>

		<!-- 内容编辑区域 -->
		<scroll-view class="content-area" scroll-y="true">
			<!-- 标题输入 -->
			<view class="title-section">
				<textarea
					class="title-input"
					placeholder="请输入标题（可选）"
					v-model="formData.title"
					:maxlength="100"
					auto-height
					@input="onTitleInput"
				/>
				<text class="char-count">{{ formData.title.length }}/100</text>
			</view>

			<!-- 内容输入 -->
			<view class="content-section">
				<textarea
					class="content-input"
					placeholder="分享你的想法..."
					v-model="formData.content"
					:maxlength="2000"
					auto-height
					@input="onContentInput"
					:focus="autoFocus"
				/>
				<text class="char-count">{{ formData.content.length }}/2000</text>
			</view>

			<!-- 图片上传区域 -->
			<view class="images-section">
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
						v-if="formData.images.length < 9"
						@click="selectImages"
					>
						<text class="add-icon">📷</text>
						<text class="add-text">添加图片</text>
					</view>
				</view>
				<text class="image-tip">最多可上传9张图片</text>
			</view>

			<!-- 标签选择区域 -->
			<view class="tags-section">
				<view class="section-title">
					<text class="title-text">选择话题</text>
					<text class="section-subtitle">最多选择5个话题</text>
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

				<!-- 热门标签 -->
				<view class="hot-tags">
					<view class="tags-title">热门话题</view>
					<view class="tags-list">
						<view
							class="tag-item"
							v-for="tag in hotTags"
							:key="tag.id"
							:class="{ 'selected': isTagSelected(tag.id) }"
							@click="toggleTag(tag)"
						>
							#{{ tag.name }}
						</view>
					</view>
				</view>

				<!-- 创建新标签 -->
				<view class="create-tag">
					<input
						class="tag-input"
						placeholder="创建新话题"
						v-model="newTagName"
						@confirm="createNewTag"
						:maxlength="20"
					/>
					<text class="create-btn" @click="createNewTag" v-if="newTagName.trim()">创建</text>
				</view>
			</view>

			<!-- 发布设置 -->
			<view class="settings-section">
				<view class="setting-item">
					<text class="setting-label">匿名发布</text>
					<switch :checked="formData.isAnonymous" @change="onAnonymousChange" />
				</view>
			</view>
		</scroll-view>

		<!-- 底部工具栏 -->
		<view class="toolbar">
			<view class="tool-item" @click="selectImages">
				<text class="tool-icon">📷</text>
				<text class="tool-text">图片</text>
			</view>

			<view class="tool-item" @click="openEmojiPanel">
				<text class="tool-icon">😊</text>
				<text class="tool-text">表情</text>
			</view>

			<view class="tool-item" @click="saveDraft">
				<text class="tool-icon">💾</text>
				<text class="tool-text">草稿</text>
			</view>
		</view>

		<!-- 表情面板 -->
		<view class="emoji-panel" v-if="showEmojiPanel" @click="closeEmojiPanel">
			<view class="emoji-content" @click.stop>
				<view class="emoji-grid">
					<text
						class="emoji-item"
						v-for="emoji in emojiList"
						:key="emoji"
						@click="insertEmoji(emoji)"
					>
						{{ emoji }}
					</text>
				</view>
			</view>
		</view>

		<!-- 加载遮罩 -->
		<view class="loading-mask" v-if="isPublishing">
			<view class="loading-content">
				<text class="loading-text">发布中...</text>
			</view>
		</view>
	</view>
</template>

<script>
/**
 * 发图文页面 - 创建图文帖子
 *
 * 功能特性：
 * - 标题和内容编辑
 * - 图片上传（最多9张）
 * - 话题标签选择和创建
 * - 匿名发布选项
 * - 草稿保存功能
 * - 表情符号插入
 * - 字数统计限制
 */

import { postApi, tagApi, fileApi } from '../../../utils/api.js'
import { verifyAndExecute, USER_ROLES } from '../../../utils/auth.js'

export default {
	data() {
		return {
			// 表单数据
			formData: {
				title: '',           // 标题
				content: '',         // 内容
				images: [],          // 图片列表 [{ url, file }]
				selectedTags: [],    // 选中的标签
				isAnonymous: false   // 是否匿名
			},

			// 标签相关
			hotTags: [],           // 热门标签
			newTagName: '',        // 新建标签名称

			// UI状态
			autoFocus: true,       // 自动聚焦
			isPublishing: false,   // 发布中状态
			showEmojiPanel: false, // 显示表情面板

			// 表情列表
			emojiList: [
				'😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
				'🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
				'😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
				'🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
				'😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
				'🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗'
			]
		}
	},

	computed: {
		/**
		 * 是否可以发布
		 * 需要有内容或图片
		 */
		canPublish() {
			return this.formData.content.trim() || this.formData.images.length > 0
		}
	},

	onLoad(options) {
		// 加载热门标签
		this.loadHotTags()

		// 如果有草稿ID，加载草稿
		if (options.draftId) {
			this.loadDraft(options.draftId)
		}
	},

	methods: {
		/**
		 * 加载热门标签
		 */
		async loadHotTags() {
			try {
				this.hotTags = await tagApi.getHotTags(20)
			} catch (error) {
				console.warn('加载热门标签失败:', error)
			}
		},

		/**
		 * 标题输入处理
		 */
		onTitleInput(e) {
			this.formData.title = e.detail.value
		},

		/**
		 * 内容输入处理
		 */
		onContentInput(e) {
			this.formData.content = e.detail.value
		},

		/**
		 * 选择图片
		 */
		selectImages() {
			const remainingCount = 9 - this.formData.images.length
			if (remainingCount <= 0) {
				uni.showToast({
					title: '最多只能上传9张图片',
					icon: 'none'
				})
				return
			}

			uni.chooseImage({
				count: remainingCount,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.uploadImages(res.tempFilePaths)
				}
			})
		},

		/**
		 * 上传图片
		 * @param {Array} filePaths - 图片文件路径数组
		 */
		async uploadImages(filePaths) {
			// 显示上传进度
			uni.showLoading({
				title: '上传中...'
			})

			try {
				for (let filePath of filePaths) {
					const imageUrl = await fileApi.uploadImage(filePath, 'post')
					this.formData.images.push({
						url: imageUrl,
						file: filePath
					})
				}

				uni.hideLoading()
				uni.showToast({
					title: '上传成功',
					icon: 'success'
				})

			} catch (error) {
				uni.hideLoading()
				console.error('图片上传失败:', error)
				uni.showToast({
					title: '上传失败',
					icon: 'none'
				})
			}
		},

		/**
		 * 移除图片
		 * @param {number} index - 图片索引
		 */
		removeImage(index) {
			this.formData.images.splice(index, 1)
		},

		/**
		 * 预览图片
		 * @param {number} current - 当前图片索引
		 */
		previewImage(current) {
			const urls = this.formData.images.map(img => img.url)
			uni.previewImage({
				urls,
				current
			})
		},

		/**
		 * 切换标签选择状态
		 * @param {Object} tag - 标签对象
		 */
		toggleTag(tag) {
			const isSelected = this.isTagSelected(tag.id)

			if (isSelected) {
				this.removeTag(tag.id)
			} else {
				if (this.formData.selectedTags.length >= 5) {
					uni.showToast({
						title: '最多只能选择5个话题',
						icon: 'none'
					})
					return
				}
				this.formData.selectedTags.push(tag)
			}
		},

		/**
		 * 检查标签是否已选中
		 * @param {number} tagId - 标签ID
		 */
		isTagSelected(tagId) {
			return this.formData.selectedTags.some(tag => tag.id === tagId)
		},

		/**
		 * 移除选中的标签
		 * @param {number} tagId - 标签ID
		 */
		removeTag(tagId) {
			this.formData.selectedTags = this.formData.selectedTags.filter(tag => tag.id !== tagId)
		},

		/**
		 * 创建新标签
		 */
		async createNewTag() {
			const tagName = this.newTagName.trim()
			if (!tagName) return

			// 检查是否已存在
			if (this.hotTags.some(tag => tag.name === tagName)) {
				uni.showToast({
					title: '话题已存在',
					icon: 'none'
				})
				return
			}

			try {
				const newTag = await tagApi.createTag({
					name: tagName,
					description: `用户创建的话题：${tagName}`
				})

				// 添加到热门标签列表
				this.hotTags.unshift(newTag)

				// 自动选中新创建的标签
				if (this.formData.selectedTags.length < 5) {
					this.formData.selectedTags.push(newTag)
				}

				// 清空输入框
				this.newTagName = ''

				uni.showToast({
					title: '话题创建成功',
					icon: 'success'
				})

			} catch (error) {
				console.error('创建标签失败:', error)
				uni.showToast({
					title: '创建失败',
					icon: 'none'
				})
			}
		},

		/**
		 * 匿名状态改变
		 */
		onAnonymousChange(e) {
			this.formData.isAnonymous = e.detail.value
		},

		/**
		 * 打开表情面板
		 */
		openEmojiPanel() {
			this.showEmojiPanel = true
		},

		/**
		 * 关闭表情面板
		 */
		closeEmojiPanel() {
			this.showEmojiPanel = false
		},

		/**
		 * 插入表情
		 * @param {string} emoji - 表情符号
		 */
		insertEmoji(emoji) {
			this.formData.content += emoji
			this.closeEmojiPanel()
		},

		/**
		 * 保存草稿
		 */
		async saveDraft() {
			if (!this.canPublish) {
				uni.showToast({
					title: '没有可保存的内容',
					icon: 'none'
				})
				return
			}

			try {
				// 保存到本地存储
				const draftData = {
					...this.formData,
					createdAt: new Date().toISOString()
				}

				uni.setStorageSync('postDraft', draftData)

				uni.showToast({
					title: '草稿已保存',
					icon: 'success'
				})

			} catch (error) {
				console.error('保存草稿失败:', error)
				uni.showToast({
					title: '保存失败',
					icon: 'none'
				})
			}
		},

		/**
		 * 加载草稿
		 * @param {string} draftId - 草稿ID
		 */
		loadDraft(draftId) {
			try {
				const draftData = uni.getStorageSync('postDraft')
				if (draftData) {
					this.formData = { ...this.formData, ...draftData }
				}
			} catch (error) {
				console.warn('加载草稿失败:', error)
			}
		},

		/**
		 * 处理取消操作
		 */
		handleCancel() {
			if (this.canPublish) {
				uni.showModal({
					title: '确认退出',
					content: '当前有未保存的内容，确定要退出吗？',
					confirmText: '保存草稿',
					cancelText: '直接退出',
					success: (res) => {
						if (res.confirm) {
							this.saveDraft()
						}
						uni.navigateBack()
					}
				})
			} else {
				uni.navigateBack()
			}
		},

		/**
		 * 处理发布操作
		 */
		async handlePublish() {
			if (!this.canPublish) {
				uni.showToast({
					title: '请输入内容或添加图片',
					icon: 'none'
				})
				return
			}

			// 需要登录才能发布
			verifyAndExecute(USER_ROLES.TRIAL, async () => {
				await this.publishPost()
			}, {
				loginPrompt: '请先登录后再发布',
				showTrialUpgrade: true
			})
		},

		/**
		 * 发布帖子
		 */
		async publishPost() {
			this.isPublishing = true

			try {
				// 构建发布数据
				const postData = {
					title: this.formData.title.trim(),
					content: this.formData.content.trim(),
					type: 0,  // 普通帖子
					tagIds: this.formData.selectedTags.map(tag => tag.id),
					imageUrls: this.formData.images.map(img => img.url),
					isAnonymous: this.formData.isAnonymous
				}

				// 发布帖子
				const result = await postApi.createPost(postData)

				// 清除草稿
				uni.removeStorageSync('postDraft')

				// 提示成功
				uni.showToast({
					title: '发布成功',
					icon: 'success'
				})

				// 延迟跳转到帖子详情或首页
				setTimeout(() => {
					uni.switchTab({
						url: '/pages/tabbar/tabbar-1/tabbar-1'
					})
				}, 1500)

			} catch (error) {
				console.error('发布失败:', error)
				uni.showToast({
					title: '发布失败，请重试',
					icon: 'none'
				})
			} finally {
				this.isPublishing = false
			}
		}
	}
}
</script>

<style lang="scss" scoped>
/* 页面容器 */
.release-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #fff;
}

/* 顶部导航栏 */
.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20upx 30upx;
	border-bottom: 2upx solid #f0f0f0;
	background-color: #fff;

	.nav-item {
		min-width: 100upx;

		.nav-text {
			font-size: 32upx;
			color: #666;

			&.publish-btn {
				color: #ccc;

				&.active {
					color: #007aff;
				}
			}
		}
	}

	.nav-title {
		font-size: 36upx;
		font-weight: bold;
		color: #333;
	}
}

/* 内容区域 */
.content-area {
	flex: 1;
	/* 移除padding，通过各个section的margin来控制间距，避免width: 100%溢出 */
}

/* 标题输入区域 */
.title-section {
	margin: 30upx 30upx 40upx 30upx; /* 顶部、左右30upx间距，底部40upx */

	.title-input {
		width: 100%;
		padding: 20upx 20upx 50upx 20upx; /* 底部留出空间给字符计数 */
		background-color: #f8f8f8;
		border-radius: 12upx;
		font-size: 32upx;
		line-height: 1.5;
		min-height: 80upx;
		box-sizing: border-box; /* 确保padding不会导致溢出 */
	}

	.char-count {
		display: block;
		text-align: right;
		margin-top: 10upx;
		font-size: 24upx;
		color: #999;
		padding-right: 10upx;
	}
}

/* 内容输入区域 */
.content-section {
	margin: 0 30upx 40upx 30upx; /* 左右30upx间距，底部40upx */

	.content-input {
		width: 100%;
		padding: 20upx 20upx 50upx 20upx; /* 底部留出空间给字符计数 */
		background-color: #f8f8f8;
		border-radius: 12upx;
		font-size: 28upx;
		line-height: 1.6;
		min-height: 200upx;
		box-sizing: border-box; /* 确保padding不会导致溢出 */
	}

	.char-count {
		display: block;
		text-align: right;
		margin-top: 10upx;
		font-size: 24upx;
		color: #999;
		padding-right: 10upx;
	}
}

/* 图片区域 */
.images-section {
	margin: 0 30upx 40upx 30upx; /* 左右30upx间距，底部40upx */

	.images-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 15upx;
		margin-bottom: 15upx;
	}

	.image-item {
		position: relative;
		width: 200upx;
		height: 200upx;

		.uploaded-image {
			width: 100%;
			height: 100%;
			border-radius: 12upx;
		}

		.image-actions {
			position: absolute;
			top: 10upx;
			right: 10upx;

			.action-btn {
				display: inline-block;
				width: 40upx;
				height: 40upx;
				line-height: 40upx;
				text-align: center;
				border-radius: 50%;
				font-size: 24upx;

				&.delete-btn {
					background-color: rgba(0, 0, 0, 0.6);
					color: #fff;
				}
			}
		}
	}

	.add-image-btn {
		width: 200upx;
		height: 200upx;
		border: 2upx dashed #ddd;
		border-radius: 12upx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;

		.add-icon {
			font-size: 40upx;
			margin-bottom: 10upx;
		}

		.add-text {
			font-size: 24upx;
			color: #999;
		}
	}

	.image-tip {
		font-size: 24upx;
		color: #999;
	}
}

/* 标签区域 */
.tags-section {
	margin: 0 30upx 40upx 30upx; /* 左右30upx间距，底部40upx */

	.section-title {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 20upx;
		flex-wrap: wrap; /* 允许换行防止溢出 */

		.title-text {
			font-size: 32upx;
			font-weight: bold;
			color: #333;
			margin-right: 20upx; /* 确保与副标题间距 */
		}

		.section-subtitle {
			font-size: 24upx;
			color: #999;
			white-space: nowrap; /* 防止文字断行 */
			flex-shrink: 0; /* 防止被挤压 */
		}
	}

	.selected-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 15upx;
		margin-bottom: 30upx;

		.selected-tag {
			display: flex;
			align-items: center;
			padding: 12upx 20upx;
			background-color: #e8f4ff;
			border-radius: 20upx;

			.tag-name {
				font-size: 26upx;
				color: #007aff;
				margin-right: 10upx;
			}

			.remove-icon {
				font-size: 20upx;
				color: #999;
			}
		}
	}

	.hot-tags {
		margin-bottom: 30upx;

		.tags-title {
			font-size: 28upx;
			color: #666;
			margin-bottom: 15upx;
		}

		.tags-list {
			display: flex;
			flex-wrap: wrap;
			gap: 15upx;

			.tag-item {
				padding: 12upx 20upx;
				background-color: #f8f8f8;
				border-radius: 20upx;
				font-size: 26upx;
				color: #666;
				transition: all 0.3s;
				max-width: calc(50% - 7.5upx); /* 限制最大宽度防止溢出 */
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
				box-sizing: border-box;

				&.selected {
					background-color: #e8f4ff;
					color: #007aff;
				}
			}
		}
	}

	.create-tag {
		display: flex;
		align-items: center;
		gap: 20upx;

		.tag-input {
			flex: 1;
			padding: 15upx 20upx;
			background-color: #f8f8f8;
			border-radius: 25upx;
			font-size: 26upx;
			box-sizing: border-box; /* 确保padding不会导致溢出 */
			min-width: 0; /* 允许flex项目收缩 */
		}

		.create-btn {
			padding: 15upx 30upx;
			background-color: #007aff;
			color: #fff;
			border-radius: 25upx;
			font-size: 26upx;
			flex-shrink: 0; /* 防止按钮被挤压 */
			white-space: nowrap;
		}
	}
}

/* 设置区域 */
.settings-section {
	margin: 0 30upx 40upx 30upx; /* 左右30upx间距，底部40upx */

	.setting-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20upx 0;

		.setting-label {
			font-size: 28upx;
			color: #333;
		}
	}
}

/* 底部工具栏 */
.toolbar {
	display: flex;
	align-items: center;
	justify-content: space-around;
	padding: 20upx 30upx;
	border-top: 2upx solid #f0f0f0;
	background-color: #fff;

	.tool-item {
		display: flex;
		flex-direction: column;
		align-items: center;

		.tool-icon {
			font-size: 40upx;
			margin-bottom: 8upx;
		}

		.tool-text {
			font-size: 22upx;
			color: #666;
		}
	}
}

/* 表情面板 */
.emoji-panel {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: flex-end;
	z-index: 1000;

	.emoji-content {
		width: 100%;
		max-height: 60vh;
		background-color: #fff;
		border-radius: 20upx 20upx 0 0;
		padding: 30upx;

		.emoji-grid {
			display: flex;
			flex-wrap: wrap;
			gap: 20upx;

			.emoji-item {
				font-size: 50upx;
				padding: 10upx;
				text-align: center;
				line-height: 1;
			}
		}
	}
}

/* 加载遮罩 */
.loading-mask {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1001;

	.loading-content {
		padding: 40upx 60upx;
		background-color: rgba(0, 0, 0, 0.8);
		border-radius: 12upx;

		.loading-text {
			font-size: 28upx;
			color: #fff;
		}
	}
}
</style>