// frontend/src/store/index.js
import { createStore } from 'vuex'

export default createStore({
    state: {
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
    },
    mutations: {
        setToken(state, token) {
            state.token = token
            localStorage.setItem('token', token)
        },
        setUserInfo(state, userInfo) {
            state.userInfo = userInfo
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
        },
        clearUserInfo(state) {
            state.token = ''
            state.userInfo = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
        }
    },
    actions: {
    },
    modules: {
    }
})