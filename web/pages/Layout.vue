<template>
  <q-layout>
    <q-header class="bg-dark">
      <q-toolbar>
        <d-banner />

        <q-space />

        <q-tabs stretch>
          <q-route-tab name="apps" label="配置" to="/apps"></q-route-tab>
          <q-route-tab
            name="clusters"
            label="集群"
            to="/clusters"
          ></q-route-tab>
          <q-route-tab name="users" label="用户" to="/users"></q-route-tab>
        </q-tabs>

        <q-separator vertical inset />

        <q-btn-dropdown stretch flat :label="email">
          <q-list>
            <q-item clickable v-close-popup>
              <q-item-section>
                <q-item-label>修改密码</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>

        <q-btn stretch flat @click="logout">
          <q-icon name="mdi-logout" />
          <q-tooltip>登出</q-tooltip>
        </q-btn>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view class="q-pa-sm" />
    </q-page-container>

    <q-footer class="bg-dark text-center">
      <q-btn
        dense
        unelevated
        size="xs"
        type="a"
        href="https://www.apache.org/licenses/LICENSE-2.0"
        target="_blank"
        no-caps
        class="text-center"
        >https://www.apache.org/licenses/LICENSE-2.0</q-btn
      >
    </q-footer>
  </q-layout>
</template>

<script>
import DBanner from "../components/DBanner.vue";

export default {
  components: {
    DBanner
  },
  data: () => ({
    navTab: "",
    email: ""
  }),
  created() {
    const email = this.$q.cookies.get("email");
    const state = !!email;
    if (state) {
      this.email = email;
    } else {
      this.$router.push("/login");
    }
  },
  methods: {
    logout() {
      const cookies = this.$q.cookies;
      cookies.remove("email");
      cookies.remove("token");
      this.$router.push("/login");
    }
  }
};
</script>
