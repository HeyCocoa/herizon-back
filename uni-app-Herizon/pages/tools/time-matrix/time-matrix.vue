<template>
	<view class="page-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: topNavPadding }">
			<view class="nav-left" @click="goBack">
				<text class="icon">←</text>
			</view>
			<view class="nav-title">时间管理矩阵</view>
			<view class="nav-right" @click="showHelp">
				<text class="help-icon">?</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-scroll" scroll-y>
			<!-- 说明卡片 -->
			<view class="info-card">
				<text class="info-icon">⏰</text>
				<text class="info-text">基于艾森豪威尔矩阵,帮你区分任务优先级,提升工作效率</text>
			</view>

			<!-- 四象限矩阵 -->
			<view class="matrix-container">
				<view class="matrix-row">
					<view class="matrix-label top-label">重要</view>
				</view>
				<view class="matrix-row">
					<view class="matrix-label left-label">紧急</view>
					<view class="matrix-grid">
						<!-- 第一象限:重要且紧急 -->
						<view class="quadrant q1" @click="selectQuadrant(1)">
							<view class="quadrant-header">
								<text class="quadrant-title">立即处理</text>
								<text class="quadrant-count">({{ quadrants[0].tasks.length }})</text>
							</view>
							<view class="task-list">
								<view class="task-item" v-for="(task, index) in quadrants[0].tasks" :key="index" @longpress="deleteTask(1, index)">
									<text class="task-text">{{ task }}</text>
								</view>
								<view class="empty-hint" v-if="quadrants[0].tasks.length === 0">
									<text class="empty-text">点击添加任务</text>
								</view>
							</view>
						</view>

						<!-- 第二象限:重要不紧急 -->
						<view class="quadrant q2" @click="selectQuadrant(2)">
							<view class="quadrant-header">
								<text class="quadrant-title">计划安排</text>
								<text class="quadrant-count">({{ quadrants[1].tasks.length }})</text>
							</view>
							<view class="task-list">
								<view class="task-item" v-for="(task, index) in quadrants[1].tasks" :key="index" @longpress="deleteTask(2, index)">
									<text class="task-text">{{ task }}</text>
								</view>
								<view class="empty-hint" v-if="quadrants[1].tasks.length === 0">
									<text class="empty-text">点击添加任务</text>
								</view>
							</view>
						</view>
					</view>
					<view class="matrix-label right-label">不紧急</view>
				</view>
				<view class="matrix-row">
					<view class="matrix-label left-label" style="visibility: hidden;">占位</view>
					<view class="matrix-grid">
						<!-- 第三象限:紧急不重要 -->
						<view class="quadrant q3" @click="selectQuadrant(3)">
							<view class="quadrant-header">
								<text class="quadrant-title">授权他人</text>
								<text class="quadrant-count">({{ quadrants[2].tasks.length }})</text>
							</view>
							<view class="task-list">
								<view class="task-item" v-for="(task, index) in quadrants[2].tasks" :key="index" @longpress="deleteTask(3, index)">
									<text class="task-text">{{ task }}</text>
								</view>
								<view class="empty-hint" v-if="quadrants[2].tasks.length === 0">
									<text class="empty-text">点击添加任务</text>
								</view>
							</view>
						</view>

						<!-- 第四象限:不重要不紧急 -->
						<view class="quadrant q4" @click="selectQuadrant(4)">
							<view class="quadrant-header">
								<text class="quadrant-title">减少/排除</text>
								<text class="quadrant-count">({{ quadrants[3].tasks.length }})</text>
							</view>
							<view class="task-list">
								<view class="task-item" v-for="(task, index) in quadrants[3].tasks" :key="index" @longpress="deleteTask(4, index)">
									<text class="task-text">{{ task }}</text>
								</view>
								<view class="empty-hint" v-if="quadrants[3].tasks.length === 0">
									<text class="empty-text">点击添加任务</text>
								</view>
							</view>
						</view>
					</view>
				</view>
				<view class="matrix-row">
					<view class="matrix-label bottom-label">不重要</view>
				</view>
			</view>

			<!-- 操作提示 -->
			<view class="tips-card">
				<text class="tips-title">💡 使用技巧</text>
				<text class="tips-item">• 点击象限添加任务</text>
				<text class="tips-item">• 长按任务删除</text>
				<text class="tips-item">• 优先处理第一象限(红色)</text>
				<text class="tips-item">• 重点投入第二象限(绿色),预防问题</text>
			</view>

			<!-- 象限说明 -->
			<view class="guide-card">
				<text class="guide-title">📚 四象限说明</text>
				<view class="guide-item">
					<text class="guide-number q1-bg">1</text>
					<view class="guide-content">
						<text class="guide-name">重要且紧急 - 立即处理</text>
						<text class="guide-desc">危机、截止日期临近的任务、紧急问题</text>
						<text class="guide-example">例:明天要提交的报告、客户投诉</text>
					</view>
				</view>
				<view class="guide-item">
					<text class="guide-number q2-bg">2</text>
					<view class="guide-content">
						<text class="guide-name">重要不紧急 - 计划安排</text>
						<text class="guide-desc">长期规划、能力提升、关系维护</text>
						<text class="guide-example">例:职业规划、学习新技能、健康管理</text>
					</view>
				</view>
				<view class="guide-item">
					<text class="guide-number q3-bg">3</text>
					<view class="guide-content">
						<text class="guide-name">紧急不重要 - 授权他人</text>
						<text class="guide-desc">部分会议、电话、邮件、打断</text>
						<text class="guide-example">例:临时的小任务、可转交的工作</text>
					</view>
				</view>
				<view class="guide-item">
					<text class="guide-number q4-bg">4</text>
					<view class="guide-content">
						<text class="guide-name">不重要不紧急 - 减少/排除</text>
						<text class="guide-desc">消遣娱乐、无价值的社交、浪费时间的活动</text>
						<text class="guide-example">例:刷短视频、无目的浏览网页</text>
					</view>
				</view>
			</view>

			<!-- 底部间距 -->
			<view class="bottom-space"></view>
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
			quadrants: [
				{ id: 1, name: '立即处理', tasks: [] },
				{ id: 2, name: '计划安排', tasks: [] },
				{ id: 3, name: '授权他人', tasks: [] },
				{ id: 4, name: '减少/排除', tasks: [] }
			]
		};
	},
	mounted() {
		this.loadTasks();
	},
	methods: {
		goBack() {
			uni.navigateBack();
		},

		showHelp() {
			uni.showModal({
				title: '关于时间管理矩阵',
				content: '艾森豪威尔矩阵帮助你根据任务的重要性和紧急性进行分类管理。\n\n重点投入第二象限(重要不紧急),可以减少第一象限(危机)的发生。',
				showCancel: false,
				confirmText: '知道了'
			});
		},

		selectQuadrant(quadrantId) {
			const quadrant = this.quadrants[quadrantId - 1];
			uni.showModal({
				title: `添加任务 - ${quadrant.name}`,
				editable: true,
				placeholderText: '输入任务内容...',
				success: (res) => {
					if (res.confirm && res.content && res.content.trim()) {
						this.addTask(quadrantId, res.content.trim());
					}
				}
			});
		},

		addTask(quadrantId, taskText) {
			const index = quadrantId - 1;
			this.quadrants[index].tasks.push(taskText);
			this.saveTasks();
			uni.showToast({
				title: '已添加',
				icon: 'success',
				duration: 1500
			});
		},

		deleteTask(quadrantId, taskIndex) {
			const index = quadrantId - 1;
			const taskText = this.quadrants[index].tasks[taskIndex];
			uni.showModal({
				title: '删除任务',
				content: `确定删除"${taskText}"吗?`,
				success: (res) => {
					if (res.confirm) {
						this.quadrants[index].tasks.splice(taskIndex, 1);
						this.saveTasks();
						uni.showToast({
							title: '已删除',
							icon: 'success',
							duration: 1500
						});
					}
				}
			});
		},

		saveTasks() {
			const data = {
				quadrants: this.quadrants,
				timestamp: Date.now()
			};
			uni.setStorageSync('time_matrix_tasks', JSON.stringify(data));
		},

		loadTasks() {
			try {
				const saved = uni.getStorageSync('time_matrix_tasks');
				if (saved) {
					const data = JSON.parse(saved);
					if (data.quadrants) {
						this.quadrants = data.quadrants;
					}
				}
			} catch (e) {
				console.error('Failed to load tasks:', e);
			}
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
.nav-right {
	display: flex;
	justify-content: flex-end;
}

.help-icon {
	width: 24px;
	height: 24px;
	line-height: 24px;
	text-align: center;
	background-color: #667eea;
	color: #ffffff;
	border-radius: 50%;
	font-size: 14px;
	font-weight: bold;
}

.content-scroll {
  flex: 1;
  padding: 24px 24px 40px;
  box-sizing: border-box;
}
/* 信息卡片 */
.info-card {
	display: flex;
	align-items: center;
	padding: 12px 15px;
	margin-bottom: 10px;
	background-color: #fff7ed;
	border-radius: 10px;
	border-left: 3px solid #ff9500;
}

.info-icon {
	font-size: 20px;
	margin-right: 10px;
}

.info-text {
	flex: 1;
	font-size: 14px;
	color: #8b5a00;
	line-height: 20px;
}

/* 矩阵容器 */
.matrix-container {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.matrix-row {
	display: flex;
	align-items: stretch;
}

.matrix-label {
	font-size: 14px;
	font-weight: 600;
	color: #666666;
	display: flex;
	align-items: center;
	justify-content: center;
}

.top-label, .bottom-label {
	width: 100%;
	height: 30px;
	margin-left: 50px;
}

.left-label, .right-label {
	width: 50px;
	writing-mode: vertical-rl;
}

.matrix-grid {
	flex: 1;
	display: flex;
	gap: 10px;
	min-height: 180px;
}

.quadrant {
	flex: 1;
	border-radius: 8px;
	padding: 12px;
	display: flex;
	flex-direction: column;
}

.q1 {
	background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
	border: 2px solid #ef5350;
}

.q2 {
	background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
	border: 2px solid #66bb6a;
}

.q3 {
	background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
	border: 2px solid #ffa726;
}

.q4 {
	background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
	border: 2px solid #ab47bc;
}

.quadrant-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 10px;
	padding-bottom: 8px;
	border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.quadrant-title {
	font-size: 14px;
	font-weight: 600;
	color: #333333;
}

.quadrant-count {
	font-size: 12px;
	color: #666666;
}

.task-list {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.task-item {
	background-color: rgba(255, 255, 255, 0.8);
	border-radius: 6px;
	padding: 8px 10px;
}

.task-text {
	font-size: 13px;
	color: #333333;
	line-height: 18px;
}

.empty-hint {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.empty-text {
	font-size: 12px;
	color: #999999;
}

/* 提示卡片 */
.tips-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.tips-title {
	display: block;
	font-size: 15px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 10px;
}

.tips-item {
	display: block;
	font-size: 13px;
	color: #666666;
	line-height: 22px;
	margin-bottom: 5px;
}

/* 指南卡片 */
.guide-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.guide-title {
	display: block;
	font-size: 15px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 15px;
}

.guide-item {
	display: flex;
	margin-bottom: 15px;
	padding-bottom: 15px;
	border-bottom: 1px solid #e5e5e5;
}

.guide-item:last-child {
	border-bottom: none;
	margin-bottom: 0;
	padding-bottom: 0;
}

.guide-number {
	width: 30px;
	height: 30px;
	line-height: 30px;
	text-align: center;
	color: #ffffff;
	font-size: 16px;
	font-weight: bold;
	border-radius: 6px;
	margin-right: 12px;
	flex-shrink: 0;
}

.q1-bg {
	background-color: #ef5350;
}

.q2-bg {
	background-color: #66bb6a;
}

.q3-bg {
	background-color: #ffa726;
}

.q4-bg {
	background-color: #ab47bc;
}

.guide-content {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.guide-name {
	font-size: 14px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 5px;
}

.guide-desc {
	font-size: 13px;
	color: #666666;
	line-height: 20px;
	margin-bottom: 5px;
}

.guide-example {
	font-size: 12px;
	color: #999999;
	font-style: italic;
}

/* 底部间距 */
.bottom-space {
	height: 20px;
}
</style>












