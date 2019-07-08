import Vue from "vue"
import VueRouter from "vue-router"

Vue.use(VueRouter)

const routes = [
    {
        path: "/login",
        meta: { title: "登录 - DUIC" },
        component: () => import("@/pages/Login")
    },
    {
        path: "/",
        meta: { title: "主页 - DUIC" },
        component: () => import("@/pages/Main"),
        children: [
            {
                path: "/home",
                component: () => import("@/pages/App.vue")
            }
        ]
    }
]

const router = new VueRouter({
    routes: routes
})

router.beforeEach((to, from, next) => {
    let title = to.meta.title || "配置中心 - DUIC"
    document.title = title
    next()
})

export { router }