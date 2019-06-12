import Vue from "vue"

import "@fortawesome/fontawesome-free/css/all.css"

import Vuetify from "vuetify/lib"
import "vuetify/src/stylus/app.styl"
Vue.use(Vuetify, {
  iconfont: "fa"
})

import router from "./router"
import store from "./store"
import App from "./App.vue"

Vue.config.productionTip = false

new Vue({
  el: "#app",
  render: h => h(App),
  router,
  store
})
