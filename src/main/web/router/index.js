import VueRouter from 'vue-router'

const routes = [
    {
        path: "/login", meta: {title: "登录"},
        component: (r) => {
            require(["@/pages/Login.vue"], r)
        }
    },
    {
        path: "/", meta: {title: "DuiC 配置中心"},
        component: (r) => require(["@/pages/Dashboard.vue"], r),
        children: [
            {
                path: "/apps", meta: {title: "配置列表"},
                component: (r) => require(['@/pages/apps/Apps.vue'], r)
            },
            {
                path: "/app-histories", meta: {title: "配置列表"},
                component: (r) => require(['@/pages/apps/AppHistory.vue'], r)
            },
            {
                path: "/users", meta: {title: "用户列表"},
                component: (r) => require(['@/pages/users/Users.vue'], r)
            }
        ]
    }
]

export const router = new VueRouter({routes})
router.beforeEach((to, from, next) => {
    let title = to.meta.title || 'DuiC 配置管理中心'
    document.title = title
    next()
})