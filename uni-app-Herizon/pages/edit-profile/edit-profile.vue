<!-- 编辑个人资料页面 - 修改头像和昵称 -->
<template>
	<view class="page-container">
		<!-- 头像区域 -->
		<view class="avatar-section">
			<view class="avatar-preview" @click="chooseAvatar">
				<image
					v-if="formData.avatar"
					:src="formData.avatar"
					class="avatar-image"
					mode="aspectFill"
				></image>
				<view v-else class="avatar-placeholder">
					<text class="placeholder-icon">👤</text>
					<text class="placeholder-text">点击上传头像</text>
				</view>
				<view class="avatar-mask">
					<text class="change-text">更换头像</text>
				</view>
			</view>
		</view>

		<!-- 编辑表单 -->
		<view class="form-section">
			<!-- 昵称 -->
			<view class="form-group">
				<view class="form-label">
					<text class="label-text">昵称</text>
					<text class="label-tip">显示在社区中的名字</text>
				</view>
				<input
					class="form-input"
					v-model="formData.nickname"
					placeholder="请输入昵称(2-20个字符)"
					:maxlength="20"
				/>
				<view class="char-count">
					<text>{{ formData.nickname.length }}/20</text>
				</view>
			</view>
		</view>

		<!-- 底部保存按钮 -->
		<view class="footer-actions">
			<button class="save-btn" @click="saveProfile" :disabled="saving">
				{{ saving ? '保存中...' : '保存' }}
			</button>
		</view>
	</view>
</template>

<script>
/**
 * 个人资料编辑页面
 *
 * 功能:
 * - 上传/更换头像
 * - 编辑昵称
 * - 使用现有users表字段(avatar, nickname),无需数据库修改
 */
import { userApi, fileApi } from '@/utils/api.js'
import { getAuthInfo, setUserInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			// 表单数据
			formData: {
				nickname: '',
				avatar: ''
			},

			// 状态
			saving: false
		}
	},

	onLoad() {
		this.loadUserProfile()
	},

	methods: {
		/**
		 * 加载用户资料
		 * 从本地存储获取当前用户信息
		 */
		async loadUserProfile() {
			try {
				const userInfo = getAuthInfo()
				if (userInfo) {
					this.formData.nickname = userInfo.nickname || ''
					this.formData.avatar = userInfo.avatar || ''
				}
			} catch (error) {
				console.error('加载用户资料失败:', error)
			}
		},

		/**
		 * 选择头像
		 * 调用系统相册选择图片并上传
		 */
		chooseAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.uploadAvatar(res.tempFilePaths[0])
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
				}
			})
		},

		/**
		 * 上传头像
		 * 调用文件上传API
		 *
		 * @param {String} filePath - 本地图片路径
		 */
		async uploadAvatar(filePath) {
			uni.showLoading({ title: '上传中...', mask: true })

			try {
				// 上传图片到服务器
				const imageUrl = await fileApi.uploadImage(filePath, 'avatar')

				// 更新本地数据
				this.formData.avatar = imageUrl

				uni.showToast({ title: '头像上传成功', icon: 'success' })
			} catch (error) {
				console.error('上传头像失败:', error)
				uni.showToast({
					title: error.message || '上传失败',
					icon: 'none'
				})
			} finally {
				uni.hideLoading()
			}
		},

		/**
		 * 保存个人资料
		 * 调用用户资料保存昵称和头像到后端
		 */
		async saveProfile() {
			try {
				// 验证数据
				if (this.formData.nickname.trim().length < 2) {
					uni.showToast({
						title: '昵称至少需要2个字符',
						icon: 'none'
					})
					return
				}

			this.saving = true

			const trimmedNickname = this.formData.nickname.trim()
			const userInfo = getAuthInfo()
			const updatePayload = { nickname: trimmedNickname }
			if (this.formData.avatar && this.formData.avatar !== userInfo?.avatar) {
				updatePayload.avatar = this.formData.avatar
			}

			await userApi.updateProfile(updatePayload)

			// 更新本地存储的用户信息
			if (userInfo) {
				const updatedUser = {
					...userInfo,
					nickname: trimmedNickname,
					avatar: updatePayload.avatar !== undefined ? updatePayload.avatar : userInfo.avatar
				}
				setUserInfo(updatedUser)
			}

				uni.showToast({
					title: '保存成功',
					icon: 'success',
					duration: 1500
				})

				// 延迟返回
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)

			} catch (error) {
				console.error('保存个人资料失败:', error)
				uni.showToast({
					title: error.message || '保存失败',
					icon: 'none'
				})
			} finally {
				this.saving = false
			}
		}
	}
}
</script>

<style lang="scss" scoped>
/* 页面容器 - 遵循CLAUDE.md布局规范 */
.page-container {
	height: 100vh;
	background-color: #f5f5f5;
	overflow: hidden;
}

/* 背景图区域 */
.background-section {
	height: 300rpx;
	background-color: #ffffff;
	margin-bottom: 20rpx;
}

.background-preview {
	position: relative;
	width: 100%;
	height: 100%;
	overflow: hidden;
}

.background-image {
	width: 100%;
	height: 100%;
}

.background-placeholder {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #f33e54, #ff6b35);
}

.placeholder-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	color: rgba(255, 255, 255, 0.8);
}

.placeholder-text {
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.9);
}

.background-mask {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	height: 80rpx;
	background: linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,0.5));
	display: flex;
	align-items: center;
	justify-content: center;
}

.change-text {
	font-size: 28rpx;
	color: #ffffff;
}

/* 表单区域 */
.form-section {
	background-color: #ffffff;
	padding: 40rpx 30rpx;
}

.form-group {
	margin-bottom: 30rpx;
}

.form-label {
	margin-bottom: 20rpx;
}

.label-text {
	display: block;
	font-size: 32rpx;
	font-weight: bold;
	color: #333333;
	margin-bottom: 8rpx;
}

.label-tip {
	font-size: 24rpx;
	color: #999999;
}

.form-textarea {
	width: 100%;
	min-height: 200rpx;
	background-color: #f8f8f8;
	border: 1px solid #e5e5e5;
	border-radius: 12rpx;
	padding: 20rpx;
	font-size: 30rpx;
	color: #333333;
	line-height: 1.6;
	box-sizing: border-box;
}

.form-textarea:focus {
	border-color: #f33e54;
	background-color: #ffffff;
}

.char-count {
	margin-top: 10rpx;
	text-align: right;
	font-size: 24rpx;
	color: #999999;
}

/* 底部按钮 */
.footer-actions {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 20rpx 30rpx;
	background-color: #ffffff;
	border-top: 1px solid #f0f0f0;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.save-btn {
	width: 100%;
	height: 88rpx;
	background-color: #f33e54;
	color: #ffffff;
	border: none;
	border-radius: 12rpx;
	font-size: 32rpx;
	font-weight: bold;
	display: flex;
	align-items: center;
	justify-content: center;
}

.save-btn[disabled] {
	opacity: 0.6;
}
</style>
