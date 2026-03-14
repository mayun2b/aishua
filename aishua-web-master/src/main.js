import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { Button, Cell, CellGroup, Radio, RadioGroup, Checkbox, CheckboxGroup, Field, Divider, Popup, Card, Form, Slider, DropdownMenu, DropdownItem, Toast, Dialog, Notify, Loading, NavBar, Progress } from 'vant';
import 'vant/lib/index.css';

const app = createApp(App)
app.use(router)
// 注册需要的Vant组件
app.use(Button)
  .use(Cell)
  .use(CellGroup)
  .use(Radio)
  .use(RadioGroup)
  .use(Checkbox)
  .use(CheckboxGroup)
  .use(Field)
  .use(Divider)
  .use(Popup)
  .use(Card)
  .use(Form)
  .use(Slider)
  .use(DropdownMenu)
  .use(DropdownItem)
  .use(Toast)
  .use(Dialog)
  .use(Notify)
  .use(Loading)
  .use(NavBar)
  .use(Progress);

app.mount('#app')