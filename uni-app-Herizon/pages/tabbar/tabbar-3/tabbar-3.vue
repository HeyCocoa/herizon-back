<!-- 发布页面模板:展示三种内容发布选项 -->
<template>
	<!-- 主容器:支持动画切换 -->
	<view class="content" :class="{'active':active}">
		<!-- 应用Logo:位于页面上方 -->
		<image class="logo" :class="{'active':active}" src="../../../static/logo.png"  mode="aspectFit"></image>

		<!-- 发布选项容器:位于页面底部 -->
		<view class="tabbar-box-wrap">
			<!-- 发布选项卡片:包含三个发布类型 -->
			<view class="tabbar-box">
				<!-- 发布图文帖子 -->
				<view class="tabbar-box-item" @click="goToPage('/pages/tabbar-3-detial/tabbar-3-release/tabbar-3-release')">
					<image class="box-image" src="../../../static/img/release.png" mode="aspectFit"></image>
					<text class="explain">发图文</text>
				</view>

				<!-- 发起投票 -->
				<view class="tabbar-box-item" @click="goToPage('/pages/tabbar-3-detial/tabbar-3-qa/tabbar-3-qa')">
					<image class="box-image" src="../../../static/img/qa.png" mode="aspectFit"></image>
					<text class="explain">投票</text>
				</view>
			</view>
		</view>
	</view>
</template> 

<script>
/**
 * 发布页面 - TabBar第3页
 *
 * 提供内容发布入口,支持两种发布类型:
 * - 发图文: 发布图文帖子(支持最多3张图片)
 * - 投票: 发起投票帖(支持2-5个投票选项)
 *
 * 注意:视频发布功能已于2025-10-01系统变更时移除
 *
 * 使用动画效果提升用户体验
 * 需要登录权限才能进入发布页面
 */

import { isLoggedIn, navigateToLogin } from '../../../utils/auth.js'

export default {
	data() {
		return {
			active: false  // 控制页面动画状态
		};
	},
	onLoad() {
		// 页面加载完成时的初始化
	},
	/**
	 * 页面显示时触发
	 * 启用页面动画效果
	 */
	onShow() {
		// 页面显示时立即启用动画
		this.active = true;
	},
	/**
	 * 页面隐藏时触发
	 * 重置页面动画状态
	 */
	onHide() {
		this.active = false;
	},
	methods: {
		/**
		 * 导航到指定页面
		 * 需要先检查登录状态,未登录时跳转到登录页面
		 * @param {string} url - 目标页面路径
		 */
		goToPage(url) {
			if (!url) return;

			// 检查登录状态
			if (!isLoggedIn()) {
				// 未登录,显示登录提示并跳转到登录页面
				uni.showModal({
					title: '请先登录',
					content: '发布内容需要登录账号,请先登录后再试',
					confirmText: '去登录',
					cancelText: '取消',
					success: (res) => {
						if (res.confirm) {
							navigateToLogin();
						}
					}
				});
				return;
			}

			// 已登录,正常跳转到发布页面
			uni.navigateTo({
				url
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.content {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 100%;
	/* #ifdef H5 */
	height: calc(100vh - var(--window-bottom) - var(--window-top));
	/* #endif */
	/* #ifndef H5 */
	height: 100vh;
	/* #endif */
	transition: opacity 0.3s;
	background: #999;
	opacity: 0;
	&.active {
		opacity: 1;
	}
	.logo {
		position: relative;
		margin-top: -400upx;
		width: 200upx;
		height: 200upx;
		// z-index: -1;
		opacity: 0;
		transition: opacity 0.3s;
		&.active {
			opacity: 1;
		}
	}
}
.tabbar-box-wrap {
	position: absolute;
	width: 100%;
	padding: 50upx;
	box-sizing: border-box;
	bottom: 0;
	left: 0;
	.tabbar-box {
		position: relative;
		display: flex;
		width: 100%;
		background: #fff;
		border-radius: 20upx;
		padding: 15upx 20upx;
		box-sizing: border-box;
		z-index: 2;
		box-shadow: 0px 2px 5px 2px rgba(0, 0, 0, 0.1);
		&:after {
			content: '';
			position: absolute;
			bottom: -16upx;
			left: 0;
			right: 0;
			margin: auto;
			width: 50upx;
			height: 50upx;
			transform: rotate(45deg);
			background: #fff;
			z-index: 1;
			box-shadow: 2px 2px 5px 1px rgba(0, 0, 0, 0.1);
			border-radius: 2px;
		}
		&:before {
			content: '';
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: #ffffff;
			border-radius: 20upx;
			z-index: 2;
		}
		.tabbar-box-item {
			// position: relative;
			width: 100%;
			z-index: 3;
			margin: 10upx;
			color: $uni-color-subtitle;
			text-align: center;
			font-size: $uni-font-size-base;
			.box-image {
				width: 100%;
				height: $uni-img-size-lg;
			}
		}
	}
}
</style>
