/*
 * Copyright 2017-2019 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                path: "/app-histories", meta: {title: "配置修改历史记录"},
                component: (r) => require(['@/pages/apps/AppHistory.vue'], r)
            },
            {
                path: "/users", meta: {title: "用户列表"},
                component: (r) => require(['@/pages/users/Users.vue'], r)
            },
            {
                path: "/clusters", meta: {title: "集群状态"},
                component: (r) => require(['@/pages/servers/Servers.vue'], r)
            },
            {
                path: "/api-test", meta: {title: "配置 API 测试"},
                component: (r) => require(['@/pages/ApiTest.vue'], r)
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