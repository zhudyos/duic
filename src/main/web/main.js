import Vue from 'vue'

// quasar
import "./vendor/quasar"
import "./styles/quasar.styl"

import { router } from "./router"
import Main from "./pages/Main.vue"

Vue.config.productionTip = false

new Vue({
  el: "#app",
  render: h => h(Main),
  router,
})
