import storage from 'store'
import store from "@/store";
import {
  login,
  getInfo,
  logout
} from '@/api/auth/login'
import {
  ACCESS_TOKEN,
  REFRESH_TOKEN,
  EXPIRE_TIME
} from '@/store/mutation-types'
import {
  welcome
} from '@/utils/util'

const user = {
  state: {
    token: '',
    refreshToken: '',
    expireTime: '',
    name: '',
    welcome: '',
    avatar: '',
    roles: [],
    info: {},
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      storage.set(ACCESS_TOKEN, token);
      state.token = token
    },
    SET_REFRESH_TOKEN: (state, token) => {
      storage.set(REFRESH_TOKEN, token);
      state.refreshToken = token
    },
    SET_EXPIRE_TIME: (state, expireTime) => {
      storage.set(EXPIRE_TIME, expireTime);
      state.expireTime = expireTime
    },
    SET_NAME: (state, {
      name,
      welcome
    }) => {
      state.name = name
      state.welcome = welcome
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      storage.set("username", info.name);
      storage.set("orgname", info.orgPd.organize_name);
      state.info = info
    },
  },

  actions: {
    // 登录
    Login({
      commit
    }, userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo).then(response => {
          console.log('=====================')
          console.log(response)
          const data = response.data
          commit('SET_TOKEN', data.access_token)
          commit('SET_REFRESH_TOKEN', data.refresh_token)
          const current = new Date()
          const expireTime = current.setTime(current.getTime() + 1000 * data.expires_in)
          commit('SET_EXPIRE_TIME', expireTime)
          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo({
      commit
    }) {
      return new Promise((resolve, reject) => {
        getInfo().then(response => {
          const result = response.data.data;
          if (result.role && result.role.permissions.length > 0) {
            const role = result.role
            role.permissions = result.role.permissions
            role.permissions.map(per => {
              if (per.actionEntitySet != null && per.actionEntitySet.length > 0) {
                const action = per.actionEntitySet.map(action => {
                  return action.action
                })
                per.actionList = action
              }
            })
            role.permissionList = role.permissions.map(permission => {
              return permission.permissionId
            })
            commit('SET_ROLES', result.role)
            commit('SET_INFO', result)
          } else {
            reject(new Error('getInfo: roles must be a non-null array !'))
          }

          commit('SET_NAME', {
            name: result.name,
            welcome: welcome()
          })
          commit('SET_AVATAR', result.avatar)

          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 登出
    Logout({
      commit,
      state
    }) {
      return new Promise((resolve) => {
        logout(state.token).then(() => {
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          storage.remove(ACCESS_TOKEN)
          resolve()
        }).catch((err) => {
          console.log('logout fail:', err)
          // resolve()
        }).finally(() => {})
      })
    },

    // 设置token
    SetToken({
      commit
    }, token) {
      storage.set(ACCESS_TOKEN, token)
      commit('SET_TOKEN', token)
    },
  }
}

export default user