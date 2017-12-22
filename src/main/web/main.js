import Vue from 'vue';
import iView from 'iview';
import {router} from './router/index';
import store from './store';
import App from './app.vue';
import 'iview/dist/styles/iview.css';
import VueClipboard from 'vue-clipboard2';

Vue.use(iView);
Vue.use(VueClipboard);

new Vue({
    el: '#app',
    router: router,
    store: store,
    render: h => h(App),
    mounted() {
        // this.$store.commit('updateMenulist');
    }
});
