// with polyfills
// 原生语言扩展支持
// import 'core-js/stable' 是一个 JavaScript 语句，用于导入 core-js 库的 stable 版本。

// core-js 是一个常用的 JavaScript 库，它为 JavaScript 原生语言提供了许多实用的功能和特性，如数组、字符串、函数等的扩展方法，以及对 ES6 等新版本 JavaScript 语言的支持。

// 通过使用 import 'core-js/stable' 语句，可以导入 core-js 库的 stable 版本，从而在代码中使用 core-js 提供的扩展方法和特性。一般来说，stable 版本是 core-js 库的稳定版本，在性能和稳定性方面都有很好的表现。因此，import 'core-js/stable' 语句通常用于项目的生产环境。
import 'core-js/stable'


// Regenerator-runtime 是一个 JavaScript 库，用于支持将现有的 JavaScript 代码转换为使用 ES6 的 async/await 特性的代码。

// JavaScript 原生语言不支持 async/await 特性，但它可以通过使用第三方库，如 Regenerator-runtime，实现对这些特性的支持。通过使用 Regenerator-runtime，可以将现有的 JavaScript 代码转换为使用 async/await 特性的代码，从而提高代码的可读性和可维护性。
import 'regenerator-runtime/runtime'

import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// i18n 是“internationalization”的缩写，表示国际化。

// 国际化（i18n）是指让软件、网站或应用程序能够适应不同的语言、地区和文化的过程。它的目的是让软件、网站或应用程序能够支持多种语言，从而提高用户体验和拓展全球市场。

// 实现国际化通常需要在软件、网站或应用程序中增加多语言支持，即在代码中包含多种语言版本的内容。然后，在运行时，根据用户的语言偏好或地区设置，选择合适的语言版本进行显示。

// 国际化是软件、网站或应用程序开发的重要一环，它能够帮助软件、网站或应用程序扩展全球市场，提高用户体验，并增加用户群的多样性。
import i18n from './locales'

// 自定义工具库 切换样式
import bootstrap from './core/bootstrap'
import { ConfigProvider, Icon, Button, Tag, Menu, Dropdown, Avatar, Spin, Result, Form, Tabs, Input, Checkbox, Select, Radio, TimePicker, Slider, DatePicker, InputNumber, Row, Col, Modal, Alert, Divider, notification, message, Table, Pagination, Breadcrumb, Space, FormModel, Card, Tree, Descriptions, Upload, List, Transfer, TreeSelect, skeleton, Drawer } from 'ant-design-vue'
import ProLayout, { PageHeaderWrapper } from '@ant-design-vue/pro-layout'
import { PageLoading } from '@/components'
import themeConfig from './config/theme.config.js'
import Dialog from '@/components/Dialog'
import utils from '@/utils/utils.js';

import action from '@/core/directives/action.js'
import actionDauth from '@/core/directives/action-dauth.js'


import VueQuillEditor from 'vue-quill-editor'
import 'quill/dist/quill.core.css';
import 'quill/dist/quill.snow.css';
import 'quill/dist/quill.bubble.css';

// 路由守卫
import './router/router-guards'
// 其他
import './styles/global.less'
import ElementUI from "element-ui";
import AsyncComputed from 'vue-async-computed'

Vue.use(AsyncComputed)

Vue.use(VueQuillEditor)
Vue.use(ElementUI);

// Ant Design Vue
Vue.use(ConfigProvider)
Vue.use(Icon)
Vue.use(Tag)
Vue.use(Button)
Vue.use(Menu)
Vue.use(Dropdown)
Vue.use(Avatar)
Vue.use(Spin)
Vue.use(Result)
Vue.use(Form)
Vue.use(Tabs)
Vue.use(Input)
Vue.use(Checkbox)
Vue.use(Select)
Vue.use(Radio)
Vue.use(TimePicker)
Vue.use(Slider)
Vue.use(DatePicker)
Vue.use(InputNumber)
Vue.use(Row)
Vue.use(Col)
Vue.use(Modal)
Vue.use(Alert)
Vue.use(Divider)
Vue.use(Table)
Vue.use(Pagination)
Vue.use(Breadcrumb)
Vue.use(Space)
Vue.use(Dialog)
Vue.use(FormModel)
Vue.use(Card)
Vue.use(Tree)
Vue.use(Descriptions)
Vue.use(Upload)
Vue.use(List)
Vue.use(Transfer)
Vue.use(TreeSelect)
Vue.use(utils)
Vue.use(action)
Vue.use(actionDauth)
Vue.use(skeleton)
Vue.use(Drawer)

Vue.prototype.$confirm = Modal.confirm
Vue.prototype.$message = message
Vue.prototype.$notification = notification
Vue.prototype.$info = Modal.info
Vue.prototype.$success = Modal.success
Vue.prototype.$error = Modal.error
Vue.prototype.$warning = Modal.warning

// ProLayout
Vue.component('pro-layout', ProLayout)
Vue.component('page-container', PageHeaderWrapper)
Vue.component('page-header-wrapper', PageHeaderWrapper)
window.umi_plugin_ant_themeVar = themeConfig.theme

// Global imports
Vue.use(PageLoading)

Vue.config.productionTip = false

import uploader from 'vue-simple-uploader'
Vue.use(uploader)
// 加载基础ElementUI

import "../package/theme/element-variables.scss";
import { vuePlugin } from "@/highlight";
import "highlight.js/styles/atom-one-dark-reasonable.css";
Vue.use(vuePlugin);
import MyPD from "../package/index.js";
Vue.use(MyPD);
import "../package/theme/index.scss";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css";
import "bpmn-js-properties-panel/dist/assets/bpmn-js-properties-panel.css"; // 右边工具栏样式

// 引入拖拽表单
import KFormDesign from './components/k-form-design/packages'
import './components/k-form-design/styles/form-design.less'
import 'ant-design-vue/dist/antd.less'

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

new Vue({
  router,
  store,
  i18n,
  created: bootstrap,
  render: h => h(App),
}).$mount('#app')
