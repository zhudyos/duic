import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export const store = new Vuex.Store({
    state: {
        loginState: false
    },
    mutations: {
        loginState(state, v) {
            state.loginState = v
        }
    }
})