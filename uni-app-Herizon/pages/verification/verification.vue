<!-- 身份认证问卷页面 - 体验用户升级为正式用户 -->
<template>
	<view class="verification-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-item" @click="handleBack">
				<text class="nav-text">返回</text>
			</view>
			<view class="nav-title">女性身份认证</view>
			<view class="nav-item" @click="handleSubmit">
				<text class="nav-text submit-btn" :class="{ 'active': canSubmit }">提交</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-area" scroll-y="true">
			<!-- 欢迎信息 -->
			<view class="welcome-section">
				<view class="welcome-icon">👩‍💼</view>
				<text class="welcome-title">欢迎加入Herizon女性社区</text>
				<text class="welcome-desc">为了营造安全、互助的女性交流空间,请完成女性身份认证,通过后即可解锁社区全部权益。</text>
			</view>

			<!-- 问卷内容 -->
			<view class="questionnaire-section">
				<!-- 基本信息 -->
				<view class="question-group">
					<view class="group-title">
						<text class="title-text">身份与基本信息</text>
						<text class="required-mark">*</text>
					</view>

					<!-- 真实姓名 -->
					<view class="question-item">
						<text class="question-label">真实姓名</text>
						<input
							class="question-input"
							type="text"
							placeholder="请输入您的真实姓名"
							v-model="formData.realName"
							:maxlength="20"
						/>
					</view>

					<!-- 性别身份确认 -->
					<view class="question-item">
						<text class="question-label">请确认您的性别身份</text>
						<view class="option-group column">
							<view
								class="option-item"
								v-for="gender in genderOptions"
								:key="gender.value"
								:class="{
									'selected': formData.genderIdentity === gender.value,
									'warning': gender.value !== 'female'
								}"
								@click="selectGender(gender.value)"
							>
								<text class="option-text">{{ gender.label }}</text>
							</view>
						</view>
						<text class="gender-hint">Herizon仅向确认以女性身份参与社区的成员开放正式权限。</text>
					</view>

					<!-- 年龄范围 -->
					<view class="question-item">
						<text class="question-label">年龄范围</text>
						<view class="option-group">
							<view
								class="option-item"
								v-for="age in ageOptions"
								:key="age.value"
								:class="{ 'selected': formData.ageRange === age.value }"
								@click="selectAge(age.value)"
							>
								<text class="option-text">{{ age.label }}</text>
							</view>
						</view>
					</view>

					<!-- 所在城市 -->
					<view class="question-item">
						<text class="question-label">所在城市</text>
						<input
							class="question-input"
							type="text"
							placeholder="请输入您所在的城市"
							v-model="formData.city"
							:maxlength="20"
						/>
					</view>
				</view>

				<!-- 职业信息 -->
				<view class="question-group">
					<view class="group-title">
						<text class="title-text">职业信息</text>
						<text class="required-mark">*</text>
					</view>

					<!-- 所属行业 -->
					<view class="question-item">
						<text class="question-label">所属行业</text>
						<view class="option-group">
							<view
								class="option-item"
								v-for="industry in industryOptions"
								:key="industry.value"
								:class="{ 'selected': formData.industry === industry.value }"
								@click="selectIndustry(industry.value)"
							>
								<text class="option-text">{{ industry.label }}</text>
							</view>
						</view>
					</view>

					<!-- 职位级别 -->
					<view class="question-item">
						<text class="question-label">职位级别</text>
						<view class="option-group">
							<view
								class="option-item"
								v-for="level in positionLevelOptions"
								:key="level.value"
								:class="{ 'selected': formData.positionLevel === level.value }"
								@click="selectPositionLevel(level.value)"
							>
								<text class="option-text">{{ level.label }}</text>
							</view>
						</view>
					</view>

					<!-- 工作年限 -->
					<view class="question-item">
						<text class="question-label">工作年限</text>
						<view class="option-group">
							<view
								class="option-item"
								v-for="year in workYearOptions"
								:key="year.value"
								:class="{ 'selected': formData.workYears === year.value }"
								@click="selectWorkYears(year.value)"
							>
								<text class="option-text">{{ year.label }}</text>
							</view>
						</view>
					</view>

					<!-- 公司规模 -->
					<view class="question-item">
						<text class="question-label">公司规模</text>
						<view class="option-group">
							<view
								class="option-item"
								v-for="size in companySizeOptions"
								:key="size.value"
								:class="{ 'selected': formData.companySize === size.value }"
								@click="selectCompanySize(size.value)"
							>
								<text class="option-text">{{ size.label }}</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 使用意向 -->
				<view class="question-group">
					<view class="group-title">
						<text class="title-text">使用意向</text>
						<text class="optional-mark">可选</text>
					</view>

					<!-- 使用目的 -->
					<view class="question-item">
						<text class="question-label">您希望在Herizon获得什么?(多选)</text>
						<view class="checkbox-group">
							<view
								class="checkbox-item"
								v-for="purpose in purposeOptions"
								:key="purpose.value"
								:class="{ 'selected': formData.purposes.includes(purpose.value) }"
								@click="togglePurpose(purpose.value)"
							>
								<text class="checkbox-icon">{{ formData.purposes.includes(purpose.value) ? '✓' : '' }}</text>
								<text class="checkbox-text">{{ purpose.label }}</text>
							</view>
						</view>
					</view>

					<!-- 关注领域 */
					<view class="question-item">
						<text class="question-label">您最关注的领域?(多选)</text>
						<view class="checkbox-group">
							<view
								class="checkbox-item"
								v-for="interest in interestOptions"
								:key="interest.value"
								:class="{ 'selected': formData.interests.includes(interest.value) }"
								@click="toggleInterest(interest.value)"
							>
								<text class="checkbox-icon">{{ formData.interests.includes(interest.value) ? '✓' : '' }}</text>
								<text class="checkbox-text">{{ interest.label }}</text>
							</view>
						</view>
					</view>

					<!-- 自我介绍 -->
					<view class="question-item">
						<text class="question-label">简单介绍一下自己(可选)</text>
						<textarea
							class="question-textarea"
							placeholder="请简单介绍一下您的职业背景、兴趣爱好或其他想要分享的内容..."
							v-model="formData.selfIntroduction"
							:maxlength="500"
							auto-height
						></textarea>
						<text class="char-count">{{ formData.selfIntroduction.length }}/500</text>
					</view>
				</view>

				<!-- 隐私协议 -->
				<view class="agreement-section">
					<view class="agreement-item" @click="togglePrivacyAgreement">
						<text class="checkbox-icon">{{ formData.agreePrivacy ? '✓' : '' }}</text>
						<text class="agreement-text">
							我已阅读并同意
							<text class="link-text" @click.stop="viewPrivacyPolicy">《隐私政策》</text>
							和
							<text class="link-text" @click.stop="viewUserAgreement">《用户协议》</text>
						</text>
					</view>
				</view>
			</view>

			<!-- 底部间距 -->
			<view class="bottom-space"></view>
		</scroll-view>

		<!-- 提交确认弹窗 -->
		<view class="modal-overlay" v-if="showSubmitModal" @click="hideSubmitModal">
			<view class="submit-modal" @click.stop>
				<view class="modal-header">
					<text class="modal-title">提交确认</text>
				</view>
				<view class="modal-content">
					<text class="modal-text">确定要提交女性身份认证信息吗?</text>
					<text class="modal-hint">管理员审核通过后,您将获得Herizon女性社区的正式访问权限。</text>
				</view>
				<view class="modal-actions">
					<button class="modal-btn cancel-btn" @click="hideSubmitModal">取消</button>
					<button class="modal-btn confirm-btn" @click="confirmSubmit">确认提交</button>
				</view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-overlay" v-if="isSubmitting">
			<view class="loading-content">
				<text class="loading-text">提交中...</text>
				<view class="loading-progress">
					<view class="progress-bar" :style="{ width: submitProgress + '%' }"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	// 导入必要的工具和API
	import { userApi } from '../../utils/api.js'
	import { isLoggedIn, USER_ROLES, getCurrentUserRole, getUserDisplayInfo, setUserInfo } from '../../utils/auth.js'

	export default {
		data() {
			return {
				// 表单数据
				formData: {
					realName: '',        // 真实姓名
					genderIdentity: '',  // 性别身份确认
					ageRange: '',        // 年龄范围
					city: '',            // 所在城市
					industry: '',        // 所属行业
					positionLevel: '',   // 职位级别
					workYears: '',       // 工作年限
					companySize: '',     // 公司规模
					purposes: [],        // 使用目的(多选)
					interests: [],       // 关注领域(多选)
					selfIntroduction: '', // 自我介绍
					agreePrivacy: false  // 同意隐私协议
				},

				// 选项数据
				genderOptions: [
					{ value: 'female', label: '我确认自己认同并以女性身份参与社区' },
					{ value: 'other', label: '我暂无法确认或不属于女性,了解社区适用范围' }
				],

				ageOptions: [
					{ value: '18-25', label: '18-25岁' },
					{ value: '26-30', label: '26-30岁' },
					{ value: '31-35', label: '31-35岁' },
					{ value: '36-40', label: '36-40岁' },
					{ value: '40+', label: '40岁以上' }
				],

				industryOptions: [
					{ value: 'tech', label: '科技互联网' },
					{ value: 'finance', label: '金融银行' },
					{ value: 'consulting', label: '咨询服务' },
					{ value: 'education', label: '教育培训' },
					{ value: 'healthcare', label: '医疗健康' },
					{ value: 'media', label: '媒体广告' },
					{ value: 'retail', label: '零售消费' },
					{ value: 'manufacturing', label: '制造业' },
					{ value: 'government', label: '政府机关' },
					{ value: 'other', label: '其他' }
				],

				positionLevelOptions: [
					{ value: 'entry', label: '初级员工' },
					{ value: 'intermediate', label: '中级员工' },
					{ value: 'senior', label: '高级员工' },
					{ value: 'team_lead', label: '团队主管' },
					{ value: 'manager', label: '部门经理' },
					{ value: 'director', label: '总监级别' },
					{ value: 'vp', label: 'VP/副总裁' },
					{ value: 'executive', label: 'C级高管' }
				],

				workYearOptions: [
					{ value: '0-1', label: '0-1年' },
					{ value: '1-3', label: '1-3年' },
					{ value: '3-5', label: '3-5年' },
					{ value: '5-8', label: '5-8年' },
					{ value: '8-15', label: '8-15年' },
					{ value: '15+', label: '15年以上' }
				],

				companySizeOptions: [
					{ value: 'startup', label: '创业公司(<50人)' },
					{ value: 'small', label: '小型公司(50-200人)' },
					{ value: 'medium', label: '中型公司(200-1000人)' },
					{ value: 'large', label: '大型公司(1000-5000人)' },
					{ value: 'enterprise', label: '大型企业(5000人以上)' }
				],

				purposeOptions: [
					{ value: 'career_growth', label: '职业发展指导' },
					{ value: 'skill_learning', label: '技能学习提升' },
					{ value: 'networking', label: '扩展人脉网络' },
					{ value: 'job_opportunities', label: '获取工作机会' },
					{ value: 'industry_insights', label: '行业信息洞察' },
					{ value: 'mentorship', label: '寻找导师指导' },
					{ value: 'sharing_experience', label: '分享个人经验' },
					{ value: 'work_life_balance', label: '工作生活平衡' }
				],

				interestOptions: [
					{ value: 'leadership', label: '领导力发展' },
					{ value: 'entrepreneurship', label: '创业创新' },
					{ value: 'tech_trends', label: '科技趋势' },
					{ value: 'career_planning', label: '职业规划' },
					{ value: 'workplace_skills', label: '职场技能' },
					{ value: 'industry_analysis', label: '行业分析' },
					{ value: 'personal_branding', label: '个人品牌' },
					{ value: 'work_efficiency', label: '工作效率' }
				],

				// 界面状态
				showSubmitModal: false,
				isSubmitting: false,
				submitProgress: 0
			}
		},

		computed: {
			/**
			 * 是否可以提交
			 * 必须填写基本信息并同意隐私协议
			 */
			canSubmit() {
				return this.formData.realName.trim() &&
					   this.formData.genderIdentity === 'female' &&
					   this.formData.ageRange &&
					   this.formData.city.trim() &&
					   this.formData.industry &&
					   this.formData.positionLevel &&
					   this.formData.workYears &&
					   this.formData.companySize &&
					   this.formData.agreePrivacy
			}
		},

		/**
		 * 页面加载时初始化
		 */
		onLoad() {
			this.checkUserStatus()
		},

		methods: {
			/**
			 * 检查用户状态
			 */
			checkUserStatus() {
				if (!isLoggedIn()) {
					uni.showModal({
						title: '需要登录',
						content: '请先登录后再进行身份认证',
						showCancel: false,
						success: () => {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}
					})
					return
				}

				const currentRole = getCurrentUserRole()
				if (currentRole >= USER_ROLES.VERIFIED) {
					uni.showModal({
						title: '已完成认证',
						content: '您已经是正式用户,无需重复认证',
						showCancel: false,
						success: () => {
							uni.navigateBack()
						}
					})
				}
			},

			/**
			 * 选择性别身份
			 */
			selectGender(value) {
				this.formData.genderIdentity = value
				if (value !== 'female') {
					uni.showToast({
						title: 'Herizon仅向女性用户开放正式功能',
						icon: 'none'
					})
				}
			},

			/**
			 * 选择年龄范围
			 */
			selectAge(value) {
				this.formData.ageRange = value
			},

			/**
			 * 选择行业
			 */
			selectIndustry(value) {
				this.formData.industry = value
			},

			/**
			 * 选择职位级别
			 */
			selectPositionLevel(value) {
				this.formData.positionLevel = value
			},

			/**
			 * 选择工作年限
			 */
			selectWorkYears(value) {
				this.formData.workYears = value
			},

			/**
			 * 选择公司规模
			 */
			selectCompanySize(value) {
				this.formData.companySize = value
			},

			/**
			 * 切换使用目的
			 */
			togglePurpose(value) {
				const index = this.formData.purposes.indexOf(value)
				if (index >= 0) {
					this.formData.purposes.splice(index, 1)
				} else {
					this.formData.purposes.push(value)
				}
			},

			/**
			 * 切换关注领域
			 */
			toggleInterest(value) {
				const index = this.formData.interests.indexOf(value)
				if (index >= 0) {
					this.formData.interests.splice(index, 1)
				} else {
					this.formData.interests.push(value)
				}
			},

			/**
			 * 切换隐私协议同意状态
			 */
			togglePrivacyAgreement() {
				this.formData.agreePrivacy = !this.formData.agreePrivacy
			},

			/**
			 * 查看隐私政策
			 */
			viewPrivacyPolicy() {
				uni.showModal({
					title: '隐私政策',
					content: '这里是隐私政策的详细内容...',
					showCancel: false
				})
			},

			/**
			 * 查看用户协议
			 */
			viewUserAgreement() {
				uni.showModal({
					title: '用户协议',
					content: '这里是用户协议的详细内容...',
					showCancel: false
				})
			},

			/**
			 * 处理返回
			 */
			handleBack() {
				if (this.hasUnsavedData()) {
					uni.showModal({
						title: '确认退出',
						content: '退出后填写的信息将不会保存,确定要退出吗?',
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
			 * 检查是否有未保存的数据
			 */
			hasUnsavedData() {
				return this.formData.realName.trim() ||
					   this.formData.genderIdentity ||
					   this.formData.city.trim() ||
					   this.formData.selfIntroduction.trim() ||
					   this.formData.ageRange ||
					   this.formData.industry ||
					   this.formData.purposes.length > 0
			},

			/**
			 * 处理提交
			 */
			handleSubmit() {
				if (!this.canSubmit) {
					this.showValidationErrors()
					return
				}

				this.showSubmitModal = true
			},

			/**
			 * 显示验证错误
			 */
			showValidationErrors() {
				if (!this.formData.realName.trim()) {
					uni.showToast({ title: '请输入真实姓名', icon: 'none' })
					return
				}
				if (this.formData.genderIdentity !== 'female') {
					uni.showToast({ title: '请确认您的女性身份', icon: 'none' })
					return
				}
				if (!this.formData.ageRange) {
					uni.showToast({ title: '请选择年龄范围', icon: 'none' })
					return
				}
				if (!this.formData.city.trim()) {
					uni.showToast({ title: '请输入所在城市', icon: 'none' })
					return
				}
				if (!this.formData.industry) {
					uni.showToast({ title: '请选择所属行业', icon: 'none' })
					return
				}
				if (!this.formData.positionLevel) {
					uni.showToast({ title: '请选择职位级别', icon: 'none' })
					return
				}
				if (!this.formData.workYears) {
					uni.showToast({ title: '请选择工作年限', icon: 'none' })
					return
				}
				if (!this.formData.companySize) {
					uni.showToast({ title: '请选择公司规模', icon: 'none' })
					return
				}
				if (!this.formData.agreePrivacy) {
					uni.showToast({ title: '请同意隐私政策和用户协议', icon: 'none' })
					return
				}
			},

			/**
			 * 隐藏提交弹窗
			 */
			hideSubmitModal() {
				this.showSubmitModal = false
			},

			/**
			 * 确认提交
			 */
			async confirmSubmit() {
				try {
					this.isSubmitting = true
					this.submitProgress = 0
					this.showSubmitModal = false

					// 模拟提交进度
					const progressInterval = setInterval(() => {
						if (this.submitProgress < 90) {
							this.submitProgress += 10
						}
					}, 200)

					// 调用身份认证API(发送JSON字符串到后端)
					// request.js已解包Result对象
					await userApi.applyVerification(this.formData)

					clearInterval(progressInterval)
					this.submitProgress = 100

					uni.showToast({
						title: '提交成功!',
						icon: 'success'
					})

					// 延迟跳转
					setTimeout(() => {
						uni.showModal({
							title: '提交成功',
							content: '您的女性身份认证申请已提交,管理员审核通过后即可升级为正式用户,享受女性社区完整功能。审核结果会通过系统通知您。',
							showCancel: false,
							success: () => {
								uni.switchTab({
									url: '/pages/tabbar/tabbar-1/tabbar-1'
								})
							}
						})
					}, 1500)

				} catch (error) {
					console.error('提交认证失败:', error)
					uni.showToast({
						title: error.message || '提交失败',
						icon: 'none'
					})
				} finally {
					this.isSubmitting = false
					this.submitProgress = 0
				}
			}
		}
	}
</script>

<style scoped>
	/* 主容器样式 */
	.verification-container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
		display: flex;
		flex-direction: column;
		overflow-x: hidden; /* 仅限制横向滚动,保留纵向滚动能力 */
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

	.submit-btn {
		color: #ccc;
		transition: all 0.3s;
	}

	.submit-btn.active {
		color: #f33e54;
		font-weight: bold;
	}

	/* 内容区域 */
	.content-area {
		flex: 1;
		margin-top: 88rpx;
		padding: 30rpx;
		box-sizing: border-box;
		min-height: 0; /* 允许在flex容器中正确收缩以启用内部滚动 */
		overflow-x: hidden;
	}

	/* 欢迎信息 */
	.welcome-section {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 50rpx 40rpx;
		margin-bottom: 30rpx;
		text-align: center;
	}

	.welcome-icon {
		font-size: 120rpx;
		margin-bottom: 30rpx;
	}

	.welcome-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
		display: block;
	}

	.welcome-desc {
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
	}

	/* 问卷部分 */
	.questionnaire-section .question-group {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 40rpx;
		margin-bottom: 30rpx;
	}

	.questionnaire-section .group-title {
		display: flex;
		align-items: center;
		margin-bottom: 30rpx;
		padding-bottom: 20rpx;
		border-bottom: 2rpx solid #f0f0f0;
	}

	.questionnaire-section .title-text {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-right: 10rpx;
	}

	.questionnaire-section .required-mark {
		color: #f33e54;
		font-size: 30rpx;
		font-weight: bold;
	}

	.questionnaire-section .optional-mark {
		font-size: 24rpx;
		color: #999;
		background-color: #f8f8f8;
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
	}

	.questionnaire-section .question-item {
		margin-bottom: 40rpx;
	}

	.questionnaire-section .question-item:last-child {
		margin-bottom: 0;
	}

	.questionnaire-section .question-label {
		font-size: 30rpx;
		color: #333;
		margin-bottom: 20rpx;
		display: block;
		font-weight: 500;
	}

	.questionnaire-section .question-input {
		width: 100%;
		height: 80rpx;
		background-color: #f8f8f8;
		border: 2rpx solid #e5e5e5;
		border-radius: 12rpx;
		padding: 0 20rpx;
		font-size: 30rpx;
		color: #333;
		transition: border-color 0.3s;
	}

	.questionnaire-section .question-input:focus {
		border-color: #f33e54;
		background-color: #fff;
	}

	.questionnaire-section .question-textarea {
		width: 100%;
		min-height: 160rpx;
		background-color: #f8f8f8;
		border: 2rpx solid #e5e5e5;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 30rpx;
		color: #333;
		line-height: 1.6;
		transition: border-color 0.3s;
	}

	.questionnaire-section .question-textarea:focus {
		border-color: #f33e54;
		background-color: #fff;
	}

	.questionnaire-section .char-count {
		font-size: 24rpx;
		color: #999;
		text-align: right;
		margin-top: 10rpx;
	}

	.questionnaire-section .option-group {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.questionnaire-section .option-group.column {
		flex-direction: column;
	}

	.questionnaire-section .option-item {
		padding: 20rpx 30rpx;
		background-color: #f8f8f8;
		border: 2rpx solid #e5e5e5;
		border-radius: 25rpx;
		transition: all 0.3s;
	}

	.questionnaire-section .option-item.selected {
		background-color: #fff0f1;
		border-color: #f33e54;
	}

	.questionnaire-section .option-item.warning {
		background-color: #fff8f0;
		border-color: #f6b26b;
	}

	.questionnaire-section .option-text {
		font-size: 28rpx;
		color: #666;
	}

	.questionnaire-section .option-item.selected .option-text {
		color: #f33e54;
		font-weight: 500;
	}

	.questionnaire-section .option-item.warning .option-text {
		color: #c96a1b;
	}

	.questionnaire-section .checkbox-group {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.questionnaire-section .checkbox-item {
		display: flex;
		align-items: center;
		padding: 20rpx;
		background-color: #f8f8f8;
		border: 2rpx solid #e5e5e5;
		border-radius: 12rpx;
		transition: all 0.3s;
	}

	.questionnaire-section .checkbox-item.selected {
		background-color: #fff0f1;
		border-color: #f33e54;
	}

	.questionnaire-section .checkbox-icon {
		width: 40rpx;
		height: 40rpx;
		border: 2rpx solid #e5e5e5;
		border-radius: 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 20rpx;
		font-size: 24rpx;
		color: #fff;
		background-color: #fff;
		transition: all 0.3s;
	}

	.questionnaire-section .checkbox-item.selected .checkbox-icon {
		background-color: #f33e54;
		border-color: #f33e54;
	}

	.questionnaire-section .checkbox-text {
		flex: 1;
		font-size: 28rpx;
		color: #666;
	}

	.questionnaire-section .checkbox-item.selected .checkbox-text {
		color: #333;
		font-weight: 500;
	}

	.gender-hint {
		font-size: 24rpx;
		color: #999;
		display: block;
		margin-top: 12rpx;
		line-height: 1.6;
	}

	/* 协议部分 */
	.agreement-section {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 40rpx;
		margin-bottom: 30rpx;
	}

	.agreement-item {
		display: flex;
		align-items: flex-start;
	}

	.agreement-text {
		flex: 1;
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
		margin-left: 20rpx;
	}

	.link-text {
		color: #f33e54;
		text-decoration: underline;
	}

	/* 弹窗样式 */
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

	.submit-modal {
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
		text-align: center;
	}

	.modal-text {
		font-size: 28rpx;
		color: #666;
		margin-bottom: 15rpx;
	}

	.modal-hint {
		font-size: 26rpx;
		color: #999;
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
