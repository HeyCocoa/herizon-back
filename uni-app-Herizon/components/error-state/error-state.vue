<template>
	<!--
		错误状态组件
		用途:在请求失败、网络错误等场景显示友好的错误提示
		支持类型:network(网络错误)、server(服务器错误)、forbidden(权限错误)、error(通用错误)
	-->
	<view class="error-state">
		<text class="error-icon">{{ errorIcon }}</text>
		<text class="error-title">{{ errorTitle }}</text>
		<text class="error-message">{{ displayMessage }}</text>
		<button v-if="showRetry" class="retry-btn" @click="handleRetry">
			{{ retryText }}
		</button>
	</view>
</template>

<script>
/**
 * ErrorState 错误状态组件
 *
 * @description 用于显示各类错误状态,提供友好的错误提示和重试功能
 * @property {String} type - 错误类型:network, server, forbidden, error
 * @property {String} message - 自定义错误消息(可选)
 * @property {Boolean} showRetry - 是否显示重试按钮,默认true
 * @property {String} retryText - 重试按钮文字,默认"重试"
 * @event {Function} retry - 点击重试按钮时触发
 */
export default {
	name: 'ErrorState',
	props: {
		// 错误类型
		type: {
			type: String,
			default: 'error', // network, server, forbidden, error
			validator: (value) => {
				return ['network', 'server', 'forbidden', 'error'].includes(value)
			}
		},
		// 自定义错误消息
		message: {
			type: String,
			default: ''
		},
		// 是否显示重试按钮
		showRetry: {
			type: Boolean,
			default: true
		},
		// 重试按钮文字
		retryText: {
			type: String,
			default: '重试'
		}
	},
	computed: {
		/**
		 * 根据错误类型返回对应的图标
		 */
		errorIcon() {
			const iconMap = {
				network: '📡',
				server: '⚠️',
				forbidden: '🔒',
				error: '❌'
			}
			return iconMap[this.type] || '❌'
		},

		/**
		 * 根据错误类型返回对应的标题
		 */
		errorTitle() {
			const titleMap = {
				network: '网络连接失败',
				server: '服务器繁忙',
				forbidden: '权限不足',
				error: '出错了'
			}
			return titleMap[this.type] || '出错了'
		},

		/**
		 * 显示的错误消息(优先使用自定义消息)
		 */
		displayMessage() {
			if (this.message) {
				return this.message
			}

			const messageMap = {
				network: '请检查网络连接后重试',
				server: '服务器正在维护中,请稍后重试',
				forbidden: '您没有访问权限',
				error: '请稍后重试'
			}
			return messageMap[this.type] || '请稍后重试'
		}
	},
	methods: {
		/**
		 * 处理重试按钮点击
		 */
		handleRetry() {
			this.$emit('retry')
		}
	}
}
</script>

<style lang="scss" scoped>
/* 错误状态容器 */
.error-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 60rpx;
	text-align: center;
	min-height: 400rpx;
}

/* 错误图标 */
.error-icon {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.8;
}

/* 错误标题 */
.error-title {
	font-size: 32rpx;
	color: #333;
	font-weight: 500;
	margin-bottom: 16rpx;
}

/* 错误消息 */
.error-message {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 40rpx;
	padding: 0 20rpx;
}

/* 重试按钮 */
.retry-btn {
	background: #f33e54;
	color: white;
	border: none;
	padding: 24rpx 60rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	box-shadow: 0 4rpx 12rpx rgba(243, 62, 84, 0.3);
	transition: all 0.3s;
}

/* 重试按钮点击效果 */
.retry-btn:active {
	opacity: 0.8;
	transform: scale(0.98);
}
</style>
