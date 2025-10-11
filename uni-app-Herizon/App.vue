<script>
/**
 * Herizon女性社区uni-app主应用程序
 *
 * 全局生命周期管理:
 * - onLaunch: 应用启动时执行
 * - onShow: 应用进入前台时执行
 * - onHide: 应用进入后台时执行
 */
import { getUserDisplayInfo, USER_ROLES } from './utils/auth.js'

export default {
	/**
	 * 应用启动时触发
	 * 设置底部导航栏的角标显示
	 */
	onLaunch: function() {
		// 预加载关键页面,防止微信开发者工具的代码依赖分析过滤
		this.preloadPages();

		// 设置全局导航拦截器
		this.setupNavigationInterceptor();

		// 延迟1秒后设置导航栏角标
		setTimeout(() => {
			// 设置"关注"页面的角标数字
			uni.setTabBarBadge({
				index: 1,  // 对应tabbar配置中的第2个选项
				text: '31'
			});
			// 设置"消息"页面的红点提示
			uni.showTabBarRedDot({
				index: 3   // 对应tabbar配置中的第4个选项
			});
		}, 1000);
	},

	/**
	 * 应用进入前台时触发
	 */
	onShow: function() {
		// 应用进入前台
	},

	/**
	 * 应用进入后台时触发
	 */
	onHide: function() {
		// 应用进入后台
	},

	methods: {
		/**
		 * 预加载关键页面
		 * 确保登录、注册等页面被微信开发者工具正确识别
		 */
		preloadPages: function() {
			// 确保关键页面路径被引用,防止被代码依赖分析忽略
			const criticalPages = [
				'/pages/login/login',
				'/pages/register/register',
				'/pages/verification/verification'
			];
			// 关键页面路径已引用,防止被代码依赖分析忽略
		},

		/**
		 * 设置全局导航拦截器
		 * 拦截试用用户访问需要认证的功能
		 */
		setupNavigationInterceptor: function() {
			// 需要认证的tabbar页面索引
			const restrictedTabPages = {
				'/pages/tabbar/tabbar-2/tabbar-2': '话题', // Topics
				'/pages/tabbar/tabbar-3/tabbar-3': '发布', // Post
				'/pages/tabbar/tabbar-4/tabbar-4': '工具'  // Tools
			};

			// 拦截switchTab
			const originalSwitchTab = uni.switchTab;
			uni.switchTab = function(options) {
				const userInfo = getUserDisplayInfo();
				const isTrialUser = userInfo && userInfo.role === USER_ROLES.TRIAL;

				if (isTrialUser && restrictedTabPages[options.url]) {
					uni.showModal({
						title: '需要身份认证',
						content: `试用用户需要完成身份认证才能访问${restrictedTabPages[options.url]}功能,是否前往认证页面?`,
						confirmText: '去认证',
						cancelText: '取消',
						success: (res) => {
							if (res.confirm) {
								uni.navigateTo({
									url: '/pages/verification/verification'
								});
							}
						}
					});
					return;
				}

				return originalSwitchTab.call(uni, options);
			};
		}
	}
};
</script>

<style>
/*每个页面公共css */ 
</style>
