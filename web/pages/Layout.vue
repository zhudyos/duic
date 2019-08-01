<template>
  <q-layout view="hHh lpR fFf" class>
    <q-header elevated class="bg-black">
      <q-toolbar>
        <q-toolbar-title>DUIC 配置中心</q-toolbar-title>

        <q-space />

        <q-tabs stretch shrink>
          <q-route-tab name="a" label="配置" to="#" exact />
          <q-route-tab name="c" label="集群" to="#" exact />
          <q-route-tab name="b" label="用户" to="#" exact />
        </q-tabs>

        <q-separator dark vertical />

        <q-btn stretch flat label="kevinz@weghst.com" />
        <q-btn stretch flat>
          <q-icon name="fas fa-sign-out-alt" />
        </q-btn>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view class="q-pa-sm" />
    </q-page-container>

    <q-footer elevated class="bg-grey-8 text-white">
      <q-toolbar>
        <q-toolbar-title>
          <q-avatar>
            <img src="../assets/images/duic60x60.png" />
          </q-avatar>Title
        </q-toolbar-title>
      </q-toolbar>
    </q-footer>
  </q-layout>
</template>

<script>
export default {
  data() {
    return { tab: 'tab1' }
  },
}
</script>