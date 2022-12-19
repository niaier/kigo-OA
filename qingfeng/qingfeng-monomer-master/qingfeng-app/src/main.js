// with polyfills
import 'core-js/stable'
import 'regenerator-runtime/runtime'

import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/'
import i18n from './locales'
import { VueAxios } from './utils/request'
// 路由守卫
import './router/router-guards'

import './utils/filter' // global filter

import 'ant-design-vue/dist/antd.less'

Vue.config.productionTip = false

import { TreeSelect } from 'ant-design-vue'
Vue.use(TreeSelect)
import Dialog from '@/components/Dialog'
Vue.use(Dialog)
import { Form ,Field,Notify ,Button, NavBar, Tabbar, TabbarItem, Swipe, SwipeItem, Lazyload, NoticeBar,Grid, GridItem, Tab, Tabs, List, Image as VanImage, Icon, Divider,Col, Row ,RadioGroup, Radio, DatetimePicker,Popup,Uploader,PullRefresh,ImagePreview,Empty } from 'vant';
import 'vant/lib/index.css';
Vue.use(Form)
Vue.use(Field)
Vue.use(Notify)
Vue.use(Button)
Vue.use(NavBar)
Vue.use(Tabbar)
Vue.use(TabbarItem)
Vue.use(Swipe)
Vue.use(SwipeItem)
Vue.use(Lazyload)
Vue.use(NoticeBar)
Vue.use(Grid)
Vue.use(GridItem)
Vue.use(Tab)
Vue.use(Tabs)
Vue.use(List)
Vue.use(VanImage)
Vue.use(Icon)
Vue.use(Divider)
Vue.use(Col)
Vue.use(Row)
Vue.use(RadioGroup)
Vue.use(Radio)
Vue.use(DatetimePicker)
Vue.use(Popup)
Vue.use(Uploader)
Vue.use(PullRefresh)
Vue.use(ImagePreview)
// Vue.use(Dialog)
Vue.use(Empty)

// 引入拖拽表单
import KFormDesign from './components/k-form-design/packages'
import './components/k-form-design/styles/form-design.less'

// 引入自定义组件
import currentUserOrganize from './components/k-form-design/customize/currentUserOrganize'
import selectUserOrganize from './components/k-form-design/customize/selectUserOrganize'

Vue.config.productionTip = false
// 使用KFormDesign的setConfig函数添加自定义组件
KFormDesign.setConfig({
  title: '青锋定义字段',
  list: [
    {
      type: 'loginUserOrganize', // 组件类型
      label: '当前用户/组织', // 组件名称
      // icon: 'icon-zidingyiyemian',
      component: currentUserOrganize, // 组件
      options: {
        option_type:"",
        defaultValue: "", // 默认值
        multiple: false, // 多选
        disabled: false, // 禁用
        width: '100%', // 宽度
        min: 0, // 最小值
        max: 99, // 最大值
        clearable: true, // 可清除
        placeholder: '请选择', // 占位内容
        showSearch: false // 可搜索
      },
      model: '', // 数据字段
      key: '',
      rules: [ // 校验规则
        {
          required: false,
          message: '必填项'
        }
      ]
    },
    {
      type: 'selectUserOrganize', // 组件类型
      label: '选择用户/组织', // 组件名称
      // icon: 'icon-zidingyiyemian',
      component: selectUserOrganize, // 组件
      options: {
        option_type:"",
        defaultValue: "", // 默认值
        multiple: false, // 多选
        disabled: false, // 禁用
        width: '100%', // 宽度
        min: 0, // 最小值
        max: 99, // 最大值
        clearable: true, // 可清除
        placeholder: '请选择', // 占位内容
        showSearch: false // 可搜索
      },
      model: '', // 数据字段
      key: '',
      rules: [ // 校验规则
        {
          required: false,
          message: '必填项'
        }
      ]
    }
  ]
})

Vue.use(KFormDesign)

// mount axios to `Vue.$http` and `this.$http`
Vue.use(VueAxios)

new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
