import axios from 'axios';
import Vue from 'vue';
import iView from 'iview';
import VueRouter from 'vue-router';
import {routers} from './router';

Vue.use(VueRouter);

// 路由配置
const RouterConfig = {
    // mode: 'history',
    routes: routers
};

export const router = new VueRouter(RouterConfig);

router.beforeEach((to, from, next) => {
    iView.LoadingBar.start();

    var title = to.meta.title || 'DuiC Admin';
    window.document.title = title;
    next();
});

router.afterEach((to) => {
    iView.LoadingBar.finish();
    window.scrollTo(0, 0);
});

// error interceptors
axios.interceptors.response.use((resp) => {
    return resp
}, (err) => {
    var resp = err.response;
    if (resp.status === 401) {
        location.href = '#/login';
    } else if (resp.status === 403) {
        location.href = '#/403'
    } else if (resp.status === 404) {
        location.href = '#/404'
    } else if (resp.status === 500) {
        location.href = '#/500'
    }
    return Promise.reject(err)
});
