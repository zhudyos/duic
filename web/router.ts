import Vue from 'vue'
import VueRouter from 'vue-router'
Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    meta: { title: '登录 - DUIC' },
    component: () => import('@/pages/Login.vue'),
  },
  {
    path: '/',
    meta: { title: '主页 - DUIC' },
    component: () => import('@/pages/Main.vue'),
    children: [
      {
        path: '/home',
        component: () => import('@/pages/App.vue'),
      },
    ],
  },
]

const router = new VueRouter({ routes })

// tslint:disable-next-line: variable-name
router.beforeEach((to, _from, next) => {
  const title = to.meta.title || '配置中心 - DUIC'
  document.title = title
  next()
})

export default router
