import Vue from 'vue'
import './quasar'
import Main from './pages/Main.vue'
import router from './router'

Vue.config.productionTip = false

new Vue({
  render: (h) => h(Main),
  router,
}).$mount('#app')
