<template>
	<view class="page-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: topNavPadding }">
			<view class="nav-left" @click="goBack">
				<text class="icon">←</text>
			</view>
			<view class="nav-title">职业兴趣测评</view>
			<view class="nav-right"></view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-scroll" scroll-y>
			<!-- 介绍页面 -->
			<view v-if="!testStarted && !showResult" class="intro-container">
				<view class="intro-card">
					<text class="intro-icon">🧭</text>
					<text class="intro-title">霍兰德职业兴趣测试</text>
					<text class="intro-subtitle">探索你的职业倾向,发现最适合的职业方向</text>
				</view>

				<view class="info-card">
					<text class="info-title">关于测试</text>
					<text class="info-text">霍兰德职业兴趣理论将人格分为六大类型(RIASEC),通过测评帮助你了解自己的职业兴趣和能力倾向。</text>
				</view>

				<view class="types-card">
					<text class="types-title">六大职业类型</text>
					<view class="type-item">
						<text class="type-code">R</text>
						<view class="type-info">
							<text class="type-name">现实型 (Realistic)</text>
							<text class="type-desc">喜欢动手操作,务实稳重</text>
						</view>
					</view>
					<view class="type-item">
						<text class="type-code">I</text>
						<view class="type-info">
							<text class="type-name">研究型 (Investigative)</text>
							<text class="type-desc">喜欢思考探索,善于分析</text>
						</view>
					</view>
					<view class="type-item">
						<text class="type-code">A</text>
						<view class="type-info">
							<text class="type-name">艺术型 (Artistic)</text>
							<text class="type-desc">富有创造力,追求美感</text>
						</view>
					</view>
					<view class="type-item">
						<text class="type-code">S</text>
						<view class="type-info">
							<text class="type-name">社会型 (Social)</text>
							<text class="type-desc">乐于助人,善于沟通</text>
						</view>
					</view>
					<view class="type-item">
						<text class="type-code">E</text>
						<view class="type-info">
							<text class="type-name">企业型 (Enterprising)</text>
							<text class="type-desc">善于领导,追求成就</text>
						</view>
					</view>
					<view class="type-item">
						<text class="type-code">C</text>
						<view class="type-info">
							<text class="type-name">常规型 (Conventional)</text>
							<text class="type-desc">注重细节,有条不紊</text>
						</view>
					</view>
				</view>

				<view class="test-info-card">
					<text class="test-info-item">📝 测试题数:60题</text>
					<text class="test-info-item">⏱️ 预计时间:10-15分钟</text>
					<text class="test-info-item">💡 建议:根据第一直觉作答</text>
				</view>

				<button class="start-btn" @click="startTest">开始测试</button>
			</view>

			<!-- 测试进行中 -->
			<view v-if="testStarted && !showResult" class="test-container">
				<!-- 进度条 -->
				<view class="progress-card">
					<view class="progress-info">
						<text class="progress-text">进度:{{ currentQuestionIndex + 1 }} / {{ questions.length }}</text>
						<text class="progress-percent">{{ Math.round(((currentQuestionIndex + 1) / questions.length) * 100) }}%</text>
					</view>
					<view class="progress-bar">
						<view class="progress-fill" :style="{width: ((currentQuestionIndex + 1) / questions.length) * 100 + '%'}"></view>
					</view>
				</view>

				<!-- 问题卡片 -->
				<view class="question-card">
					<text class="question-number">第 {{ currentQuestionIndex + 1 }} 题</text>
					<text class="question-text">{{ currentQuestion.text }}</text>
					<view class="options-container">
						<view
							class="option-btn"
							:class="{selected: selectedAnswer === option.value}"
							v-for="(option, index) in options"
							:key="index"
							@click="selectAnswer(option.value)"
						>
							<text class="option-text">{{ option.label }}</text>
						</view>
					</view>
				</view>

				<!-- 导航按钮 -->
				<view class="nav-buttons">
					<button class="nav-btn prev-btn" @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</button>
					<button
						class="nav-btn next-btn"
						@click="nextQuestion"
						:disabled="selectedAnswer === null"
					>
						{{ currentQuestionIndex === questions.length - 1 ? '查看结果' : '下一题' }}
					</button>
				</view>
			</view>

			<!-- 测试结果 -->
			<view v-if="showResult" class="result-container">
				<view class="result-header">
					<text class="result-icon">🎯</text>
					<text class="result-title">测评结果</text>
					<text class="result-subtitle">你的职业兴趣类型</text>
				</view>

				<!-- 主要类型 -->
				<view class="main-type-card">
					<text class="main-type-code">{{ topTypes[0].code }}</text>
					<text class="main-type-name">{{ topTypes[0].name }}</text>
					<text class="main-type-score">得分:{{ topTypes[0].score }}</text>
				</view>

				<!-- 雷达图占位 -->
				<view class="radar-card">
					<text class="radar-title">六维能力雷达图</text>
					<view class="radar-placeholder">
						<view class="radar-center"></view>
						<view class="radar-labels">
							<text class="radar-label" v-for="(type, index) in categoryInfo" :key="index">
								{{ type.code }}: {{ scores[type.code] }}
							</text>
						</view>
					</view>
				</view>

				<!-- 详细得分 -->
				<view class="scores-card">
					<text class="scores-title">详细得分</text>
					<view class="score-item" v-for="(type, index) in sortedScores" :key="index">
						<view class="score-info">
							<text class="score-code">{{ type.code }}</text>
							<text class="score-name">{{ type.name }}</text>
						</view>
						<view class="score-bar-container">
							<view class="score-bar">
								<view class="score-fill" :style="{width: (type.score / 50) * 100 + '%'}"></view>
							</view>
							<text class="score-value">{{ type.score }}</text>
						</view>
					</view>
				</view>

				<!-- 性格特征 -->
				<view class="traits-card">
					<text class="traits-title">性格特征</text>
					<text class="traits-text">{{ topTypes[0].description }}</text>
					<view class="traits-list">
						<text class="trait-item" v-for="(trait, index) in topTypes[0].traits" :key="index">{{ trait }}</text>
					</view>
				</view>

				<!-- 适合职业 -->
				<view class="careers-card">
					<text class="careers-title">适合职业</text>
					<view class="career-tags">
						<text class="career-tag" v-for="(career, index) in topTypes[0].careers" :key="index">{{ career }}</text>
					</view>
				</view>

				<!-- 重新测试 -->
				<button class="restart-btn" @click="restartTest">重新测试</button>
			</view>
		</scroll-view>
	</view>
</template>

<script>
export default {
		onLoad() {
		const info = uni.getSystemInfoSync()
		let safeTop = info.statusBarHeight || 0

		if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
			const rect = uni.getMenuButtonBoundingClientRect()
			if (rect && rect.top) {
				safeTop = Math.max(safeTop, rect.top)
			}
		}

		if (!safeTop) {
			safeTop = 20
		}

		this.statusBarHeight = safeTop
		this.topNavPadding = safeTop + 'px'
	},
	data() {
		return {
			statusBarHeight: 0,
			topNavPadding: '64px',
			testStarted: false,
			showResult: false,
			currentQuestionIndex: 0,
			selectedAnswer: null,
			answers: [],
			scores: {
				R: 0,
				I: 0,
				A: 0,
				S: 0,
				E: 0,
				C: 0
			},
			options: [
				{ label: '非常不同意', value: 1 },
				{ label: '不同意', value: 2 },
				{ label: '中立', value: 3 },
				{ label: '同意', value: 4 },
				{ label: '非常同意', value: 5 }
			],
			// 霍兰德职业兴趣测试题库(60题,每个类型10题)
			questions: [
				// R - 现实型 (10题)
				{ text: '我喜欢动手制作或修理东西', category: 'R' },
				{ text: '我喜欢户外活动和体力劳动', category: 'R' },
				{ text: '我擅长使用工具和机械', category: 'R' },
				{ text: '我喜欢实际操作而不是理论学习', category: 'R' },
				{ text: '我喜欢与物品打交道而不是与人打交道', category: 'R' },
				{ text: '我喜欢看到自己工作的实际成果', category: 'R' },
				{ text: '我喜欢遵循明确的步骤完成任务', category: 'R' },
				{ text: '我喜欢种植、养殖等农业活动', category: 'R' },
				{ text: '我喜欢操作机器或设备', category: 'R' },
				{ text: '我喜欢体育运动和健身活动', category: 'R' },

				// I - 研究型 (10题)
				{ text: '我喜欢思考和解决复杂问题', category: 'I' },
				{ text: '我喜欢独立研究和探索', category: 'I' },
				{ text: '我喜欢阅读科学和技术类书籍', category: 'I' },
				{ text: '我喜欢进行实验和分析数据', category: 'I' },
				{ text: '我喜欢学习新的理论和概念', category: 'I' },
				{ text: '我喜欢深入钻研某个专业领域', category: 'I' },
				{ text: '我喜欢提出问题并寻找答案', category: 'I' },
				{ text: '我喜欢逻辑推理和批判性思考', category: 'I' },
				{ text: '我喜欢了解事物的运作原理', category: 'I' },
				{ text: '我喜欢独自工作而不受打扰', category: 'I' },

				// A - 艺术型 (10题)
				{ text: '我喜欢创作艺术作品(绘画、音乐、写作等)', category: 'A' },
				{ text: '我喜欢表达自己的想法和情感', category: 'A' },
				{ text: '我喜欢设计和美化事物', category: 'A' },
				{ text: '我喜欢尝试新的创意和想法', category: 'A' },
				{ text: '我欣赏艺术和美的事物', category: 'A' },
				{ text: '我喜欢自由发挥,不受规则限制', category: 'A' },
				{ text: '我喜欢参与表演或展示活动', category: 'A' },
				{ text: '我喜欢用独特的方式解决问题', category: 'A' },
				{ text: '我喜欢装饰和布置空间', category: 'A' },
				{ text: '我喜欢沉浸在想象和幻想中', category: 'A' },

				// S - 社会型 (10题)
				{ text: '我喜欢帮助他人解决问题', category: 'S' },
				{ text: '我喜欢与人交流和互动', category: 'S' },
				{ text: '我喜欢教导和培训他人', category: 'S' },
				{ text: '我关心他人的感受和需求', category: 'S' },
				{ text: '我喜欢参与团队活动', category: 'S' },
				{ text: '我喜欢倾听他人的烦恼', category: 'S' },
				{ text: '我喜欢组织社交活动', category: 'S' },
				{ text: '我喜欢为他人提供建议和支持', category: 'S' },
				{ text: '我喜欢与不同的人建立联系', category: 'S' },
				{ text: '我喜欢志愿服务和公益活动', category: 'S' },

				// E - 企业型 (10题)
				{ text: '我喜欢领导和管理团队', category: 'E' },
				{ text: '我喜欢说服和影响他人', category: 'E' },
				{ text: '我喜欢制定计划和达成目标', category: 'E' },
				{ text: '我喜欢竞争和挑战', category: 'E' },
				{ text: '我喜欢承担责任和风险', category: 'E' },
				{ text: '我喜欢销售和推广产品', category: 'E' },
				{ text: '我喜欢组织和协调资源', category: 'E' },
				{ text: '我喜欢商业活动和创业', category: 'E' },
				{ text: '我喜欢在公开场合发言', category: 'E' },
				{ text: '我追求成功和社会认可', category: 'E' },

				// C - 常规型 (10题)
				{ text: '我喜欢有条理和规律的工作', category: 'C' },
				{ text: '我喜欢处理数据和文件', category: 'C' },
				{ text: '我喜欢遵循既定的流程和规则', category: 'C' },
				{ text: '我注重细节和准确性', category: 'C' },
				{ text: '我喜欢整理和分类信息', category: 'C' },
				{ text: '我喜欢稳定和可预测的环境', category: 'C' },
				{ text: '我喜欢财务管理和记账工作', category: 'C' },
				{ text: '我喜欢办公室工作和行政事务', category: 'C' },
				{ text: '我喜欢核对和检查工作', category: 'C' },
				{ text: '我喜欢使用办公软件和系统', category: 'C' }
			],
			categoryInfo: [
				{
					code: 'R',
					name: '现实型 (Realistic)',
					description: '你喜欢动手操作,务实稳重,擅长使用工具和机械。你偏好有明确规则和步骤的工作,喜欢看到实际成果。',
					traits: ['• 务实稳重', '• 动手能力强', '• 注重实际效果', '• 喜欢户外活动'],
					careers: ['工程师', '建筑师', '机械师', '农艺师', '飞行员', '运动教练', '技术工人']
				},
				{
					code: 'I',
					name: '研究型 (Investigative)',
					description: '你喜欢思考和探索,善于分析问题。你偏好独立研究,喜欢深入钻研专业领域,追求知识和真理。',
					traits: ['• 善于分析', '• 逻辑思维强', '• 喜欢独立研究', '• 追求知识'],
					careers: ['科学家', '研究员', '数据分析师', '医生', '程序员', '工程师', '教授']
				},
				{
					code: 'A',
					name: '艺术型 (Artistic)',
					description: '你富有创造力,追求美感和自我表达。你喜欢用独特的方式看待世界,偏好自由和灵活的工作环境。',
					traits: ['• 富有创造力', '• 想象力丰富', '• 追求美感', '• 个性独特'],
					careers: ['设计师', '作家', '音乐家', '画家', '演员', '摄影师', '文案策划']
				},
				{
					code: 'S',
					name: '社会型 (Social)',
					description: '你乐于助人,善于沟通。你关心他人感受,喜欢与人互动,在团队合作中表现出色。',
					traits: ['• 善于沟通', '• 富有同理心', '• 乐于助人', '• 团队合作强'],
					careers: ['教师', 'HR', '心理咨询师', '社工', '护士', '销售顾问', '客户服务']
				},
				{
					code: 'E',
					name: '企业型 (Enterprising)',
					description: '你善于领导和管理,追求成就和认可。你喜欢影响他人,勇于承担责任和风险。',
					traits: ['• 领导能力强', '• 善于说服', '• 目标导向', '• 勇于挑战'],
					careers: ['企业管理者', '创业者', '销售经理', '市场总监', '律师', '政治家', '投资人']
				},
				{
					code: 'C',
					name: '常规型 (Conventional)',
					description: '你注重细节,有条不紊。你喜欢稳定和可预测的环境,擅长处理数据和文件工作。',
					traits: ['• 注重细节', '• 有条理', '• 追求准确', '• 喜欢稳定'],
					careers: ['会计师', '审计师', '行政助理', '档案管理员', '银行职员', '数据录入员', '秘书']
				}
			]
		};
	},
	computed: {
		currentQuestion() {
			return this.questions[this.currentQuestionIndex];
		},
		sortedScores() {
			return this.categoryInfo.map(cat => ({
				code: cat.code,
				name: cat.name,
				score: this.scores[cat.code]
			})).sort((a, b) => b.score - a.score);
		},
		topTypes() {
			return this.sortedScores.slice(0, 3).map(type => {
				const info = this.categoryInfo.find(cat => cat.code === type.code);
				return {
					...type,
					...info
				};
			});
		}
	},
	methods: {
		goBack() {
			if (this.testStarted && !this.showResult) {
				uni.showModal({
					title: '提示',
					content: '测试尚未完成,确定要退出吗?',
					success: (res) => {
						if (res.confirm) {
							uni.navigateBack();
						}
					}
				});
			} else {
				uni.navigateBack();
			}
		},

		startTest() {
			this.testStarted = true;
			this.currentQuestionIndex = 0;
			this.answers = [];
			this.scores = { R: 0, I: 0, A: 0, S: 0, E: 0, C: 0 };
		},

		selectAnswer(value) {
			this.selectedAnswer = value;
		},

		prevQuestion() {
			if (this.currentQuestionIndex > 0) {
				this.currentQuestionIndex--;
				this.selectedAnswer = this.answers[this.currentQuestionIndex] || null;
			}
		},

		nextQuestion() {
			if (this.selectedAnswer === null) return;

			// 保存答案
			this.answers[this.currentQuestionIndex] = this.selectedAnswer;

			// 累加得分
			const category = this.currentQuestion.category;
			this.scores[category] += this.selectedAnswer;

			// 判断是否完成测试
			if (this.currentQuestionIndex === this.questions.length - 1) {
				this.showResult = true;
				this.testStarted = false;
			} else {
				this.currentQuestionIndex++;
				this.selectedAnswer = this.answers[this.currentQuestionIndex] || null;
			}
		},

		restartTest() {
			this.showResult = false;
			this.testStarted = false;
			this.currentQuestionIndex = 0;
			this.answers = [];
			this.selectedAnswer = null;
			this.scores = { R: 0, I: 0, A: 0, S: 0, E: 0, C: 0 };
		}
	}
};
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
}
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  padding-top: calc(env(safe-area-inset-top) + 12px);
  background-color: #ffffff;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
}
.nav-left,
.nav-right {
  width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-left .icon {
  font-size: 20px;
  color: #1f2937;
}
.nav-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}
.content-scroll {
  flex: 1;
  padding: 24px 24px 40px;
  box-sizing: border-box;
}
/* 介绍页面 */
.intro-container {
	padding-bottom: 20px;
}

.intro-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 10px;
	padding: 30px 20px;
	margin-bottom: 10px;
	text-align: center;
}

.intro-icon {
	font-size: 48px;
	display: block;
	margin-bottom: 10px;
}

.intro-title {
	display: block;
	font-size: 24px;
	font-weight: bold;
	color: #ffffff;
	margin-bottom: 5px;
}

.intro-subtitle {
	display: block;
	font-size: 14px;
	color: rgba(255, 255, 255, 0.8);
}

.info-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.info-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 8px;
}

.info-text {
	display: block;
	font-size: 14px;
	color: #666666;
	line-height: 22px;
}

.types-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.types-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 12px;
}

.type-item {
	display: flex;
	align-items: center;
	margin-bottom: 12px;
}

.type-code {
	width: 40px;
	height: 40px;
	line-height: 40px;
	text-align: center;
	background-color: #667eea;
	color: #ffffff;
	font-size: 18px;
	font-weight: bold;
	border-radius: 8px;
	margin-right: 12px;
}

.type-info {
	flex: 1;
}

.type-name {
	display: block;
	font-size: 14px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 2px;
}

.type-desc {
	display: block;
	font-size: 12px;
	color: #666666;
}

.test-info-card {
	background-color: #fff7ed;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 15px;
	border-left: 3px solid #ff9500;
}

.test-info-item {
	display: block;
	font-size: 14px;
	color: #8b5a00;
	line-height: 24px;
}

.start-btn {
	width: 100%;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #ffffff;
	font-size: 16px;
	font-weight: 600;
	border-radius: 10px;
	padding: 15px;
	border: none;
}

/* 测试进行中 */
.test-container {
	padding-bottom: 20px;
}

.progress-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 15px;
}

.progress-info {
	display: flex;
	justify-content: space-between;
	margin-bottom: 10px;
}

.progress-text {
	font-size: 14px;
	color: #666666;
}

.progress-percent {
	font-size: 14px;
	font-weight: 600;
	color: #667eea;
}

.progress-bar {
	height: 8px;
	background-color: #e5e5e5;
	border-radius: 4px;
	overflow: hidden;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
	transition: width 0.3s ease;
}

.question-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 20px;
	margin-bottom: 15px;
}

.question-number {
	display: block;
	font-size: 13px;
	color: #667eea;
	margin-bottom: 10px;
}

.question-text {
	display: block;
	font-size: 16px;
	font-weight: 500;
	color: #333333;
	line-height: 24px;
	margin-bottom: 20px;
}

.options-container {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.option-btn {
	padding: 15px;
	background-color: #f5f5f5;
	border-radius: 8px;
	text-align: center;
	border: 2px solid transparent;
	transition: all 0.2s;
}

.option-btn.selected {
	background-color: #e8eaf6;
	border-color: #667eea;
}

.option-text {
	font-size: 14px;
	color: #333333;
}

.nav-buttons {
	display: flex;
	gap: 10px;
}

.nav-btn {
	flex: 1;
	padding: 15px;
	border-radius: 10px;
	font-size: 15px;
	font-weight: 600;
	border: none;
}

.prev-btn {
	background-color: #f5f5f5;
	color: #666666;
}

.prev-btn:disabled {
	opacity: 0.5;
}

.next-btn {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #ffffff;
}

.next-btn:disabled {
	opacity: 0.5;
}

/* 测试结果 */
.result-container {
	padding-bottom: 20px;
}

.result-header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 10px;
	padding: 30px 20px;
	margin-bottom: 10px;
	text-align: center;
}

.result-icon {
	font-size: 48px;
	display: block;
	margin-bottom: 10px;
}

.result-title {
	display: block;
	font-size: 24px;
	font-weight: bold;
	color: #ffffff;
	margin-bottom: 5px;
}

.result-subtitle {
	display: block;
	font-size: 14px;
	color: rgba(255, 255, 255, 0.8);
}

.main-type-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 25px;
	margin-bottom: 10px;
	text-align: center;
}

.main-type-code {
	display: block;
	font-size: 48px;
	font-weight: bold;
	color: #667eea;
	margin-bottom: 10px;
}

.main-type-name {
	display: block;
	font-size: 20px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 8px;
}

.main-type-score {
	display: block;
	font-size: 14px;
	color: #666666;
}

.radar-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.radar-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 12px;
}

.radar-placeholder {
	height: 200px;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f5f5;
	border-radius: 8px;
	position: relative;
}

.radar-center {
	width: 120px;
	height: 120px;
	border: 2px solid #667eea;
	border-radius: 50%;
	background-color: rgba(102, 126, 234, 0.1);
}

.radar-labels {
	position: absolute;
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	padding: 10px;
}

.radar-label {
	font-size: 12px;
	color: #666666;
	background-color: #ffffff;
	padding: 4px 8px;
	border-radius: 4px;
}

.scores-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.scores-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 15px;
}

.score-item {
	margin-bottom: 15px;
}

.score-info {
	display: flex;
	align-items: center;
	margin-bottom: 8px;
}

.score-code {
	width: 30px;
	height: 30px;
	line-height: 30px;
	text-align: center;
	background-color: #667eea;
	color: #ffffff;
	font-size: 14px;
	font-weight: bold;
	border-radius: 6px;
	margin-right: 10px;
}

.score-name {
	font-size: 14px;
	color: #333333;
}

.score-bar-container {
	display: flex;
	align-items: center;
	gap: 10px;
}

.score-bar {
	flex: 1;
	height: 20px;
	background-color: #e5e5e5;
	border-radius: 10px;
	overflow: hidden;
}

.score-fill {
	height: 100%;
	background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.score-value {
	font-size: 14px;
	font-weight: 600;
	color: #667eea;
	min-width: 30px;
	text-align: right;
}

.traits-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.traits-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 10px;
}

.traits-text {
	display: block;
	font-size: 14px;
	color: #666666;
	line-height: 22px;
	margin-bottom: 12px;
}

.traits-list {
	display: flex;
	flex-direction: column;
	gap: 5px;
}

.trait-item {
	font-size: 13px;
	color: #333333;
	line-height: 20px;
}

.careers-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 15px;
}

.careers-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 12px;
}

.career-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
}

.career-tag {
	padding: 8px 12px;
	background-color: #e8eaf6;
	color: #667eea;
	font-size: 13px;
	border-radius: 6px;
}

.restart-btn {
	width: 100%;
	background-color: #f5f5f5;
	color: #666666;
	font-size: 16px;
	font-weight: 600;
	border-radius: 10px;
	padding: 15px;
	border: none;
}
</style>












