import storage from 'store'
import {
  TABBAR,
} from '@/store/mutation-types'

const main = {
  state: {
    tabbar: '0',
  },

  mutations: {
    SET_TABBAR: (state, tabbar) => {
      storage.set(TABBAR, tabbar);
      state.tabbar = tabbar
    },
  },

  actions: {
    SetTabbar({
      commit
    }, tabbar) {
      storage.set(TABBAR, tabbar)
      commit('SET_TABBAR', tabbar)
    },
  },
}

export default main