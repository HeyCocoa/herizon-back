<!-- 发视频页面 - 创建视频帖子 -->
<template>
	<view class="video-release-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-item" @click="handleCancel">
				<text class="nav-text">取消</text>
			</view>
			<view class="nav-title">发视频</view>
			<view class="nav-item" @click="handlePublish">
				<text class="nav-text publish-btn" :class="{ 'active': canPublish }">发布</text>
			</view>
		</view>

		<!-- 内容编辑区域 -->
		<scroll-view class="content-area" scroll-y="true">
			<!-- 视频上传区域 -->
			<view class="video-section">
				<view class="section-title">视频内容</view>

				<!-- 视频预览区域 -->
				<view class="video-preview" v-if="formData.videoUrl">
					<video
						class="video-player"
						:src="formData.videoUrl"
						:poster="formData.videoCover"
						controls
						@loadedmetadata="onVideoLoaded"
						@error="onVideoError"
					/>
					<view class="video-info">
						<text class="video-duration">{{ formatDuration(videoDuration) }}</text>
						<text class="video-size">{{ formatFileSize(videoSize) }}</text>
					</view>
					<view class="video-actions">
						<text class="action-btn" @click="selectVideoCover">选择封面</text>
						<text class="action-btn delete-btn" @click="removeVideo">重新选择</text>
					</view>
				</view>

				<!-- 视频上传按钮 -->
				<view class="video-upload" v-else>
					<view class="upload-area" @click="selectVideo">
						<text class="upload-icon">🎬</text>
						<text class="upload-text">选择视频</text>
						<text class="upload-tip">支持MP4格式，最大100MB</text>
					</view>
				</view>

				<!-- 上传进度 -->
				<view class="upload-progress" v-if="uploadProgress > 0 && uploadProgress < 100">
					<view class="progress-bar">
						<view class="progress-fill" :style="{ width: uploadProgress + '%' }"></view>
					</view>
					<text class="progress-text">上传中 {{ uploadProgress }}%</text>
				</view>
			</view>

			<!-- 封面选择区域 -->
			<view class="cover-section" v-if="formData.videoUrl">
				<view class="section-title">选择封面</view>
				<scroll-view class="cover-list" scroll-x="true" show-scrollbar="false">
					<!-- 自动生成的封面 -->
					<view
						class="cover-item"
						v-for="(cover, index) in autoCoverList"
						:key="index"
						:class="{ 'selected': formData.videoCover === cover }"
						@click="selectCover(cover)"
					>
						<image class="cover-image" :src="cover" mode="aspectFill" />
						<text class="cover-label">自动</text>
					</view>

					<!-- 自定义上传封面 -->
					<view class="cover-item upload-cover" @click="uploadCustomCover">
						<text class="upload-cover-icon">📷</text>
						<text class="upload-cover-text">自定义</text>
					</view>
				</scroll-view>
			</view>

			<!-- 标题输入 -->
			<view class="title-section">
				<view class="section-title">标题</view>
				<textarea
					class="title-input"
					placeholder="输入视频标题"
					v-model="formData.title"
					:maxlength="100"
					auto-height
					@input="onTitleInput"
				/>
				<text class="char-count">{{ formData.title.length }}/100</text>
			</view>

			<!-- 描述输入 -->
			<view class="description-section">
				<view class="section-title">描述</view>
				<textarea
					class="description-input"
					placeholder="描述一下你的视频..."
					v-model="formData.description"
					:maxlength="500"
					auto-height
					@input="onDescriptionInput"
				/>
				<text class="char-count">{{ formData.description.length }}/500</text>
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

				<view class="setting-item">
					<text class="setting-label">允许下载</text>
					<switch :checked="formData.allowDownload" @change="onDownloadChange" />
				</view>
			</view>
		</scroll-view>

		<!-- 加载遮罩 -->
		<view class="loading-mask" v-if="isUploading || isPublishing">
			<view class="loading-content">
				<text class="loading-text" v-if="isUploading">视频上传中...</text>
				<text class="loading-text" v-else-if="isPublishing">发布中...</text>
			</view>
		</view>
	</view>
</template>

<script>
/**
 * 发视频页面 - 创建视频帖子
 *
 * 功能特性：
 * - 视频选择和上传
 * - 自动生成和自定义封面
 * - 标题和描述编辑
 * - 话题标签选择
 * - 匿名发布和下载权限设置
 * - 视频格式和大小限制
 */

import { postApi, tagApi, fileApi } from '../../../utils/api.js'
import { verifyAndExecute, USER_ROLES } from '../../../utils/auth.js'

export default {
	data() {
		return {
			// 表单数据
			formData: {
				title: '',           // 标题
				description: '',     // 描述
				videoUrl: '',        // 视频URL
				videoCover: '',      // 视频封面
				selectedTags: [],    // 选中的标签
				isAnonymous: false,  // 是否匿名
				allowDownload: true  // 允许下载
			},

			// 视频相关
			videoFile: null,       // 视频文件
			videoDuration: 0,      // 视频时长（秒）
			videoSize: 0,          // 视频大小（字节）
			autoCoverList: [],     // 自动生成的封面列表

			// 上传状态
			isUploading: false,    // 上传中
			uploadProgress: 0,     // 上传进度
			isPublishing: false,   // 发布中

			// 标签相关
			hotTags: [],           // 热门标签
			newTagName: ''         // 新建标签名称
		}
	},

	computed: {
		/**
		 * 是否可以发布
		 * 需要有视频和标题
		 */
		canPublish() {
			return this.formData.videoUrl && this.formData.title.trim()
		}
	},

	onLoad(options) {
		// 加载热门标签
		this.loadHotTags()
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
		 * 选择视频
		 */
		selectVideo() {
			uni.chooseVideo({
				sourceType: ['album', 'camera'],
				maxDuration: 300,  // 最大5分钟
				camera: 'back',
				success: (res) => {
					this.handleVideoSelected(res)
				},
				fail: (error) => {
					console.error('选择视频失败:', error)
				}
			})
		},

		/**
		 * 处理视频选择
		 * @param {Object} videoRes - 选择视频的结果
		 */
		async handleVideoSelected(videoRes) {
			const { tempFilePath, duration, size } = videoRes

			// 检查文件大小（100MB限制）
			const maxSize = 100 * 1024 * 1024
			if (size > maxSize) {
				uni.showToast({
					title: '视频文件过大，请选择小于100MB的视频',
					icon: 'none'
				})
				return
			}

			// 保存视频信息
			this.videoFile = tempFilePath
			this.videoDuration = duration
			this.videoSize = size

			// 上传视频
			await this.uploadVideo(tempFilePath)
		},

		/**
		 * 上传视频
		 * @param {string} filePath - 视频文件路径
		 */
		async uploadVideo(filePath) {
			this.isUploading = true
			this.uploadProgress = 0

			try {
				// 模拟上传进度
				const progressTimer = setInterval(() => {
					if (this.uploadProgress < 90) {
						this.uploadProgress += Math.random() * 10
					}
				}, 500)

				// 上传视频文件
				const videoUrl = await fileApi.uploadVideo(filePath)

				// 清除进度定时器
				clearInterval(progressTimer)
				this.uploadProgress = 100

				// 保存视频URL
				this.formData.videoUrl = videoUrl

				// 生成视频封面
				await this.generateVideoCover()

				uni.showToast({
					title: '视频上传成功',
					icon: 'success'
				})

			} catch (error) {
				console.error('视频上传失败:', error)
				uni.showToast({
					title: '视频上传失败',
					icon: 'none'
				})
			} finally {
				this.isUploading = false
				this.uploadProgress = 0
			}
		},

		/**
		 * 生成视频封面
		 */
		async generateVideoCover() {
			// 这里可以调用后端API生成视频封面
			// 目前先使用模拟数据
			this.autoCoverList = [
				`${this.formData.videoUrl}?t=0.1`,   // 0.1秒处截图
				`${this.formData.videoUrl}?t=0.5`,   // 50%处截图
				`${this.formData.videoUrl}?t=0.8`    // 80%处截图
			]

			// 默认选择第一个封面
			if (this.autoCoverList.length > 0) {
				this.formData.videoCover = this.autoCoverList[0]
			}
		},

		/**
		 * 选择封面
		 * @param {string} coverUrl - 封面URL
		 */
		selectCover(coverUrl) {
			this.formData.videoCover = coverUrl
		},

		/**
		 * 上传自定义封面
		 */
		uploadCustomCover() {
			uni.chooseImage({
				count: 1,
				sourceType: ['album', 'camera'],
				success: async (res) => {
					try {
						const coverUrl = await fileApi.uploadImage(res.tempFilePaths[0], 'cover')
						this.formData.videoCover = coverUrl

						uni.showToast({
							title: '封面上传成功',
							icon: 'success'
						})
					} catch (error) {
						console.error('封面上传失败:', error)
						uni.showToast({
							title: '封面上传失败',
							icon: 'none'
						})
					}
				}
			})
		},

		/**
		 * 选择视频封面（从视频中截取）
		 */
		selectVideoCover() {
			if (!this.videoFile) return

			// 这里可以调用视频编辑API来选择特定时间点的截图
			uni.showToast({
				title: '功能开发中',
				icon: 'none'
			})
		},

		/**
		 * 移除视频
		 */
		removeVideo() {
			uni.showModal({
				title: '确认删除',
				content: '确定要重新选择视频吗？',
				success: (res) => {
					if (res.confirm) {
						this.formData.videoUrl = ''
						this.formData.videoCover = ''
						this.videoFile = null
						this.videoDuration = 0
						this.videoSize = 0
						this.autoCoverList = []
					}
				}
			})
		},

		/**
		 * 视频加载完成
		 */
		onVideoLoaded(e) {
			console.log('视频加载完成:', e)
		},

		/**
		 * 视频加载错误
		 */
		onVideoError(e) {
			console.error('视频加载错误:', e)
			uni.showToast({
				title: '视频加载失败',
				icon: 'none'
			})
		},

		/**
		 * 标题输入处理
		 */
		onTitleInput(e) {
			this.formData.title = e.detail.value
		},

		/**
		 * 描述输入处理
		 */
		onDescriptionInput(e) {
			this.formData.description = e.detail.value
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
		 * 下载权限改变
		 */
		onDownloadChange(e) {
			this.formData.allowDownload = e.detail.value
		},

		/**
		 * 处理取消操作
		 */
		handleCancel() {
			if (this.formData.videoUrl || this.formData.title.trim()) {
				uni.showModal({
					title: '确认退出',
					content: '当前有未保存的内容，确定要退出吗？',
					success: (res) => {
						if (res.confirm) {
							uni.navigateBack()
						}
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
					title: '请选择视频并输入标题',
					icon: 'none'
				})
				return
			}

			// 需要登录才能发布
			verifyAndExecute(USER_ROLES.TRIAL, async () => {
				await this.publishVideo()
			}, {
				loginPrompt: '请先登录后再发布',
				showTrialUpgrade: true
			})
		},

		/**
		 * 发布视频
		 */
		async publishVideo() {
			this.isPublishing = true

			try {
				// 构建发布数据
				const postData = {
					title: this.formData.title.trim(),
					content: this.formData.description.trim(),
					type: 0,  // 普通帖子
					tagIds: this.formData.selectedTags.map(tag => tag.id),
					videoUrl: this.formData.videoUrl,
					videoCover: this.formData.videoCover,
					isAnonymous: this.formData.isAnonymous,
					allowDownload: this.formData.allowDownload
				}

				// 发布视频帖子
				const result = await postApi.createPost(postData)

				// 提示成功
				uni.showToast({
					title: '发布成功',
					icon: 'success'
				})

				// 延迟跳转到首页
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
		},

		/**
		 * 格式化时长显示
		 * @param {number} duration - 时长（秒）
		 */
		formatDuration(duration) {
			const minutes = Math.floor(duration / 60)
			const seconds = Math.floor(duration % 60)
			return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
		},

		/**
		 * 格式化文件大小
		 * @param {number} size - 文件大小（字节）
		 */
		formatFileSize(size) {
			if (size < 1024) return size + 'B'
			if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
			return (size / (1024 * 1024)).toFixed(1) + 'MB'
		}
	}
}
</script>

<style lang="scss" scoped>
/* 页面容器 */
.video-release-container {
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
	padding: 30upx;
}

/* 公共区域标题 */
.section-title {
	font-size: 32upx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20upx;
}

/* 视频区域 */
.video-section {
	margin-bottom: 40upx;

	.video-preview {
		position: relative;
		border-radius: 16upx;
		overflow: hidden;
		background-color: #000;

		.video-player {
			width: 100%;
			height: 400upx;
		}

		.video-info {
			position: absolute;
			top: 20upx;
			right: 20upx;
			display: flex;
			flex-direction: column;
			align-items: flex-end;

			.video-duration,
			.video-size {
				padding: 8upx 12upx;
				background-color: rgba(0, 0, 0, 0.6);
				color: #fff;
				font-size: 22upx;
				border-radius: 8upx;
				margin-bottom: 8upx;
			}
		}

		.video-actions {
			position: absolute;
			bottom: 20upx;
			right: 20upx;
			display: flex;
			gap: 15upx;

			.action-btn {
				padding: 12upx 20upx;
				background-color: rgba(0, 0, 0, 0.6);
				color: #fff;
				font-size: 24upx;
				border-radius: 20upx;

				&.delete-btn {
					background-color: rgba(255, 68, 68, 0.8);
				}
			}
		}
	}

	.video-upload {
		.upload-area {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			height: 400upx;
			border: 2upx dashed #ddd;
			border-radius: 16upx;
			background-color: #fafafa;

			.upload-icon {
				font-size: 80upx;
				margin-bottom: 20upx;
			}

			.upload-text {
				font-size: 32upx;
				color: #333;
				margin-bottom: 10upx;
			}

			.upload-tip {
				font-size: 24upx;
				color: #999;
			}
		}
	}

	.upload-progress {
		margin-top: 20upx;

		.progress-bar {
			width: 100%;
			height: 8upx;
			background-color: #f0f0f0;
			border-radius: 4upx;
			overflow: hidden;

			.progress-fill {
				height: 100%;
				background-color: #007aff;
				transition: width 0.3s;
			}
		}

		.progress-text {
			display: block;
			text-align: center;
			font-size: 24upx;
			color: #666;
			margin-top: 10upx;
		}
	}
}

/* 封面选择区域 */
.cover-section {
	margin-bottom: 40upx;

	.cover-list {
		white-space: nowrap;

		.cover-item {
			position: relative;
			display: inline-block;
			width: 160upx;
			height: 120upx;
			margin-right: 20upx;
			border-radius: 12upx;
			overflow: hidden;
			border: 4upx solid transparent;

			&.selected {
				border-color: #007aff;
			}

			.cover-image {
				width: 100%;
				height: 100%;
			}

			.cover-label {
				position: absolute;
				bottom: 6upx;
				right: 6upx;
				padding: 4upx 8upx;
				background-color: rgba(0, 0, 0, 0.6);
				color: #fff;
				font-size: 20upx;
				border-radius: 4upx;
			}

			&.upload-cover {
				display: inline-flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				background-color: #f8f8f8;
				border: 2upx dashed #ddd;

				.upload-cover-icon {
					font-size: 40upx;
					margin-bottom: 8upx;
				}

				.upload-cover-text {
					font-size: 20upx;
					color: #666;
				}
			}
		}
	}
}

/* 标题输入区域 */
.title-section {
	position: relative;
	margin-bottom: 40upx;

	.title-input {
		width: 100%;
		padding: 20upx;
		background-color: #f8f8f8;
		border-radius: 12upx;
		font-size: 32upx;
		line-height: 1.5;
		min-height: 80upx;
	}

	.char-count {
		position: absolute;
		right: 20upx;
		bottom: 10upx;
		font-size: 24upx;
		color: #999;
	}
}

/* 描述输入区域 */
.description-section {
	position: relative;
	margin-bottom: 40upx;

	.description-input {
		width: 100%;
		padding: 20upx;
		background-color: #f8f8f8;
		border-radius: 12upx;
		font-size: 28upx;
		line-height: 1.6;
		min-height: 150upx;
	}

	.char-count {
		position: absolute;
		right: 20upx;
		bottom: 10upx;
		font-size: 24upx;
		color: #999;
	}
}

/* 标签区域 */
.tags-section {
	margin-bottom: 40upx;

	.section-title {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 20upx;

		.title-text {
			font-size: 32upx;
			font-weight: bold;
			color: #333;
		}

		.section-subtitle {
			font-size: 24upx;
			color: #999;
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
		}

		.create-btn {
			padding: 15upx 30upx;
			background-color: #007aff;
			color: #fff;
			border-radius: 25upx;
			font-size: 26upx;
		}
	}
}

/* 设置区域 */
.settings-section {
	margin-bottom: 40upx;

	.setting-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20upx 0;
		border-bottom: 2upx solid #f0f0f0;

		&:last-child {
			border-bottom: none;
		}

		.setting-label {
			font-size: 28upx;
			color: #333;
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