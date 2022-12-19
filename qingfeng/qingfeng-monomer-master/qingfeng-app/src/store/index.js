import Vue from 'vue'
import Vuex from 'vuex'

import user from './modules/user'
import main from './modules/main'

import getters from './getters'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    user,
    main
  },
  state: {},
  mutations: {},
  actions: {},
  getters
})