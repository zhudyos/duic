import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export const store = new Vuex.Store({
    state: {
        loginState: false,
        loginEmail: ''
    },
    mutations: {
        loginState(state, v) {
            state.loginState = v
        },
        loginEmail(state, v) {
            state.loginEmail = v
        }
    }
})