import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import 'vant/lib/index.css'

async function bootstrap() {
  await store.dispatch('auth/bootstrap')

  const app = createApp(App)
  app.use(store)
  app.use(router)
  app.mount('#app')
}

bootstrap()
