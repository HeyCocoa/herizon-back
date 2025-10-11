<template>
	<view class="page-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: topNavPadding }">
			<view class="nav-left" @click="goBack">
				<text class="icon">←</text>
			</view>
			<view class="nav-title">面试题库</view>
			<view class="nav-right"></view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-scroll" scroll-y>
			<!-- 头部卡片 -->
			<view class="header-card">
				<text class="header-icon">💼</text>
				<text class="header-title">面试题库</text>
				<text class="header-subtitle">精选常见面试问题及参考答案</text>
			</view>

			<!-- 分类导航 -->
			<view class="category-nav">
				<view
					class="category-item"
					:class="{active: currentCategory === category.id}"
					v-for="category in categories"
					:key="category.id"
					@click="switchCategory(category.id)"
				>
					<text class="category-icon">{{ category.icon }}</text>
					<text class="category-name">{{ category.name }}</text>
					<text class="category-count">({{ category.count }})</text>
				</view>
			</view>

			<!-- 搜索框 -->
			<view class="search-bar">
				<input
					class="search-input"
					v-model="searchKeyword"
					placeholder="搜索面试问题..."
					@input="searchQuestions"
				/>
				<text class="search-icon">🔍</text>
			</view>

			<!-- 问题列表 -->
			<view class="questions-list">
				<view
					class="question-item"
					v-for="(question, index) in filteredQuestions"
					:key="question.id"
					@click="viewQuestion(question)"
				>
					<view class="question-header">
						<text class="question-title">{{ question.question }}</text>
						<text class="bookmark-icon" @click.stop="toggleBookmark(question.id)">
							{{ isBookmarked(question.id) ? '★' : '☆' }}
						</text>
					</view>
					<view class="question-tags">
						<text class="question-tag" v-for="(tag, idx) in question.tags" :key="idx">{{ tag }}</text>
					</view>
				</view>

				<view class="empty-state" v-if="filteredQuestions.length === 0">
					<text class="empty-icon">🔍</text>
					<text class="empty-text">没有找到相关问题</text>
				</view>
			</view>

			<!-- 随机练习按钮 -->
			<view class="practice-btn-container">
				<button class="practice-btn" @click="randomPractice">随机练习</button>
			</view>
		</scroll-view>

		<!-- 问题详情弹窗 -->
		<view class="modal" v-if="showModal" @click="closeModal">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">问题详情</text>
					<text class="modal-close" @click="closeModal">×</text>
				</view>
				<scroll-view class="modal-body" scroll-y>
					<text class="detail-question">{{ selectedQuestion.question }}</text>

					<view class="detail-section">
						<text class="detail-label">问题分类</text>
						<text class="detail-category">{{ getCategoryName(selectedQuestion.category) }}</text>
					</view>

					<view class="detail-section">
						<text class="detail-label">回答模板</text>
						<text class="detail-template">{{ selectedQuestion.answerTemplate }}</text>
					</view>

					<view class="detail-section" v-if="selectedQuestion.sampleAnswer">
						<text class="detail-label">参考答案</text>
						<text class="detail-answer">{{ selectedQuestion.sampleAnswer }}</text>
					</view>

					<view class="detail-section">
						<text class="detail-label">💡 面试技巧</text>
						<text class="detail-tip" v-for="(tip, index) in selectedQuestion.tips" :key="index">
							{{ index + 1 }}. {{ tip }}
						</text>
					</view>

					<view class="detail-tags">
						<text class="detail-tag" v-for="(tag, index) in selectedQuestion.tags" :key="index">{{ tag }}</text>
					</view>
				</scroll-view>
			</view>
		</view>
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
			currentCategory: 'all',
			searchKeyword: '',
			showModal: false,
			selectedQuestion: {},
			bookmarks: [],
			categories: [
				{ id: 'all', name: '全部', icon: '📚', count: 0 },
				{ id: 'behavioral', name: '行为面试', icon: '💭', count: 0 },
				{ id: 'technical', name: '技术面试', icon: '💻', count: 0 },
				{ id: 'case', name: '案例面试', icon: '📊', count: 0 },
				{ id: 'hr', name: 'HR面试', icon: '🤝', count: 0 }
			],
			// 面试题库(示例题目,可扩展)
			questions: [
				// 行为面试
				{
					id: 1,
					category: 'behavioral',
					question: '请描述一次你在工作中遇到困难并克服的经历',
					tags: ['STAR法', '问题解决', '抗压能力'],
					answerTemplate: '使用STAR法回答:情境(Situation)、任务(Task)、行动(Action)、结果(Result)',
					sampleAnswer: '在上一份工作中(情境),我负责的项目遇到了技术难题导致进度延期(任务)。我主动加班研究解决方案,并与团队成员沟通协作(行动),最终提前2天完成项目交付,客户满意度达到95%(结果)。',
					tips: [
						'选择真实案例,避免编造',
						'突出你的主动性和创造力',
						'量化结果(如节省成本、提升效率)',
						'展示你从中学到的经验'
					]
				},
				{
					id: 2,
					category: 'behavioral',
					question: '你如何处理与同事的冲突?',
					tags: ['沟通能力', '团队协作', '冲突管理'],
					answerTemplate: '描述具体冲突 → 你的应对方式 → 沟通过程 → 最终结果',
					sampleAnswer: '曾与同事在项目方案上有分歧。我主动约他面谈,倾听他的想法,并分享我的考量。我们共同分析利弊,最终达成折中方案,项目顺利推进。',
					tips: [
						'展示你的同理心和倾听能力',
						'强调通过沟通解决问题',
						'避免指责他人',
						'说明学到的团队协作经验'
					]
				},
				{
					id: 3,
					category: 'behavioral',
					question: '描述一次你主导的项目或活动',
					tags: ['领导力', '项目管理', '主动性'],
					answerTemplate: '项目背景 → 你的角色 → 关键行动 → 成果与影响',
					sampleAnswer: '我主导了公司年度营销活动(背景)。作为项目负责人(角色),我制定详细计划、协调跨部门资源、监控执行进度(行动)。活动吸引了5000+用户参与,品牌曝光量提升200%(成果)。',
					tips: [
						'突出你的领导和组织能力',
						'说明如何激励团队成员',
						'量化项目成果',
						'提及遇到的挑战及应对'
					]
				},
				{
					id: 4,
					category: 'behavioral',
					question: '你如何平衡工作和生活?',
					tags: ['时间管理', '工作生活平衡', '自我管理'],
					answerTemplate: '时间管理方法 → 优先级排序 → 具体实践 → 效果',
					sampleAnswer: '我使用待办清单管理任务,按优先级排序。工作时间专注高效,下班后坚持运动和阅读。周末陪伴家人,保持身心健康。这让我在工作中保持高效,同时享受生活。',
					tips: [
						'展示你的时间管理能力',
						'说明工作高效的方法',
						'提及业余爱好和放松方式',
						'避免说"加班就是生活"'
					]
				},
				{
					id: 5,
					category: 'behavioral',
					question: '你最大的优点和缺点是什么?',
					tags: ['自我认知', '职业发展', '个人成长'],
					answerTemplate: '优点:具体特质 + 实例证明;缺点:真实但可改进 + 改进行动',
					sampleAnswer: '优点是学习能力强,我能快速掌握新技能并应用到工作中。缺点是有时过于追求完美导致效率下降,但我正在学习区分任务重要性,优先保证核心质量。',
					tips: [
						'优点要有具体案例支撑',
						'缺点要真实但不致命',
						'说明你如何改进缺点',
						'避免说"我没有缺点"或"我太完美主义"'
					]
				},

				// 技术面试(以互联网产品/运营为例)
				{
					id: 6,
					category: 'technical',
					question: '如何设计一个用户增长策略?',
					tags: ['用户增长', '产品运营', '数据分析'],
					answerTemplate: '现状分析 → 目标设定 → 策略设计 → 执行计划 → 效果评估',
					sampleAnswer: '首先分析现有用户来源和留存数据,设定增长目标(如3个月新增10万用户)。设计AARRR漏斗策略:获取(社交媒体投放)、激活(优化注册流程)、留存(Push通知)、变现(会员体系)、推荐(邀请奖励)。通过A/B测试优化各环节,定期review数据调整策略。',
					tips: [
						'展示数据驱动思维',
						'说明具体增长方法',
						'提及A/B测试和迭代',
						'关注ROI和长期价值'
					]
				},
				{
					id: 7,
					category: 'technical',
					question: '如何提升产品的用户留存率?',
					tags: ['用户留存', '产品设计', '用户体验'],
					answerTemplate: '留存分析 → 问题定位 → 策略设计 → 实施与验证',
					sampleAnswer: '首先通过留存曲线找到流失节点,分析流失原因(如功能复杂、价值不明确)。针对性优化:简化操作流程、强化核心功能、增加用户激励(积分体系)、定期触达(个性化推送)。通过cohort分析验证效果,持续迭代。',
					tips: [
						'使用数据定位问题',
						'提出具体改进措施',
						'说明如何验证效果',
						'关注不同阶段用户需求'
					]
				},
				{
					id: 8,
					category: 'technical',
					question: '如何评估一个功能是否值得开发?',
					tags: ['产品决策', '优先级管理', '需求分析'],
					answerTemplate: '价值评估 → 成本评估 → 优先级排序 → 决策',
					sampleAnswer: '使用RICE模型评估:Reach(影响用户数)、Impact(影响程度)、Confidence(把握度)、Effort(开发成本)。综合考虑用户需求强度、商业价值、技术可行性、战略匹配度。高价值低成本的功能优先开发。',
					tips: [
						'展示决策框架(如RICE、Kano)',
						'考虑多维度因素',
						'说明如何权衡取舍',
						'关注ROI和战略意义'
					]
				},

				// 案例面试
				{
					id: 9,
					category: 'case',
					question: '如果你是某短视频App的产品经理,如何提升DAU?',
					tags: ['产品策略', '用户增长', '商业分析'],
					answerTemplate: '现状分析 → 目标拆解 → 策略设计 → 优先级排序',
					sampleAnswer: '现状:分析当前DAU构成、使用时长、留存率。目标:3个月提升20% DAU。策略:1) 内容端:优化推荐算法、扶持优质创作者;2) 产品端:增加互动功能(弹幕、连麦);3) 运营端:话题挑战活动、Push通知优化;4) 渠道端:社交分享激励。优先级:先优化推荐算法(影响大成本低),再推互动功能。',
					tips: [
						'结构化思考(现状-目标-策略)',
						'多维度分析(产品/运营/技术)',
						'数据支撑观点',
						'考虑实施优先级和资源限制'
					]
				},
				{
					id: 10,
					category: 'case',
					question: '估算北京市每天外卖订单量',
					tags: ['数据分析', '市场估算', '逻辑思维'],
					answerTemplate: '拆解问题 → 假设前提 → 计算过程 → 得出结论',
					sampleAnswer: '假设:北京常住人口2000万,50%使用外卖(1000万人),平均每周点3次外卖,每天约430万单(1000万*3/7)。考虑工作日/周末差异,工作日占比60%(约500万单),周末40%(约350万单)。因此平均每天约430-500万单。',
					tips: [
						'清晰列出假设条件',
						'拆解问题(人口×使用率×频次)',
						'考虑不同场景差异',
						'结论合理即可,过程比结果重要'
					]
				},
				{
					id: 11,
					category: 'case',
					question: '设计一款面向老年人的社交产品',
					tags: ['产品设计', '用户研究', '创新思维'],
					answerTemplate: '用户分析 → 需求洞察 → 功能设计 → 商业模式',
					sampleAnswer: '用户:60+岁,退休,孤独感强,不熟悉复杂操作。需求:情感陪伴、兴趣社交、健康管理。功能:1) 语音/视频通话(大图标);2) 兴趣小组(广场舞、养生);3) 家庭相册(子女共享);4) 健康提醒(吃药、体检)。UI:超大字体、高对比度、语音交互。商业化:会员服务、健康产品电商。',
					tips: [
						'深入理解目标用户痛点',
						'功能设计贴合用户特点',
						'考虑产品可行性',
						'思考商业价值'
					]
				},

				// HR面试
				{
					id: 12,
					category: 'hr',
					question: '为什么选择我们公司?',
					tags: ['求职动机', '职业规划', '公司了解'],
					answerTemplate: '公司优势 → 岗位匹配 → 个人发展',
					sampleAnswer: '贵公司在XX行业处于领先地位,产品和文化都很吸引我。这个岗位与我的技能高度匹配,我在XX方面的经验能为团队创造价值。同时公司提供的成长机会符合我的职业规划,我希望在这里长期发展。',
					tips: [
						'提前研究公司背景、产品、文化',
						'说明你能为公司带来的价值',
						'展示你的诚意和热情',
						'避免只谈薪资福利'
					]
				},
				{
					id: 13,
					category: 'hr',
					question: '你的职业规划是什么?',
					tags: ['职业发展', '目标导向', '自我规划'],
					answerTemplate: '短期目标 → 中期目标 → 长期愿景',
					sampleAnswer: '短期(1-2年):深耕XX领域,成为团队核心成员,完成X个重点项目。中期(3-5年):晋升至XX职位,带领团队,拓展新业务。长期:成为行业专家,为公司战略发展贡献更大价值。',
					tips: [
						'展示清晰的职业路径',
						'目标与应聘岗位相关',
						'体现成长意愿和野心',
						'务实但有追求'
					]
				},
				{
					id: 14,
					category: 'hr',
					question: '你期望的薪资是多少?',
					tags: ['薪资谈判', '市场了解', '自我定位'],
					answerTemplate: '市场调研 → 自我评估 → 期望范围',
					sampleAnswer: '我了解到同行业同职级的薪资范围在XX-XX万。结合我X年经验和XX技能,我的期望薪资是XX万。当然,我更看重平台和发展机会,具体薪资可以协商。',
					tips: [
						'提前调研市场薪资水平',
						'给出合理范围而非固定数字',
						'强调能力和价值',
						'表示灵活和开放态度'
					]
				},
				{
					id: 15,
					category: 'hr',
					question: '你有什么问题要问我们吗?',
					tags: ['反向提问', '求职策略', '深入了解'],
					answerTemplate: '工作内容 → 团队文化 → 发展机会',
					sampleAnswer: '我想了解:1) 这个岗位的核心职责和考核标准是什么?2) 团队的工作氛围和协作方式如何?3) 公司对这个岗位的培养计划是怎样的?4) 接下来的面试流程和时间安排?',
					tips: [
						'准备3-5个有质量的问题',
						'避免问薪资福利(HR面可以问)',
						'体现你的专业性和热情',
						'不要说"没有问题"'
					]
				},
				{
					id: 16,
					category: 'hr',
					question: '你为什么从上家公司离职?',
					tags: ['离职原因', '职业动机', '沟通技巧'],
					answerTemplate: '客观原因 → 正面表述 → 未来期望',
					sampleAnswer: '上家公司给了我很好的成长机会,我也取得了一些成绩。但随着个人能力提升,我希望在更大的平台承担更多责任,这也是我选择贵公司的原因。',
					tips: [
						'避免抱怨前公司或同事',
						'正面表述离职原因(寻求成长)',
						'不要说薪资低、加班多等负面理由',
						'表达对新机会的期待'
					]
				}
			]
		};
	},
	computed: {
		filteredQuestions() {
			let result = this.questions;

			// 分类筛选
			if (this.currentCategory !== 'all') {
				result = result.filter(q => q.category === this.currentCategory);
			}

			// 关键词搜索
			if (this.searchKeyword.trim()) {
				const keyword = this.searchKeyword.toLowerCase();
				result = result.filter(q =>
					q.question.toLowerCase().includes(keyword) ||
					q.tags.some(tag => tag.toLowerCase().includes(keyword))
				);
			}

			return result;
		}
	},
	mounted() {
		// 加载书签
		this.loadBookmarks();
		// 计算各分类题目数量
		this.calculateCategoryCounts();
	},
	methods: {
		goBack() {
			uni.navigateBack();
		},

		switchCategory(categoryId) {
			this.currentCategory = categoryId;
		},

		searchQuestions() {
			// 搜索逻辑在computed中实现
		},

		viewQuestion(question) {
			this.selectedQuestion = question;
			this.showModal = true;
		},

		closeModal() {
			this.showModal = false;
		},

		getCategoryName(categoryId) {
			const category = this.categories.find(c => c.id === categoryId);
			return category ? category.name : '';
		},

		toggleBookmark(questionId) {
			const index = this.bookmarks.indexOf(questionId);
			if (index > -1) {
				this.bookmarks.splice(index, 1);
			} else {
				this.bookmarks.push(questionId);
			}
			this.saveBookmarks();
		},

		isBookmarked(questionId) {
			return this.bookmarks.includes(questionId);
		},

		loadBookmarks() {
			const saved = uni.getStorageSync('interview_bookmarks');
			if (saved) {
				this.bookmarks = JSON.parse(saved);
			}
		},

		saveBookmarks() {
			uni.setStorageSync('interview_bookmarks', JSON.stringify(this.bookmarks));
		},

		randomPractice() {
			const randomIndex = Math.floor(Math.random() * this.questions.length);
			this.viewQuestion(this.questions[randomIndex]);
		},

		calculateCategoryCounts() {
			this.categories[0].count = this.questions.length; // all
			this.categories[1].count = this.questions.filter(q => q.category === 'behavioral').length;
			this.categories[2].count = this.questions.filter(q => q.category === 'technical').length;
			this.categories[3].count = this.questions.filter(q => q.category === 'case').length;
			this.categories[4].count = this.questions.filter(q => q.category === 'hr').length;
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
/* 头部卡片 */
.header-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 10px;
	padding: 25px 20px;
	margin-bottom: 10px;
	text-align: center;
}

.header-icon {
	font-size: 40px;
	display: block;
	margin-bottom: 8px;
}

.header-title {
	display: block;
	font-size: 22px;
	font-weight: bold;
	color: #ffffff;
	margin-bottom: 5px;
}

.header-subtitle {
	display: block;
	font-size: 13px;
	color: rgba(255, 255, 255, 0.8);
}

/* 分类导航 */
.category-nav {
	display: flex;
	gap: 8px;
	margin-bottom: 10px;
	overflow-x: auto;
	padding-bottom: 5px;
}

.category-item {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	padding: 10px 15px;
	background-color: #ffffff;
	border-radius: 20px;
	border: 2px solid transparent;
}

.category-item.active {
	background-color: #e8eaf6;
	border-color: #667eea;
}

.category-icon {
	font-size: 18px;
	margin-right: 5px;
}

.category-name {
	font-size: 14px;
	color: #333333;
	font-weight: 500;
}

.category-count {
	font-size: 12px;
	color: #666666;
	margin-left: 3px;
}

/* 搜索框 */
.search-bar {
	position: relative;
	margin-bottom: 10px;
}

.search-input {
	width: 100%;
	padding: 12px 40px 12px 15px;
	background-color: #ffffff;
	border-radius: 10px;
	font-size: 14px;
	border: 1px solid #e5e5e5;
}

.search-icon {
	position: absolute;
	right: 15px;
	top: 50%;
	transform: translateY(-50%);
	font-size: 18px;
}

/* 问题列表 */
.questions-list {
	margin-bottom: 10px;
}

.question-item {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.question-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 10px;
}

.question-title {
	flex: 1;
	font-size: 15px;
	font-weight: 500;
	color: #333333;
	line-height: 22px;
}

.bookmark-icon {
	font-size: 20px;
	color: #ff9500;
	margin-left: 10px;
	flex-shrink: 0;
}

.question-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.question-tag {
	font-size: 11px;
	color: #667eea;
	background-color: #e8eaf6;
	padding: 4px 8px;
	border-radius: 4px;
}

/* 空状态 */
.empty-state {
	text-align: center;
	padding: 50px 20px;
}

.empty-icon {
	font-size: 48px;
	display: block;
	margin-bottom: 10px;
}

.empty-text {
	font-size: 14px;
	color: #999999;
}

/* 练习按钮 */
.practice-btn-container {
	margin-bottom: 20px;
}

.practice-btn {
	width: 100%;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #ffffff;
	font-size: 16px;
	font-weight: 600;
	border-radius: 10px;
	padding: 15px;
	border: none;
}

/* 弹窗 */
.modal {
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

.modal-content {
	width: 90%;
	max-height: 80vh;
	background-color: #ffffff;
	border-radius: 15px;
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15px 20px;
	border-bottom: 1px solid #e5e5e5;
}

.modal-title {
	font-size: 18px;
	font-weight: 600;
	color: #333333;
}

.modal-close {
	font-size: 32px;
	color: #999999;
	line-height: 1;
}

.modal-body {
	flex: 1;
	padding: 20px;
	overflow-y: auto;
}

.detail-question {
	display: block;
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	line-height: 24px;
	margin-bottom: 20px;
}

.detail-section {
	margin-bottom: 20px;
}

.detail-label {
	display: block;
	font-size: 14px;
	font-weight: 600;
	color: #667eea;
	margin-bottom: 8px;
}

.detail-category {
	display: block;
	font-size: 13px;
	color: #666666;
}

.detail-template {
	display: block;
	font-size: 14px;
	color: #333333;
	line-height: 22px;
	background-color: #f5f5f5;
	padding: 12px;
	border-radius: 8px;
}

.detail-answer {
	display: block;
	font-size: 14px;
	color: #333333;
	line-height: 22px;
	background-color: #e8f5e9;
	padding: 12px;
	border-radius: 8px;
}

.detail-tip {
	display: block;
	font-size: 13px;
	color: #666666;
	line-height: 22px;
	margin-bottom: 8px;
}

.detail-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	margin-top: 15px;
}

.detail-tag {
	font-size: 12px;
	color: #667eea;
	background-color: #e8eaf6;
	padding: 6px 10px;
	border-radius: 6px;
}
</style>











