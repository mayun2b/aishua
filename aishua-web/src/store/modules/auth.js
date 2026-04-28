import authApi from '../../modules/auth/api/auth'
import {
  clearAuthStorage,
  loadAuthStorage,
  saveAuthStorage
} from '../../modules/auth/utils/authStorage'

function createInitialState() {
  const { token, user } = loadAuthStorage()
  return {
    token,
    user,
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

      if (!state.user) {
        try {
          const response = await authApi.fetchProfile()
          commit('setUser', response.data)
        } catch (error) {
          commit('clearAuth')
        }
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
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.isAdmin === 1,
    bootstrapped: (state) => state.bootstrapped
  }
}
