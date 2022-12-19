const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [{
    path: '/',
    name: 'index',
    component: RouteView,
    component: () => import('@/views/main/index'),
    meta: {
      title: 'menu.exception',
      icon: 'warning',
      permission: ['exception']
    },
  }, {
    path: '/process',
    name: 'process',
    component: () => import('@/views/process/index')
  }, {
    path: '/formInfo',
    name: 'formInfo',
    component: () => import('@/views/process/info/formInfo')
  }, {
    path: '/formCheck',
    name: 'formCheck',
    component: () => import('@/views/process/check/formCheck')
  },{
    path: '/send',
    name: 'send',
    component: () => import('@/views/process/send/index')
  },{
    path: '/PEdit',
    name: 'PEdit',
    component: () => import('@/views/process/send/PEdit')
  },
  {
    path: '/my',
    name: 'my',
    component: () => import('@/views/my/index')
  },
  {
    path: '/newInfo',
    name: 'newInfo',
    component: () => import('@/views/main/newInfo')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/Login')
  },
  {
    path: '/updatePwd',
    name: 'updatePwd',
    component: () => import('@/views/my/updatePwd')
  },
  {
    path: '/updateUser',
    name: 'updateUser',
    component: () => import('@/views/my/updateUser')
  },
  {
    path: '/404',
    component: () => import( /* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]