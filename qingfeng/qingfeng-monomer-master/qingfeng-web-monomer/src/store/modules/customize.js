import storage from 'store'
import { TABLE_TYPE,TABLE_NAME } from '@/store/mutation-types'

const customize = {
  state: {
    table_type: '',
    table_name: '',
  },

  mutations: {
    SET_TABLE_TYPE: (state, table_type) => {
      storage.set(TABLE_TYPE,table_type);
      state.table_type = table_type
    },
    SET_TABLE_NAME: (state, table_name) => {
      storage.set(TABLE_NAME,table_name);
      state.table_name = table_name
    },
  },

  actions: {
    // 设置table_type
    SetTableType({ commit }, table_type) {
      storage.set(TABLE_TYPE, table_type)
      commit('SET_TABLE_TYPE', table_type)
    },
   // 设置table_name
   SetTableName({ commit }, table_name) {
    storage.set(TABLE_NAME, table_name)
    commit('SET_TABLE_TYPE', table_name)
  },
  },
}

export default customize
