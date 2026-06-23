import authApi from '../../modules/auth/api/auth'
import {
  clearAuthStorage,
  loadAuthStorage,
  saveAuthStorage
} from '../../modules/auth/utils/authStorage'

function createInitialState() {
  const { token, user } = loadAuthStorage()
  return {
    // 当前登录态token，空串表示未登录
    token,
    // 当前登录用户信息
    user,
    // 是否已完成应用启动阶段的权限初始化
    bootstrapped: false
  }
}

export default {
  namespaced: true,
  state: createInitialState(),
  mutations: {
    setAuth(state, payload) {
      state.token = payload.token
      state.user = payload.user
      saveAuthStorage(payload)
    },
    setUser(state, user) {
      state.user = user
      saveAuthStorage({ token: state.token, user })
    },
    clearAuth(state) {
      state.token = ''
      state.user = null
      clearAuthStorage()
    },
    setBootstrapped(state, value) {
      state.bootstrapped = value
    }
  },
  actions: {
    async bootstrap({ state, commit }) {
      if (state.bootstrapped) {
        return
      }

      if (!state.token) {
        commit('setBootstrapped', true)
        return
      }

      // 不管是否有用户信息，都验证token是否有效
      try {
        const response = await authApi.fetchProfile()
        // 校验用户信息是否有效，兼容id和userId字段
        if (!response || !response.data || !(response.data.id || response.data.userId)) {
          throw new Error('用户信息无效')
        }
        commit('setUser', response.data)
      } catch (error) {
        commit('clearAuth')
      }

      commit('setBootstrapped', true)
    },
    async login({ commit }, payload) {
      const response = await authApi.login(payload)
      commit('setAuth', response.data)
      commit('setBootstrapped', true)
      return response.data
    },
    async register(_, payload) {
      const response = await authApi.register(payload)
      return response.data
    },
    logout({ commit }) {
      commit('clearAuth')
      commit('setBootstrapped', true)
    }
  },
  getters: {
    token: (state) => state.token,
    currentUser: (state) => state.user,
    isAuthenticated: (state) => Boolean(state.token) && Boolean(state.user),
    isAdmin: (state) => state.user?.isAdmin === 1,
    bootstrapped: (state) => state.bootstrapped
  }
}
