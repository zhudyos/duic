import Vue from 'vue'
import VueRouter from 'vue-router'
import Vuetify from 'vuetify'
import Vuelidate from 'vuelidate'
import VueClipboard from 'vue-clipboard2'

Vue.use(VueRouter)
Vue.use(Vuetify)
Vue.use(Vuelidate)
Vue.use(VueClipboard)

// ===================================================================================================
import 'vuetify/dist/vuetify.css'

import {router} from './router/index'
import {store} from "./store/index"
import Main from './pages/Main.vue'

new Vue({
    el: "#app",
    render: h => h(Main),
    router,
    store
})

// ===================================================================================================
import axios from 'axios'
import Cookies from 'js-cookie'

axios.interceptors.response.use((response) => {
    return response
}, (error) => {
    // 未认证
    if (error.response.status === 401) {
        store.commit('loginState', false)
        Cookies.remove('token')
    }
    return Promise.reject(error)
})