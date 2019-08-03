import Vue from "vue";
import "./quasar";
import Main from "./pages/Main.vue";
import router from "./router";
import axios from "axios";

Vue.config.productionTip = false;

new Vue({
  render: h => h(Main),
  router
}).$mount("#app");

// 注册 axios 拦截器

axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    const response = error.response;
    if (response) {
      if (response.status === 401) {
        router.go("/login");
        return Promise.resolve(error);
      }
    }

    return Promise.reject(error);
  }
);
