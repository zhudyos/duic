<template>
  <q-layout class="bg-grey-10 text-white">
    <q-header class="bg-grey-9">
      <q-toolbar>
        <q-banner dark class="bg-grey-9">
          <template v-slot:avatar>
            <q-avatar>
              <img src="../assets/images/duic60x60.png" />
            </q-avatar>
          </template>
          <span class="text-h6">配置中心</span>
        </q-banner>

        <q-space />

        <q-tabs stretch shrink>
          <q-route-tab name="apps" label="配置" to="/apps"></q-route-tab>
          <q-route-tab name="clusters" label="集群" to="/clusters"></q-route-tab>
          <q-route-tab name="users" label="用户" to="/users"></q-route-tab>
        </q-tabs>

        <q-separator dark vertical inset />

        <q-btn-dropdown stretch flat :label="email">
          <q-list dark class="bg-grey-9">
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

    <q-footer elevated class="bg-grey-9 text-weight-regular q-pa-xs">
      <div class="row justify-center">
        <div class="col-xs-6 col-md-3">
          <q-btn
            flat
            dense
            unelevated
            size="xs"
            type="a"
            href="https://www.apache.org/licenses/LICENSE-2.0"
            target="_blank"
          >https://www.apache.org/licenses/LICENSE-2.0</q-btn>
        </div>
      </div>
    </q-footer>
  </q-layout>
</template>

<script>
export default {
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