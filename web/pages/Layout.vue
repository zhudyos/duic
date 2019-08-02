<template>
  <q-layout view="hHh lpR fFf" class>
    <q-header elevated class="bg-black">
      <q-toolbar>
        <q-toolbar-title>DUIC 配置中心</q-toolbar-title>

        <q-space />

        <q-tabs stretch shrink>
          <q-route-tab name="a" label="配置" to="/apps?page=1&size=20" />
          <q-route-tab name="c" label="集群" to="#" />
          <q-route-tab name="b" label="用户" to="/users?page=1&size=20" />
        </q-tabs>

        <q-separator vertical />

        <q-btn-dropdown stretch flat :label="email">
          <q-list>
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