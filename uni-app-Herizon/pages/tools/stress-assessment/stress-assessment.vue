<template>
	<view class="page-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: topNavPadding }">
			<view class="nav-left" @click="goBack">
				<text class="icon">←</text>
			</view>
			<view class="nav-title">职场压力测评</view>
			<view class="nav-right"></view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-scroll" scroll-y>
			<!-- 介绍页面 -->
			<view v-if="!testStarted && !showResult" class="intro-container">
				<view class="intro-card">
					<text class="intro-icon">🧘‍♀️</text>
					<text class="intro-title">职场压力测评</text>
					<text class="intro-subtitle">了解你的压力水平,找到平衡之道</text>
				</view>

				<view class="info-card">
					<text class="info-title">关于测评</text>
					<text class="info-text">职场压力是现代女性面临的重要挑战。通过科学的压力评估,帮助你识别压力源,掌握应对策略,保持身心健康。</text>
				</view>

				<view class="dimensions-card">
					<text class="dimensions-title">评估维度</text>
					<view class="dimension-item">
						<text class="dimension-icon">💼</text>
						<text class="dimension-name">工作负荷</text>
						<text class="dimension-desc">任务量、工作时长、加班频率</text>
					</view>
					<view class="dimension-item">
						<text class="dimension-icon">👥</text>
						<text class="dimension-name">人际关系</text>
						<text class="dimension-desc">团队协作、上下级关系、职场人际</text>
					</view>
					<view class="dimension-item">
						<text class="dimension-icon">📈</text>
						<text class="dimension-name">职业发展</text>
						<text class="dimension-desc">晋升机会、技能提升、职业规划</text>
					</view>
					<view class="dimension-item">
						<text class="dimension-icon">⚖️</text>
						<text class="dimension-name">工作生活平衡</text>
						<text class="dimension-desc">家庭时间、个人爱好、休息质量</text>
					</view>
					<view class="dimension-item">
						<text class="dimension-icon">😊</text>
						<text class="dimension-name">身心健康</text>
						<text class="dimension-desc">睡眠质量、情绪状态、体力状况</text>
					</view>
				</view>

				<view class="test-info-card">
					<text class="test-info-item">📝 测评题数:20题</text>
					<text class="test-info-item">⏱️ 预计时间:5-8分钟</text>
					<text class="test-info-item">💡 建议:根据最近一个月的真实感受作答</text>
				</view>

				<button class="start-btn" @click="startTest">开始测评</button>
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
				<view class="result-header" :class="stressLevelClass">
					<text class="result-icon">{{ stressLevelIcon }}</text>
					<text class="result-title">测评结果</text>
					<text class="result-level">{{ stressLevelText }}</text>
					<text class="result-score">压力指数:{{ totalScore }}/100</text>
				</view>

				<!-- 压力等级说明 -->
				<view class="level-card">
					<text class="level-title">{{ stressLevelText }}</text>
					<text class="level-desc">{{ stressLevelDescription }}</text>
				</view>

				<!-- 维度得分 -->
				<view class="scores-card">
					<text class="scores-title">各维度得分</text>
					<view class="score-item" v-for="(dim, index) in dimensionScores" :key="index">
						<view class="score-info">
							<text class="score-icon">{{ dim.icon }}</text>
							<text class="score-name">{{ dim.name }}</text>
						</view>
						<view class="score-bar-container">
							<view class="score-bar">
								<view class="score-fill" :style="{width: (dim.score / 20) * 100 + '%'}"></view>
							</view>
							<text class="score-value">{{ dim.score }}/20</text>
						</view>
					</view>
				</view>

				<!-- 建议 -->
				<view class="suggestions-card">
					<text class="suggestions-title">💡 缓解建议</text>
					<view class="suggestion-item" v-for="(suggestion, index) in suggestions" :key="index">
						<text class="suggestion-number">{{ index + 1 }}</text>
						<text class="suggestion-text">{{ suggestion }}</text>
					</view>
				</view>

				<!-- 女性专属建议 -->
				<view class="women-card">
					<text class="women-title">👩 女性职场健康提示</text>
					<text class="women-tip">• 学会说"不":不要过度承担工作和家庭责任</text>
					<text class="women-tip">• 寻求支持:与信任的同事、朋友或专业人士沟通</text>
					<text class="women-tip">• 关注身体:定期体检,注意生理周期对情绪的影响</text>
					<text class="women-tip">• 保留个人时间:每天至少30分钟属于自己的时间</text>
					<text class="women-tip">• 职业规划:明确目标可以减少迷茫带来的焦虑</text>
				</view>

				<!-- 重新测试 -->
				<button class="restart-btn" @click="restartTest">重新测评</button>
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
			totalScore: 0,
			dimensionScores: [],
			options: [
				{ label: '从不', value: 1 },
				{ label: '很少', value: 2 },
				{ label: '有时', value: 3 },
				{ label: '经常', value: 4 },
				{ label: '总是', value: 5 }
			],
			// 职场压力测评题库(20题,5个维度各4题)
			questions: [
				// 工作负荷 (4题)
				{ text: '我感觉工作任务过多,难以完成', dimension: 'workload' },
				{ text: '我经常需要加班或将工作带回家', dimension: 'workload' },
				{ text: '我的工作时间安排很紧张,缺少休息', dimension: 'workload' },
				{ text: '我感到工作压力超出了我的承受能力', dimension: 'workload' },

				// 人际关系 (4题)
				{ text: '我与同事之间存在紧张或冲突', dimension: 'relationships' },
				{ text: '我难以与上级或下属有效沟通', dimension: 'relationships' },
				{ text: '我在团队中感到孤立或不被支持', dimension: 'relationships' },
				{ text: '职场人际关系让我感到焦虑或疲惫', dimension: 'relationships' },

				// 职业发展 (4题)
				{ text: '我对职业发展前景感到迷茫', dimension: 'career' },
				{ text: '我担心自己的技能跟不上行业变化', dimension: 'career' },
				{ text: '我感觉晋升机会渺茫或不公平', dimension: 'career' },
				{ text: '我对目前的工作缺少成就感', dimension: 'career' },

				// 工作生活平衡 (4题)
				{ text: '工作占用了我大部分的个人时间', dimension: 'balance' },
				{ text: '我很少有时间陪伴家人或朋友', dimension: 'balance' },
				{ text: '我已经很久没有享受个人爱好了', dimension: 'balance' },
				{ text: '我常常因为工作忽视了家庭责任', dimension: 'balance' },

				// 身心健康 (4题)
				{ text: '我经常感到疲劳或精力不足', dimension: 'health' },
				{ text: '我的睡眠质量很差,经常失眠', dimension: 'health' },
				{ text: '我容易焦虑、烦躁或情绪低落', dimension: 'health' },
				{ text: '我出现了身体不适(头痛、胃痛等)', dimension: 'health' }
			],
			dimensionInfo: [
				{ key: 'workload', name: '工作负荷', icon: '💼' },
				{ key: 'relationships', name: '人际关系', icon: '👥' },
				{ key: 'career', name: '职业发展', icon: '📈' },
				{ key: 'balance', name: '工作生活平衡', icon: '⚖️' },
				{ key: 'health', name: '身心健康', icon: '😊' }
			]
		};
	},
	computed: {
		currentQuestion() {
			return this.questions[this.currentQuestionIndex];
		},
		stressLevelClass() {
			if (this.totalScore <= 40) return 'level-low';
			if (this.totalScore <= 60) return 'level-medium';
			if (this.totalScore <= 80) return 'level-high';
			return 'level-severe';
		},
		stressLevelIcon() {
			if (this.totalScore <= 40) return '😊';
			if (this.totalScore <= 60) return '😐';
			if (this.totalScore <= 80) return '😰';
			return '😫';
		},
		stressLevelText() {
			if (this.totalScore <= 40) return '压力较小';
			if (this.totalScore <= 60) return '压力中等';
			if (this.totalScore <= 80) return '压力较大';
			return '压力过大';
		},
		stressLevelDescription() {
			if (this.totalScore <= 40) {
				return '你的压力水平处于健康范围内,继续保持良好的工作生活平衡。偶尔的压力是正常的,记得定期自我关照。';
			}
			if (this.totalScore <= 60) {
				return '你正在经历一定的工作压力,需要开始关注自己的身心状态。建议调整工作节奏,增加休息时间,必要时寻求支持。';
			}
			if (this.totalScore <= 80) {
				return '你的压力水平较高,可能已经影响到工作效率和生活质量。强烈建议采取减压措施,如运动、冥想,或咨询专业人士。';
			}
			return '你正处于高压力状态,长期下去可能导致职业倦怠或健康问题。请立即采取行动调整工作状态,必要时寻求心理咨询帮助。';
		},
		suggestions() {
			const allSuggestions = {
				low: [
					'保持规律作息,每天保证7-8小时睡眠',
					'继续培养兴趣爱好,丰富业余生活',
					'定期与朋友家人联络,维护社交关系',
					'适当运动,每周至少3次有氧运动',
					'学习新技能,保持职业竞争力'
				],
				medium: [
					'识别主要压力源,制定针对性的应对计划',
					'学习时间管理技巧,提高工作效率',
					'每天安排15-30分钟放松时间(冥想、散步)',
					'与信任的人倾诉,不要独自承受压力',
					'设定工作边界,避免工作侵占私人时间'
				],
				high: [
					'立即减少工作量,必要时向上级寻求支持',
					'每天进行深呼吸或冥想练习(至少10分钟)',
					'保证充足睡眠,避免咖啡因和酒精',
					'寻求专业心理咨询,不要讳疾忌医',
					'考虑短期休假,给自己充电的机会'
				],
				severe: [
					'强烈建议寻求专业心理咨询或医疗帮助',
					'评估当前工作是否适合,考虑职业调整',
					'立即停止加班,优先保障个人健康',
					'建立应急支持系统(家人、朋友、专业人士)',
					'每天进行身体放松练习,关注身体信号'
				]
			};

			if (this.totalScore <= 40) return allSuggestions.low;
			if (this.totalScore <= 60) return allSuggestions.medium;
			if (this.totalScore <= 80) return allSuggestions.high;
			return allSuggestions.severe;
		}
	},
	methods: {
		goBack() {
			if (this.testStarted && !this.showResult) {
				uni.showModal({
					title: '提示',
					content: '测评尚未完成,确定要退出吗?',
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
			this.totalScore = 0;
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

			// 判断是否完成测试
			if (this.currentQuestionIndex === this.questions.length - 1) {
				this.calculateResult();
				this.showResult = true;
				this.testStarted = false;
			} else {
				this.currentQuestionIndex++;
				this.selectedAnswer = this.answers[this.currentQuestionIndex] || null;
			}
		},

		calculateResult() {
			// 计算总分
			this.totalScore = this.answers.reduce((sum, score) => sum + score, 0);

			// 计算各维度得分
			const dimensionTotals = {};
			this.questions.forEach((q, index) => {
				const dim = q.dimension;
				if (!dimensionTotals[dim]) {
					dimensionTotals[dim] = 0;
				}
				dimensionTotals[dim] += this.answers[index];
			});

			this.dimensionScores = this.dimensionInfo.map(dim => ({
				icon: dim.icon,
				name: dim.name,
				score: dimensionTotals[dim.key] || 0
			}));
		},

		restartTest() {
			this.showResult = false;
			this.testStarted = false;
			this.currentQuestionIndex = 0;
			this.answers = [];
			this.selectedAnswer = null;
			this.totalScore = 0;
			this.dimensionScores = [];
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

.dimensions-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.dimensions-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 12px;
}

.dimension-item {
	display: flex;
	align-items: center;
	margin-bottom: 12px;
	padding: 10px;
	background-color: #f5f5f5;
	border-radius: 8px;
}

.dimension-icon {
	font-size: 24px;
	margin-right: 12px;
}

.dimension-name {
	font-size: 14px;
	font-weight: 600;
	color: #333333;
	min-width: 100px;
}

.dimension-desc {
	flex: 1;
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
	border-radius: 10px;
	padding: 30px 20px;
	margin-bottom: 10px;
	text-align: center;
}

.result-header.level-low {
	background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.result-header.level-medium {
	background: linear-gradient(135deg, #f2994a 0%, #f2c94c 100%);
}

.result-header.level-high {
	background: linear-gradient(135deg, #ee0979 0%, #ff6a00 100%);
}

.result-header.level-severe {
	background: linear-gradient(135deg, #c33764 0%, #1d2671 100%);
}

.result-icon {
	font-size: 48px;
	display: block;
	margin-bottom: 10px;
}

.result-title {
	display: block;
	font-size: 18px;
	color: rgba(255, 255, 255, 0.9);
	margin-bottom: 8px;
}

.result-level {
	display: block;
	font-size: 24px;
	font-weight: bold;
	color: #ffffff;
	margin-bottom: 5px;
}

.result-score {
	display: block;
	font-size: 14px;
	color: rgba(255, 255, 255, 0.8);
}

.level-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.level-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 8px;
}

.level-desc {
	display: block;
	font-size: 14px;
	color: #666666;
	line-height: 22px;
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

.score-icon {
	font-size: 20px;
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
	min-width: 40px;
	text-align: right;
}

.suggestions-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.suggestions-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 15px;
}

.suggestion-item {
	display: flex;
	margin-bottom: 12px;
	padding: 12px;
	background-color: #f5f5f5;
	border-radius: 8px;
}

.suggestion-number {
	width: 24px;
	height: 24px;
	line-height: 24px;
	text-align: center;
	background-color: #667eea;
	color: #ffffff;
	border-radius: 50%;
	font-size: 12px;
	font-weight: 600;
	margin-right: 10px;
	flex-shrink: 0;
}

.suggestion-text {
	flex: 1;
	font-size: 14px;
	color: #333333;
	line-height: 24px;
}

.women-card {
	background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 15px;
}

.women-title {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 12px;
}

.women-tip {
	display: block;
	font-size: 14px;
	color: #555555;
	line-height: 24px;
	margin-bottom: 8px;
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












