import Vue from 'vue'
import VueRouter from 'vue-router'
import Vuetify from 'vuetify'
import VueClipboard from 'vue-clipboard2'
// ===================================================================================================
import 'vuetify/dist/vuetify.css'

import {router} from './router/index'
import {store} from "./store/index"
import Main from './pages/Main.vue'
// ===================================================================================================
import axios from 'axios'
import Cookies from 'js-cookie'

Vue.use(VueRouter)
Vue.use(Vuetify)
Vue.use(VueClipboard)

new Vue({
    el: "#app",
    render: h => h(Main),
    router,
    store
})

axios.interceptors.response.use((response) => {
    return response
}, (error) => {
    // 未认证
    if (error.response) {
        if (error.response.status === 401) {
            store.commit('loginState', false)
            Cookies.remove('token')
        }
    } else {
        alert('服务不可用')
    }
    return Promise.reject(error)
})

// ===================================================================================================
/**
 * 通知。
 * @param text 通知文本
 * @param ops 选项
 */
Vue.prototype.$notice = function (text, ops) {
    const props = ops || {top: true, color: 'error'}
    props.value = true

    const instance = new Vue({
        render(h) {
            return h('v-snackbar', {
                props: props,
                on: {
                    input() {
                        instance.$el.remove()
                    }
                }
            }, text)
        }
    })

    const component = instance.$mount()
    document.body.appendChild(component.$el)
}

/**
 * vuetify confirm 实现。
 * @param text 提示文本
 * @param confirmFun 确认回调函数
 * @param cancelFun 取消回调函数
 */
Vue.prototype.$confirm = function (text, confirmFun, cancelFun) {
    const instance = new Vue({
        template: `<v-dialog v-model="value" max-width="290">
    <v-card>
        <v-card-title primary-title class="title">确认</v-card-title>
        <v-card-text class="body-2">${text}</v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn flat color="info" @click="cancel">取消</v-btn>
            <v-btn flat color="success" @click="confirm">确认</v-btn>
        </v-card-actions>
    </v-card>
</v-dialog>`,
        data: {
            value: true
        },
        methods: {
            confirm() {
                this.c(confirmFun)
            },
            cancel() {
                this.c(cancelFun)
            },
            c(f) {
                instance.value = false
                instance.$el.remove()
                f && f()
            }
        },
        watch: {
            value() {
                this.cancel()
            }
        }
    })

    const component = instance.$mount()
    document.body.appendChild(component.$el)
}