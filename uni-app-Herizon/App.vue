<script>
/**
 * Herizon女性社区uni-app主应用程序
 *
 * 全局生命周期管理：
 * - onLaunch: 应用启动时执行
 * - onShow: 应用进入前台时执行
 * - onHide: 应用进入后台时执行
 */
export default {
	/**
	 * 应用启动时触发
	 * 设置底部导航栏的角标显示
	 */
	onLaunch: function() {
		console.log('App Launch');

		// 预加载关键页面，防止微信开发者工具的代码依赖分析过滤
		this.preloadPages();

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
		console.log('App Show');
	},

	/**
	 * 应用进入后台时触发
	 */
	onHide: function() {
		console.log('App Hide');
	},

	methods: {
		/**
		 * 预加载关键页面
		 * 确保登录、注册等页面被微信开发者工具正确识别
		 */
		preloadPages: function() {
			// 确保关键页面路径被引用，防止被代码依赖分析忽略
			const criticalPages = [
				'/pages/login/login',
				'/pages/register/register',
				'/pages/verification/verification'
			];
			console.log('Critical pages referenced:', criticalPages);
		}
	}
};
</script>

<style>
/*每个页面公共css */ 
</style>
