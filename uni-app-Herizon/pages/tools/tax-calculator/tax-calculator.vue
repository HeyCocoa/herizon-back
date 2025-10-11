<template>
	<view class="page-container">
		<!-- 顶部导航栏 -->
		<view class="top-nav" :style="{ paddingTop: topNavPadding }">
			<view class="nav-left" @click="goBack">
				<text class="icon">←</text>
			</view>
			<view class="nav-title">个税计算器</view>
			<view class="nav-right"></view>
		</view>

		<!-- 内容区域 -->
		<scroll-view class="content-scroll" scroll-y>
			<!-- 说明卡片 -->
			<view class="info-card">
				<text class="info-icon">💡</text>
				<text class="info-text">基于2024年中国个人所得税法,帮助您快速计算税后收入</text>
			</view>

			<!-- 输入表单 -->
			<view class="form-card">
				<view class="form-title">收入信息</view>

				<view class="form-item">
					<text class="label">税前月薪 (元)</text>
					<input
						class="input"
						type="digit"
						v-model="form.grossSalary"
						placeholder="请输入税前月薪"
						@input="calculate"
					/>
				</view>

				<view class="form-item">
					<text class="label">专项附加扣除 (元/月)</text>
					<input
						class="input"
						type="digit"
						v-model="form.specialDeduction"
						placeholder="子女教育、房贷等"
						@input="calculate"
					/>
				</view>

				<view class="form-divider">五险一金比例 (自动计算)</view>

				<view class="form-item">
					<text class="label">养老保险</text>
					<view class="input-group">
						<input class="input-small" type="digit" v-model="form.pensionRate" @input="calculate" />
						<text class="unit">%</text>
					</view>
				</view>

				<view class="form-item">
					<text class="label">医疗保险</text>
					<view class="input-group">
						<input class="input-small" type="digit" v-model="form.medicalRate" @input="calculate" />
						<text class="unit">%</text>
					</view>
				</view>

				<view class="form-item">
					<text class="label">失业保险</text>
					<view class="input-group">
						<input class="input-small" type="digit" v-model="form.unemploymentRate" @input="calculate" />
						<text class="unit">%</text>
					</view>
				</view>

				<view class="form-item">
					<text class="label">住房公积金</text>
					<view class="input-group">
						<input class="input-small" type="digit" v-model="form.housingFundRate" @input="calculate" />
						<text class="unit">%</text>
					</view>
				</view>
			</view>

			<!-- 计算结果 -->
			<view class="result-card" v-if="result.netSalary > 0">
				<view class="result-title">计算结果</view>

				<view class="result-main">
					<text class="result-label">税后实际收入</text>
					<text class="result-value">¥ {{ result.netSalary.toFixed(2) }}</text>
				</view>

				<view class="result-breakdown">
					<view class="breakdown-item">
						<text class="breakdown-label">税前月薪</text>
						<text class="breakdown-value">¥ {{ parseFloat(form.grossSalary || 0).toFixed(2) }}</text>
					</view>
					<view class="breakdown-item">
						<text class="breakdown-label">五险一金扣除</text>
						<text class="breakdown-value deduction">-¥ {{ result.socialInsurance.toFixed(2) }}</text>
					</view>
					<view class="breakdown-item">
						<text class="breakdown-label">个人所得税</text>
						<text class="breakdown-value deduction">-¥ {{ result.tax.toFixed(2) }}</text>
					</view>
					<view class="breakdown-item">
						<text class="breakdown-label">专项附加扣除</text>
						<text class="breakdown-value">-¥ {{ parseFloat(form.specialDeduction || 0).toFixed(2) }}</text>
					</view>
				</view>

				<!-- 收入分配可视化 -->
				<view class="chart-container">
					<view class="chart-title">收入分配</view>
					<view class="chart-bar">
						<view
							class="bar-segment net-income"
							:style="{width: result.netSalaryPercent + '%'}"
						>
							<text class="bar-label" v-if="result.netSalaryPercent > 15">{{ result.netSalaryPercent.toFixed(1) }}%</text>
						</view>
						<view
							class="bar-segment social-insurance"
							:style="{width: result.socialInsurancePercent + '%'}"
						>
							<text class="bar-label" v-if="result.socialInsurancePercent > 8">{{ result.socialInsurancePercent.toFixed(1) }}%</text>
						</view>
						<view
							class="bar-segment tax"
							:style="{width: result.taxPercent + '%'}"
						>
							<text class="bar-label" v-if="result.taxPercent > 5">{{ result.taxPercent.toFixed(1) }}%</text>
						</view>
					</view>
					<view class="chart-legend">
						<view class="legend-item">
							<view class="legend-color net-income"></view>
							<text class="legend-text">税后收入</text>
						</view>
						<view class="legend-item">
							<view class="legend-color social-insurance"></view>
							<text class="legend-text">五险一金</text>
						</view>
						<view class="legend-item">
							<view class="legend-color tax"></view>
							<text class="legend-text">个人所得税</text>
						</view>
					</view>
				</view>

				<!-- 税率说明 -->
				<view class="tax-info">
					<text class="tax-info-title">适用税率</text>
					<text class="tax-info-text">应纳税所得额: ¥{{ result.taxableIncome.toFixed(2) }}</text>
					<text class="tax-info-text">税率: {{ result.taxRate }}% | 速算扣除: ¥{{ result.quickDeduction }}</text>
				</view>
			</view>

			<!-- 税率表 -->
			<view class="table-card">
				<view class="table-title">2024年个人所得税税率表(月度)</view>
				<view class="table-header">
					<text class="table-col col-1">级数</text>
					<text class="table-col col-2">应纳税所得额</text>
					<text class="table-col col-3">税率</text>
					<text class="table-col col-4">速算扣除</text>
				</view>
				<view class="table-row" v-for="(bracket, index) in taxBrackets" :key="index">
					<text class="table-col col-1">{{ index + 1 }}</text>
					<text class="table-col col-2">{{ bracket.range }}</text>
					<text class="table-col col-3">{{ (bracket.rate * 100).toFixed(0) }}%</text>
					<text class="table-col col-4">{{ bracket.deduction }}</text>
				</view>
			</view>

			<!-- 使用说明 -->
			<view class="tips-card">
				<view class="tips-title">💡 使用说明</view>
				<view class="tips-list">
					<text class="tips-item">• 五险一金比例已预设为北京标准,可根据实际情况调整</text>
					<text class="tips-item">• 专项附加扣除包括:子女教育、继续教育、大病医疗、住房贷款利息、住房租金、赡养老人</text>
					<text class="tips-item">• 起征点为5000元/月(60000元/年)</text>
					<text class="tips-item">• 本计算器仅供参考,实际以税务机关为准</text>
				</view>
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
			form: {
				grossSalary: '',  // 税前月薪
				specialDeduction: '',  // 专项附加扣除
				pensionRate: 8,  // 养老保险 (北京标准)
				medicalRate: 2,  // 医疗保险
				unemploymentRate: 0.2,  // 失业保险
				housingFundRate: 12  // 住房公积金
			},
			result: {
				netSalary: 0,  // 税后收入
				socialInsurance: 0,  // 五险一金
				tax: 0,  // 个人所得税
				taxableIncome: 0,  // 应纳税所得额
				taxRate: 0,  // 适用税率
				quickDeduction: 0,  // 速算扣除
				netSalaryPercent: 0,
				socialInsurancePercent: 0,
				taxPercent: 0
			},
			// 2024年个税税率表(月度)
			taxBrackets: [
				{ range: '≤ 3,000', min: 0, max: 3000, rate: 0.03, deduction: 0 },
				{ range: '3,000 - 12,000', min: 3000, max: 12000, rate: 0.10, deduction: 210 },
				{ range: '12,000 - 25,000', min: 12000, max: 25000, rate: 0.20, deduction: 1410 },
				{ range: '25,000 - 35,000', min: 25000, max: 35000, rate: 0.25, deduction: 2660 },
				{ range: '35,000 - 55,000', min: 35000, max: 55000, rate: 0.30, deduction: 4410 },
				{ range: '55,000 - 80,000', min: 55000, max: 80000, rate: 0.35, deduction: 7160 },
				{ range: '> 80,000', min: 80000, max: Infinity, rate: 0.45, deduction: 15160 }
			]
		};
	},
	methods: {
		goBack() {
			uni.navigateBack();
		},

		// 计算税后收入
		calculate() {
			const grossSalary = parseFloat(this.form.grossSalary) || 0;

			if (grossSalary === 0) {
				this.result.netSalary = 0;
				return;
			}

			// 1. 计算五险一金扣除
			const pensionRate = parseFloat(this.form.pensionRate) / 100;
			const medicalRate = parseFloat(this.form.medicalRate) / 100;
			const unemploymentRate = parseFloat(this.form.unemploymentRate) / 100;
			const housingFundRate = parseFloat(this.form.housingFundRate) / 100;

			const socialInsurance = grossSalary * (pensionRate + medicalRate + unemploymentRate + housingFundRate);

			// 2. 计算应纳税所得额
			const standardDeduction = 5000;  // 起征点
			const specialDeduction = parseFloat(this.form.specialDeduction) || 0;
			const taxableIncome = grossSalary - socialInsurance - standardDeduction - specialDeduction;

			// 3. 计算个人所得税
			let tax = 0;
			let taxRate = 0;
			let quickDeduction = 0;

			if (taxableIncome > 0) {
				// 查找适用税率
				for (let bracket of this.taxBrackets) {
					if (taxableIncome > bracket.min && taxableIncome <= bracket.max) {
						taxRate = bracket.rate * 100;
						quickDeduction = bracket.deduction;
						tax = taxableIncome * bracket.rate - bracket.deduction;
						break;
					}
				}
			}

			// 4. 计算税后收入
			const netSalary = grossSalary - socialInsurance - tax;

			// 5. 计算百分比(用于可视化)
			const netSalaryPercent = (netSalary / grossSalary) * 100;
			const socialInsurancePercent = (socialInsurance / grossSalary) * 100;
			const taxPercent = (tax / grossSalary) * 100;

			// 6. 更新结果
			this.result = {
				netSalary,
				socialInsurance,
				tax,
				taxableIncome,
				taxRate,
				quickDeduction,
				netSalaryPercent,
				socialInsurancePercent,
				taxPercent
			};
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
/* 信息卡片 */
.info-card {
	background-color: #fff7ed;
	border-radius: 10px;
	padding: 12px 15px;
	margin-bottom: 10px;
	display: flex;
	align-items: center;
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

/* 表单卡片 */
.form-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.form-title {
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 15px;
}

.form-divider {
	font-size: 14px;
	font-weight: 600;
	color: #666666;
	margin: 15px 0 10px 0;
	padding-bottom: 8px;
	border-bottom: 1px solid #e5e5e5;
}

.form-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 15px;
}

.label {
	font-size: 15px;
	color: #333333;
	flex-shrink: 0;
}

.input {
	flex: 1;
	text-align: right;
	font-size: 15px;
	color: #333333;
	padding: 8px 12px;
	background-color: #f5f5f5;
	border-radius: 6px;
	margin-left: 10px;
}

.input-group {
	display: flex;
	align-items: center;
}

.input-small {
	width: 80px;
	text-align: right;
	font-size: 15px;
	color: #333333;
	padding: 8px 12px;
	background-color: #f5f5f5;
	border-radius: 6px;
}

.unit {
	font-size: 15px;
	color: #666666;
	margin-left: 8px;
}

/* 计算结果 */
.result-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.result-title {
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 15px;
}

.result-main {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 10px;
	padding: 20px;
	text-align: center;
	margin-bottom: 15px;
}

.result-label {
	display: block;
	font-size: 14px;
	color: rgba(255, 255, 255, 0.8);
	margin-bottom: 8px;
}

.result-value {
	display: block;
	font-size: 32px;
	font-weight: bold;
	color: #ffffff;
}

.result-breakdown {
	border-top: 1px solid #e5e5e5;
	padding-top: 15px;
}

.breakdown-item {
	display: flex;
	justify-content: space-between;
	margin-bottom: 10px;
}

.breakdown-label {
	font-size: 14px;
	color: #666666;
}

.breakdown-value {
	font-size: 14px;
	color: #333333;
	font-weight: 500;
}

.breakdown-value.deduction {
	color: #ff3b30;
}

/* 收入分配图表 */
.chart-container {
	margin-top: 20px;
	padding-top: 15px;
	border-top: 1px solid #e5e5e5;
}

.chart-title {
	font-size: 14px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 10px;
}

.chart-bar {
	display: flex;
	height: 30px;
	border-radius: 6px;
	overflow: hidden;
	margin-bottom: 10px;
}

.bar-segment {
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
}

.bar-segment.net-income {
	background-color: #34c759;
}

.bar-segment.social-insurance {
	background-color: #ff9500;
}

.bar-segment.tax {
	background-color: #ff3b30;
}

.bar-label {
	font-size: 12px;
	color: #ffffff;
	font-weight: 600;
}

.chart-legend {
	display: flex;
	justify-content: space-around;
	margin-top: 10px;
}

.legend-item {
	display: flex;
	align-items: center;
}

.legend-color {
	width: 12px;
	height: 12px;
	border-radius: 2px;
	margin-right: 5px;
}

.legend-color.net-income {
	background-color: #34c759;
}

.legend-color.social-insurance {
	background-color: #ff9500;
}

.legend-color.tax {
	background-color: #ff3b30;
}

.legend-text {
	font-size: 12px;
	color: #666666;
}

/* 税率说明 */
.tax-info {
	margin-top: 15px;
	padding: 12px;
	background-color: #f5f5f5;
	border-radius: 6px;
}

.tax-info-title {
	display: block;
	font-size: 13px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 6px;
}

.tax-info-text {
	display: block;
	font-size: 12px;
	color: #666666;
	margin-bottom: 3px;
}

/* 税率表 */
.table-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 10px;
}

.table-title {
	font-size: 16px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 12px;
}

.table-header,
.table-row {
	display: flex;
	padding: 10px 0;
	border-bottom: 1px solid #e5e5e5;
}

.table-header {
	background-color: #f5f5f5;
	font-weight: 600;
	border-radius: 6px;
	padding: 10px 5px;
	margin-bottom: 5px;
}

.table-col {
	font-size: 12px;
	color: #333333;
	text-align: center;
}

.col-1 {
	width: 15%;
}

.col-2 {
	width: 35%;
}

.col-3 {
	width: 25%;
}

.col-4 {
	width: 25%;
}

/* 使用说明 */
.tips-card {
	background-color: #ffffff;
	border-radius: 10px;
	padding: 15px;
	margin-bottom: 20px;
}

.tips-title {
	font-size: 15px;
	font-weight: 600;
	color: #333333;
	margin-bottom: 10px;
}

.tips-list {
	display: flex;
	flex-direction: column;
}

.tips-item {
	font-size: 13px;
	color: #666666;
	line-height: 22px;
	margin-bottom: 5px;
}
</style>












