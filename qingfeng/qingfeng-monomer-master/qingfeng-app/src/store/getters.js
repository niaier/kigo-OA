const getters = {
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  nickname: state => state.user.name,
  userInfo: state => state.user.info,
  tabbar: state => state.main.tabbar,
}

export default getters
