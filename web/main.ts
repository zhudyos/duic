import Vue from 'vue'

import '@/assets/scripts/lib_vendor'

import Main from './pages/Main.vue'
import router from './router'

Vue.config.productionTip = false

new Vue({
  render: (h) => h(Main),
  router,
}).$mount('#app')
