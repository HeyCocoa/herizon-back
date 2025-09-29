/**
 * Herizon女性社区uni-app入口文件
 *
 * 使用条件编译支持Vue2和Vue3版本：
 * - #ifndef VUE3: 适用于Vue2版本的初始化
 * - #ifdef VUE3: 适用于Vue3版本的SSR应用初始化
 */
import App from './App.vue'

// Vue2版本初始化
// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false  // 关闭生产环境提示
App.mpType = 'app'                // 设置应用类型为小程序应用
const app = new Vue({
	...App
})
app.$mount()  // 挂载应用
// #endif

// Vue3版本初始化（支持SSR）
// #ifdef VUE3
import {
	createSSRApp
} from 'vue'
/**
 * 创建Vue3 SSR应用实例
 * @returns {Object} 包含app实例的对象
 */
export function createApp() {
	const app = createSSRApp(App)
	return {
		app
	}
}
// #endif
