import Vue from 'vue'

import '@/assets/scripts/lib_vendor'
import Layout from '@/components/layout/index.vue'

import router from './router'

Vue.config.productionTip = false

new Vue({
  router,
  render: (h) => h(Layout),
}).$mount('#app')
