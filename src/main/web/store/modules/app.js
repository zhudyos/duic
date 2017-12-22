import {otherRouter, appRouter} from '@/router/router';

const app = {
    state: {
        menuList: [],
        authUser: {},
        routers: [
            otherRouter,
            ...appRouter
        ]
    },
    mutations: {
        updateMenulist(state, root) {
            var menus = [];
            if (root) {
                menus = appRouter;
            } else {
                appRouter.forEach((v) => {
                    if (!v.root) {
                        menus.push(v)
                    }
                });
            }
            state.menuList = menus;
        },
        updateAuthUser(state, u) {
            state.authUser = u;
        }
    }
};

export default app;
