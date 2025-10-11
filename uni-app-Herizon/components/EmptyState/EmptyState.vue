<template>
	<!--
		空状态组件(增强版)
		用途:在数据为空或加载失败时显示友好提示
		支持类型:default(默认)、posts(帖子)、search(搜索)、collection(收藏)、notification(通知)、network(网络错误)、empty(空数据)
	-->
	<view class="empty-state">
		<!-- 使用emoji图标作为默认显示,如果需要图片则使用image标签 -->
		<text v-if="useEmoji" class="empty-icon-emoji">{{ emojiIcon }}</text>
		<image v-else :src="iconSrc" class="empty-icon" mode="aspectFit"></image>

		<text class="empty-title">{{ displayTitle }}</text>
		<text class="empty-hint" v-if="displayHint">{{ displayHint }}</text>

		<!-- 操作按钮 -->
		<button v-if="showAction" @click="onAction" :class="['action-btn', actionType]">
			{{ actionText }}
		</button>

		<!-- 重试按钮(向后兼容) -->
		<button v-if="showRetry && !showAction" @click="onRetry" class="retry-btn">重试</button>
	</view>
</template>

<script>
/**
 * EmptyState 空状态组件(增强版)
 *
 * @description 通用空状态组件,用于显示数据为空、网络错误等场景
 * @property {String} type - 空状态类型:default, posts, search, collection, notification, network, empty
 * @property {String} title - 主标题文字(可选,不传则使用预设文字)
 * @property {String} hint - 提示文字(可选,不传则使用预设文字)
 * @property {Boolean} showRetry - 是否显示重试按钮,默认false(向后兼容)
 * @property {Boolean} showAction - 是否显示操作按钮,默认false
 * @property {String} actionText - 操作按钮文字
 * @property {String} actionType - 操作按钮类型:primary, success, default
 * @event {Function} retry - 点击重试按钮时触发
 * @event {Function} action - 点击操作按钮时触发
 */
export default {
	name: 'EmptyState',
	props: {
		// 空状态类型
		type: {
			type: String,
			default: 'default',
			validator: (value) => {
				return ['default', 'posts', 'search', 'collection', 'notification', 'network', 'empty'].includes(value)
			}
		},
		// 主标题(可选)
		title: {
			type: String,
			default: ''
		},
		// 提示文字(可选)
		hint: {
			type: String,
			default: ''
		},
		// 是否显示重试按钮
		showRetry: {
			type: Boolean,
			default: false
		},
		// 是否显示操作按钮
		showAction: {
			type: Boolean,
			default: false
		},
		// 操作按钮文字
		actionText: {
			type: String,
			default: '去发布'
		},
		// 操作按钮类型
		actionType: {
			type: String,
			default: 'primary'
		}
	},
	computed: {
		/**
		 * 是否使用emoji图标(默认使用emoji,更简单)
		 */
		useEmoji() {
			return true // 优先使用emoji图标
		},

		/**
		 * 根据类型返回emoji图标
		 */
		emojiIcon() {
			const emojiMap = {
				'default': '📝',
				'posts': '📝',
				'search': '🔍',
				'collection': '⭐',
				'notification': '🔔',
				'network': '📡',
				'empty': '📭'
			}
			return emojiMap[this.type] || '📝'
		},

		/**
		 * 根据类型返回对应的图标路径(备用)
		 */
		iconSrc() {
			const iconMap = {
				'default': '/static/empty-default.png',
				'posts': '/static/empty-posts.png',
				'search': '/static/empty-search.png',
				'collection': '/static/empty-collection.png',
				'notification': '/static/empty-notification.png',
				'network': '/static/empty-network.png',
				'empty': '/static/empty-data.png'
			}
			return iconMap[this.type] || iconMap['default']
		},

		/**
		 * 显示的标题(优先使用props传入的title,否则使用预设)
		 */
		displayTitle() {
			if (this.title) {
				return this.title
			}

			const titleMap = {
				'default': '暂无数据',
				'posts': '还没有帖子',
				'search': '没有找到相关内容',
				'collection': '还没有收藏内容',
				'notification': '暂无新通知',
				'network': '网络连接失败',
				'empty': '暂无内容'
			}
			return titleMap[this.type] || '暂无数据'
		},

		/**
		 * 显示的提示文字(优先使用props传入的hint,否则使用预设)
		 */
		displayHint() {
			if (this.hint) {
				return this.hint
			}

			const hintMap = {
				'default': '',
				'posts': '快来发布第一篇帖子吧',
				'search': '试试其他关键词',
				'collection': '收藏喜欢的内容,方便随时查看',
				'notification': '有新消息时会在这里显示',
				'network': '请检查网络连接后重试',
				'empty': ''
			}
			return hintMap[this.type] || ''
		}
	},
	methods: {
		/**
		 * 点击重试按钮
		 */
		onRetry() {
			this.$emit('retry')
		},

		/**
		 * 点击操作按钮
		 */
		onAction() {
			this.$emit('action')
		}
	}
}
</script>

<style lang="scss" scoped>
/* 空状态容器 - 垂直居中布局 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 40rpx;
	min-height: 400rpx;
}

/* 空状态图标(图片) */
.empty-icon {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 30rpx;
	opacity: 0.6;
}

/* 空状态图标(Emoji) */
.empty-icon-emoji {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.8;
}

/* 主标题 */
.empty-title {
	font-size: 32rpx;
	color: #333;
	font-weight: 500;
	margin-bottom: 15rpx;
	text-align: center;
}

/* 提示文字 */
.empty-hint {
	font-size: 28rpx;
	color: #999;
	text-align: center;
	line-height: 1.6;
	padding: 0 20rpx;
	margin-bottom: 20rpx;
}

/* 操作按钮 */
.action-btn {
	margin-top: 40rpx;
	padding: 20rpx 60rpx;
	color: white;
	border-radius: 50rpx;
	font-size: 28rpx;
	border: none;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
	transition: all 0.3s;

	&.primary {
		background: linear-gradient(135deg, #007aff 0%, #5ac8fa 100%);
	}

	&.success {
		background: linear-gradient(135deg, #34c759 0%, #32d74b 100%);
	}

	&.default {
		background: linear-gradient(135deg, #8e8e93 0%, #aeaeb2 100%);
	}
}

/* 重试按钮 */
.retry-btn {
	margin-top: 40rpx;
	padding: 20rpx 60rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	border-radius: 50rpx;
	font-size: 28rpx;
	border: none;
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
	transition: all 0.3s;
}

/* 按钮点击效果 */
.action-btn:active,
.retry-btn:active {
	opacity: 0.8;
	transform: scale(0.98);
}
</style>
