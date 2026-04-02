import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import * as vant from 'vant';
import 'vant/lib/index.css';

const app = createApp(App)
app.use(router)
// 批量注册Vant组件
app.use(vant);

app.mount('#app')