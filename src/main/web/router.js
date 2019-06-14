import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("./views/Login.vue")
    },
    {
      path: '/',
      name: 'home',
      component: () => import("./views/Home.vue"),
      children: [
        {
          path: "/app/content-edit",
          name: "app-content-edit",
          component: () => import("./views/apps/AppContentEdit.vue")
        }
      ]
    },
  ]
})