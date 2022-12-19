import ls from 'store'
import router from './index'
import store from '@/store'
import { ACCESS_TOKEN } from '@/store/mutation-types' // progress bar custom style

const allowList = ['login', 'register', 'registerResult'] // no redirect whitelist
const loginRoutePath = '/login'
const defaultRoutePath = '/'

router.beforeEach((to, from, next) => { 
  const token = ls.get(ACCESS_TOKEN)
  // console.log(token)
  if (token) {
    if (to.path === loginRoutePath) {
      next({ path: defaultRoutePath })
    } else {
      //验证token是否有效，无效则刷新，有效则跳过。
      if (store.getters.userInfo.name==''||store.getters.userInfo.name==undefined) {
        store.dispatch('GetInfo').then(res => {
        }).catch(() => {
          // 失败时，获取用户信息失败时，调用登出，来清空历史保留信息
          store.dispatch('Logout').then(() => {
            next({ path: loginRoutePath, query: { redirect: to.fullPath } })
          })
        })
      } else {
        next()
      }
    }
  } else {
    // not login
    if (allowList.includes(to.name)) {
      // 在免登录名单，直接进入
      next()
    } else {
      next({ path: loginRoutePath, query: { redirect: to.fullPath } })
    }
  }
  next()
})

router.afterEach(() => {
})
