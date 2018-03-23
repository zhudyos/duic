import Main from '@/views/main.vue';

// 不作为Main组件的子页面展示的页面单独写，如下
export const loginRouter = {
    path: '/login',
    name: 'login',
    meta: {
        title: 'Login - 登录'
    },
    component: resolve => {
        require(['@/views/login.vue'], resolve);
    }
};

export const page404 = {
    path: '/*',
    name: 'error-404',
    meta: {
        title: '404-页面不存在'
    },
    component: resolve => {
        require(['@/views/error-page/404.vue'], resolve);
    }
};

export const page403 = {
    path: '/403',
    meta: {
        title: '403-权限不足'
    },
    name: 'error-403',
    component: resolve => {
        require(['@//views/error-page/403.vue'], resolve);
    }
};

export const page500 = {
    path: '/500',
    meta: {
        title: '500-服务端错误'
    },
    name: 'error-500',
    component: resolve => {
        require(['@/views/error-page/500.vue'], resolve);
    }
};

// 作为Main组件的子页面展示但是不在左侧菜单显示的路由写在otherRouter里
export const otherRouter = {
    path: '/',
    name: 'otherRouter',
    component: Main,
    children: [
        {
            path: '',
            component: resolve => {
                require(['@/views/index.vue'], resolve);
            }
        },
        {
            path: '/user-create',
            name: 'user-create',
            meta: {
                title: '创建用户'
            },
            component: resolve => {
                require(['@/views/user/user-create.vue'], resolve);
            }
        },
        {
            path: '/user-update-password',
            name: 'user-update-password',
            meta: {
                title: '修改密码'
            },
            component: resolve => {
                require(['@/views/user/user-update-password.vue'], resolve);
            }
        },
        {
            path: '/main-content-edit',
            name: 'main-content-edit',
            meta: {
                title: '编辑配置'
            },
            component: resolve => {
                require(['@/views/main/main-content-edit.vue'], resolve);
            }
        },
        {
            path: '/main-content-diff',
            name: 'main-content-diff',
            meta: {
                title: '编辑历史比较'
            },
            component: resolve => {
                require(['@/views/main/main-content-diff.vue'], resolve);
            }
        },
        {
            path: '/main-create',
            name: 'main-create',
            meta: {
                title: '创建应用'
            },
            component: resolve => {
                require(['@/views/main/main-create.vue'], resolve);
            }
        },
        {
            path: '/main-edit',
            name: 'main-edit',
            meta: {
                title: '编辑应用'
            },
            component: resolve => {
                require(['@/views/main/main-edit.vue'], resolve);
            }
        },
        {
            path: '/main-history',
            name: 'main-history',
            meta: {
                title: '修改历史'
            },
            component: resolve => {
                require(['@/views/main/main-history.vue'], resolve);
            }
        }
    ]
};

// 作为Main组件的子页面展示并且在左侧菜单显示的路由写在appRouter里
export const appRouter = [
    {
        path: '/main',
        icon: 'fa fa-fire',
        name: 'main-parent',
        title: '应用管理',
        component: Main,
        children: [
            {
                path: '',
                name: 'app',
                title: '应用管理',
                meta: {
                    title: '应用管理'
                },
                component: resolve => {
                    require(['@/views/main/main.vue'], resolve);
                }
            }
        ]
    },
    {
        path: '/user',
        icon: 'fa fa-user',
        name: 'user-parent',
        title: '用户管理',
        root: true,
        component: Main,
        children: [
            {
                path: '',
                name: 'user',
                title: '用户管理',
                meta: {
                    title: '用户管理'
                },
                component: resolve => {
                    require(['@/views/user/user.vue'], resolve);
                }
            }
        ]
    }
];

// 所有上面定义的路由都要写在下面的routers里
export const routers = [
    loginRouter,
    otherRouter,
    ...appRouter,
    page500,
    page403,
    page404
];
