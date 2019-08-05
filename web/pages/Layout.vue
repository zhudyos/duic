<template>
  <q-layout>
    <q-header class="bg-black">
      <q-toolbar>
        <q-banner dark class="bg-black">
          <template v-slot:avatar>
            <q-avatar>
              <img src="../assets/images/duic60x60.png" />
            </q-avatar>
          </template>
          <span class="text-h6">配置中心</span>
        </q-banner>

        <q-space />

        <q-tabs stretch shrink>
          <q-route-tab name="a" label="配置" to="/apps" />
          <q-route-tab name="c" label="集群" to="#" />
          <q-route-tab name="b" label="用户" to="/users" />
          <q-route-tab name="d" label="OAIS" to="#" />
        </q-tabs>

        <q-separator dark vertical inset />

        <q-btn-dropdown stretch flat :label="email">
          <q-list dark class="bg-grey-7">
            <q-item clickable v-close-popup>
              <q-item-section>
                <q-item-label>修改密码</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>

        <q-btn stretch flat>
          <q-icon name="mdi-logout" />
        </q-btn>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view class="q-pa-sm" />
    </q-page-container>

    <q-footer elevated class="bg-black text-weight-regular q-pa-xs">
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
  }
};
</script>