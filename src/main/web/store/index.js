import Vue from 'vue';
import Vuex from 'vuex';

import app from './modules/app';

Vue.use(Vuex);

const store = new Vuex.Store({
    mutations: {
        //
    },
    actions: {},
    modules: {
        app
    }
});

export default store;
