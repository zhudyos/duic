import Vue from "vue";
import VueRouter from "vue-router";
Vue.use(VueRouter);

const routes = [
  {
    path: "/login",
    meta: { title: "登录 - DUIC" },
    component: () => import("./pages/Login.vue")
  },
  {
    path: "/",
    meta: { title: "主页 - DUIC" },
    component: () => import("./pages/Layout.vue"),
    children: [
      {
        path: "/apps",
        meta: { title: "配置列表 - DUIC" },
        component: () => import("./pages/apps/AppList.vue")
      },
      {
        path: "/users",
        meta: { title: "用户列表 - DUIC" },
        component: () => import("./pages/users/UserList.vue")
      }
    ]
  },
  {
    path: "/config-edit",
    meta: { title: "配置编辑" },
    component: () => import("./pages/config-edit/Index.vue")
  }
];

const router = new VueRouter({ routes });
router.beforeEach((to, from, next) => {
  const title = to.meta.title || "配置中心 - DUIC";
  document.title = title;
  next();
});

export default router;
